package service;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import pojos.Centre;

public class DistrictsDownloader {
   
      ObjectMapper mapper = new ObjectMapper();
      private Map<String, String> stateMap = new HashMap<String, String>();
      private Map<String, String> districtMap = new HashMap<String, String>();
      private void getDistrictsForState(String stateId) {
         
         String call = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/" +stateId;
         
         //System.out.println(call);
         CloseableHttpClient httpclient = HttpClients.createDefault();
         HttpGet httpGet = new HttpGet(call.toString());
         httpGet.setHeader("user-agent","Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
         try {
            CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
             HttpEntity entity = httpResponse.getEntity();
             JsonNode response = mapper.readTree(entity.getContent());
             EntityUtils.consume(entity);
             //System.out.println(response.toString());
             Iterator<JsonNode> districtIt = response.get("districts").iterator();
             while (districtIt.hasNext()) {
                JsonNode districtNode = districtIt.next();
                districtMap.put(districtNode.get("district_name").asText(), districtNode.get("district_id").asText());
             }
         } catch (Exception e) {
            System.out.println("Couldnt get districts data for " + stateId);
            e.printStackTrace();
         }
      }
      
      public void downloadData() {
          try {
            JsonNode statesNode = mapper.readTree(DistrictsDownloader.class.getResourceAsStream("/states.json"));
            Iterator<JsonNode> stateIt = statesNode.get("states").iterator();
            while (stateIt.hasNext()){
               JsonNode stateNode = stateIt.next();
               
               stateMap.put(stateNode.get("state_id").asText(), stateNode.get("state_name").asText());
               this.getDistrictsForState(stateNode.get("state_id").asText());
            }
         } catch (IOException e) {
            System.out.println("Failed to get states. Exiting..");
            System.exit(0);
         }
          
      }

      public Map<String, String> getDistrictMap() {
         return districtMap;
      }


     
}
