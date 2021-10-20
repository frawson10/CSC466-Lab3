import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;  

class InduceC45{
    public static void main(String[] args){
        ArrayList<ArrayList<String>> D = getData();
        ArrayList<ArrayList<String>> A = new ArrayList<>();
        ArrayList<String> restrictions = readRestrictions();
        A.add(D.get(0));
        A.add(D.get(1));
        A.add(restrictions);
        String classVar = D.get(2).get(0);
        D.remove(0);
        D.remove(0);
        D.remove(0);
        double threshold = 0.2;
        Node tree = c45(D, A, threshold, classVar);
        System.out.println(tree.toString());
    }

    public static Node c45(ArrayList<ArrayList<String>> D, 
        ArrayList<ArrayList<String>> A, double threshold, String classVar){
        int classVarLoc = -1;
        for(int i = 0; i<A.get(0).size(); i++){
            if(A.get(0).get(i).equals(classVar)){
                classVarLoc = i;
            }
        }
        // if purity then return leaf with class var
        boolean purityFlag = true;
        String tempClassVar = D.get(0).get(classVarLoc);
        for(ArrayList<String> point : D){
            if(point.get(classVarLoc) != tempClassVar){
                purityFlag = false;
                break;
            }
        }
        if(purityFlag){
            return new Node("", tempClassVar, null);
        }
        // if no more atributes other than class var, choose most frequent label
        else if(A.get(0).size() <= 1){
            String winner = popularityContest(D, A, classVarLoc);
            return new Node("", winner, null);
        }
        // try split
        else{
            Integer splittingAtt = selectSplittingAttribute(D, A, threshold, classVarLoc);
            if(splittingAtt == null){
                String winner = popularityContest(D, A, classVarLoc);
                return new Node("", winner, null);
            } else {
                //stuff after split
                Node tree = new Node(A.get(0).get(splittingAtt), "", new ArrayList<>());
                HashMap<String, ArrayList<ArrayList<String>>> splits = new HashMap<>();
                for(ArrayList<String> point : D){
                    if(splits.get(point.get(splittingAtt)) == null){
                        ArrayList<ArrayList<String>> temp = new ArrayList<>();
                        splits.put(point.get(splittingAtt), temp);
                    }
                    ArrayList<ArrayList<String>> temp = splits.get(point.get(splittingAtt));
                    temp.add(point);
                    splits.put(point.get(splittingAtt), temp);
                }
                for(Map.Entry<String, ArrayList<ArrayList<String>>> set : splits.entrySet()){
                    ArrayList<ArrayList<String>> newA = new ArrayList<>();
                    for(ArrayList<String> a : A){
                        newA.add(a);
                    }
                    newA.get(2).set(splittingAtt, "0");
                    Node subTree = c45(set.getValue(), newA, threshold, classVar);
                    tree.addEdge(set.getKey(), subTree);
                }
                return tree;
            }
        }
    }

    public static Integer selectSplittingAttribute(ArrayList<ArrayList<String>> D, 
    ArrayList<ArrayList<String>> A, double threshold, Integer classVarLoc){
        double p0 = baseEntropy(D, A, threshold, classVarLoc);
        HashMap<Integer, Double> gains = new HashMap<>();
        for(int i = 0; i < A.get(0).size(); i++){
            if(i == classVarLoc || A.get(2).get(i).equals("0")){
                continue;
            }
            gains.put(i, p0 - attEntropy(D, A, threshold, classVarLoc, i));
        }
        double maxGain = -1;
        int winningIdx = -1;
        for(Map.Entry<Integer, Double> set : gains.entrySet()){
            if(set.getValue() > maxGain){
                maxGain = set.getValue();
                winningIdx = set.getKey();
            }
        }
        if(gains.get(winningIdx) > threshold){
            return winningIdx;
        } else{
            return null;
        }
    }

    public static String popularityContest(ArrayList<ArrayList<String>> D, 
    ArrayList<ArrayList<String>> A, int classVarLoc){
        HashMap<String, Integer> score = new HashMap<>();
        for(ArrayList<String> point : D){
            if(score.get(point.get(classVarLoc)) == null){
                score.put(point.get(classVarLoc), 1);
            } else {
                score.put(point.get(classVarLoc), score.get(point.get(classVarLoc)) + 1);
            }
        }
        int frontRunner = -1;
        String leadingAtt = null;
        for(Map.Entry<String, Integer> set : score.entrySet()){
            if(set.getValue() > frontRunner){
                leadingAtt = set.getKey();
            }
        }
        return leadingAtt;
    }

    public static double log(double num){
        return Math.log(num)/Math.log(2);
    }

    public static double baseEntropy(ArrayList<ArrayList<String>> D,
    ArrayList<ArrayList<String>> A, double threshold, Integer classVarLoc){
        HashMap<String, Integer> score = new HashMap<>();
        for(ArrayList<String> point : D){
            if(score.get(point.get(classVarLoc)) == null){
                score.put(point.get(classVarLoc), 1);
            } else {
                score.put(point.get(classVarLoc), score.get(point.get(classVarLoc)) + 1);
            }
        }
        double entropy = 0.0;
        for(Map.Entry<String, Integer> set : score.entrySet()){
            double probability = Double.valueOf(set.getValue()) / Double.valueOf(D.size());
            entropy += (probability * log(1/probability));
        }
        return entropy;
    }

    public static double attEntropy(ArrayList<ArrayList<String>> D,
    ArrayList<ArrayList<String>> A, double threshold, Integer classVarLoc, int attIdx){
        HashMap<String, ArrayList<ArrayList<String>>> splits = new HashMap<>();
        for(ArrayList<String> point : D){
            if(splits.get(point.get(attIdx)) == null){
                ArrayList<ArrayList<String>> temp = new ArrayList<>();
                splits.put(point.get(attIdx), temp);
            }
            ArrayList<ArrayList<String>> temp = splits.get(point.get(attIdx));
            temp.add(point);
            splits.put(point.get(attIdx), temp);
        }
        double entropy = 0.0;
        for(Map.Entry<String, ArrayList<ArrayList<String>>> set : splits.entrySet()){
            double probability = Double.valueOf(set.getValue().size()) / Double.valueOf(D.size());
            entropy += (probability * baseEntropy(set.getValue(), A, threshold, classVarLoc));
        }
        return entropy;
    }

    public static ArrayList<ArrayList<String>> getData(){
        Scanner sc;
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        try {
            sc = new Scanner(new File("adult-stretch.csv"));
            while (sc.hasNextLine()){
                ArrayList<String> lineVals = new ArrayList<>();
                String[] line = sc.nextLine().split(",");
                for(String s : line){
                    lineVals.add(s);
                }
                data.add(lineVals);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static ArrayList<String> readRestrictions(){
        Scanner sc;
        ArrayList<String> restrictions = new ArrayList<>();
        try {
            sc = new Scanner(new File("restrictions.txt"));
            while (sc.hasNextLine()){
                ArrayList<String> lineVals = new ArrayList<>();
                String[] line = sc.nextLine().split(",");
                for(String s : line){
                    lineVals.add(s);
                }
                restrictions = lineVals;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return restrictions;
    }
}

class Node{
    String attribute;
    String decision;
    ArrayList<Edge> edges;
    public Node(String a, String d, ArrayList<Edge> l){
        if(a != ""){
            this.attribute = a;
        }
        if(d != ""){
            this.decision = d;
        }
        if(l != null){
            this.edges = l;
        }
    }

    public void addEdge(String label, Node subtree){
        this.edges.add(new Edge(label, subtree));
    }

    public String toString(){
        if(edges == null){
            return "\nLeaf: " + decision;
        } else{
            return "Node: " + attribute + "\nedges: " + edges.toString();
        }
    }
}

class Edge{
    String edge;
    Node next;
    public Edge(String e, Node n){
        this.edge = e;
        this.next = n;
    }

    public String toString(){
        return "\n" + edge + " node: " + next.toString();
    }
}