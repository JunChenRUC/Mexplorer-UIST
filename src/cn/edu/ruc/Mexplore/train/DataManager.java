package cn.edu.ruc.Mexplore.train;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DataManager {
	public static int[] directions = {0, 1};
	private HashMap<String, Integer> entity2id = new HashMap<>();
	private HashMap<String, Integer> relation2id = new HashMap<>();
	private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> relation2entity = new HashMap<>();
	private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> entity2relation2entity = new HashMap<>();
	
	public DataManager(){
		for(int direction : directions){
			relation2entity.put(direction, new HashMap<>());
			entity2relation2entity.put(direction, new HashMap<>());
		}
	}
	
	public void loadEntity2Id(String inputPath){
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputPath)), "UTF-8"));
			String tmpString = null;
			while((tmpString = reader.readLine()) != null) {
				String[] tokens = tmpString.split("\t");
				
				entity2id.put(tokens[0], Integer.parseInt(tokens[1]));
			}
			reader.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadRelation2Id(String inputPath){
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputPath)), "UTF-8"));		
        	String tmpString = null;
            while((tmpString = reader.readLine()) != null) {
            	String[] tokens = tmpString.split("\t");
            	
            	relation2id.put(tokens[0], Integer.parseInt(tokens[1]));
            }
            reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadTriple(String inputPath){
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputPath)), "UTF-8"));		
        	String tmpString = null;
            while((tmpString = reader.readLine()) != null) {
            	String[] tokens = tmpString.split("\t");
            	
            	int subjectId = entity2id.get(tokens[0]);
            	int objectId = entity2id.get(tokens[1]);
            	int relationId = relation2id.get(tokens[2]);
            	
            	for(int direction : directions){
            		if(direction == 0){
            			//relation 2 id
            			if(!relation2entity.get(direction).containsKey(relationId))
            				relation2entity.get(direction).put(relationId, new ArrayList<>());
            			relation2entity.get(direction).get(relationId).add(subjectId);
            			
            			//entity 2 relation 2 entity
            			if(!entity2relation2entity.get(direction).containsKey(subjectId))
            				entity2relation2entity.get(direction).put(subjectId, new HashMap<>());
            			if(!entity2relation2entity.get(direction).get(subjectId).containsKey(relationId))
            				entity2relation2entity.get(direction).get(subjectId).put(relationId, new HashSet<>());
        				entity2relation2entity.get(direction).get(subjectId).get(relationId).add(objectId);          			
            		}
            		else if(direction == 1){
            			if(!relation2entity.get(direction).containsKey(relationId))
            				relation2entity.get(direction).put(relationId, new ArrayList<>());
            			relation2entity.get(direction).get(relationId).add(objectId);
            			
            			//entity 2 relation 2 entity
            			if(!entity2relation2entity.get(direction).containsKey(objectId))
            				entity2relation2entity.get(direction).put(objectId, new HashMap<>());
            			if(!entity2relation2entity.get(direction).get(objectId).containsKey(relationId))
            				entity2relation2entity.get(direction).get(objectId).put(relationId, new HashSet<>());
        				entity2relation2entity.get(direction).get(objectId).get(relationId).add(subjectId);
            		}
            	}
            }
            reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HashMap<String, Integer> getEntity2Id(){
		return entity2id;
	}
	
	public HashMap<String, Integer> getRelation2Id(){
		return relation2id;
	}
	
	public HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> getRelation2Entity(){
		return relation2entity;
	}
	
	public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> getEntity2Relation2Entity(){
		return entity2relation2entity;
	}
}
