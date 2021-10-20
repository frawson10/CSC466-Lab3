import java.io.FileReader;
import java.util.*;
import java.util.Map.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.io.*;

class Classify{

    // args[0]: csv file
    // args[1]: json file
    public static void main(String[] args) throws Exception {
        // parsing json file and typecasting to JSONObject
        JSONObject obj = (JSONObject) new JSONParser().parse(new FileReader(args[1]));
        JSONObject node1 = (JSONObject) obj.get("node");
        Node n = new Node((String)node1.get("var"), "", (ArrayList<Edge>)node1.get("edges"));
        ConfusionMatrix matrix = new ConfusionMatrix();

        // parsing csv file
        List<String> colNames = new ArrayList<>();
        CategoryAttr cat = null;
        String line = "";
        String splitBy = ",";
        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            int currentLine = 0;
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                String[] entry = line.split(splitBy);    // use comma as separator
                if (currentLine == 0)
                    colNames = Arrays.asList(entry);
                else if (currentLine == 1) {
                    currentLine++;
                    continue;
                }
                else if (currentLine == 2 && !entry[0].isEmpty())
                    cat = new CategoryAttr(entry[0], getColIndex(entry[0], colNames));
                else {
                    getPrediction(n, entry, colNames, cat, matrix);
                }
                currentLine++;
            }
            System.out.println("Total Number Of Records Classified: " + matrix.total);
            System.out.println("Total Number Of Records Correctly Classified: " + matrix.corrCount);
            System.out.println("Total Number Of Records Incorrectly Classified: " + matrix.incorrCount);
            System.out.println("Overall Accuracy of the Classifier: " + matrix.corrCount/matrix.total);
            System.out.println("Overall Error Rate of the Classifier: " + (1-(matrix.corrCount/matrix.total)));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getColIndex(String colName, List<String> colNames) {
        for (int i=0; i<colNames.size(); i++) {
            if (colName.equals(colNames.get(i)))
                return i;
        }
        return -1;
    }

    public static void getPrediction(Node n, String[] entry, List<String> colNames, CategoryAttr cat, ConfusionMatrix m){
        boolean isLeaf = false;
        while(isLeaf == false) {
            int startIndex = getColIndex(n.attribute, colNames);
            String edgeName = entry[startIndex];
            for (int i=0; i<n.edges.toArray().length; i++) {
                if (edgeName.equals(n.edges.get(i).edge)) { // if we are on correct node
                    if (!n.edges.get(i).next.decision.isEmpty()) { // if it is a leaf node
                        if (!cat.name.isEmpty()) { // if it has category attribute
                            String decision = n.edges.get(i).next.decision;
                            if (entry[cat.col] == decision)
                                m.corrCount++;
                            else
                                m.incorrCount++;
                            m.total++;
                        }
                        isLeaf = true;
                    }
                    else { // if it is not a leaf node
                        n = new Node(n.edges.get(i).next.attribute, "", n.edges.get(i).next.edges);

                    }
                }
            }
        }
    }

    static class CategoryAttr {
        String name;
        int col;
        public CategoryAttr(String n, int c) {
            name = n;
            col = c;
        }
    }

    static class ConfusionMatrix {
        int corrCount;
        int incorrCount;
        int total;
        public ConfusionMatrix() {
            corrCount = 0;
            incorrCount = 0;
            total = 0;
        }
    }
}
