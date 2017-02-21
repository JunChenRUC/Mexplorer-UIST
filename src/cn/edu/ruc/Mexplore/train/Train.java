package cn.edu.ruc.Mexplore.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import java.util.Random;

import cn.edu.ruc.Mexplore.configuration.ConfigFactory;

public class Train {
	private static int D = 50;
	private static int thread_number = 2;
	
	private static DataManager dataManager = new DataManager();
	private static LinkedHashMap<Integer, double[]> entity2vector = new LinkedHashMap<>();
	private static LinkedHashMap<Integer, double[]> relation2vector = new LinkedHashMap<>();
	
	public static void main(String[] args) throws Exception {
		intialize();
		
		prepare();
		
		
		for(int i = 0; i < thread_number; i ++){
			Trans trans = new Trans(i);
			trans.initail(dataManager, entity2vector, relation2vector);
			trans.start();
		}
		
		output();
	}
	
	private static void intialize(){
		String commonPath = ConfigFactory.getInstance().get("dir");
		
		String inputPath_entity2id = commonPath + "entity2id.txt";
		String inputPath_relation2id = commonPath + "relation2id.txt";
		String inputPath_triples = commonPath + "train.txt";
		
		dataManager.loadEntity2Id(inputPath_entity2id);
		dataManager.loadRelation2Id(inputPath_relation2id);
		dataManager.loadTriple(inputPath_triples);
		
		System.out.println("Finishing intialization!!");
	}
	
	private static void prepare(){
		for(int id = 0; id < dataManager.getEntity2Id().size(); id ++){
			double[] vector = new double[D];
			for(int i = 0; i < D; i ++)
				vector[i] = normalRand(0, 1.0 / Math.sqrt(D), -6, 6);
			
			normalize(vector);
			entity2vector.put(id, vector);
		}
		
		for(int id = 0; id < dataManager.getRelation2Id().size(); id ++){
			double[] vector = new double[D];
			for(int i = 0; i < D; i ++)
				vector[i] = normalRand(0, 1.0 / Math.sqrt(D), -6, 6);
				
			normalize(vector);
			relation2vector.put(id, vector);
		}
		System.out.println("Finishing preparation!!");
	}

			

	private static double normalRand(double miu, double sigma, double min, double max){
		double x;
		Random random = new Random();
		do{
			x = random.nextGaussian() * sigma + miu;
		} while (x <= min || x >= max);
		
		return x;
	}
	
	private static void normalize(double[] vector) {
		double x = 0;
		for(int i = 0; i < vector.length; i ++)
			x += Math.pow(vector[i], 2);
		x = Math.sqrt(x);
		if(x > 1){
			for(int i = 0; i < vector.length; i ++)
				vector[i] /= x;
		}
	}
	
	
	private static void output(){
		String commonPath = ConfigFactory.getInstance().get("dir") + ConfigFactory.getInstance().get("model") + "/";
		File writeDir = new File(commonPath);
		if (!writeDir.isDirectory())
			writeDir.mkdirs();	
		
		String outputPath_entity2vector = commonPath + "entity2vec.bern";
		String outputPath_relation2vector = commonPath + "relation2vec.bern";
		
		BufferedWriter bufferedWriter;
		File file;
		DecimalFormat df = new DecimalFormat("0.000000" ); 
		try {
			file = new File(outputPath_entity2vector);
			file.createNewFile();
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			for(Entry<Integer, double[]> entry : entity2vector.entrySet()){
				//bufferedWriter.write(entry.getKey() + "\t");
				for(int i = 0; i < D; i ++)
					bufferedWriter.write(df.format(entry.getValue()[i]) + "\t");
				bufferedWriter.write("\n");
			}
			bufferedWriter.close();
			
			file = new File(outputPath_relation2vector);
			file.createNewFile();
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			for(Entry<Integer, double[]> entry : relation2vector.entrySet()){
				//bufferedWriter.write(entry.getKey() + "\t");
				for(int i = 0; i < D; i ++)
					bufferedWriter.write(df.format(entry.getValue()[i]) + "\t");
				bufferedWriter.write("\n");
			}
			bufferedWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
