package routers.com.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import routers.com.service.RouteServices;



@RestController
@RequestMapping("/")
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

	 
	 
	 @PostMapping
		public String getRoutes(@RequestBody String data) throws IOException { 	
			 String root = serviec.setRoot(data);
			 String jsonContent = null;
			 ClassPathResource resource = new ClassPathResource("model.lsp");
	 		if(root == "failed") {
	 			jsonContent ="All parameters required";
	 		}else {
			 if (resource.exists()) {
			 
			 File file1 = resource.getFile();
			
			 String filePath = "input.json";
			 ClassPathResource dataJson = new ClassPathResource("data.json");
			 
			 String filePath1 = dataJson.getPath();
//			 ------------------------------------------------------------------
			 
			 	FileWriter fileWriter = new FileWriter(filePath);
	            fileWriter.write(root);
	            fileWriter.close(); 
	            Path absolutePath = Paths.get(filePath).toAbsolutePath();
	            
//			 ----------------------------------------------------------------------
			 if(file1.isFile() ==true) {
				 try (LSPModeler modeler = new LSPModeler()) {
				 LSPModule main = modeler.loadModule(file1.getPath());
				 try (LocalSolver solver = modeler.createSolver()) {
	             main.setString("inFileName", absolutePath.toString());
	             System.out.println(main);
		         main.run(solver);
		         LSSolutionStatus solutionStatus = solver.getSolution().getStatus();
		         if (solutionStatus == LSSolutionStatus.Infeasible || solutionStatus == LSSolutionStatus.Inconsistent) {
		        		jsonContent = "";
		        	}
		        	
		        	jsonContent = displaySolution(main);
		        	FileWriter fileWriter1 = new FileWriter(filePath1);
		            fileWriter1.write(jsonContent);
		            fileWriter1.close(); 
				 }catch(Exception e) {
			    	   System.out.println(e);
			       }
		            modeler.delete();
				 }catch(Exception e) {
					 System.out.println(e);
				 }
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
    	LSPMap data = main.getMap("data");
    	LSPMap customers = data.getMap("customers");
    	LSPMap sites = main.getMap("outputTasks");
    	
    	LSPMap numberofTruect = main.getMap("numberofTruect");
    	
    	for(int i = 0 ; i < numberofTruect.count(); i++) {
    		Map map = new HashMap<>();
    		map.put("truck", numberofTruect.getMap(i).getString("resource"));
    		map.put("color", numberofTruect.getMap(i).getString("color"));
    		 LSPMap customer = numberofTruect.getMap(i).getMap("step");
    		 ArrayList<Map> customerArray = new ArrayList<>();
    		for(int j = 0 ; j < customer.count();  j++) {
    			Map custRecords = new HashMap<>();
    			custRecords.put("startTime", customer.getMap(j).getInt("startTime"));
    			custRecords.put("endTime", customer.getMap(j).getInt("endTime"));
    			custRecords.put("routelatitude", customer.getMap(j).getString("latitude"));
    			custRecords.put("routelongitude", customer.getMap(j).getString("longitude"));
    			custRecords.put("latitude", Double.parseDouble(customer.getMap(j).getString("lang")));
    			custRecords.put("longitude", Double.parseDouble(customer.getMap(j).getString("long")));
    			custRecords.put("name", customer.getMap(j).getString("cname"));
    			custRecords.put("id", customer.getMap(j).getString("cid"));
    			custRecords.put("routename", sites.getMap(j).getString("name"));
	    		customerArray.add(custRecords);
    		}
    		map.put("step", customerArray);
    		arraylist.add(map);
    	}
    	
//    	System.out.println(numberofTruect.count());
    	Gson gson = new Gson();
    	 String jsonString = gson.toJson(arraylist);
    
    	  return jsonString;
	}
		
		
		@PostMapping("/upload")
		public String updateJson(@RequestParam("file") MultipartFile file) throws IOException, URISyntaxException { 	
			 ClassPathResource resource = new ClassPathResource("model.lsp");
			 ;
			
			 String jsonContent = null;
			 if (resource.exists()) {
			 
			 File file1 = resource.getFile();
			
//			 ------------------------------------------------------------------
			 
			 byte[] bytes = file.getBytes();
             File uploadDir = new File(UPLOAD_DIR);
             if (!uploadDir.exists()) {
                 uploadDir.mkdirs();
             }
             
        	 Path path1 = Paths.get(uploadDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
        	 Files.write(path1, bytes);
	 
        	 ClassPathResource dataJson = new ClassPathResource("data.json");
			 
			 String filePath1 = dataJson.getPath();
//			 ----------------------------------------------------------------------
			 if(file1.isFile() ==true) {
				 LSPModeler modeler = new LSPModeler();
				 LSPModule main = modeler.loadModule(file1.getPath());
		         LocalSolver solver = modeler.createSolver();
	             main.setString("inFileName", path1.toString());
	             System.out.println(main);
		         main.run(solver);
		         LSSolutionStatus solutionStatus = solver.getSolution().getStatus();
		         if (solutionStatus == LSSolutionStatus.Infeasible || solutionStatus == LSSolutionStatus.Inconsistent) {
		        		jsonContent = "";
		        	}
		        	
		        	jsonContent = displaySolution(main);
		        	FileWriter fileWriter1 = new FileWriter(filePath1);
		            fileWriter1.write(jsonContent);
		            fileWriter1.close(); 
		            modeler.delete();
			 }else {
				 System.out.println("No");
			 }
			 
			 }

		             return jsonContent;
			
		}
	 	
		
		
		
		@GetMapping
		public ResponseEntity<byte[]> downloadRootFile() throws IOException {
			
			ClassPathResource dataJson = new ClassPathResource("data.json");
		    String filePath = dataJson.getPath();
		    FileReader file = new FileReader(filePath);
		    File file1 = new File(filePath);

	        char[] buffer = new char[(int) file1.length()];
	        file.read(buffer);
	        file.close();

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	        headers.setContentDispositionFormData("attachment", file1.getName());

	        return ResponseEntity.ok()
	                .headers(headers)
	                .body(new String(buffer).getBytes());
	    }



	}
