package cn.edu.ruc.Mexplore.extrator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DataFilter {
	private HashSet<String> film2person = new HashSet<>();
	
	private HashSet<String> film2attribute = new HashSet<>();
	private HashSet<String> filmSet = new HashSet<>();
	
	private HashSet<String> person2attribute = new HashSet<>();
	private HashSet<String> personSet = new HashSet<>();
	
	private HashMap<String, HashMap<String, HashSet<String>>> attribute2entityHash = new HashMap<>();
	
	private ArrayList<String> tripleList = new ArrayList<>();
	
	private HashSet<String> entitySet = new HashSet<String>();
	private HashSet<String> relationSet = new HashSet<String>();
	
	private HashMap<String, String> entity2context = new HashMap<>();
	
	public DataFilter(){
		String[] film2personArray = {"director", "producer", "editing", "cinematography", "musiccomposer", "writer", "starring"};
		String[] film2attributeArray = {"language", "country"};
		String[] person2attributeArray = {"nationality"};		
		
		for(String relation : film2personArray)
			film2person.add(relation);
		for(String relation : film2attributeArray)
			film2attribute.add(relation);
		for(String relation : person2attributeArray)
			person2attribute.add(relation);
	}
	
	public void mappingEntityFilter(String inputPath) throws IOException{		
		long beginTime = System.currentTimeMillis();
		File file = new File(inputPath);
		if(!file.isDirectory()){			
			loadMappingTriples_entityFilter(file);
		}
		else if(file.isDirectory()){
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
                 File childFile = new File(inputPath + "/" + filelist[i]); 
                 loadMappingTriples_entityFilter(childFile);
             }
		}		
		System.out.println("Finish filter mapping triples ! Time cost: " + (System.currentTimeMillis() - beginTime)/1000 );
		System.out.println("Movie size under mapping: " + filmSet.size());
		System.out.println("Person size under mapping: " + personSet.size());
	}
	
	public void mappingTripleFilter(String inputPath) throws IOException{		
		long beginTime = System.currentTimeMillis();
		File file = new File(inputPath);
		if(!file.isDirectory()){			
			loadMappingTriples_tripleFilter(file);
		}
		else if(file.isDirectory()){
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
                 File childFile = new File(inputPath + "/" + filelist[i]); 
                 loadMappingTriples_tripleFilter(childFile);
             }
		}		
		System.out.println("Finish filter mapping triples again! Time cost: " + (System.currentTimeMillis() - beginTime)/1000 );
		System.out.println("Entity without categories size: " + entitySet.size());
		System.out.println("Relation without categories size: " + relationSet.size());
	}
	
	public void categoryTripleFilter(String inputPath) throws IOException{		
		long beginTime = System.currentTimeMillis();
		File file = new File(inputPath);
		if(!file.isDirectory()){			
			loadCategoryTriples(file);
		}
		else if(file.isDirectory()){
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
                 File childFile = new File(inputPath + "/" + filelist[i]); 
                 loadCategoryTriples(childFile);
             }
		}		
		System.out.println("Finish filter category triples ! Time cost: " + (System.currentTimeMillis() - beginTime)/1000 );
		System.out.println("Entity with categories size: " + entitySet.size());
		System.out.println("Relation with categories size: " + relationSet.size());
	}
	
	public void abstractFilter(String inputPath) throws IOException{		
		long beginTime = System.currentTimeMillis();
		File file = new File(inputPath);
		if(!file.isDirectory()){			
			loadAbstract(file);
		}
		else if(file.isDirectory()){
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
                 File childFile = new File(inputPath + "/" + filelist[i]); 
                 loadAbstract(childFile);
             }
		}		
		System.out.println("Finish filter abstract triples ! Time cost: " + (System.currentTimeMillis() - beginTime)/1000 );
		System.out.println("Entity with abstract size: " + entity2context.size());
	}
	
	private void loadMappingTriples_entityFilter(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String tmpString = null;
        while ((tmpString = reader.readLine()) != null){
        	String[] tokens = tmpString.toLowerCase().split(" ");
        	//filter meaningless tuples     	
        	if(tokens.length != 4)
        		continue;
        	if(tokens[0].equals("#") || !tokens[0].contains("<http://dbpedia.org/resource/") || !tokens[1].contains("<http://dbpedia.org/ontology/") || !tokens[2].contains("<http://dbpedia.org/resource/")) 
        		continue;
        		
        	String subject = tokens[0].replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "").trim();
        	String predicate = tokens[1].replaceAll("<http://dbpedia.org/ontology/", "").replaceAll(">", "").trim();
        	String object =  tokens[2].replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "").trim();   	
        	
        	if(predicate.equals("starring") || predicate.equals("director")){
        		filmSet.add(subject);
        		personSet.add(object);
        	}
    	}
        reader.close();
	}
	
	private void loadMappingTriples_tripleFilter(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String tmpString = null;
        while ((tmpString = reader.readLine()) != null){
        	String[] tokens = tmpString.toLowerCase().split(" ");
        	if(tokens.length != 4)
        		continue;
        	if(tokens[0].equals("#") || !tokens[0].contains("<http://dbpedia.org/resource/") || !tokens[1].contains("<http://dbpedia.org/ontology/") || !tokens[2].contains("<http://dbpedia.org/resource/")) 
        		continue;
        		
        	String subject = tokens[0].replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "").trim();
        	String predicate = tokens[1].replaceAll("<http://dbpedia.org/ontology/", "").replaceAll(">", "").trim();	     	
        	String object = tokens[2].replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "").trim(); 
        	      			
        	if(filmSet.contains(subject) && film2person.contains(predicate)){
        		tripleList.add(subject + "\t" + object + "\t" + predicate);
        		
        		entitySet.add(subject);
	    		entitySet.add(object);
	    		relationSet.add(predicate);	
        	}
        	else if(filmSet.contains(subject) && film2attribute.contains(predicate)){
        		tripleList.add(subject + "\t" + object + "\t" + predicate);
        		
        		entitySet.add(subject);
        		entitySet.add(object);
        		relationSet.add(predicate);
        	}
        	
        	if(personSet.contains(subject) && person2attribute.contains(predicate)){
        		tripleList.add(subject + "\t" + object + "\t" + predicate);
        		
        		entitySet.add(subject);
        		entitySet.add(object);
        		relationSet.add(predicate);
        	}
        }
        reader.close();	
	}
	
	private void loadAbstract(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String tmpString = null;
        while ((tmpString = reader.readLine()) != null){
        	String[] tokens = tmpString.toLowerCase().split(" ");
        	//filter meaningless tuples
        	if(tokens[0].equals("#") || !tokens[0].contains("<http://dbpedia.org/resource/")) 
        		continue;
        	
        	String subject = tokens[0].replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "").trim();       	     	
        	String object = tmpString.toLowerCase().replace(tokens[0], "").replace(tokens[1], "").trim();
        	object = object.substring(object.indexOf("\"") + 1, object.lastIndexOf("\""));     	 

        	if(filmSet.contains(subject) || personSet.contains(subject))
        		entity2context.put(subject, subject + "\t" + object);
        }
        reader.close();
	}
	
	private void loadCategoryTriples(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String tmpString = null;
        while ((tmpString = reader.readLine()) != null){
        	String[] tokens = tmpString.toLowerCase().split(" ");
        	
        	if(tokens.length != 4)
        		continue;
        	if(tokens[0].equals("#") || !tokens[0].contains("<http://dbpedia.org/resource/") || !tokens[2].contains("<http://dbpedia.org/resource/")) 
        		continue;
        		
        	String subject = tokens[0].replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "").trim();       	
        	String predicate = "category";      	
        	String object = tokens[2].replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "").trim();     	 
        	        	
    		if(filmSet.contains(subject)){
    			predicate = "film" + predicate;
        		tripleList.add(subject + "\t" + object + "\t" + predicate);      
        		
        		if(!attribute2entityHash.containsKey(object))
        			attribute2entityHash.put(object, new HashMap<>());
        		if(!attribute2entityHash.get(object).containsKey(predicate))
        			attribute2entityHash.get(object).put(predicate, new HashSet<>());
        		attribute2entityHash.get(object).get(predicate).add(subject);
        		
        		relationSet.add(predicate);
        		entitySet.add(object);
        	}
    		else if(personSet.contains(subject)){
    			predicate = "person" + predicate;
    			tripleList.add(subject + "\t" + object + "\t" + predicate);      
        		
        		if(!attribute2entityHash.containsKey(object))
        			attribute2entityHash.put(object, new HashMap<>());
        		if(!attribute2entityHash.get(object).containsKey(predicate))
        			attribute2entityHash.get(object).put(predicate, new HashSet<>());
        		attribute2entityHash.get(object).get(predicate).add(subject);
        		
        		relationSet.add(predicate);
        		entitySet.add(object);
    		}
        }
        reader.close();	
	}
	
	public void writeFilteredMainTriples(String outputPath) throws IOException{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath)), "UTF-8"));
			
		for(String triple : tripleList){
			/*String[] tokens = triple.split("\t");
			if(attribute2entityHash.containsKey(tokens[1])){
				if(attribute2entityHash.get(tokens[1]).get("subject").size() <= 5000)
					writer.write(triple + "\n");
			}
			else {*/
				writer.write(triple + "\n");
			//}
		}
		
		writer.close();
	}
	
	public void writeEntity2Id(String outputPath) throws IOException{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath)), "UTF-8"));
			
		int count = 0;
		for(String entity : entitySet){
			/*if(attribute2entityHash.containsKey(entity))
				if(attribute2entityHash.get(entity).get("subject").size() > 5000)
					continue;
			*/
			if(filmSet.contains(entity))
				writer.write(entity + "\t" + (count ++) + "\t" + 0 + "\n");
			else if(personSet.contains(entity))
				writer.write(entity + "\t" + (count ++) + "\t" + 1 + "\n");
			else
				writer.write(entity + "\t" + (count ++) + "\t" + 2 + "\n");
		}
		writer.close();	
	}
	
	public void writeRelation2Id(String outputPath) throws IOException{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath)), "UTF-8"));
			
		int count = 0;
		for(String relation : relationSet)
			writer.write(relation + "\t" + (count ++) + "\n");
				
		writer.close();	
	}
	
	public void writeEntity2Abstract(String outputPath) throws IOException{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath)), "UTF-8"));
			
		for(String context : entity2context.values())
			writer.write(context +  "\n");
				
		writer.close();	
	}
}