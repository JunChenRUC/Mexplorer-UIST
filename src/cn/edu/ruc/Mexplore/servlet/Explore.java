package cn.edu.ruc.Mexplore.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.ruc.Mexplore.domain.Result;
import cn.edu.ruc.Mexplore.run.Process;
import net.sf.json.JSONArray;

/**
 * Servlet implementation class Explore
 */
@WebServlet("/Explore")
public class Explore extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static Process process = new Process();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Explore() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		request.setCharacterEncoding("UTF-8");
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST, GET");
        
		PrintWriter out= response.getWriter();

        long beginTime = System.currentTimeMillis();
        
        int queryType = 0;
        if(request.getParameter("type") != null)
        	queryType = Integer.parseInt(request.getParameter("type"));
        System.out.println("qeury type: " + queryType);
        String callback = request.getParameter("callback");
		
		if(queryType == 0){
			ArrayList<String> queryEntityList = new ArrayList<>();
	        if(request.getParameterValues("entity") != null){  
				String[] tokens = request.getParameterValues("entity");
				for(String token : tokens){
					System.out.print(token + "\t");
					queryEntityList.add(token.trim());
				}
	        }
	        
	        ArrayList<String> queryFeatureList = new ArrayList<>();
	        if(request.getParameterValues("feature") != null){  
				String[] tokens = request.getParameterValues("feature");
				for(String token : tokens){
					System.out.print(token + "\t");
					queryFeatureList.add(token.trim());
				}
	        }
	        
	        Result result = null;
			
	        result = process.getResult(queryEntityList, queryFeatureList);
	        
			JSONArray jsonArray = JSONArray.fromObject(result);
			out.println("" + callback + "('" + jsonArray.toString().replace("'", "\\\\'") + "')");
	        //System.out.println();
			//System.out.println(jsonArray.toString());
		}
		
		System.out.println("Time cost: " + (System.currentTimeMillis() - beginTime)/1000 );
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
