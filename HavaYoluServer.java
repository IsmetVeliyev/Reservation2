import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.security.spec.ECGenParameterSpec;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.*;

public class HavaYoluServer {
    public static volatile Boolean Info =false;
    public static ArrayList<String>InfoList;
    public static ServerSocket srv;
    public static volatile Queue<Socket> requests;
    public  static volatile Queue<Gorev> GorevKyrk;
    public static volatile HashMap<String,ObjectOutputStream> users;

    public static void main (String args[]){
        try{
            srv = new ServerSocket(5050);
            requests = new LinkedList<>();
            GorevKyrk = new LinkedList<>();
            users = new  HashMap<>();
            InfoList = new ArrayList<>();
            Thread lth = new Thread(new HavaYoluClientListener());
            lth.start();
            Thread vrth = new Thread(new VeriTabaniRequestHandler());
            vrth.start();
            while (true) {
                if(requests.peek()!=null){
                    Thread thread = new Thread(new HavaYoluCLientHandler(requests.remove()));
                    thread.start();
                } 
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }  
}

class HavaYoluClientListener implements Runnable{

    public void run(){
        try{
            while (true) {
                Socket socket = HavaYoluServer.srv.accept();
                 if(socket!=null){
                     HavaYoluServer.requests.add(socket);
                 }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


class HavaYoluCLientHandler implements Runnable{
    private Socket socket;

    public HavaYoluCLientHandler(Socket socket){
        this.socket=socket;
    }

    public void run(){
        try{
            
            
            Date date = new Date();
            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ObjectOutputStream bw0 = new ObjectOutputStream(socket.getOutputStream());
            String name = bf.readLine();
            HavaYoluServer.users.put(name,bw0);
            System.out.println(name +" ");
            bw0.writeObject((Object)"1:GetInf 2:makeReservation 3:cancelReservation");
            bw0.flush();
            while (true) {
            String choice=bf.readLine();
            if(choice.equals("1")){
                System.out.println(date);
                System.out.println(name+" looks for available seats");
                if(!HavaYoluServer.Info){
                    Gorev gv = new infReader();
                    gv.set(name,null,null);
                    HavaYoluServer.GorevKyrk.add(gv);
                }else{
                    bw0.writeObject((Object)HavaYoluServer.InfoList);
                    bw0.flush();
                }
                System.out.print("\n\n-----------\n\n");
            }else if(choice.equals("2")){
                System.out.println(date);
                String prm[] = bf.readLine().split(" ");
                System.out.println(name+" tries to book  "+prm[1]);
                Gorev gv = new writerR();
                gv.set(name,prm[0],prm[1]);
                HavaYoluServer.GorevKyrk.add(gv);
                System.out.print("\n\n-----------\n\n");
            }else if(choice.equals("3")){
                System.out.println(date);
                String prm[] = bf.readLine().split(" ");
                System.out.println(name+" tries to cancel seat "+prm[1]);
                Gorev gv = new writerC();
                gv.set(name,prm[0],prm[1]);
                HavaYoluServer.GorevKyrk.add(gv);
                System.out.print("\n\n-----------\n\n");
            }
            
        }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


class VeriTabani{
    
    public static ArrayList<String> QueryReservation(){
        ArrayList<String> list = new ArrayList<>();
        String line;
        try{
            BufferedReader bf = new BufferedReader(new FileReader("HavaYoluServer.txt"));
            while((line=bf.readLine())!=null){
                list.add(line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return list;
    }

    public static ArrayList<String> makeReservation(String name,String id, String seat){
        ArrayList<String> list = new ArrayList<>();
        
        try{
            BufferedReader bf = new BufferedReader(new FileReader("HavaYoluServer.txt"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("deneme.txt"));
            String line;
            while ((line=bf.readLine())!=null) {
                if((line.charAt(0)+"").equals(id)){
                    String flinf[] = line.split(" ");
                    for(int i=0;i<flinf.length;i++){
                        if(flinf[i].equals(seat) && flinf[i+1].equals("bos")){
                            flinf[i+1]="dolu";
                            line = String.join(" ",flinf);
                           bw.write(line);
                           bw.newLine();
                           BufferedWriter writer = new BufferedWriter(new FileWriter("bkdSeats.txt",true));
                           writer.write(name +" " + id +" " + seat+"\n");
                           writer.close();
                           list.add("rezerve edildi");
                        }else if(flinf[i].equals(seat) && flinf[i+1].equals("dolu")){
                            bw.write(line);
                            bw.newLine();
                        }

                    }
                    
                }else{
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.close();
            degistir("deneme.txt","HavaYoluServer.txt");
            
        }catch(Exception e){
            e.printStackTrace();
        }
        if(list.size()==1){
            return list;
        }
        list.add("yer dolu edilemedi");
        return list;


    }

    public static ArrayList<String> cancelReservation(String name,String id,String seat){
        ArrayList<String> list = new ArrayList<>();
        try{
            BufferedReader bf = new BufferedReader(new FileReader("HavaYoluServer.txt"));
            BufferedReader reader = new BufferedReader(new FileReader("bkdSeats.txt"));
            String line;
            BufferedWriter bw = new BufferedWriter(new FileWriter("deneme.txt"));
            boolean a=false;
            while ((line=reader.readLine())!=null) {
                String param[] = line.split(" ");
                if(param[0].equals(name) && param[1].equals(id) && param[2].equals(seat)){
                    while ((line=bf.readLine())!=null) {
                        if((line.charAt(0)+"").equals(id)){
                            String flinf[] = line.split(" ");
                            for(int i=0; i<flinf.length;i++){
                                if(flinf[i].equals(seat) && flinf[i+1].equals("dolu")){
                                    flinf[i+1]="bos";
                                    line = String.join(" ",flinf);
                                   bw.write(line);
                                   bw.newLine();
                                   BkdDegisiklik(name, id, seat);
                                    a=true;
                                    list.add("iptal edildi");
                                }else if(flinf[i].equals(seat) && flinf[i+1].equals("bos")){
                                    bw.write(line);
                                    bw.newLine();
                                }
                            }

                        }else{
                           bw.write(line);
                           bw.newLine();

                        }
                }
                
            }
        }
        bw.close();
        if(a){
            degistir("deneme.txt","HavaYoluServer.txt");
            
        }
    }catch(Exception e){
        e.printStackTrace();
    }
    if(list.size()==1){
        return list;
    }
        list.add("Tamamlanamadi");
        return list;
    }
    
    public static void degistir(String path1,String path2){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(path1));
            String line;
            BufferedWriter writer = new BufferedWriter(new FileWriter(path2));
            while((line=reader.readLine())!=null){
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            reader.close();
    
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void BkdDegisiklik(String name,String id,String seat){
        try{
            String line;
            BufferedReader bf = new BufferedReader(new FileReader("bkdSeats.txt"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("deneme1.txt"));
            while ((line=bf.readLine())!=null) {
                String param[] = line.split(" ");
                if(param[0].equals(name) && param[1].equals(id) && param[2].equals(seat)){
                    
                }else{
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.close();
            degistir("deneme1.txt","bkdSeats.txt");

        }catch(Exception e){
            e.printStackTrace();
        }


    }
}


class VeriTabaniRequestHandler implements Runnable{
    static volatile Queue<Gorev> ReaderKyrk = new LinkedList<>();
    public void run(){
        try{
            while (true) {
                Date date = new Date();
                if(HavaYoluServer.GorevKyrk.peek()!=null){
                    Gorev gv = HavaYoluServer.GorevKyrk.remove();
                    if(!(gv instanceof infReader))
                    {
                        String name = gv.getName();
                        ArrayList<String> rspList = gv.mission();
                        String resp =rspList.get(0);
                        if(resp.contains("rezerve ")){
                            System.out.print("\n\n");
                            System.out.println(date);
                            System.out.println(name + " booked seat "+gv.getSeat());
                            HavaYoluServer.Info=false;
                            HavaYoluServer.InfoList.clear();
                            System.out.print("\n\n");
                        }else if(resp.contains("iptal")){
                            System.out.print("\n\n");
                            System.out.println(date);
                            System.out.println(name + " canceled seat "+gv.getSeat());
                            HavaYoluServer.Info=false;
                            HavaYoluServer.InfoList.clear();
                            System.out.print("\n\n");

                        }
                        UniCast(name, rspList);

                        
                    } else{
                        ReaderKyrk.add(gv);

                      }
                }
                if(HavaYoluServer.GorevKyrk.peek()==null && ReaderKyrk.peek()!=null){
                    Gorev gv  = ReaderKyrk.remove();
                    String name = gv.getName();
                    ArrayList<String> rspList = gv.mission();
                    for(int i=0;i<rspList.size();i++){
                        HavaYoluServer.InfoList.add(rspList.get(i));
                    }
                    HavaYoluServer.Info=true;
                    UniCast(name, rspList);
                }
                
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void UniCast(String name, ArrayList rspList){
        try{
            ObjectOutputStream ob = HavaYoluServer.users.get(name);
            ob.writeObject((Object)rspList);
            ob.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}




