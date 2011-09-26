package net.dougharris.utility;

import net.dougharris.utility.P;
import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.UnsignedInputStream;
import net.dougharris.utility.DumpHex;
import net.dougharris.utility.stakmods.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Properties;
import java.util.Enumeration; // Change this, it is only for one property file
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;

import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class stak{
//JDH iUser moved here to make it globally available
  static InputStream iUser;
  static String options="i-o-I_O_D_d_v-V-L_";
  static P cmdArgs;
  static Properties providerClasses;
  static HashMap providers;
  static HashMap dropProviders;
  static HashMap shortProviders;
  static private byte[] magicBytes = new byte[4];
  static private int major;
  static private int minor;
  static private long timezone;
  static private long junk;
  static private long snaplen;
  static private long sigfigs;
  static private long linktype;
  static private String linkTag;
  static private byte[] data=null;

  static private long timeStamp;
  static private long runStamp;
  static private long oRunningStamp;
  static private long diffTime;
  static private long dumpStart;
  static private boolean bigEndian;
  static private StringBuffer dumpBuffer=new StringBuffer();
  static private PrintWriter outputWriter;
  static private UnsignedInputStream dumpStream;
  static String[] mainArgs;

  public static HashMap getProviderClasses(){
    Properties defaultProviderClasses;
    defaultProviderClasses=new Properties();
    defaultProviderClasses.put("ether", "net.dougharris.utility.stakmods.EPProvider");
    defaultProviderClasses.put("etherPrism", "net.dougharris.utility.stakmods.EPPrismProvider");
    defaultProviderClasses.put("ether11", "net.dougharris.utility.stakmods.EP11Provider");
    defaultProviderClasses.put("arp", "net.dougharris.utility.stakmods.ARPProvider");
    defaultProviderClasses.put("ip", "net.dougharris.utility.stakmods.IPProvider");
    defaultProviderClasses.put("icmp", "net.dougharris.utility.stakmods.ICMPProvider");
    defaultProviderClasses.put("udp", "net.dougharris.utility.stakmods.UDPProvider");
    defaultProviderClasses.put("dns", "net.dougharris.utility.stakmods.DNSProvider");
    defaultProviderClasses.put("tcp", "net.dougharris.utility.stakmods.TCPConnectionProvider");
    defaultProviderClasses.put("http", "net.dougharris.utility.stakmods.HTTPProvider");
    defaultProviderClasses.put("rtp", "net.dougharris.utility.stakmods.RTPProvider");
    defaultProviderClasses.put("rtcp", "net.dougharris.utility.stakmods.RTCPProvider");
    defaultProviderClasses.put("raw", "net.dougharris.utility.stakmods.RAWProvider");
    providerClasses = new Properties(defaultProviderClasses);
    try{
      String propFileName=System.getProperty("java.home")+File.separator+
        "lib"+File.separator+"dump.properties";
      File propFile=new File(propFileName);
      if ((propFile!=null)&&propFile.exists()&&propFile.canRead()){
        InputStream propInputStream = 
          new BufferedInputStream(new FileInputStream(propFile));
        providerClasses.load(propInputStream);
        propInputStream.close();
      }
    } catch(IOException x){
       System.exit(1);
    }
    Enumeration providerClassKeys = providerClasses.propertyNames();
    String providerClassKey = null;
    String providerClassName = null;
    Class c=null;
    Provider pc;
    providers = new HashMap();
    while (providerClassKeys.hasMoreElements()){
      try{
        providerClassKey = (String)providerClassKeys.nextElement();
        providerClassName = providerClasses.getProperty(providerClassKey);
  if (null != providerClassName){
          c = Class.forName(providerClassName);
          pc=(Provider)c.newInstance();
    providers.put(providerClassKey, pc);
  }
      } catch(ClassNotFoundException x){
        System.err.println(x.getClass().getName()+" for "+providerClassName);
      } catch(InstantiationException x){
        System.err.println(x.getClass().getName()+" for "+providerClassName);
      } catch(IllegalAccessException x){
        System.err.println(x.getClass().getName()+" for "+providerClassName);
      }
    }
    return providers;
  }

  public static String formatStamp(long us){
    StringBuffer b=new StringBuffer();
    // the stamp is in microseconds//
    long ms=us/1000;
    us = us-1000*ms;
    long s =ms/1000;
    ms = ms-1000*s;
    b.append(s+"."+DumpHex.decPrint(ms)+"."+DumpHex.decPrint(us));
    return b.toString();
  }

  public static void main(String[] args) throws Exception{
    Runtime.getRuntime().addShutdownHook(new Shutdown());
    String shortList;
    String dropList;
    shortProviders = new HashMap();
    dropProviders = new HashMap();
    String token;
    Set packetHandlers = getProviderClasses().keySet();
    Iterator handlerNames = packetHandlers.iterator();
    int howManyCaptures=0;

    cmdArgs=P.arseArgs(options, args);
    mainArgs=cmdArgs.getParams();
    setLogging();
    setInputOutputStreams();
    if (null!=(shortList=cmdArgs.getProperty("-d"))){
      StringTokenizer k = new StringTokenizer(shortList,":");
      while(k.hasMoreTokens()){
	token=k.nextToken();
        shortProviders.put(token,"");
      }
    }
    if (null!=(dropList=cmdArgs.getProperty("-D"))){
      StringTokenizer k = new StringTokenizer(dropList,":");
      while(k.hasMoreTokens()){
	token=k.nextToken();
        dropProviders.put(token,"");
      }
    }
    while(handlerNames.hasNext()){
      Logger.getLogger((String)handlerNames.next());
    }
    (new stak()).run();
  }

  static void setLogging(){
    Level logLevel;
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

  private static void setInputOutputStreams(){
    String inFileName=null;
    String outFileName=null;
    //InputStream iUser;
    OutputStream oUser;
    try{
      if(null!=(inFileName=cmdArgs.getProperty("-I"))){
        iUser=new FileInputStream(inFileName);
      } else if (cmdArgs.getFlag("-i")) {
        iUser=System.in;
      } else {
        iUser = new FileInputStream("capture.dump");
      }
	dumpStream=new UnsignedInputStream(iUser);
     }catch(Exception x){
	  P.exit(x);
     }
     try{
      if(null!=(outFileName=cmdArgs.getProperty("-O"))){
        oUser=new FileOutputStream(outFileName);
      } else if (cmdArgs.getFlag("-o")){
        oUser=System.out;
      } else {
        oUser=new FileOutputStream("capture.out");
      }
      outputWriter=new PrintWriter(oUser);
    } catch(IOException x){
	    P.exit(x);
    }
  }

  public void run(){
    try{
      parseDump();
// casual.dump did not end here
System.err.println("parseDump ended");//JDHE
      outputWriter.close();
    } catch(EOFException x){
System.err.println("Parsing from dumpStream done ");
//JDH Already written for each Capture      outputWriter.print(dumpBuffer.toString());
      outputWriter.close();
System.err.println("Writing in outputWriter done ");
    } catch(IOException x){
      System.err.println("IOException");
      System.exit(13); //JDH
    } catch(NullPointerException x){
      System.err.println("NullPointerException");
      System.exit(3); //JDH
    }
  }

  public void parseDump() throws EOFException{
    try{
      dumpStream.readFully(magicBytes);
      if (magicBytes[0]==(byte)161){
        bigEndian=true;
      } else {
        bigEndian=false;
      }
      major = dumpStream.readUnsignedShort(bigEndian);
      minor = dumpStream.readUnsignedShort(bigEndian);
      timezone=dumpStream.readUnsignedInt(bigEndian); // 4
      junk=dumpStream.readUnsignedInt(bigEndian);
      snaplen = dumpStream.readUnsignedInt(bigEndian);
      linktype= dumpStream.readUnsignedInt(bigEndian);
//      linkTag=(linktype==119)?"etherPrism":"ether";
switch ((int)linktype){
  case 119:linkTag="etherPrism";break;
  case 127:linkTag="etherPrism";break;
  default: linkTag="ether";break;
}

      dumpBuffer.append("Parse  of a tcpdump file with parameters:\n");
      dumpBuffer.append("  ");
      dumpBuffer.append("(Magic:");
      dumpBuffer.append(DumpHex.bytesPrint(magicBytes));
      dumpBuffer.append(" Major:");
      dumpBuffer.append(major);
      dumpBuffer.append(" Minor:");
      dumpBuffer.append(minor);
      dumpBuffer.append(" timezone:");
      dumpBuffer.append(timezone);
      dumpBuffer.append(" Junk:");
      dumpBuffer.append(junk);
      dumpBuffer.append(" SnapLen:");
      dumpBuffer.append(snaplen);
      dumpBuffer.append(" LinkType:");
      dumpBuffer.append(linktype);
      dumpBuffer.append(")\n");
      dumpBuffer.append("\n");
      outputWriter.print(dumpBuffer.toString());
      dumpBuffer= new StringBuffer();

      Capture c;
//JDHQ How does it know it has come to the end of the dumpStream???? 
      for(int captureNumber=0;;captureNumber++){
      if (iUser.available()==0){
        break;
      }
//Create a Capture container to hold this capture
        c = new Capture(captureNumber);
//Parse the dumpStream into this container
        c.parseCapture(dumpStream);
//Never got here on icmp //JDHE
        timeStamp=c.getTimeStamp();
        if (0 == captureNumber){
          dumpStart = timeStamp;
          dumpBuffer.append("Capturing began at: ");
          dumpBuffer.append(new Date(dumpStart/1000));
          dumpBuffer.append("\n");
          dumpBuffer.append("\n");
          runStamp=0;
        }
        oRunningStamp = runStamp;
        runStamp = timeStamp-dumpStart;
        diffTime = runStamp-oRunningStamp;
        dumpBuffer.append("["+formatStamp(runStamp)+"]");
        dumpBuffer.append("("+formatStamp(diffTime)+") "); 
        dumpBuffer.append(c.toString());
        outputWriter.print(dumpBuffer.toString());
        dumpBuffer= new StringBuffer();
      } //JDH done with this Capture
      outputWriter.close();
    } catch(EOFException x){
System.err.println("parseDump ended");
throw x;
    } catch(Exception x){
System.err.println("Exception");
System.exit(11);
    }
  }

  class Capture{
    String captureString;;
    long captureLength;
    long totalLength;
    long timeStamp;
    int captureNumber;

    public Capture(int captureNumber){
      this.captureNumber=captureNumber;
    }

    public long getTimeStamp(){
      return this.timeStamp;
    }

    public int getNumber(){
      return this.captureNumber;
    }

    public long getCaptureLength(){
      return this.captureLength;
    }

    public long getTotalLength(){
      return this.totalLength;
    }

    public void 
    parseCapture(UnsignedInputStream captureStream) throws IOException, EOFException{
      byte[] capturedBytes=null;
      long sec  = captureStream.readUnsignedInt(bigEndian);
      long usec = captureStream.readUnsignedInt(bigEndian);
      captureLength = captureStream.readUnsignedInt(bigEndian);
      if (captureLength > snaplen){
System.err.println("captureLength is greater than snaplen");//JDH
        throw new EOFException();
      }
      this.totalLength   = dumpStream.readUnsignedInt(bigEndian);
      this.timeStamp = sec*1000000+usec;
      capturedBytes= new byte[(int)captureLength];
      captureStream.readFully(capturedBytes);
      captureString = parsePacketStack(capturedBytes);
    }//end parse of Capture

    public String parsePacketStack(byte[] capturedBytes){
      PacketInputStream i;
      Provider p;
      StringBuffer b;
      String pTag;
      String pKey;
      int pLength;
      String reportType;

      b = new StringBuffer();
      pTag=linkTag;
      i= new PacketInputStream(capturedBytes);
      pLength = (int)getCaptureLength();
/*
If pTag is null then we do nothing, and do not go up the parse stack.
This is the only way to keep from going up
And this is the only place where you can go up
*/
      while(null !=pTag){
	pKey=(String)(GenericProvider.parseTags(pTag)).get(0);
        p= (Provider)providers.get(pKey);
	/*
	 * The p just returned should never be null
         * because we should be using only tags that
         are in the approved list
	 */
        if (p==null){
          System.exit(2);
        }
/**
This is really the central point of the whole thing.
Here we have for example p as an IPProvider which 
sets its length to pLength and uses pTag to parse
and when it has done will have set its pTag to begin
with the appropriate key for the next parse, that is,
the User which should now become the Provider.

In particular it will set the MessageType and the MessageLength
to be used by the next Provider.
*/
	try{
          p= p.parse(i, pLength, pTag); 
 	}catch(Exception x){
          System.exit(4);
	}
// The report is based on the entry pKey not the one after parsing.
// But the content of the report is after parsing
// It has to report before it can change providers
	reportType="regular";
	if (shortProviders.containsKey(pKey)){
	  reportType="short";
	}
	if (dropProviders.containsKey(pKey)){
	  reportType="drop";
	}
/*
  Here it reports on itself 
  and then changes to the new Provider
*/
        b.append(p.toString(reportType));
	pTag=p.getMessageTag();
	pLength=p.getMessageLength();
cmdArgs.fine(
  "JDH P:"+p.getTag()+"@"+p.getLength()+
  " becomes "+
  "P:"+p.getMessageTag()+"@"+p.getMessageLength()
);
      } //looping on pTag
      return b.toString();
    }

    public String toString(){
    /*
     * This shows the entire capture as a string
     * Needs the actual capture in here!
     */
      StringBuffer b=new StringBuffer();
      b.append("  CAPTURE-");
      b.append(DumpHex.decPrint(getNumber()));
      b.append(" ");
      b.append(captureLength);
      b.append("/");
      b.append(totalLength);
      b.append("\n");
      b.append(captureString);
      b.append("\n");
      return b.toString();
    }
  }//end Capture definition

  static class Shutdown extends Thread{
    public void run(){
outputWriter.close();//JDHE
      System.err.println("\n\n\n\n\nThanks for using stak\n\n");
    }
  }
}
