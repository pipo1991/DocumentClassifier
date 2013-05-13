package pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Pattern {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        FileWriter fw=new FileWriter("REPORT.txt");
        //Reading the stop words file
        FileReader fr=new FileReader("english-stop-words.txt");
        BufferedReader br=new BufferedReader(fr);
        HashSet<String> stopWords=new HashSet<String>();
        String[]arr=br.readLine().split(",");
        stopWords.addAll(Arrays.asList(arr));
        br.close();
        
        HashMap<String,String> classifier=new HashMap<String, String>();
        HashMap<String,Double> wordexist=new HashMap<String, Double>();
        //Number of Samples
        int n=200;
        //Reading Training Folder
        String path="Train";
        String[] folders=getfolderNamesinFolder(path);
        for(int i=0;i<folders.length;i++){
            String[] files=getfileNamesInFolder(path+"\\"+folders[i]);
            for(int j=0;j<files.length;j++){
               String []train1=trainFile(path+"\\"+folders[i]+"\\"+files[j], n,stopWords); 
               for(int z=0;z<train1.length;z++){
                   String[] str=train1[z].split(" ");
                   String word=str[0];
                   double prob= Double.parseDouble(str[1]);
                   if(wordexist.containsKey(word)){
                       double valIn=wordexist.get(word);
                       if(valIn<prob){
                         wordexist.put(word, prob);
                         classifier.put(word, folders[i]);
                       }
                   }else{
                       wordexist.put(word, prob);
                       classifier.put(word, folders[i]);
                   }
               }
            }
        }
        
        
        
        
        //Testing Part
        path="Test";
        folders=getfolderNamesinFolder(path);
        for(int i=0;i<folders.length;i++){
            int positiveR=0;
            fw.write(folders[i]+":\n");
            String[] files=getfileNamesInFolder(path+"\\"+folders[i]);
            for(int j=0;j<files.length;j++){
               Prob[]probOfClass=new Prob[folders.length];
               for(int z=0;z<folders.length;z++){
                   probOfClass[z]=new Prob(folders[z], 0);
               }
               String test1=testFile(path+"\\"+folders[i]+"\\"+files[j], classifier, wordexist,probOfClass);
               fw.write(folders[i]+"\\"+files[j]+" is trained as "+test1+".\n");
               if(test1.equals(folders[i])){
                   positiveR++;
               }
            }
            fw.write("Accuracy for this folder is "+(double)(positiveR)/files.length+".\n");
            fw.write("\n\n");
        }
    }
    
    static String[]getfolderNamesinFolder(String path){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String[] arr = new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory()) {
                arr[i] = listOfFiles[i].getName();
            }
        }
        return arr;
    }
    
    static String[] getfileNamesInFolder(String path){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String[] arr=new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                arr[i]=listOfFiles[i].getName();
            }
        }
        return arr;
    }
    
    static String[] trainFile(String path,int n,HashSet<String> stopWords) throws FileNotFoundException, IOException{
        BufferedReader br=new BufferedReader(new FileReader(path));
        String m="";
        LinkedList<Word> list=new LinkedList<Word>();
        HashSet<String> hs=new HashSet<String>();
        int len=0;
        while((m=br.readLine())!=null){
            StringTokenizer st=new StringTokenizer(m);
            int counttokens=st.countTokens();
            for(int i=0;i<counttokens;i++){
               String temp=removeNonAlpha(st.nextToken());
               if(!temp.trim().equals("") && !stopWords.contains(temp.toLowerCase())){
                   len++;
                   if(hs.contains(temp)){
                       for(int j=0;j<list.size();j++){
                          if(list.get(j).getName().equals(temp)) {
                              list.get(j).inc();
                              break;
                          }
                       }
                   }else{
                       list.add(new Word(temp, 1));
                   }
               }
            }
        }
        Collections.sort(list);
        String[] arr=new String[Math.min(list.size(), n)];
        for(int i=0;i<arr.length;i++){
          Word tempW=list.removeLast();
          arr[i]=tempW.getName()+" "+((double)(tempW.n+1)/(len+1)); 
        }
        return arr;
    }
    
    static String testFile(String path,HashMap<String,String> classifier,HashMap<String,Double> wordexist,Prob[]arr) 
            throws FileNotFoundException, IOException{
        BufferedReader br=new BufferedReader(new FileReader(path));
        String m="";
        String ans="";
        while((m=br.readLine())!=null){
            StringTokenizer st=new StringTokenizer(m);
            int counttokens=st.countTokens();
            for(int i=0;i<counttokens;i++){
               String temp=removeNonAlpha(st.nextToken());
               if(!temp.trim().equals("")){
                   if(classifier.containsKey(temp)){
                       String className=classifier.get(temp);
                       double probTemp=wordexist.get(temp);
                       for(int j=0;j<arr.length;j++){
                           if(arr[j].getName().equals(className)){
                               arr[j].probAdd(probTemp);
                               break;
                           }
                       }
                   }
               }
            }
        }
        double max=-1;
        String strMa="";
        for(int i=0;i<arr.length;i++){
            if(max<arr[i].totalProb){
                max=arr[i].totalProb;
                strMa=arr[i].getName();
            }
        }
        return strMa;
    }
    
    static String removeNonAlpha(String x){
        StringBuilder temp=new StringBuilder("");
        for(int i=0;i<x.length();i++){
            if(x.charAt(i)>='a' && x.charAt(i)<='z'){
                temp.append(x.charAt(i));
            }else if(x.charAt(i)>='A' && x.charAt(i)<='Z'){
                temp.append(x.charAt(i));
            }
        }
        return temp.toString();
    }
}

class Word implements Comparable<Word> {
    String Name;
    int n;

    public Word(String Word, int n) {
        this.Name = Word;
        this.n = n;
    }
    
    public void inc(){
        n++;
    }
    
    public String getName(){
        return Name;
    }
    
    @Override
    public int compareTo(Word o) {
        if(this.n>o.n){
            return 1;
        }
        else if(this.n==o.n){
            return 0;
        }
        return -1;
    }
}

class Prob implements Comparable<Prob> {
    String Name;
    double totalProb;

    public Prob(String Word, double n) {
        this.Name = Word;
        this.totalProb = n;
    }
    
    public void probAdd(double probA){
        totalProb+=probA;
    }
    
    public String getName(){
        return Name;
    }
    
    @Override
    public int compareTo(Prob o) {
        if(this.totalProb>o.totalProb){
            return 1;
        }
        else if(this.totalProb==o.totalProb){
            return 0;
        }
        return -1;
    }
}
