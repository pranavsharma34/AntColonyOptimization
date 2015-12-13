import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SolutionWriter {

        /**
         * @param args
         * @throws IOException
         * @throws NumberFormatException
         */
        public static void main(String[] args) throws NumberFormatException, IOException {

                int[] arr = new int[] { 29, 22, 19, 49, 28, 15, 45, 43, 33, 34, 35, 38, 39, 37, 36, 47, 23,
                                4, 14, 5, 3, 24, 11, 27, 26, 25, 46, 13, 12, 51, 10, 50, 32, 42, 9, 8, 7, 40, 18,
                                44, 31, 48, 0, 21, 30, 17, 2, 16, 20, 41, 6, 1 };

                final BufferedReader br = new BufferedReader(new FileReader(new File("files/berlin52.tsp")));

                final ArrayList<AntColonyOptimization.Record> records = new ArrayList<AntColonyOptimization.Record>();
                boolean readAhead = false;
                String line;
                while ((line = br.readLine()) != null) {

                        if (line.equals("EOF")) {
                                break;
                        }

                        if (readAhead) {
                                String[] split = line.split(" ");
                                records.add(new AntColonyOptimization.Record(Double.parseDouble(split[1]), Double
                                                .parseDouble(split[2])));
                        }

                        if (line.equals("NODE_COORD_SECTION")) {
                                readAhead = true;
                        }
                }

                br.close();

                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("files/my.tsp")));

                for (int j = 0; j < arr.length; j++) {
                        int i = arr[j];
                        writer.write(records.get(i).x + " " + records.get(i).y + "\n");
                }
                writer.write(records.get(arr[0]).x + " " + records.get(arr[0]).y + "\n");
                writer.flush();
                writer.close();
        }
}