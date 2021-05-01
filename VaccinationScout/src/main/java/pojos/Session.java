package pojos;

import java.util.List;

public class Session {
     private String session_id;
     private String date;
     private int available_capacity;
     private int min_age_limit ;
     private String vaccine ;
     private List<String> slots ;
     
     
   public String getSession_id() {
      return session_id;
   }
   public void setSession_id(String session_id) {
      this.session_id = session_id;
   }
   public String getDate() {
      return date;
   }
   public void setDate(String date) {
      this.date = date;
   }
   public int getAvailable_capacity() {
      return available_capacity;
   }
   public void setAvailable_capacity(int available_capacity) {
      this.available_capacity = available_capacity;
   }
   public int getMin_age_limit() {
      return min_age_limit;
   }
   public void setMin_age_limit(int min_age_limit) {
      this.min_age_limit = min_age_limit;
   }
   public String getVaccine() {
      return vaccine;
   }
   public void setVaccine(String vaccine) {
      this.vaccine = vaccine;
   }
   public List<String> getSlots() {
      return slots;
   }
   public void setSlots(List<String> slots) {
      this.slots = slots;
   }
}
