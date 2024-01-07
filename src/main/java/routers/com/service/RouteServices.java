package routers.com.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import routers.com.model.Customer;
import routers.com.model.Root;
import routers.com.routeImpl.RouteImp;

@Service
public class RouteServices implements RouteImp{

	
	
	@Override
	public String setRoot(String data) {
		Gson gson = new Gson();
		Root root = gson.fromJson(data, Root.class);
//		ArrayList mainData = new ArrayList<>(); 
		Boolean status = true;
		Map rmap = new HashMap<>();
		rmap.put("endHorizon", root.getEndHorizon());
		rmap.put("depotId", root.getDepotId());
		rmap.put("depotName",root.getDepotName());
		rmap.put("depotLatitude", root.getDepotLatitude());
		rmap.put("depotLongitude", root.getDepotLongitude());
		
		
		if(root.getCustomers() !=null && !root.getCustomers().isEmpty()) {
			ArrayList<Map> customerData = new ArrayList<>();
			for(int i =0 ; i< root.getCustomers().size(); i++) {
				Map map = new HashMap<>();

				 if(root.getCustomers().get(i).getId()==null || root.getCustomers().get(i).getId().isEmpty() || root.getCustomers().get(i).getName()==null || root.getCustomers().get(i).getName().isEmpty() || root.getCustomers().get(i).getLatitude()==null || root.getCustomers().get(i).getLatitude().isNaN() ||  root.getCustomers().get(i).getLongitude()==null || root.getCustomers().get(i).getLongitude().isNaN() ||  root.getCustomers().get(i).getServiceTime()==null ||  root.getCustomers().get(i).getTimeWindowStart()==null || root.getCustomers().get(i).getTimeWindowEnd()==null ||  root.getCustomers().get(i).getDemand()==null ){					
					 status = false;
				 }else {
					 	map.put("id", root.getCustomers().get(i).getId());
						map.put("name", root.getCustomers().get(i).getName());
						map.put("latitude", root.getCustomers().get(i).getLatitude());
						map.put("longitude", root.getCustomers().get(i).getLongitude());
						map.put("serviceTime", root.getCustomers().get(i).getServiceTime());
						map.put("timeWindowStart", root.getCustomers().get(i).getTimeWindowStart());
						map.put("timeWindowEnd", root.getCustomers().get(i).getTimeWindowEnd());
						map.put("demand", root.getCustomers().get(i).getDemand());
						customerData.add(map);
				 }
				
			}
			
			rmap.put("customers", customerData);
		}
		
		
		rmap.put("truckCapacity", root.getTruckCapacity());
		rmap.put("truckSpeed", root.getTruckSpeed());
			
			if(root.getTrucks() !=null && !root.getTrucks().isEmpty()) {
				ArrayList<Map> truckData = new ArrayList<>();
				for(int i =0 ; i< root.getTrucks().size(); i++) {
					Map map1 = new HashMap<>();
					if(root.getTrucks().get(i).getId() == null || root.getTrucks().get(i).getId().isEmpty() ||  root.getTrucks().get(i).getColor() == null ||  root.getTrucks().get(i).getColor().isEmpty()) {
					status = false;	
					}
					map1.put("id", root.getTrucks().get(i).getId());
					map1.put("color", root.getTrucks().get(i).getColor());
					truckData.add(map1);
				}
				rmap.put("trucks", truckData);
			}
			
//			mainData.add(rmap);
			System.out.println(status);
			
			if(status==false) {
				return "failed";
			}else {
				String json = gson.toJson(rmap);
				
				return json;
			}
			
		
				
//		System.out.println(json);
		
	}

	@Override
	public Root getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

}
