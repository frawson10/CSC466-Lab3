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
        double threshold = 0.3;
        c45(D, A, threshold, classVar);
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
                
            }
        }
        return null;
    }

    public static Integer selectSplittingAttribute(ArrayList<ArrayList<String>> D, 
    ArrayList<ArrayList<String>> A, double threshold, Integer classVarLoc){
        double p0 = baseEntropy(D, A, threshold, classVarLoc);
        HashMap<Integer, Double> attEntropies = new HashMap<>();
        for(int i = 0; i < A.get(0).size(); i++){
            if(i == classVarLoc || A.get(2).get(i).equals("0")){
                continue;
            }
            attEntropies.put(i, attEntropy(D, A, threshold, classVarLoc, i));
        }
        return null;
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
        
        return 0;
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
}

class Edge{
    String edge;
    Node next;
    public Edge(String e, Node n){
        this.edge = e;
        this.next = n;
    }
}