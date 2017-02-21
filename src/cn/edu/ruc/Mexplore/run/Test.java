package cn.edu.ruc.Mexplore.run;

import java.util.ArrayList;

import cn.edu.ruc.Mexplore.domain.Entity;
import cn.edu.ruc.Mexplore.domain.Feature;
import cn.edu.ruc.Mexplore.domain.Result;
import net.sf.json.JSONArray;

public class Test {
	private static Process process = new Process();
	
	public static void main(String[] args) {
		resultFind();
		//getSimilarity();
	}
	
	public static void resultFind(){
		long beginTime = System.currentTimeMillis();
		
		ArrayList<String> queryEntityList = new ArrayList<>();
		queryEntityList.add("forrest gump");
		//queryEntityList.add("cast away");
		
		ArrayList<String> queryFeatureList = new ArrayList<>();
		//queryFeatureList.add("tom hanks_starring_1");
		
		Result result = null;
		result = process.getResult(queryEntityList, queryFeatureList);
		
		System.out.println("Time cost: " + (System.currentTimeMillis() - beginTime)/1000 );
		
		System.out.println("\n--------------------------\n");
		System.out.println("Query entity list: ");
		for (Entity queryEntity : result.getQueryEntityList())
			System.out.println(queryEntity);
		
		System.out.println("\n--------------------------\n");
		System.out.println("Query feature list: ");
		for (Feature queryFeature : result.getQueryFeatureList())
			System.out.println(queryFeature);

		System.out.println("\n--------------------------\n");
		System.out.println("Result entity list: ");
		for (Entity resultEntity : result.getResultEntityList())
			System.out.println(resultEntity);
		
		System.out.println("\n--------------------------\n");
		System.out.println("Result feature list: ");
		for(Feature resultFeature : result.getResultFeatureList())
			System.out.println(resultFeature);
		
		JSONArray jsonArray = JSONArray.fromObject(result);
		System.out.println(jsonArray.toString());
	}
	
	public static void getSimilarity(){
		System.out.println(process.getSimilarity("forrest gump", "apollo 13 (film)"));
	}
}
