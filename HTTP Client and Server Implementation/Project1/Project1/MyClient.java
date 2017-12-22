import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyClient {
	@SuppressWarnings("deprecation")
    public static void main(String [] args) {
      String serverName = "";
	  int port = -1;
	  String method = "";
	  String file = ""; 
	  String request = "";
	  String data = "";
	  byte[] bs;
	  int i=0;
          
          PrintStream ps = null;
          DataInputStream dis = null;
          Socket client = null;
	  
		if(args.length == 4){
		  serverName = args[0];
		  port = Integer.parseInt(args[1]);
		  method = args[2];
		  file = args[3];
		}	
		else{
			System.out.println("Enter correct parameters!");
			return;
		}
      try {
         System.out.println("Connecting to " + serverName + " on port " + port);
         
		 if(method.toLowerCase().equals("get")){
			 //System.out.println("GET method called");
			 client = new Socket(serverName, port);
			 request = method.toUpperCase() + " " + file + " HTTP/1.1\r\nHost: " + serverName + "\r\n";
			 System.out.println(request);
			 ps = new PrintStream(client.getOutputStream());
			 ps.println(request);
			 
			 dis = new DataInputStream(client.getInputStream());
			 System.out.println("Server Response : \n");
			 data = dis.readLine();
			 while(data!=null){
				 System.out.println(data);
				 data = dis.readLine();
			 }
			 
		 }
		 else if(method.toLowerCase().equals("put")){
			 //System.out.println("PUT method called");
			 request = method.toUpperCase() + " " + file + " HTTP/1.1\r\nHost: " + serverName + "\r\n";
			 client = new Socket(serverName, port);
			 dis = new DataInputStream(client.getInputStream());
			 ps = new PrintStream(client.getOutputStream());
			 
			 File f = new File(file);
			 if(f.exists()){
				 //System.out.println("File exists");
				 int number = (int) f.length();
				 ps.println(request);
				 ps.println(number);
				 FileInputStream fis = new FileInputStream(file);
				 //System.out.println(f.length());
			     bs = new byte[(int)f.length()];
				 fis.read(bs);
				 ps.write(bs);
				 
				 //ps.write(bs);
				 System.out.println("Server Response : \n");
				 
				 System.out.println(dis.readLine());
			 }
			 else{
				 System.out.println("File not present");
				 return;
			 }
		 }
		 else{
			 System.out.println("Incorrect Method!");
		 }
		 
		}catch(IOException e) {
			e.printStackTrace();
		}
      finally{
		  
            try {
                ps.close();
                dis.close();
                client.close();
            } catch (IOException ex) {
                Logger.getLogger(MyClient.class.getName()).log(Level.SEVERE, null, ex);
            }
		}
    }
}
