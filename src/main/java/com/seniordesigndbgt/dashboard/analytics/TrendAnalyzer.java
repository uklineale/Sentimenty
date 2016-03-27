package com.seniordesigndbgt.dashboard.analytics;

import com.seniordesigndbgt.dashboard.dao.PressDAO;
import com.seniordesigndbgt.dashboard.dao.TrendDAO;
import com.seniordesigndbgt.dashboard.dao.TwitterDAO;
import com.seniordesigndbgt.dashboard.model.Press;
import com.seniordesigndbgt.dashboard.model.Trend;
import com.seniordesigndbgt.dashboard.model.Twitter;
import org.springframework.beans.factory.annotation.Autowired;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TrendAnalyzer {

    public String algorithm = "x mentions in y time, if x > threshold";
    private static Map<String, Integer> frequencyMap;
    private List<Map.Entry<String,Integer>> trends;
    private static final int THRESHOLD = 2;
    private static final int NUM_OF_KEYWORDS = 4;
    @Autowired
    private PressDAO _pressDao;
    @Autowired
    private TwitterDAO _twitterDao;
    @Autowired
    private TrendDAO _trendDao;



    public TrendAnalyzer() {
        this.frequencyMap = new LinkedHashMap<String, Integer>();
    }

    public void findNewTrends() {
        System.out.println("\nStart");
    //Get press keywords
        List<Press> pressList = _pressDao.getAll();
        String allKeywords = "";
        for (Press article : pressList) {
            if (article.getKeywords() != null) {
                String[] articleKeywordSplit = article.getKeywords().split(",");
                allKeywords += article.getKeywords() + " ";
                for (String keyword : articleKeywordSplit) {
                    allKeywords += keyword + " ";
                }
            }
        }
        allKeywords = allKeywords.replace(",", " ");



        String keyString = findKeywords(allKeywords);
        System.out.println("all keywords: " + keyString);
        String[] keywordSplit = keyString.split(",");
        List<Trend> trends = new ArrayList<Trend>();
        for (String keyword : keywordSplit) {
            String mentions = "";
            for (Press article : pressList) {
                if (article.getKeywords() != null && article.getKeywords().contains(keyword)) {
                    mentions += article.getId() + ", ";
                }
            }
            trends.add(new Trend(keyword, mentions));
        }
        for (Trend trend : trends) {
            _trendDao.save(trend);
        }
        for (Trend databaseTrend : _trendDao.getAll()) {
            System.out.println(databaseTrend.getMentions());
        }

    }
    /**
    * Finds the top constant number of keywords, returned as a comma separated string*/
    public String findKeywords(String text){
        List<Map.Entry<String,Integer>> allWords = sortTrends(updateFrequencyMap(text,
                new LinkedHashMap<String, Integer>()));
        String[] allWordsArray = new String[allWords.size()];
                for (int i = 0; i < allWords.size(); i++){
                        allWordsArray[i] = allWords.get(i).getKey();
                    }
                InputStream modelIn;
                POSModel model = null;
                try {
                        modelIn = new FileInputStream("en-pos-maxent.bin");
                        model = new POSModel(modelIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                POSTaggerME tagger = new POSTaggerME(model);
                String[] tagged = tagger.tag(allWordsArray);
                List<Map.Entry<String,Integer>> nounsAndCounts = new LinkedList<Map.Entry<String, Integer>>();
                //Check if the words are nouns
                        for (int i = 0; i < tagged.length; i++){
                        if (tagged[i].equals("NN") || tagged[i].equals("NNS") ||
                                        tagged[i].equals("NNP") || tagged[i].equals("NNPS")){
                                nounsAndCounts.add(allWords.get(i));
                          }
                    }
        List<String> keyWords = new LinkedList<String>();
        for (int i = 0; i < NUM_OF_KEYWORDS; i++){
            if ( i < nounsAndCounts.size())
                keyWords.add(nounsAndCounts.get(i).getKey());
        }
        String keyWordsString = "";
        for (String word : keyWords) {
            keyWordsString += word;
            keyWordsString += ",";
        }
        keyWordsString = keyWordsString.substring(0,keyWordsString.length()-1);
        return keyWordsString;
    }

    public Map<String, Integer> getFrequencyMap() {
        return frequencyMap;
    }

    public Map<String, Integer> updateFrequencyMap(String text, Map<String,Integer> map){
        //Sanitize input
        text = sanitizeInput(text);
        String[] splitArray = text.split(" ");


        for (String currWord : splitArray) {
            if (map.containsKey(currWord)) {
                int currCount = map.get(currWord);
                map.put(currWord, currCount + 1);
            } else {
                map.put(currWord, 1);
            }
        }
//        printFrequencyMap(map);
        return map;

    }
    /**
     * Gets rid of punctuation and articles
     * List of articles is incomplete*/
    public String sanitizeInput(String text){
        text = text.toLowerCase();
        String[] stopList = {".",",","!","?"," in "," the "," to "," a "," an "," as "," and "," has "," of "," or ",
                " for "," up "," with "," on "," off "," into "," it "," have "," by ","is ","this ", "said ", "that ", "deustche", "bank ",
                " at "};
        for (int i = 0; i < stopList.length; i++){
            text = text.replace(stopList[i], "");
        }
        return text;
    }

    /*
    * Takes map data, sorts it into List, returns List
    * */
    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> sortTrends(Map<K,V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        return list;
    }

    private void printFrequencyMap(Map<String,Integer> map){
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

}
