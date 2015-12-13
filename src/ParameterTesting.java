import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

/*
 * TODO in-comment stuff if you need to run this. (Constants in AntColonyOptimization class needs to be non-final)
 */
public class ParameterTesting {

        static TreeSet<TestRecord> alphaQueue;

        public static void main(String[] args) throws IOException,
                        InterruptedException, ExecutionException {

                alphatesting();
                betatesting();
                // initPheroTest();
        }

        public static void alphatesting() throws IOException, InterruptedException,
                        ExecutionException {

                TreeSet<TestRecord> queue = new TreeSet<ParameterTesting.TestRecord>();

                for (double i = -10.0d; i <= 10.0d; i += 0.1d) {
                        double[] avg = new double[5];
                        for (int times = 0; times < 5; times++) {
                                System.out.println("Testing: " + i);
                                AntColonyOptimization opt = new AntColonyOptimization();
                                // AntColonyOptimization.ALPHA = i;
                                final double result = opt.start();
                                avg[times] = result;
                        }

                        double best = Double.MAX_VALUE;
                        double sum = 0.0;
                        for (double d : avg) {
                                sum += d;
                                if (best > d)
                                        best = d;
                        }

                        double average = sum / avg.length;
                        queue.add(new TestRecord(i, average, best));

                        System.out.println("Best alpha found until now was: "
                                        + queue.first().toString());
                }

                System.out.println("Best alpha found was: " + queue.first().toString());
                if (queue.size() > 5) {
                        TreeSet<TestRecord> temp = new TreeSet<ParameterTesting.TestRecord>();
                        for (TestRecord t : queue) {
                                temp.add(t);
                        }
                        queue = temp;
                }
                System.out.println(queue.toString());
                alphaQueue = queue;
        }

        public static void betatesting() throws IOException, InterruptedException,
                        ExecutionException {

                TreeSet<TestRecord> queue = new TreeSet<ParameterTesting.TestRecord>();

                for (TestRecord r : alphaQueue) {
                        for (double i = 0d; i <= 10.0d; i += 0.1d) {
                                double[] avg = new double[5];
                                for (int times = 0; times < 5; times++) {
                                        System.out.println("Testing: " + i);
                                        AntColonyOptimization opt = new AntColonyOptimization();
                                        // AntColonyOptimization.ALPHA = r.parameter;
                                        // AntColonyOptimization.BETA = i;
                                        double start = opt.start();
                                        avg[times] = start;
                                }

                                double best = Double.MAX_VALUE;
                                double sum = 0.0;
                                for (double d : avg) {
                                        sum += d;
                                        if (best > d)
                                                best = d;
                                }

                                double average = sum / avg.length;
                                queue.add(new TestRecord(i, average, best, r));

                                System.out.println("Best BETA found until now was: "
                                                + queue.first().toString());

                        }
                }

                System.out.println("Best BETA found was: " + queue.first().toString());
                if (queue.size() > 5) {
                        TreeSet<TestRecord> temp = new TreeSet<ParameterTesting.TestRecord>();
                        for (TestRecord t : queue) {
                                temp.add(t);
                        }
                        queue = temp;
                }
                System.out.println(queue.toString());
                alphaQueue = queue;
        }

        public static void testingPhero() throws IOException, InterruptedException,
                        ExecutionException {
                double bestAlpha = -10.0d;
                double bestResult = Double.MAX_VALUE;
                for (double i = 0d; i <= 10.0d; i += 0.1d) {
                        System.out.println("Testing: " + i);
                        AntColonyOptimization opt = new AntColonyOptimization();
                        // AntColonyOptimization.PHEROMONE_PERSISTENCE = i;
                        double result = opt.start();
                        if (result < bestResult) {
                                bestAlpha = i;
                                bestResult = result;
                        }
                        System.out.println("Best phero found was: " + bestAlpha);
                }

                System.out.println("Best phero found was: " + bestAlpha);
        }

        static class TestRecord implements Comparable<TestRecord> {
                double parameter;
                double avg;
                double best;

                TestRecord nestedAlpha;

                public TestRecord(double parameter, double avg, double best) {
                        super();
                        this.parameter = parameter;
                        this.avg = avg;
                        this.best = best;
                }

                public TestRecord(double parameter, double avg, double best,
                                TestRecord nestedAlpha) {
                        super();
                        this.parameter = parameter;
                        this.avg = avg;
                        this.best = best;
                        this.nestedAlpha = nestedAlpha;
                }

                // TODO test with best and avg
                @Override
                public int compareTo(TestRecord o) {
                        return Double.compare(best, o.best);
                }

                @Override
                public String toString() {
                        return "[parameter=" + parameter + ", avg=" + avg + ", best="
                                        + best + ", nestedAlpha=" + nestedAlpha + "]";
                }

        }

}