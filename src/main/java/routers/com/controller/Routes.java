package routers.com.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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


@RestController
@RequestMapping("/api/v1")
public class Routes {
	 private static final String UPLOAD_DIR = "./uploads"; 
	 public static final String LSP_PATH = "./static/uploads/model.lsp";
	 
	 private final ResourceLoader resourceLoader;

	 
	 @Autowired
	    public Routes(ResourceLoader resourceLoader) {
	        this.resourceLoader = resourceLoader;
	    }

	 
	 
	 	@PostMapping("/upload")
		public ResponseEntity<byte[]> getRoutes(@RequestParam("file") MultipartFile file) throws IOException {
	 		String fileName = "model.lsp"; 
	 		String jsonContent;
	        String filePath;
			
				filePath = resourceLoader.getResource("classpath:static/uploads/" + fileName).getFile().getAbsolutePath();
			
				LSPModeler modeler = new LSPModeler();
	 			String lspPath = LSP_PATH;

	 			
	 		
	         	LSPModule main = modeler.loadModule(filePath);
	         	LocalSolver solver = modeler.createSolver();
	         	 byte[] bytes = file.getBytes();
	             File uploadDir = new File(UPLOAD_DIR);
	             if (!uploadDir.exists()) {
	                 uploadDir.mkdirs();
	             }
	            
	        	 Path path = Paths.get(uploadDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
	             Files.write(path, bytes);
	             String jsonPath = new String(Files.readAllBytes(path));
	             main.setString("inFileName", jsonPath);
		         main.run(solver);
		         LSSolutionStatus solutionStatus = solver.getSolution().getStatus();
		        	
		        	if (solutionStatus == LSSolutionStatus.Infeasible || solutionStatus == LSSolutionStatus.Inconsistent) {
		        		jsonContent = "";
		        	}
		        	
		        	jsonContent = displaySolution(main);
		        	byte[] bytes1 = new ObjectMapper().writeValueAsBytes(jsonContent);

		        	 HttpHeaders headers = new HttpHeaders();
		             headers.setContentType(MediaType.APPLICATION_JSON);
		             headers.setContentDispositionFormData("attachment", "data.json"); // Set file name for download

		             return ResponseEntity.ok().headers(headers).body(bytes);
			
		}
	 	
		public String displaySolution(LSPModule main) {
			 String json;
	    	ArrayList<Map> arraylist = new ArrayList<>();
	    	
	    	LSPMap sites = main.getMap("outputTasks");
	    	for(int i =0 ; i < sites.count(); i++ ) {
	    		Map map = new HashMap();
	    		map.put("name", sites.getMap(i).getString("name"));
	    		map.put("truck", sites.getMap(i).getString("resource"));
	    		map.put("color", sites.getMap(i).getString("color"));
	    		map.put("startTime", sites.getMap(i).getInt("startTime"));
	    		map.put("endTime", sites.getMap(i).getInt("endTime"));
	    		map.put("latitude", sites.getMap(i).getInt("latitude"));
	    		map.put("longitude", sites.getMap(i).getInt("longitude"));
	    		map.put("isDepot", sites.getMap(i).getInt("isDepot"));
	    		arraylist.add(map);
	    	}
	    	
	    	ObjectMapper objectMapper = new ObjectMapper();
	    	 
	               try {
					json = objectMapper.writeValueAsString(arraylist);
					
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					json = "";
				}
	    	  return json;
		}



	}
