package service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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

import pojos.Centre;
import pojos.Session;

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
      String call = "https://cowin.gov.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id="+distId+"&date=" + dateString;
      
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
    System.out.println("Downloading district data ..");
    DistrictsDownloader data = new DistrictsDownloader();
    data.downloadData();
    Map<String, String> districts = data.getDistrictMap();
    
    System.out.println("Enter the exact name of your district as you see in Cowin/Aarogya Setu application and press enter:");
    System.out.println();
    System.out.println();
    System.out.println();
    Scanner in = new Scanner(System.in);
    String districtName = in.nextLine();
    if (districts.containsKey(districtName)) {
       System.out.println("District found! Lets go..");
       
       Thread.sleep(1000);
    } else {
       System.out.println("District not found. Exiting ..");
       System.exit(0);
    }
    String districtId = districts.get(districtName);
    //System.out.println(districtId);
    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
    DateTime today = DateTime.now();
    int weeksToCheck = 6;
   // playUndertakertheme();
    while (true) {
       for (int i =0; i<weeksToCheck; i++) {
          DateTime dateToPass = today.plusDays(7*i);
          //String districtId = "294"; // Bangalore
          System.out.println("Checking week of " + dateToPass.toString(formatter) + ":");
          Map<String, List<Centre>> response = checker.callAPI(districtId, dateToPass.toString(formatter));
          boolean found = checker.getAvailability(response);
          if (found) {
             System.out.println("Woohoo!");
             playUndertakertheme();
             break;
          } 
          System.out.println();
       }
       System.out.println("checking again in 30 seconds..");
       Thread.sleep(1000*60); // check every 30 seconds
    }
   }

   private boolean getAvailability(Map<String, List<Centre>> response) {
     int age = 18;
      boolean found = false;
      List<Centre> centres = response.get("centers");
      Set<String> elgibleCentres = new HashSet<String>();
      Set<String> availableCentres = new HashSet<String>();
      outer:
      for (Centre centre: centres) {
         List<Session> sessions = centre.getSessions();
         for (Session session: sessions) {
            int minAge = session.getMin_age_limit();
            if (minAge==age) {  // 18+ adults
               elgibleCentres.add(centre.getName());
               int availability = session.getAvailable_capacity();
               if (availability>0) {
                  availableCentres.add(centre.getName());
                  System.out.println("Slot found!");
                  System.out.println(availability + " slots avaiable at "  + centre.getName() + " on date " + session.getDate());
                  System.out.println();
                  found = true;
               }
               
            }
         }
      }
      System.out.println(centres.size() + " total centres found.");
      System.out.println(elgibleCentres.size() + " centers are allowing for 18+ adults");
      
      System.out.println(availableCentres.size() + " centers have slots available" );
      
      if (!found) System.out.print(" :(");
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
