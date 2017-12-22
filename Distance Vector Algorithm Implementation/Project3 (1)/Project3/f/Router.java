import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;


class MainThread extends Thread
{
	String operation;
	String routingTableRow;
	InetAddress ipAddress;
	int portNumber;
    
	
	
	public MainThread(String operation) {
		super();
		this.operation = operation;
	}
	public  MainThread(String operation,String routingTableRow,InetAddress ipAddress,int portNumber) {
		// TODO Auto-generated constructor stub
		this.operation=operation;
		this.routingTableRow=routingTableRow;
		this.ipAddress=ipAddress;
		this.portNumber=portNumber;
	
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		if(operation.equals("readRoutingInformation"))
		{
			Router.readRoutingTableFromSocket();
			
		}
		else if(operation.equals("sendRoutingInformation"))
		{
			while(true)
			{
			//read the inputfile for checking cost changes
			Router.readInputFile();
			
			//print the routing table
			Router.printRoutingTable();
			
			//send the routing table
			Router.sendRoutingTable();
			
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			
			
		}
		else if(operation.equals("updateroutingtable"))
		{
			
			Router.updateRoutingtable(routingTableRow,ipAddress,portNumber);
			// recalculate the routing table
			Router.performDistanceVectorAlgorithm();
			
		}
	
	}
	
}

public class Router {

	
	static int totalNoOfRouters;
	static Double [][] routingTable;
	static int myPortNumber;
	static int myRouterNumber;
	static File inputFile;
	static int noOfNeighbourRouters;
	static ArrayList<Integer> neighbourRoutersNumber;
	static ArrayList<String> neighbourName;
	static ArrayList<String> neighbourIpAddress;
	static ArrayList<Integer> neighbourPortNumbers;
	static ArrayList<Double> neighbourCost;
	static ArrayList<Integer> hopRouterList;
	static HashMap<Integer,Integer>hopRouterListHashMap;
	static String inputFilePath;
	static DatagramSocket my_router_socket;
	static ArrayList<Double>initalneighbourRouterCost;
	static int outputNumber=1;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		 totalNoOfRouters=Integer.parseInt(args[0]);
		 routingTable=new Double[totalNoOfRouters][totalNoOfRouters];
		 inputFilePath=args[1];
		 myPortNumber=Integer.parseInt(args[2]);
		 myRouterNumber=Integer.parseInt(args[3])-1;
		 inputFile=new File(inputFilePath);
		 BufferedReader br;
		 noOfNeighbourRouters=0;
		 neighbourName=new ArrayList<String>();
		 neighbourRoutersNumber=new ArrayList<Integer>();
		 neighbourIpAddress= new ArrayList<String>();
		neighbourPortNumbers=new ArrayList<Integer>();
		neighbourCost=new ArrayList<Double>();
		hopRouterList=new ArrayList<Integer>();
		hopRouterListHashMap=new HashMap<Integer,Integer>();
		initalneighbourRouterCost=new ArrayList<Double>();
		
		try {
			my_router_socket= new DatagramSocket(myPortNumber);
			System.out.println("Router "+myRouterNumber +" at port "+myPortNumber +" is ready !!!!!");
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	
		
		try {
			br = new BufferedReader(new FileReader(inputFile));
			noOfNeighbourRouters = Integer.parseInt(br.readLine());
			
			for(int i=0;i<noOfNeighbourRouters;i++)
			{
				String[] neighbourCostLine=br.readLine().split(" ");
				//neighbourName.add(neighbourCostLine[0]);
				neighbourRoutersNumber.add(Integer.parseInt(neighbourCostLine[0]));
				neighbourCost.add(Double.parseDouble(neighbourCostLine[1]));
				initalneighbourRouterCost.add(Double.parseDouble(neighbourCostLine[1]));
				
			}
			br.close();
			System.out.println("Neighbour Routers are");
			for(Integer x : neighbourRoutersNumber)
			{
				System.out.println(x);
			}
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Printing the neighbour cost");
		for(Double x:neighbourCost)
		{
			System.out.println(x);
		}
		if(args.length==noOfNeighbourRouters+4)

		{
			for(int i=4;i<args.length;i++)
			{
				String IpAddressPort=args[i];
				String[] IpAddressPortofNeighbour=IpAddressPort.split(":");
				neighbourIpAddress.add(IpAddressPortofNeighbour[0]);
				neighbourPortNumbers.add(Integer.parseInt(IpAddressPortofNeighbour[1]));
			}
			
		}
		else
		{
			System.out.println("Please provide valid inputs");
			return;
		}
		
		System.out.println("Total no of routers :"+ totalNoOfRouters);
		System.out.println("Total no of Neighbour routers"+noOfNeighbourRouters);
		System.out.println("Negibours address");
		
		for(int j=0;j<noOfNeighbourRouters;j++)
		{
			System.out.println(" Neighbour Router Number/Name: "+neighbourRoutersNumber.get(j));
			System.out.println("IP Address: "+neighbourIpAddress.get(j));
			System.out.println("Port Number: "+neighbourPortNumbers.get(j));
		}
		
		initializeRoutingTable();
		//Printing Routing table after initializing
		
		//printRoutingTable();
		for(int i=0;i<totalNoOfRouters;i++)
		{
			for(int j=0;j<totalNoOfRouters;j++)
			{
				System.out.print(routingTable[i][j].toString()+ " ");
				
			}
			System.out.println();
		}
		
		//starting a thread for reading the router table from neighbour routers
		
		MainThread readingThread=new MainThread("readRoutingInformation");
		readingThread.start();
		MainThread sendingThread=new MainThread("sendRoutingInformation");
		sendingThread.start();
		

	}
	
	static void initializeRoutingTable()
	{
		
		for(int i=0;i<totalNoOfRouters;i++)
		{
			for(int j=0;j<totalNoOfRouters;j++)
			{
				if(i==myRouterNumber)
				{
					if(j==myRouterNumber)
					{
						routingTable[i][j]=0.0;
						hopRouterList.add(myRouterNumber+1);
						hopRouterListHashMap.put(j,myRouterNumber+1);
						
					}
					else
					{
						//find the neighbour 
						if(neighbourRoutersNumber.contains(j+1))
						{
							int position=neighbourRoutersNumber.indexOf(j+1);
							routingTable[i][j]=neighbourCost.get(position);
						//	hopRouterList.set(j,neighbourRoutersNumber.get(position));
							//hopRouterList.add(j, neighbourRoutersNumber.get(position))
							hopRouterList.add(neighbourRoutersNumber.get(position));
							hopRouterListHashMap.put(j,neighbourRoutersNumber.get(position));
						}
						else
						{
							routingTable[i][j]=Double.MAX_VALUE;
							hopRouterList.add(0);
							hopRouterListHashMap.put(j,0);
							
						}
			
					}
					
				}
				else
				{
					routingTable[i][j]=Double.MAX_VALUE;
				}
				
			}
		}
		
	}
	
	static void readRoutingTableFromSocket()
	{
		
		while (true) {
			try {
				String operation = "updateRoutingTable";
				byte[] data = new byte[1024];
				int size = data.length;
				DatagramPacket packet = new DatagramPacket(data, size);
				my_router_socket.receive(packet);
				int length = packet.getLength();
				String routingTableRow = new String(packet.getData(), 0, length);
				//MainThread updateRoutingTableThread = new MainThread(operation, routingTableRow,packet.getAddress(),packet.getPort());
				//updateRoutingTableThread.start();
				updateRoutingtable(routingTableRow,packet.getAddress(),packet.getPort());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	static void updateRoutingtable(String routingTableRow,InetAddress ipAddress,int portNumber)
	{
		
		
		boolean foundFlag=false;
		int routerNumber = -1;
		String[] routingTableValues=routingTableRow.split(":");
		String ipAddressString=ipAddress.toString();
		String actualIPAddress=ipAddressString.substring(1);
	   for(int i=0;i<noOfNeighbourRouters;i++)
	  {
		  if(neighbourIpAddress.get(i).equals(actualIPAddress))
		  {
			  for(int j=0;j<noOfNeighbourRouters;j++)
			  {
				 if(neighbourPortNumbers.get(j)==portNumber)
				 {
					foundFlag=true;
					routerNumber=neighbourRoutersNumber.get(j);
					System.out.println("Recieved info from Router"+routerNumber);
				 }
			  }
		  }
	  }
	  if(foundFlag==false || routerNumber ==-1)
	  {
		System.out.println("Couldnt find the router informations.. Please check ip address and port numbers");  
	  }
	  else
	  {
		  System.out.println("Going to update the routing table");
		  for(int i=0;i<totalNoOfRouters;i++)
		  {
		    routingTable[routerNumber-1][i]=Double.parseDouble(routingTableValues[i]);
		  }
		  Router.performDistanceVectorAlgorithm();
	  }
	  
	}
	
	
	static void recalculateTable()
	{
		
		for(int i=0;i<totalNoOfRouters;i++)

		{
			
			if (i == myRouterNumber) {
				continue;
			} 
		
			Double minValue=Double.MAX_VALUE;
			
			for(int j=0;j<neighbourRoutersNumber.size();j++)
			{
				
				
					int ind = neighbourRoutersNumber.get(j)-1;
				//	System.out.println("Current value"+routingTable[myRouterNumber][i]);
					//System.out.println("Caluclated value"+routingTable[myRouterNumber][ind] + routingTable[ind][i]);
					if (minValue > neighbourCost.get(j)+ routingTable[ind][i]) {
						//hopRouterList.set(j,neighbourRoutersNumber.get(i));
						minValue= neighbourCost.get(j) + routingTable[ind][i];
						hopRouterListHashMap.put(i, neighbourRoutersNumber.get(j));
						routingTable[myRouterNumber][i] = neighbourCost.get(j) + routingTable[ind][i];
						System.out.println("After updating value is"+routingTable[myRouterNumber][i]);
					}

				
			}
			
			
		}
	}
	
	static void performDistanceVectorAlgorithm()
	{
		
		for(int i=0;i<totalNoOfRouters;i++)

		{
			
			if (i == myRouterNumber) {
				continue;
			} 
		
			Double minValue=Double.MAX_VALUE;
			
			for(int j=0;j<neighbourRoutersNumber.size();j++)
			{
				
				
					int ind = neighbourRoutersNumber.get(j)-1;
				//	System.out.println("Current value"+routingTable[myRouterNumber][i]);
					//System.out.println("Caluclated value"+routingTable[myRouterNumber][ind] + routingTable[ind][i]);
					if (minValue > neighbourCost.get(j)+ routingTable[ind][i]) {
						//hopRouterList.set(j,neighbourRoutersNumber.get(i));
						minValue= neighbourCost.get(j) + routingTable[ind][i];
						hopRouterListHashMap.put(i, neighbourRoutersNumber.get(j));
						routingTable[myRouterNumber][i] = neighbourCost.get(j) + routingTable[ind][i];
						System.out.println("After updating value is"+routingTable[myRouterNumber][i]);
					}

				
			}
			
			
		}
	}
	
	static void performDistanceVectorAlgorithmOld()
	{
		
		for(int i=0;i<totalNoOfRouters;i++)

		{
			
			if (i == myRouterNumber) {
				continue;
			} 
		
			//Double minValue=Double.MAX_VALUE;
			
			for(int j=0;j<neighbourRoutersNumber.size();j++)
			{
				
				
					int ind = neighbourRoutersNumber.get(j)-1;
				//	System.out.println("Current value"+routingTable[myRouterNumber][i]);
					//System.out.println("Caluclated value"+routingTable[myRouterNumber][ind] + routingTable[ind][i]);
					if (routingTable[myRouterNumber][i] > routingTable[myRouterNumber][ind] + routingTable[ind][i]) {
						//hopRouterList.set(j,neighbourRoutersNumber.get(i));
						//minValue= routingTable[myRouterNumber][ind] + routingTable[ind][i];
						hopRouterListHashMap.put(i, neighbourRoutersNumber.get(j));
						routingTable[myRouterNumber][i] = routingTable[myRouterNumber][ind] + routingTable[ind][i];
						System.out.println("After updating value is"+routingTable[myRouterNumber][i]);
					}

				
			}
			
			
		}
		
			
		/*//hopRouterList.clear();
			for (int i = 0; i < neighbourRoutersNumber.size(); i++) {
				
				Double minvValue=Double.MAX_VALUE;
				int ind = neighbourRoutersNumber.get(i)-1;
				for (int j = 0; j <totalNoOfRouters; j++) {
					
					
					if (j == myRouterNumber) {
						continue;
					} 
					else {

						//if (routingTable[myRouterNumber][j] > routingTable[myRouterNumber][ind] + routingTable[ind][j]) {
					//	if (routingTable[myRouterNumber][j] > neighbourCost.get(i) + routingTable[ind][j]) {
						if (minvValue > routingTable[myRouterNumber][ind] + routingTable[ind][j]) {
							//hopRouterList.set(j,neighbourRoutersNumber.get(i));
							hopRouterListHashMap.put(j, neighbourRoutersNumber.get(i));
							routingTable[myRouterNumber][j] = routingTable[myRouterNumber][ind] + routingTable[ind][j];
						}

					}
				}
			}*/
			
			
		}
	
	static void readInputFile()
	{
		
		System.out.println("Start reading file before sending");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(inputFile));
			noOfNeighbourRouters = Integer.parseInt(br.readLine());	
			neighbourCost.clear();
			for(int i=0;i<noOfNeighbourRouters;i++)
			{
				String[] neighbourCostLine=br.readLine().split(" ");
				//neighbourName.add(neighbourCostLine[0]);
				//neighbourRoutersNumber.add(Integer.parseInt(neighbourCostLine[0])-1);
				neighbourCost.add(Double.parseDouble(neighbourCostLine[1]));
				//initalneighbourRouterCost.add(Double.parseDouble(neighbourCostLine[1]));
				
			}
			br.close();
			/*System.out.println("Printing intial values");
			for(Double x :initalneighbourRouterCost) {
				System.out.println(x);
			}
			
			System.out.println("New values");
			for(Double x :neighbourCost) {
				System.out.println(x);
			}*/
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean changeCostFlag=false;
		for(int i=0;i<noOfNeighbourRouters;i++)
		{
			
			
		//	if(i==myRouterNumber)
			//	continue;
			/*System.out.println("Priting two values");
			System.out.println(initalneighbourRouterCost.get(i));
			System.out.println(neighbourCost.get(i));*/
			if(!(initalneighbourRouterCost.get(i).equals(neighbourCost.get(i))))
			{
				changeCostFlag=true;
				int neighbourRouter=neighbourRoutersNumber.get(i)-1;
				System.out.println("Change cost for the link "+(myRouterNumber+1)+ "->"+neighbourRoutersNumber.get(i));
				System.out.println("initial cost was"+initalneighbourRouterCost.get(i));
				System.out.println("new cost is"+neighbourCost.get(i));
				
				
				if(initalneighbourRouterCost.get(i)<neighbourCost.get(i))
				{
					System.out.println("Bad news");
					routingTable[myRouterNumber][neighbourRouter]=neighbourCost.get(i);
				}
				else
				{
					System.out.println("Good News");
					routingTable[myRouterNumber][neighbourRouter]=neighbourCost.get(i);
				}
			}
			
		}
		
		//if there is a change perform the routing algorithm
		if(changeCostFlag)
		{
			System.out.println("Printing the routing table before performing algo,after link cost");
			printRoutingTable();
			
	//performDistanceVectorAlgorithm();
			recalculateTable();
		System.out.println("Printing the routing table after performing algo,after link cost");
		printRoutingTable();
		initalneighbourRouterCost.clear();
		for(int i=0;i<noOfNeighbourRouters;i++)
		{
			initalneighbourRouterCost.add(neighbourCost.get(i));
		}
		}
		else
			System.out.println("no cost changes in file");
		
		
	}
	
	static void printRoutingTable()
	{
		
		System.out.println("Output Number"+outputNumber);
		outputNumber++;
		/*System.out.println("Hop routers list");
		for(int x :hopRouterList)
		{
			System.out.println(x);
		}*/
		System.out.println("-------------------------------------------------------------");
		for(int i=0;i<totalNoOfRouters;i++)
		{
			System.out.println("Shortest Path from router "+(myRouterNumber+1)+"-"+(i+1)+ ":"+ "the next hop router is "+ hopRouterListHashMap.get(i)+" and the cost is "+routingTable[myRouterNumber][i]);
		}
		
		System.out.println("-------------------------------------------------------------");
		System.out.println();
		System.out.println();
		System.out.println("Complete routing table");
		System.out.println();
		System.out.println("-------------------------------------------------------------");
		for(int i=0;i<totalNoOfRouters;i++)
		{
			for(int j=0;j<totalNoOfRouters;j++)
			{
				System.out.print(routingTable[i][j].toString()+ " ");
				
			}
			System.out.println();
		}
		System.out.println("-------------------------------------------------------------");
	}
	
	static void sendRoutingTable()
	{
	    /*ArrayList<Double> myRoutingTableRow=new ArrayList<Double>();
		for(int j=0;j<totalNoOfRouters;j++)
		{
			myRoutingTableRow.add(routingTable[myRouterNumber][j]);
		}*/
		
		try {
			
			
			for (int i = 0; i < noOfNeighbourRouters; i++) {
				String data = "";
				for (int j = 0; j < totalNoOfRouters; j++) {
					if (neighbourRoutersNumber.get(i).equals(hopRouterListHashMap.get(j)) && ((neighbourRoutersNumber.get(i)-1)!=j)) 
                    {
						
						//System.out.println("shortest path from "+(myRouterNumber+1)+"-"+neighbourRoutersNumber.get(i)+ ":the next hop is"+hopRouterList.get(j));
						data = data + Double.MAX_VALUE + ":";
					} else {
					
						data += routingTable[myRouterNumber][j]+ ":";
					}
				}
				
				System.out.println("----------------------------------------------------------------------");
				System.out.println("Sending Routing information to router" +neighbourRoutersNumber.get(i));
				System.out.println("routing information is"+data);
				System.out.println("----------------------------------------------------------------------");
				System.out.println();
				InetAddress ipAddress=InetAddress.getByName(neighbourIpAddress.get(i));
				DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length,ipAddress,neighbourPortNumbers.get(i));
			//	packet.setAddress(InetAddress.getByName(neighbourIpAddress.get(i)));
				//packet.setPort(neighbourPortNumbers.get(neighbourPortNumbers.get(i)));
				my_router_socket.send(packet);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
