import java.util.concurrent.Flow.Subscriber;

import javax.imageio.plugins.bmp.BMPImageWriteParam;

import java.util.*;
abstract public class Gorev {
   protected String name;
   protected String id;
   protected String seat;
   VeriTabani vs = new VeriTabani();

     abstract public ArrayList<String> mission();

     public void set(String name,String id,String seat){
      this.name=name;
      this.id=id;
      this.seat=seat;
     }

     public String getName(){
      return this.name;
     }

     public String getSeat(){
      return this.seat;
     }

     public String getId(){
      return this.id;
     }
      
     
}



 class infReader extends Gorev{

    public ArrayList<String> mission(){
     return VeriTabani.QueryReservation();
    }
    
    
}

 class writerR  extends Gorev{
   String name;
   String id;
   String seat;

   public writerR(){}
    
    public ArrayList<String> mission(){
       return VeriTabani.makeReservation(super.name, super.id, super.seat);
    }

}

class writerC extends Gorev{
   String name;
   String id;
   String seat;

  public writerC(){}

   public ArrayList<String> mission(){
      return VeriTabani.cancelReservation(super.name, super.id, super.seat);

   }

}