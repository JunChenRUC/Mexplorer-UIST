package cn.edu.ruc.Mexplore.extrator;

import java.io.File;
import java.io.IOException;

public class BatchProcess {
	private static String inputPath = "D://DBpedia_2014/";
	private static String outputPath = "D://kb_data/instance_processed/";
	
	public static void main(String[] args) throws IOException {
		//Path
		File dir = new File(outputPath);
		if(!dir.exists())		
			dir.mkdirs();
		
		//input
		//String inputPath_data_type = commonPath + inputPath + "instance_type";
		String inputPath_mapping = inputPath + "instance_mapping";
		String inputPath_category = inputPath + "instance_category";
		String inputPath_abstract = inputPath + "instance_abstract";
		
		//ouput
		//String outputPath_data_type_filter = commonPath + outputPath + "instance_type_filter.txt";	
		String outputPath_mapping_filter = outputPath + "train.txt";	
		String outputPath_entity2id = outputPath + "entity2id.txt";
		String outputPath_relation2id = outputPath + "relation2id.txt";
		String outputPath_abstract = outputPath + "entity2abstract.txt";
		//String outputPath_data_relation_statistic = commonPath + outputPath + "relation_statistic.txt";
		//String outputPath_data_relation_actor = commonPath + outputPath + "relation_actor.txt";
		
		DataFilter dataFilter = new DataFilter();
		
		//Entity filter
		//dataFilter.typeEntityFilter(inputPath_data_type);
		dataFilter.mappingEntityFilter(inputPath_mapping);
		//dataFilter.writeFilteredTypeTriples(outputPath_data_type_filter);
		
		//Triple filter
		dataFilter.mappingTripleFilter(inputPath_mapping);
		
		//Abstract filter
		dataFilter.abstractFilter(inputPath_abstract);
		
		//Category filter
		dataFilter.categoryTripleFilter(inputPath_category);
		

		dataFilter.writeFilteredMainTriples(outputPath_mapping_filter);
		dataFilter.writeEntity2Id(outputPath_entity2id);
		dataFilter.writeRelation2Id(outputPath_relation2id);
		dataFilter.writeEntity2Abstract(outputPath_abstract);
		//dataFilter.writeRelation(outputPath_data_relation_statistic);
		//dataFilter.writeActorRelation(outputPath_data_relation_actor);
		
		
		System.gc();
	}
}
