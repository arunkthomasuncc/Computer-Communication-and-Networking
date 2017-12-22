import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class ThreadServer extends Thread {

    Socket socket;
    PrintStream ps;
    DataInputStream dis;

    ThreadServer(Socket socket) {
        this.socket = socket;
    }

	@SuppressWarnings("deprecation")
    public void run() {
        try {
            System.out.println("Connection: " + socket.getRemoteSocketAddress() + " Opened");
            InputStream in = null;
            OutputStream out = null;
            dis = null;
            ps = null;
            String request = "";
            String fileName = "";
            String method = "";
            String rootPath = "Files";
            String response = "";

            byte[] b;

            in = socket.getInputStream();
            dis = new DataInputStream(in);
            request = dis.readLine();
            System.out.println(request);
            System.out.println(dis.readLine());
            System.out.println(dis.readLine());

            out = socket.getOutputStream();
            String[] splitStr = request.trim().split("\\s+");
            method = splitStr[0];
            fileName = splitStr[1];
            //System.out.println("server go request for 0> " + method);
            //System.out.println("server go request for 1> " + fileName);

            File file = new File(rootPath);
            if (file.exists()) {
                //System.out.println("Root path exists");
            } else {
                //System.out.println("Does not exist");
                file.mkdir();
            }
            rootPath = rootPath + "/" + fileName;
            file = new File(rootPath);

            if (method.toLowerCase().equals("get")) {
                if (file.exists()) {
                    //System.out.println("Your file is present");
                    response = "HTTP/1.1 200 OK\r\n\r\n";
                    try {
                        FileInputStream fin = new FileInputStream(rootPath);
                        int i = 0;
                        while ((i = fin.read()) != -1) {
                            response = response + (char) i;
                        }
                        fin.close();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                } else {
                    //System.out.println("Your file is not present on SERVER");
                    response = "HTTP/1.1 404 Not Found\r\n";
                }
            } else if (method.toLowerCase().equals("put")) {
                System.out.println("Saving your file!");
                int size = Integer.parseInt(dis.readLine());
                b = new byte[size];
                //System.out.println(size);
                file = new File(rootPath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(rootPath);

                for (int k = 0; k < b.length; k++) {
                    int l = dis.read();
                    fos.write((char) l);
                }
                fos.close();

                System.out.println("Created!");
                response = "HTTP/1.1 200 OK File Created";

            } else {
                response = "HTTP/1.1 301 Bad Request";
            }
            ps = new PrintStream(out);
            ps.println(response);
            System.out.println("Connection: " + socket.getRemoteSocketAddress() + " Closed");
        } catch (IOException ex) {
            Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
				if(ps!=null && dis!=null){
					ps.close();
					dis.close();
				}
                socket.close();

            } catch (IOException ex) {
                System.out.println("Error: " + ex);
            }
        }
        //System.out.println("Arraylist before " + MyServer.list.size());
        MyServer.list.remove(this);
        //System.out.println("Arraylist after " + MyServer.list.size());
    }
}

public class MyServer {

    public static ArrayList<ThreadServer> list = new ArrayList<ThreadServer>();
    static ServerSocket serverSocket = null;

    public static void main(String[] args) throws IOException {

        int port = 0;
        if (args.length != 1) {
            System.out.println("Enter Correct Parameters!");
			return;
        } else {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Server started!");
        
        Socket socket = null;
        ThreadServer newConnection;

        try {
            serverSocket = new ServerSocket(port);
			closeAllThreads(serverSocket);

            while (true) {
                socket = serverSocket.accept();
                if (socket != null) {
                    newConnection = new ThreadServer(socket);
                    newConnection.start();
                    list.add(newConnection);
                } else {
                    System.out.println("Some Error Occured!");
                    return;
                }
            }
        } catch (IOException ex) {
            System.out.println("Can't get socket input stream. ");
        }
    }

    public static void closeAllThreads(ServerSocket ss){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("--Closing all Connections--");
                    for (int i = 0; i < list.size(); i++) {
                        ThreadServer server = (ThreadServer) list.get(i);
                        server.ps.close();
                        server.dis.close();
                        server.socket.close();
                    }
                    System.out.println("--Closing Server--");
                    
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ));
    } 
}