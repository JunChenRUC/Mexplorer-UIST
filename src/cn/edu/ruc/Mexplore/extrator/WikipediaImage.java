package cn.edu.ruc.Mexplore.extrator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import cn.edu.ruc.Mexplore.data.LoadDictionary;

public class WikipediaImage {
	private static String commonPath = "D://";
	private static String dataset = "DBpedia";
	private static String version = "_2014";
	private static String inputPath = dataset + version + "/instance_processed/";
	
	public static void main(String[] args) throws IOException {
		String inputPath_entity2id = commonPath + inputPath + "entity2id.txt";
		String outputPath_entity2image = commonPath + inputPath + "entity2image.txt";
		
		LoadDictionary loadDictionary = new LoadDictionary();
		loadDictionary.loadEntity2Id(inputPath_entity2id);
		HashMap<String, Integer> entity2id = loadDictionary.getEntity2Id();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath_entity2image)), "UTF-8"));
        
        for(String query : entity2id.keySet()) {
        	String json = "";

    		String htmlUrl = "http://en.wikipedia.org/w/index.php?search=" + query;
    		json = getHtmlContent(htmlUrl);
        	
    		System.out.println(query + "\t" + json);
        	//writer.write(json + "\n");
        }
        writer.close();
	}
	
	public static String getHtmlContent(String htmlUrl) {  
        URL url;  
        String tmpString;  
        StringBuffer sb = new StringBuffer();  
        try {  
            url = new URL(htmlUrl);  
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));  
            while ((tmpString = in.readLine()) != null) {  
                sb.append(tmpString);  
                System.out.println(tmpString);
            }  
            in.close();  
        } catch (final MalformedURLException me) {  
            System.out.println("error!");  
            me.getMessage();  
        } catch (final IOException e) {  
            e.printStackTrace();  
        }  
        return sb.toString();  
    }
}
