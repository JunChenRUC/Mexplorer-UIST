package cn.edu.ruc.Mexplore.run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import cn.edu.ruc.Mexplore.data.DataManager;
import cn.edu.ruc.Mexplore.domain.Entity;
import cn.edu.ruc.Mexplore.domain.Feature;
import cn.edu.ruc.Mexplore.domain.FeatureKey;
import cn.edu.ruc.Mexplore.domain.Relation;
import cn.edu.ruc.Mexplore.domain.Result;
import cn.edu.ruc.Mexplore.utility.QuickSortEntity;
import cn.edu.ruc.Mexplore.utility.QuickSortFeature;
import cn.edu.ruc.Mexplore.utility.QuickSortRelation;

public class Process {
	private DataManager dataManager;
	
	public Process() {
		install();
	}
	
	public void install() {
		dataManager = new DataManager();
	}

	public Result getResult(ArrayList<String> queryEntityList, ArrayList<String> queryFeatureList){	
		Result result = new Result();
		
		//Query parser
		for(String queryEntity : queryEntityList)
			result.addQueryEntity(dataManager.getEncodedEntity(queryEntity));
		
		for(String queryFeature : queryFeatureList)
			result.addQueryFeature(dataManager.getEncodedFeature(queryFeature));
		
		result.setResultEntityList(getEntity(result.getQueryEntityList(), result.getQueryFeatureList()));
		
		result.setResultFeatureList(getExplanation(result.getQueryEntityList(), result.getResultEntityList()));
		
		for(Entity queryEntity : result.getQueryEntityList()){
			for(Feature feature : result.getResultFeatureList())
				if(feature.getEntitySet().contains(queryEntity))
					queryEntity.addFeature(new Feature(feature.getRelation(), feature.getTarget(), feature.getScore()));
		}
		
		for(Entity resultEntity : result.getResultEntityList()){
			for(Feature feature : result.getResultFeatureList())
				if(feature.getEntitySet().contains(resultEntity))
					resultEntity.addFeature(new Feature(feature.getRelation(), feature.getTarget(), feature.getScore()));
		}
		
		dataManager.decodeResult(result);	
		
		return result;
	}
	
	//get entities by features or entities
	public ArrayList<Entity> getEntity(ArrayList<Entity> queryEntityList, ArrayList<Feature> queryFeatureList){
		ArrayList<Entity> entity2distanceList = new ArrayList<>();
		if(!queryEntityList.isEmpty() && !queryFeatureList.isEmpty()){
			entity2distanceList = getFeature2Entity(queryEntityList, queryFeatureList);
			int count = 0;
			for(Entity result : getEntity2Entity(queryEntityList)){
				entity2distanceList.get(count).setScore(entity2distanceList.get(count).getScore() + result.getScore());
				count ++;
			}
		}
		else if(!queryFeatureList.isEmpty() && queryEntityList.isEmpty())
			entity2distanceList = getFeature2Entity(queryEntityList, queryFeatureList);
		else if(queryFeatureList.isEmpty() && !queryEntityList.isEmpty())
			entity2distanceList = getEntity2Entity(queryEntityList);
		
		return rankEntity(entity2distanceList, DataManager.output_size_entity);
	}
	
	public ArrayList<Entity> getEntity2Entity(ArrayList<Entity> queryEntityList){
		//HashMap<Integer, double[]> query2vector_feature = new HashMap<>();
		HashMap<Integer, double[]> query2vector_entity = new HashMap<>();
		
		for(Entity queryEntity : queryEntityList)		
			query2vector_entity.put(queryEntity.getId(), dataManager.getVector(queryEntity.getId(), queryEntity.getType()));
		
		ArrayList<Entity> entity2distanceList = new ArrayList<>();
		
		for(Entry<Integer, double[]> entity2vectorEntry : dataManager.getEntity2Vector().entrySet()){
			if(query2vector_entity.containsKey(entity2vectorEntry.getKey()))
				continue;
			
			double score = 0;
			for(double[] queryVector : query2vector_entity.values())
				score += getScoreOfEntities(queryVector, entity2vectorEntry.getValue());
			
			entity2distanceList.add(new Entity(DataManager.entity_type, entity2vectorEntry.getKey(), score));
		}	
		
		return entity2distanceList;
	}
	
	public ArrayList<Entity> getFeature2Entity(ArrayList<Entity> queryEntityList, ArrayList<Feature> queryFeatureList){
		HashMap<Integer, double[]> query2vector_entity = new HashMap<>();
		
		for(Entity queryEntity : queryEntityList)		
			query2vector_entity.put(queryEntity.getId(), dataManager.getVector(queryEntity.getId(), queryEntity.getType()));
				
		HashMap<double[], double[]> query2vector_feature = new HashMap<double[], double[]>();
		for(Feature queryFeature : queryFeatureList){
			double[] targetVector = dataManager.getVector(queryFeature.getTarget().getId(), queryFeature.getTarget().getType());
			double[] relationVector = dataManager.getVector(queryFeature.getRelation().getId(), queryFeature.getRelation().getType());
			query2vector_feature.put(targetVector, relationVector);
		}
		
		ArrayList<Entity> entity2distanceList = new ArrayList<>();
						
		for(Entry<Integer, double[]> entity2vectorEntry : dataManager.getEntity2Vector().entrySet()){
			if(query2vector_entity.containsKey(entity2vectorEntry.getKey()))
				continue;
			
			double score = 0;
			double[] sourceVector = entity2vectorEntry.getValue();
			
			for(Entry<double[], double[]> feature2vectorEntry : query2vector_feature.entrySet()){						
				double[] targetVector = feature2vectorEntry.getKey();
				double[] relationVector = feature2vectorEntry.getValue();
				
				score += getScoreOfEntities(getHead(targetVector, relationVector), sourceVector);
			}
			entity2distanceList.add(new Entity(DataManager.entity_type, entity2vectorEntry.getKey(), score));
		}
		
		return entity2distanceList;
	}
	
	//get explanation
	public ArrayList<Feature> getExplanation(ArrayList<Entity> queryEntityList, ArrayList<Entity> resultEntityList){		
		ArrayList<Feature> featureList = new ArrayList<>();
		
		for(Relation relation : getRelation(queryEntityList, resultEntityList))
			featureList.addAll(getFeature(queryEntityList, resultEntityList, relation));
	
		return featureList;
	}
	
	public ArrayList<Relation> getRelation(ArrayList<Entity> queryEntityList, ArrayList<Entity> resultEntityList){
		HashMap<Integer, Relation> relationHash = new HashMap<>();
		
		int triple_size_max = 1, triple_size_min = Integer.MAX_VALUE;	
		for(Entity queryEntity : queryEntityList){
			for(int i = 0; i < 2; i ++){
				if(dataManager.getTripleHash().get(i).containsKey(queryEntity.getId())){
					for(HashSet<Integer> entitySet : dataManager.getTripleHash().get(i).get(queryEntity.getId()).values()){
						if(entitySet.size() > triple_size_max)
							triple_size_max = entitySet.size();
						if(entitySet.size() < triple_size_min)
							triple_size_min = entitySet.size();	
					}
				}
			}
		}
		
		for(Entity resultEntity : resultEntityList){
			for(int i = 0; i < 2; i ++){
				if(dataManager.getTripleHash().get(i).containsKey(resultEntity.getId())){
					for(HashSet<Integer> entitySet : dataManager.getTripleHash().get(i).get(resultEntity.getId()).values())	{
						if(entitySet.size() > triple_size_max)
							triple_size_max = entitySet.size();
						if(entitySet.size() < triple_size_min)
							triple_size_min = entitySet.size();	
					}
				}
			}
		}
		
		for(Entity queryEntity : queryEntityList){
			for(int i = 0; i < 2; i ++){
				if(dataManager.getTripleHash().get(i).containsKey(queryEntity.getId())){
					for(Entry<Integer, HashSet<Integer>> relation2entityEntry : dataManager.getTripleHash().get(i).get(queryEntity.getId()).entrySet()){
						Relation relation = new Relation(DataManager.relation_type, relation2entityEntry.getKey(), i);
						
						double score = (double)relation2entityEntry.getValue().size() / (triple_size_min + triple_size_max);;
						score = (- score * Math.log(score)) * queryEntity.getScore();
						relation.setScore(score);

						if(!relationHash.containsKey(relation.getId()))
							relationHash.put(relation.getId(), relation);
						else
							relationHash.get(relation.getId()).setScore(relationHash.get(relation.getId()).getScore() + relation.getScore());
					}
				}
			}
		}
		
		for(Entity resultEntity : resultEntityList){
			for(int i = 0; i < 2; i ++){
				if(dataManager.getTripleHash().get(i).containsKey(resultEntity.getId())){
					for(Entry<Integer, HashSet<Integer>> relation2entityEntry : dataManager.getTripleHash().get(i).get(resultEntity.getId()).entrySet()){
						Relation relation = new Relation(DataManager.relation_type, relation2entityEntry.getKey(), i);
						
						double score = (double)relation2entityEntry.getValue().size() / (triple_size_min + triple_size_max);;
						score = (- score * Math.log(score)) * resultEntity.getScore();
						relation.setScore(score);

						if(!relationHash.containsKey(relation.getId()))
							relationHash.put(relation.getId(), relation);
						else
							relationHash.get(relation.getId()).setScore(relationHash.get(relation.getId()).getScore() + relation.getScore());
					}
				}
			}
		}

		return rankRelation(new ArrayList<Relation>(relationHash.values()), DataManager.output_size_feature);
	}
	
	public ArrayList<Feature> getFeature(ArrayList<Entity> queryEntityList, ArrayList<Entity> resultEntityList, Relation relation){
		HashMap<FeatureKey, Feature> featureHash = new HashMap<>();
		
		for(Entity queryEntity : queryEntityList){
			if(!dataManager.getTripleHash().get(relation.getDirection()).containsKey(queryEntity.getId()))
				continue;
			if(!dataManager.getTripleHash().get(relation.getDirection()).get(queryEntity.getId()).containsKey(relation.getId()))
				continue;
			for(int targetId : dataManager.getTripleHash().get(relation.getDirection()).get(queryEntity.getId()).get(relation.getId())){
				Entity target = dataManager.getEncodedEntity(targetId);
				target.setScore(getSimilarity(queryEntity, relation, target));
				Feature feature = new Feature(relation, target);
				feature.setScore(target.getScore() * queryEntity.getScore() * relation.getScore());
				
				//queryEntity.addFeature(new Feature(feature.getRelation(), feature.getTarget(), feature.getScore()));
				FeatureKey featureKey = new FeatureKey(feature.getRelation().getId(), feature.getTarget().getId());
				if(!featureHash.containsKey(featureKey)){
					feature.addEntity(new Entity(queryEntity.getType(), queryEntity.getId(), queryEntity.getScore()));
					featureHash.put(featureKey, feature);
				}
				else{
					Feature oldFeature = featureHash.get(featureKey);
					oldFeature.addEntity(new Entity(queryEntity.getType(), queryEntity.getId(), queryEntity.getScore()));
					oldFeature.setScore(oldFeature.getScore() + feature.getScore());
				}
			}
		}
		
		for(Entity resultEntity : resultEntityList){
			if(!dataManager.getTripleHash().get(relation.getDirection()).containsKey(resultEntity.getId()))
				continue;
			if(!dataManager.getTripleHash().get(relation.getDirection()).get(resultEntity.getId()).containsKey(relation.getId()))
				continue;
			for(int targetId : dataManager.getTripleHash().get(relation.getDirection()).get(resultEntity.getId()).get(relation.getId())){
				Entity target = dataManager.getEncodedEntity(targetId);
				target.setScore(getSimilarity(resultEntity, relation, target));
				Feature feature = new Feature(relation, target);
				feature.setScore(target.getScore() * resultEntity.getScore() * relation.getScore());
				
				//resultEntity.addFeature(new Feature(feature.getRelation(), feature.getTarget(), feature.getScore()));
				FeatureKey featureKey = new FeatureKey(feature.getRelation().getId(), feature.getTarget().getId());
				if(!featureHash.containsKey(featureKey)){
					feature.addEntity(new Entity(resultEntity.getType(), resultEntity.getId(), resultEntity.getScore()));
					featureHash.put(featureKey, feature);
				}
				else{
					Feature oldFeature = featureHash.get(featureKey);
					oldFeature.addEntity(new Entity(resultEntity.getType(), resultEntity.getId(), resultEntity.getScore()));
					oldFeature.setScore(oldFeature.getScore() + feature.getScore());
				}
			}
		}

		return rankFeature(new ArrayList<Feature>(featureHash.values()), DataManager.output_size_feature);
	}
	
	public double getSimilarity(String string1, String string2){
		Entity entity1 = dataManager.getEncodedEntity(string1);
		Entity entity2 = dataManager.getEncodedEntity(string2);
		
		return getScoreOfEntities(dataManager.getVector(entity1.getId(), entity1.getType()), dataManager.getVector(entity2.getId(), entity2.getType()));
	}
	
	public double getSimilarity(Entity entity1, Relation relation, Entity entity2){	
		double[] rVector = dataManager.getVector(relation.getId(), DataManager.relation_type);
		
		double[] eVector1 = dataManager.getVector(entity1.getId(), entity1.getType());
		double[] eVector2 = dataManager.getVector(entity2.getId(), entity2.getType());
		
		double score;
		if(relation.getDirection() == 0)
			score = getScoreOfEntities(getTail(eVector1, rVector), eVector2);
		else
			score = getScoreOfEntities(getHead(eVector1, rVector), eVector2);
		
		return score;
	}
	
	public double[] getHead(double[] eVector_tail, double[] rVector){
		double[] eVector_head = new double[DataManager.D_relation];
		for(int i = 0; i < DataManager.D_relation; i ++)
			eVector_head[i] = eVector_tail[i] - rVector[i];
		return eVector_head;
	}
	
	public double[] getTail(double[] eVector_head, double[] rVector){
		double[] vector_tail = new double[DataManager.D_relation];
		for(int i = 0; i < DataManager.D_relation; i ++)
			vector_tail[i] = eVector_head[i] + rVector[i];
		return vector_tail;
	}
	
	public double getScoreOfEntities(double[] eVector1, double[] eVector2){
		double score = 0;
		int dimension = eVector1.length;
		for(int i = 0; i < dimension; i ++)
			score += Math.pow(eVector1[i] - eVector2[i], 2);
		return (Math.sqrt(dimension) - Math.sqrt(score)) / Math.sqrt(dimension);
	}
	
	public ArrayList<Entity> rankEntity(ArrayList<Entity> data, int k){  	
		k = (k > data.size() ? data.size() : k);
		
		Entity[] topk = new Entity[k];
        		
		for(int i = 0; i < k; i++)
            topk[i] = data.get(i);   
          
        QuickSortEntity quickSort = new QuickSortEntity(); 
        quickSort.initialSort(topk);
           
        for(int i = k; i < data.size();i++){   
            if(data.get(i).getScore() > quickSort.getMin().getScore())
            	quickSort.replace(data.get(i)); 
        }  
        
        ArrayList<Entity> resultList = new ArrayList<>();
        for(Entity result : topk)
        	resultList.add(result);
        
        return resultList;  
	}  
	
	public ArrayList<Relation> rankRelation(ArrayList<Relation> arrayList, int k){  	
		k = (k > arrayList.size() ? arrayList.size() : k);
		
		Relation[] topk = new Relation[k];
        		
		for(int i = 0; i < k; i++)
            topk[i] = arrayList.get(i);   
       
        QuickSortRelation quickSort = new QuickSortRelation(); 
        quickSort.initialSort(topk);
           
        for(int i = k; i < arrayList.size();i++){   
            if(arrayList.get(i).getScore() > quickSort.getMin().getScore())
            	quickSort.replace(arrayList.get(i)); 
        }  
        
        ArrayList<Relation> relationList = new ArrayList<>();
        for(Relation relation : topk)
        	relationList.add(relation);
        
        return relationList;  
	} 
	
	public ArrayList<Feature> rankFeature(ArrayList<Feature> data, int k){  	
		k = (k > data.size() ? data.size() : k);
		
		Feature[] topk = new Feature[k];
        		
		for(int i = 0; i < k; i++)
            topk[i] = data.get(i);   
          
        QuickSortFeature quickSort = new QuickSortFeature(); 
        quickSort.initialSort(topk);
           
        for(int i = k; i < data.size();i++){   
            if(data.get(i).getScore() > quickSort.getMin().getScore())
            	quickSort.replace(data.get(i)); 
        }  
        
        ArrayList<Feature> featureList = new ArrayList<>();
        for(Feature result : topk)
        	featureList.add(result);
        
        return featureList;  
	} 
}
