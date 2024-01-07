package routers.com.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import localsolver.LSExpression;
import localsolver.LSModel;
import localsolver.LSSolutionStatus;
import localsolver.LocalSolver;
import localsolver.modeler.LSPMap;
import localsolver.modeler.LSPModeler;
import localsolver.modeler.LSPModule;
import routers.com.model.Root;
import routers.com.service.RouteServices;



@RestController
@RequestMapping("/api/v1")
public class Routes {
	 private static final String UPLOAD_DIR = "./uploads"; 
	 public static final String LSP_PATH = "./uploads/model.lsp";
	 
	 private final ResourceLoader resourceLoader;

	 @Autowired
	 private RouteServices serviec;

	 
	 @Autowired
	    public Routes(ResourceLoader resourceLoader) {
	        this.resourceLoader = resourceLoader;
	    }

	 
	 
	 	@PostMapping("/upload")
		public String getRoutes(@RequestBody String data) throws IOException { 	
			 String root = serviec.setRoot(data);
			 String jsonContent = null;
	 		if(root == "failed") {
	 			jsonContent ="All parameters required";
	 		}else {
	 			
	 		
	 		
	 		ClassPathResource resource = new ClassPathResource("model.lsp");
			 	
			
			System.out.println(resource.getFile().getAbsolutePath());
			 if (resource.exists()) {
			 
			 File file1 = resource.getFile();
			 String filePath = "data.json";
//			 ------------------------------------------------------------------
			 
			 	FileWriter fileWriter = new FileWriter(filePath);
	            fileWriter.write(root);
	            fileWriter.close(); 
	            Path absolutePath = Paths.get(filePath).toAbsolutePath();
	            
//			 ----------------------------------------------------------------------
			 if(file1.isFile() ==true) {
				 LSPModeler modeler = new LSPModeler();
				 LSPModule main = modeler.loadModule(file1.getPath());
		         LocalSolver solver = modeler.createSolver();
	             main.setString("inFileName", absolutePath.toString());
	             System.out.println(main);
		         main.run(solver);
		         LSSolutionStatus solutionStatus = solver.getSolution().getStatus();
		         if (solutionStatus == LSSolutionStatus.Infeasible || solutionStatus == LSSolutionStatus.Inconsistent) {
		        		jsonContent = "";
		        	}
		        	
		        	jsonContent = displaySolution(main);
			 }else {
				 System.out.println("No");
			 }
			 }
	 		}
			return jsonContent;
			
		}
	 	
		public String displaySolution(LSPModule main) {
			 String json;
	    	ArrayList<Map> arraylist = new ArrayList<>();
	    	
	    	LSPMap sites = main.getMap("outputTasks");
	    	LSPMap fromtoRoute = main.getMap("fromtoRoute");
	    	for(int i =0 ; i < sites.count(); i++ ) {
	    		Map map = new HashMap();
	    		
	    		map.put("name", sites.getMap(i).getString("name"));
	    		map.put("truck", sites.getMap(i).getString("resource"));
	    		map.put("color", sites.getMap(i).getString("color"));
	    		map.put("startTime", sites.getMap(i).getInt("startTime"));
	    		map.put("endTime", sites.getMap(i).getInt("endTime"));
	    		map.put("latitude", sites.getMap(i).getDouble("latitude"));
	    		map.put("longitude", sites.getMap(i).getDouble("longitude"));
	    		map.put("routename", fromtoRoute.getMap(i).getString("name"));
	    		map.put("routelatitude", fromtoRoute.getMap(i).getString("latitude"));
	    		map.put("routelongitude", fromtoRoute.getMap(i).getString("longitude"));
	    		arraylist.add(map);
	    	}
	    	Gson gson = new Gson();
	    	 String jsonString = gson.toJson(arraylist);
	    
	    	  return jsonString;
		}



	}
