package cn.edu.ruc.Mexplore.train;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

public class Trans extends Thread{
	private int threadId;
	
	private double rate = 0.01;
	private int sample_number = 1;
	private double margin_low = 0.1, margin_medium = 0.3, margin_high = 1;
	private double train_times = 50;
	private int measure = 2;
	private int D = 50;
	private double sum;
	private int bad_positive, bad_negative;
	private double distance_min, distance_max;
	
	private int direction;
	private DataManager dataManager;
	private LinkedHashMap<Integer, double[]> entity2vector;
	private LinkedHashMap<Integer, double[]> relation2vector;
	
	private Random rand = new Random();
	
	public Trans(int threadId){
		this.threadId = threadId;
		this.direction = 0;//threadId % 2;
	}
	
	public void initail(DataManager dataManager, LinkedHashMap<Integer, double[]> entity2vector, LinkedHashMap<Integer, double[]> relation2vector){
		this.dataManager = dataManager;
		this.entity2vector = entity2vector;
		this.relation2vector = relation2vector;
	}
	
	public void run(){
		System.out.println("Starting thread " +  threadId );
		for(int i = 0; i < train_times; i ++){
			sum = 0;
			bad_positive = 0;
			bad_negative = 0;
			distance_min = Double.MAX_VALUE;
			distance_max = 0;
			for(int relationId : relation2vector.keySet()){	
				//for(int direction : DataManager.directions){
				for(int sourceId : dataManager.getRelation2Entity().get(direction).get(relationId)){
					zoom(sourceId, relationId, direction);
				}
				//}
			}
			System.out.println("thread id=" + threadId + "\techo=" + i + "\tsum=" + sum + "\tbad positive=" + bad_positive + "\tbad negative=" + bad_negative + "\tdistance max=" + distance_max + "\tdistance min=" + distance_min);
		}
	}
	
	private void zoom(int sourceId, int relationId, int direction){
		//make similar entity close
		for(int targetId : getPositive(sourceId, relationId, direction)){
			double distance = getDistance(sourceId, relationId, targetId, direction);
			if(distance < distance_min)
				distance_min = distance;
			
			if(distance > margin_low){
				bad_positive ++;
				sum += distance - margin_low;
				//System.out.println("positive: " + distance);
				gradient(sourceId, relationId, targetId, direction, true);
			}
		}
		//make unsimilar entity far
		for(int targetId : getNegative_low(sourceId, relationId, direction)){
			double distance = getDistance(sourceId, relationId, targetId, direction);
			if(distance > distance_max)
				distance_max = distance;
			
			if(distance < margin_medium){
				bad_negative ++;
				sum += margin_medium - distance;
				//System.out.println("negative: " + distance);
				gradient(sourceId, relationId, targetId, direction, false);
			}
		}	
		//make unsimilar entity far
		for(int targetId : getNegative_high(sourceId, relationId, direction)){
			double distance = getDistance(sourceId, relationId, targetId, direction);
			if(distance > distance_max)
				distance_max = distance;
			
			if(distance < margin_high){
				bad_negative ++;
				sum += margin_high - distance;
				//System.out.println("negative: " + distance);
				gradient(sourceId, relationId, targetId, direction, false);
			}
		}
	}
	
	private ArrayList<Integer> getPositive(int sourceId, int relationId, int direction){
		ArrayList<Integer> targetIdList = new ArrayList<>(dataManager.getEntity2Relation2Entity().get(direction).get(sourceId).get(relationId));	
		return targetIdList;
	}
	
	private ArrayList<Integer> getNegative_low(int sourceId, int relationId, int direction){
		ArrayList<Integer> targetIdList = new ArrayList<>();
		int max_entity = dataManager.getRelation2Entity().get((direction + 1)%2).get(relationId).size();
		while(targetIdList.size() < sample_number){
			int targetId_negative = 0;
			do{
				targetId_negative = dataManager.getRelation2Entity().get((direction + 1)%2).get(relationId).get(rand.nextInt(max_entity));
			} while(dataManager.getEntity2Relation2Entity().get(direction).get(sourceId).get(relationId).contains(targetId_negative));
			targetIdList.add(targetId_negative);
		}
		
		return targetIdList;
	}
	
	private ArrayList<Integer> getNegative_high(int sourceId, int relationId, int direction){
		ArrayList<Integer> targetIdList = new ArrayList<>();
		int max_relation = dataManager.getRelation2Entity().get((direction + 1)%2).size();
		while(targetIdList.size() < sample_number){
			int relationId_negative = 0;
			do{
				relationId_negative = rand.nextInt(max_relation);
			} while(relationId_negative == relationId);
			
			int max_entity = dataManager.getRelation2Entity().get((direction + 1)%2).get(relationId_negative).size();
			int targetId_negative = 0;
			do{
				targetId_negative = dataManager.getRelation2Entity().get((direction + 1)%2).get(relationId_negative).get(rand.nextInt(max_entity));
			} while(dataManager.getEntity2Relation2Entity().get(direction).get(sourceId).get(relationId).contains(targetId_negative));
			
			targetIdList.add(targetId_negative);
		}
		
		return targetIdList;
	}
	
	private void gradient(int sourceId, int relationId, int targetId, int direction, boolean flag){
		if(flag){
			for (int i = 0; i  < D; i ++) {
				double x = 0;
				
				if(direction == 0){
					x = entity2vector.get(targetId)[i] - entity2vector.get(sourceId)[i] - relation2vector.get(relationId)[i];
					if (x > 0)
						x = -rate;
					else
						x = rate;
				}
				else if(direction == 1){
					x = entity2vector.get(sourceId)[i] - entity2vector.get(targetId)[i] - relation2vector.get(relationId)[i];
					if (x > 0)
						x = rate;
					else
						x = -rate;
				}
				
				relation2vector.get(relationId)[i] -= x;
				entity2vector.get(sourceId)[i] -= x;
				entity2vector.get(targetId)[i] += x;
			}
		}
		else{
			for (int i = 0; i  < D; i ++) {
				double x = 0;
				
				if(direction == 0){
					x = entity2vector.get(targetId)[i] - entity2vector.get(sourceId)[i] - relation2vector.get(relationId)[i];
					if (x > 0)
						x = rate;
					else
						x = -rate;
				}
				else if(direction == 1){
					x = entity2vector.get(sourceId)[i] - entity2vector.get(targetId)[i] - relation2vector.get(relationId)[i];
					if (x > 0)
						x = -rate;
					else
						x = rate;
				}
				
				relation2vector.get(relationId)[i] -= x;
				entity2vector.get(sourceId)[i] -= x;
				entity2vector.get(targetId)[i] += x;
			}
		}
		normalize(relation2vector.get(relationId));
		normalize(entity2vector.get(sourceId));
		normalize(entity2vector.get(targetId));
	}
		
	private double getDistance(int sourceId, int relationId, int targetId, int direction){
		double[] sourceVector = entity2vector.get(sourceId);
		double[] relationVector = relation2vector.get(relationId);
		double[] taregetVector = entity2vector.get(targetId);
		
		double distance = 0;
		if(direction == 0){
			if(measure == 1){
				for(int i = 0; i < D; i ++)
					distance += Math.abs(taregetVector[i] - sourceVector[i] - relationVector[i]);
			}
			else if(measure == 2){
				for(int i = 0; i < D; i ++)
					distance += Math.pow(taregetVector[i] - sourceVector[i] - relationVector[i], 2);
			}
		}
		else if(direction == 1){
			if(measure == 1){
				for(int i = 0; i < D; i ++)
					distance += Math.abs(taregetVector[i] - sourceVector[i] + relationVector[i]);
			}
			else if(measure == 2){
				for(int i = 0; i < D; i ++)
					distance += Math.pow(taregetVector[i] - sourceVector[i] + relationVector[i], 2);
			}
		}
		
		if(measure == 1)
			;//distance = distance * Math.log(dataManager.getEntity2Relation2Entity().get((1 + direction)%2).get(targetId).get(relationId).size() + Math.E) / Math.log(1024);
		else if(measure == 2)
			distance = Math.sqrt(distance);// * Math.log(dataManager.getEntity2Relation2Entity().get((1 + direction)%2).get(targetId).get(relationId).size() + Math.E) / Math.log(1024);
		
		return distance;
	}
	
	private void normalize(double[] vector) {
		double x = 0;
		for(int i = 0; i < vector.length; i ++)
			x += Math.pow(vector[i], 2);
		x = Math.sqrt(x);
		if(x > 1){
			for(int i = 0; i < vector.length; i ++)
				vector[i] /= x;
		}
	}
}
