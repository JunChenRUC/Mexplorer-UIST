package cn.edu.ruc.Mexplore.extrator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {
	private static String commonPath = "D://";
	private static String inputPath = "DBpedia_2016-04/instance_mapping/infobox_properties_mapped_en.ttl";
	
	private static void load(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String tmpString = null;
        while ((tmpString = reader.readLine()) != null){
        	String query = "<http://dbpedia.org/resource/The_Lord_of_the_Rings:_The_Return_of_the_King>";
        	//String query = "<http://dbpedia.org/resource/The_Godfather_Part_II>";
        	if(tmpString.toLowerCase().startsWith(query.toLowerCase()))
        		System.out.println(tmpString);
        }
        reader.close();	
	}
	
	public static void main(String[] args) throws IOException {
		load(new File(commonPath + inputPath));
	}

}
