package net.dougharris.utility;

import net.dougharris.utility.jockmods.Chargen;
import net.dougharris.utility.jockmods.DiscardOutputStream;
import net.dougharris.utility.jockmods.EchoProcessor;
import net.dougharris.utility.jockmods.Processor;
import net.dougharris.utility.jockmods.DatagramProcessorPump;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

public class juke{
  static String options="A-e-i-o-s-S-v-V-y-b_n_N_c_C_p_P_h_H_d_D_r_R_w_W_l_L_E_";
  static P cmdArgs;

  /* configuration variables */
  static boolean echo;
  static boolean server;
  static boolean source;
  static boolean serverSource;
  static boolean useStdin;
  static boolean useStdout;
  static int operationCount;
  static int operatorCount;
  static int operatorNumber;
  static Level logLevel;

  /* timing variables */
  static int startDelay;
  static int spinDelay;
  static int minConnectDelay;
  static int maxConnectDelay;

  static InetAddress serverHost;
  static InetAddress clientHost;
  static int serverPort;
  static int clientPort;

  static int sizeRead;
  static int sizeReadBuffer;
  static int sizeWrite;
  static int sizeWriteBuffer;

  static int backlog;
  static String processorClassName;
  static Processor processor;
  static DiscardOutputStream discard=new DiscardOutputStream();

  Chargen chargen;

  static class Client implements Runnable{
    private int whichOperator;

    public void start(){
      new Thread(this).start();
    }

    public void run(){
      DatagramSocket clientSocket;
      try{
        clientSocket = new DatagramSocket();
        clientSocket.connect(new InetSocketAddress(serverHost, serverPort));
        whichOperator = ObjectArrangment(clientSocket);
        cmdArgs.config(whichOperator+" socket connected.");
        //P.ause(1000); //JDH This prevents resets in a source/echo mismatch
        clientSocket.close();
        cmdArgs.config(whichOperator+" socket closed.");
      }catch(IOException x){
        throw new RuntimeException(x);
      }
    }
  }

  static class Server implements Runnable{
    private int whichOperator;
    private DatagramSocket serviceSocket;

    Server(DatagramSocket serviceSocket){
      this.serviceSocket=serviceSocket;
    }

    public void start(){
      new Thread(this).start();
    }

    public void run(){
      try{
        whichOperator=ObjectArrangment(serviceSocket);
      } catch (IOException x){
        throw new RuntimeException(x);
      }
    }
  }

  static class Sender implements Runnable{
    private DatagramSocket senderSocket;
    private InputStream iUser=useStdin?System.in:new Chargen();
    private Thread runThread;
    private byte[] bWrite;
    private int whichOperator;

    Sender(DatagramSocket senderSocket, int whichOperator){
      this.senderSocket=senderSocket;
      this.whichOperator=whichOperator;
    }

    public void start(){
      runThread = new Thread(this);
      runThread.start();
    }

    public void run(){
      cmdArgs.config(whichOperator+" sender starting.");
      bWrite = new byte[sizeWrite];
      DatagramPacket pWrite = new DatagramPacket(bWrite, sizeWrite);
      int nWrite=0;
      int iterationLimit=operationCount;
      try{
        P.ause(startDelay);
        cmdArgs.fine(" starting Sender");
        for (int iterationCount=0;;iterationCount++){
          if ((iterationLimit>0)&&(iterationCount==iterationLimit)) break;
          if (-1==(nWrite=iUser.read(bWrite))) break;
          senderSocket.send(pWrite);
          cmdArgs.fine("at +"+iterationCount+" wrote "+nWrite);
          P.ause(spinDelay);
        }
      } catch(IOException x){
        throw new RuntimeException(x);
      }
    }
  }

  static class Receiver implements Runnable{
    private DatagramSocket receiverSocket;
    private OutputStream oUser = useStdout?(OutputStream)System.out:discard;
    private Thread runThread;
    private int whichOperator;

    Receiver(DatagramSocket receiverSocket, int whichOperator){
      this.receiverSocket=receiverSocket;
      this.whichOperator=whichOperator;
    }

    public void start(){
      runThread = new Thread(this);
      runThread.start();
    }

    public void join(){
      try{runThread.join();}catch(InterruptedException ignored){}
    }

    public void run(){
      cmdArgs.config(whichOperator+" run Receiver.");
      byte[] bRead = new byte[sizeRead];
      DatagramPacket pRead = new DatagramPacket(bRead, bRead.length);
      int nRead;
      try{
        P.ause(startDelay);
	boolean shouldRun=true;
        for(int iterationCount=0;;iterationCount++){
	  if ((operationCount>0)&&(iterationCount>=operationCount)){
	    break;
	  }
          receiverSocket.receive(pRead);
	  nRead=pRead.getLength();
          cmdArgs.fine(whichOperator+" received "+nRead);
          oUser.write(pRead.getData(),0, nRead);
          P.ause(spinDelay);
	  shouldRun=false; //JDH not sure
        }
	receiverSocket.close(); // JDH not sure
        cmdArgs.config(whichOperator+" end run.");
      } catch(IOException x){
        throw new RuntimeException(x);
      }
    }
  }

  static class ReceiveAndSend implements Runnable{
    private DatagramSocket processSocket;
    private DatagramPacket iPacket;
    private DatagramPacket oPacket;
    private InputStream i;
    private OutputStream o;
    private int whichOperator;

    private ReceiveAndSend(DatagramSocket processSocket, int whichOperator){
      this.processSocket=processSocket;
      this.whichOperator=whichOperator;
    }

    public void run(){
      processor = (Processor) P.getInstance(processorClassName);
      cmdArgs.config(whichOperator+" run processor.");
      new DatagramProcessorPump(iPacket, processor, oPacket).run();
    }
  }

  public static void main(String[] args){
    cmdArgs = P.arseArgs(options,args);

    setLogging();
    setArgs();
    setNames();

    if (server){
      Listen();
    } else {
      Connect();
    }
  }

/**
  * java.util.logging.Level has 7 levels plus the extremes of NONE and ALL.
  * jock uses four of these levels internally,
  * and uses the -v and -V flags to set them.
  * The default logging if no flags are specified is SEVERE.
  * In the jock code this level is accomplished with the severe() method.
  * This is only used just before throwing a RuntimeException.
  * For a moderate amount of logging, limited to basic information
  * only, the -v flag by itself produces Level.INFO logging.
  * In the jock code this level is accomplished with the info() method.
  * If you would like logging up through addressing, and creating objects,
  * the -V flag produces CONFIG level logging, done in the code with config().
  * Finally, the finest logging that the jock code itself performs is specified
  * using both flags -v and -V, and done in the code by fine().
  * 
  * You may wish to send the logging information to a file, rather than
  * to the default standard error stream, and this is done by giving the
    name of that file as the value of the property -L.
  */
  static void setLogging(){
    logLevel=Level.SEVERE;
    boolean logInfo = cmdArgs.getFlag("-v");
    boolean logFine = cmdArgs.getFlag("-V");
    if (!logFine){
      if (logInfo){ // -v
        logLevel=Level.INFO;
      }
    } else { // logFine
      if (!logInfo){ // -V
        logLevel=Level.CONFIG;
      } else { // -v -V
        logLevel=Level.FINE;
      }
    }
    cmdArgs.setLogLevel(logLevel);
    if (null!=cmdArgs.getProperty("-L")){
      String logFileName=cmdArgs.getProperty("-L");
      cmdArgs.setLogFile(logFileName);
    }
  }

  static void setArgs(){
    server      = cmdArgs.getFlag("-s");
    cmdArgs.info("server "+server);
    echo       = cmdArgs.getFlag("-e");
    cmdArgs.info("echo "+echo);
    serverSource = cmdArgs.getFlag("-S");
    cmdArgs.info("serverSource "+serverSource);
    source=!server^serverSource;//either both false or both true
    sizeRead   = cmdArgs.getIntProperty("-r",1024);//JDH change
    sizeWrite  = cmdArgs.getIntProperty("-w",1024);//JDH change
    sizeReadBuffer   = cmdArgs.getIntProperty("-R",0);//JDH change
    sizeWriteBuffer  = cmdArgs.getIntProperty("-W",0);//JDH change
    useStdin  = cmdArgs.getFlag("-i");
    useStdout = cmdArgs.getFlag("-o");
    startDelay = cmdArgs.getIntProperty("-D",0);
    spinDelay  = cmdArgs.getIntProperty("-d",0);
    minConnectDelay = cmdArgs.getIntProperty("-c",0);
    maxConnectDelay = cmdArgs.getIntProperty("-C",0);
    operationCount  = cmdArgs.getIntProperty("-n",1);
    operatorCount   = cmdArgs.getIntProperty("-N",1);
    backlog    = cmdArgs.getIntProperty("-b" ,5);
    processorClassName = cmdArgs.getProperty("-E","net.dougharris.utility.EchoProcessor");
    if (null!=cmdArgs.getProperty("-E")){
      echo=true;
    }
    if (useStdin&&(null==cmdArgs.getProperty("-n"))){
      operationCount=0;
    }
  }

  static void setSocketArgs(DatagramSocket s) throws SocketException{
    if (null!=cmdArgs.getProperty("-W")){
      s.setSendBufferSize(sizeWriteBuffer);
    }
    if (null!=cmdArgs.getProperty("-R")){
      s.setReceiveBufferSize(sizeReadBuffer);
    }
  }

  static void setNames(){
    try{
      InetAddress local=InetAddress.getLocalHost();
      String localName=local.getHostName();
      clientHost = InetAddress.getByName(cmdArgs.getProperty("-H",localName));
      clientPort = cmdArgs.getIntProperty("-P", 0);
      cmdArgs.config("client address "+clientHost+":"+clientPort);
      serverHost = InetAddress.getByName(cmdArgs.getProperty("-h",localName));
      serverPort = cmdArgs.getIntProperty("-p",8888);
      cmdArgs.config("server address "+serverHost+":"+serverPort);
    } catch (UnknownHostException x){
      P.exception(x,1);
    } catch (NumberFormatException x){
      P.exception(x,1);
    }
  }

  static int ObjectArrangment(DatagramSocket s) throws IOException{
    int whichOperator=operatorNumber++;
    if (!source){ //this object is not to be the source
      if(echo){
        new ReceiveAndSend(s, whichOperator).run();
      } else {
         new Receiver(s, whichOperator).run();
      }
    } else { // this object is to be the source
      Receiver r=null;
      if (echo){
        r=new Receiver(s,whichOperator);
        r.start();
      }
      new Sender(s,whichOperator).run();
      if (r!=null){
        r.join();
      }
    }
    return whichOperator;
  }

  static void Listen(){
    DatagramSocket serviceSocket;
    DatagramSocket listenSocket=null;
    DatagramPacket listenPacket=null;
    int serverSpawned=0;
    boolean shouldRun = true;
    cmdArgs.config("Running as Listen()");
    try{
System.err.println("listening at "+serverHost+":"+serverPort);
      listenSocket = new DatagramSocket(serverPort, serverHost);
      if (!cmdArgs.getFlag("-N")){
        serviceSocket = listenSocket;
        new Server(serviceSocket).run();
      } else {
        while(shouldRun){
          listenSocket.receive(listenPacket);
	  SocketAddress clientAddress = listenPacket.getSocketAddress();
          serviceSocket = new DatagramSocket(serverPort, serverHost);
	  serviceSocket.connect(clientAddress);
          if ((operatorCount>0)&&(serverSpawned++>operatorCount)){
            break;
          }
          new Server(serviceSocket).start();
        }
      }
      listenSocket.close();
    }catch (IOException x){
      throw new RuntimeException(x);
    }
  }

/**
 * This is called to handle Connections, thus from a client,
 * which for jock is equivalent to not having the -s flag set.
 * If the -N propery is not specified then it makes a single connection
 * and when that is completed it dies.  If that property is specified
 * with the value 0 then it keeps making connections at a rate
 * specified by the combined values of the -c/-C properties (what
 * is the default). Otherwise it spawns the specified number of threads,
 * at the specified rate.
**/
  static void Connect(){
    int whichOperator=0;
    cmdArgs.config("Running Connector");
    if (!cmdArgs.getFlag("-N")){
      new Client().run();
    } else {
      for(int clientsSpawned=0;;clientsSpawned++){
        if ((operatorCount>0)&&(clientsSpawned >= operatorCount)){
	  break;
	}
        P.ause(minConnectDelay,maxConnectDelay);
        new Client().start();
      }
    }
  }
}
