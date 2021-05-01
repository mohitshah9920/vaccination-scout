package cowin.pojos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cowin.pojos.Centre;
import cowin.pojos.Session;

public class VaccineAvailibilityChecker {
   
   ObjectMapper mapper = new ObjectMapper();
   
   // gurgaon - 188
   // bangalore - bbmp -294
   //mumbai - 395
   // south delhi - 149
   public VaccineAvailibilityChecker() {
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      }
   
   
   private Map<String, List<Centre>> callAPI(String distId, String dateString) throws JsonProcessingException, UnsupportedOperationException, IOException {
      Map<String,List<Centre>> response= null;
//      String distId ="294";
//      String dateString = "06-05-2021"; 
      String call = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id="+distId+"&date=" + dateString;
      
      //System.out.println(call);
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(call.toString());
      CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
     
      try {
          
          HttpEntity entity = httpResponse.getEntity();
          TypeReference<HashMap<String,List<Centre>>> typeRef  = new TypeReference<HashMap<String,List<Centre>>>() {};
          response = mapper.readValue(entity.getContent(), typeRef);
          EntityUtils.consume(entity);
      }
      catch (Exception e) {
         System.out.println(httpResponse.getStatusLine());
         System.out.println(EntityUtils.toString(httpResponse.getEntity()));
     
      } finally {
         httpResponse.close();
      }
      return response;
   }
   public static void main(String[] args) throws JsonProcessingException, UnsupportedOperationException, IOException, InterruptedException, UnsupportedAudioFileException, LineUnavailableException {
    VaccineAvailibilityChecker checker = new VaccineAvailibilityChecker();
    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
    DateTime today = DateTime.now();
    int weeksToCheck = 6;
   // playUndertakertheme();
    while (true) {
       for (int i =0; i<weeksToCheck; i++) {
          DateTime dateToPass = today.plusDays(7*i);
          String districtId = "294"; // Bangalore
          System.out.println("Checking week of " + dateToPass.toString(formatter));
          Map<String, List<Centre>> response = checker.callAPI(districtId, dateToPass.toString(formatter));
          boolean found = checker.getAvailability(response);
          if (found) {
             System.out.println("Woohoo!");
             playUndertakertheme();
             break;
          }
       }
       Thread.sleep(1000*30); // check every 30 seconds
    }
   }

   private boolean getAvailability(Map<String, List<Centre>> response) {
     
      boolean found = false;
      int avaiableCenters = 0;
      List<Centre> centres = response.get("centers");
      outer:
      for (Centre centre: centres) {
         List<Session> sessions = centre.getSessions();
         for (Session session: sessions) {
            int minAge = session.getMin_age_limit();
            if (minAge==18) {  // 18+ adults
               avaiableCenters++;
               int availability = session.getAvailable_capacity();
               if (availability>0) {
                  System.out.println("Slot found!");
                  System.out.println(availability + " slots avaiable at "  + centre.getName() + " on date " + session.getDate());
                  found = true;
               }
               continue outer;
            }
         }
      }
      System.out.println(avaiableCenters + " centers available for 18+ adults out of " + centres.size());
      System.out.println("No slots found :(");
      return found;
   }

   private static void playUndertakertheme() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
      
      AudioInputStream audioInputStream =
            AudioSystem.getAudioInputStream(
                  VaccineAvailibilityChecker.class.getResource("/undertaker.wav"));
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
   }
}
