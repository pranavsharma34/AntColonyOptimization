import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cern.jet.random.Uniform;

public final class AntColonyOptimization {

        // greedy
        public static final double ALPHA = -0.2d;
        // rapid selection
        public static final double BETA = 9.6d;

        // heuristic parameters
        public static final double Q = 0.0001d; // somewhere between 0 and 1
        public static final double PHEROMONE_PERSISTENCE = 0.3d; // between 0 and 1
        public static final double INITIAL_PHEROMONES = 0.8d; // can be anything

        // use power of 2
        public static final int numOfAgents = 2048 * 20;
        private static final int poolSize = Runtime.getRuntime().availableProcessors();

        private Uniform uniform;

        private final ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);

        private final ExecutorCompletionService<WalkedWay> agentCompletionService = new ExecutorCompletionService<WalkedWay>(
                        threadPool);

        final double[][] matrix;
        final double[][] invertedMatrix;
        private final double[][] pheromones;
        private final Object[][] mutexes;

        public AntColonyOptimization() throws IOException {
                // read the matrix
                matrix = readMatrixFromFile();
                invertedMatrix = invertMatrix();
                pheromones = initializePheromones();
                mutexes = initializeMutexObjects();
                // (double min, double max, int seed)
                uniform = new Uniform(0, matrix.length - 1, (int) System.currentTimeMillis());
        }

        private final Object[][] initializeMutexObjects() {
                final Object[][] localMatrix = new Object[matrix.length][matrix.length];
                int rows = matrix.length;
                for (int columns = 0; columns < matrix.length; columns++) {
                        for (int i = 0; i < rows; i++) {
                                localMatrix[columns][i] = new Object();
                        }
                }

                return localMatrix;
        }

        final double readPheromone(int x, int y) {
                // double p;
                // synchronized (mutexes[x][y]) {
                // p = pheromones[x][y];
                // }
                // return p;
                return pheromones[x][y];
        }

        final void adjustPheromone(int x, int y, double newPheromone) {
                synchronized (mutexes[x][y]) {
                        final double result = calculatePheromones(pheromones[x][y], newPheromone);
                        if (result >= 0.0d) {
                                pheromones[x][y] = result;
                        } else {
                                pheromones[x][y] = 0;
                        }
                }
        }

        private final double calculatePheromones(double current, double newPheromone) {
                final double result = (1 - AntColonyOptimization.PHEROMONE_PERSISTENCE) * current
                                + newPheromone;
                return result;
        }

        final void adjustPheromone(int[] way, double newPheromone) {
                synchronized (pheromones) {
                        for (int i = 0; i < way.length - 1; i++) {
                                pheromones[way[i]][way[i + 1]] = calculatePheromones(
                                                pheromones[way[i]][way[i + 1]], newPheromone);
                        }
                        pheromones[way[way.length - 1]][way[0]] = calculatePheromones(
                                        pheromones[way.length - 1][way[0]], newPheromone);
                }
        }

        private final double[][] initializePheromones() {
                final double[][] localMatrix = new double[matrix.length][matrix.length];
                int rows = matrix.length;
                for (int columns = 0; columns < matrix.length; columns++) {
                        for (int i = 0; i < rows; i++) {
                                localMatrix[columns][i] = INITIAL_PHEROMONES;
                        }
                }

                return localMatrix;
        }

        
        
        private final double[][] readMatrixFromFile() throws IOException {
        	
                final BufferedReader br = new BufferedReader(new FileReader(new File("c:\\Users\\pranavsharma34\\workspace\\AntColonyOptimization\\src\\files\\sample2.txt")));

                final LinkedList<Record> records = new LinkedList<Record>();

                boolean readAhead = false;
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                		
                	    if (line.equals("EOF")) {
                                break;
                        }

                        if (readAhead && count < 50) {
                        	    String[] split = line.trim().split(",");
                        	    if (split.length > 3 && split[3].equals("Double"))
                        	    {
                        	    	records.add(new Record(Double.parseDouble(split[1].trim()), Double
                                                .parseDouble(split[2].trim())));
                        	    	count++;
                        	    }
                        }

                        if (line.equals("@id,@lon,@lat,name,operator,manufacturer,manufacturer:type,generator:manufacturer,generator:type,generator:output:electricity,power_rating,rotor:diameter,height,ref,note")) {
                                readAhead = true;
                        }
                }

                br.close();
                
                final double[][] localMatrix = new double[records.size()][records.size()];

                int rIndex = 0;
                for (Record r : records) {
                        int hIndex = 0;
                        for (Record h : records) {
                                localMatrix[rIndex][hIndex] = calculateEuclidianDistance(r.x, r.y, h.x, h.y);
                                hIndex++;
                        }
                        rIndex++;
                }

                return localMatrix;
        }

        private final double[][] invertMatrix() {
                double[][] local = new double[matrix.length][matrix.length];
                for (int i = 0; i < matrix.length; i++) {
                        for (int j = 0; j < matrix.length; j++) {
                                local[i][j] = invertDouble(matrix[i][j]);
                        }
                }
                return local;
        }

        private final double invertDouble(double distance) {
                if (distance == 0)
                        return 0;
                else
                        return 1.0d / distance;
        }

        private final double calculateEuclidianDistance(double x1, double y1, double x2, double y2) {
                return Math.abs((Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))));
        }

        final double start() throws InterruptedException, ExecutionException {

                WalkedWay bestDistance = null;

                int agentsSend = 0;
                int agentsDone = 0;
                int agentsWorking = 0;
                for (int agentNumber = 0; agentNumber < numOfAgents; agentNumber++) {
                        agentCompletionService.submit(new Agent(this, getGaussianDistributionRowIndex()));
                        agentsSend++;
                        agentsWorking++;
                        while (agentsWorking >= poolSize) {
                                WalkedWay way = agentCompletionService.take().get();
                                if (bestDistance == null || way.distance < bestDistance.distance) {
                                        bestDistance = way;
                                        System.out.println("Agent returned with new best distance of: " + way.distance);
                                }
                                agentsDone++;
                                agentsWorking--;
                        }
                }
                final int left = agentsSend - agentsDone;
                System.out.println("Waiting for " + left + " agents to finish their random walk!");

                for (int i = 0; i < left; i++) {
                        WalkedWay way = agentCompletionService.take().get();
                        if (bestDistance == null || way.distance < bestDistance.distance) {
                                bestDistance = way;
                                System.out.println("Agent returned with new best distance of: " + way.distance);
                        }
                }

                threadPool.shutdownNow();
                System.out.println("Found best so far: " + bestDistance.distance);
                System.out.println(Arrays.toString(bestDistance.way));

                return bestDistance.distance;

        }

        private final int getGaussianDistributionRowIndex() {
                return uniform.nextInt();
        }

        static class Record {
                double x;
                double y;

                public Record(double x, double y) {
                        super();
                        this.x = x;
                        this.y = y;
                }
        }

        static class WalkedWay {
                int[] way;
                double distance;

                public WalkedWay(int[] way, double distance) {
                        super();
                        this.way = way;
                        this.distance = distance;
                }
        }

        public static void main(String[] args) throws IOException, InterruptedException,
                        ExecutionException {

                long start = System.currentTimeMillis();
                AntColonyOptimization antColonyOptimization = new AntColonyOptimization();
                antColonyOptimization.start();
                System.out.println("Took: " + (System.currentTimeMillis() - start) + " ms!");
        }

}