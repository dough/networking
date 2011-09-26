package net.dougharris.utility.stakmods;

import net.dougharris.utility.P;
import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.UnsignedInputStream;
import net.dougharris.utility.DumpHex;
import net.dougharris.utility.stakmods.Provider;
import net.dougharris.utility.stakmods.GenericProvider;
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
import java.io.Serializable;
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

public  class Capture implements Serializable{
//    String captureString;;
    long timeStamp;
    int captureNumber;
    long captureLength;
    long totalLength;
    Provider[] providerArray;
transient boolean bigEndian;
transient  long snaplen;
transient String linkTag;
transient ArrayList  providerList =  new ArrayList();
transient HashMap providers;

    public Capture(int captureNumber, boolean bigEndian, long snaplen, String linkTag, HashMap providers){
      this.captureNumber=captureNumber;
      this.bigEndian=bigEndian;
      this.snaplen=snaplen;
      this.linkTag=linkTag;
      this.providers=providers;
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
System.err.println("entered parseCapture");
      byte[] capturedBytes=null;
      long sec  = captureStream.readUnsignedInt(bigEndian);
      long usec = captureStream.readUnsignedInt(bigEndian);
      captureLength = captureStream.readUnsignedInt(bigEndian);
System.err.println("  got captureLength:"+captureLength);
      if (captureLength > snaplen){
System.err.println("captureLength is greater than snaplen");//JDH
        throw new EOFException();
      }
      //JDH QUESTIONthis.totalLength   = dumpStream.readUnsignedInt(bigEndian);
      this.totalLength   = captureStream.readUnsignedInt(bigEndian);
      this.timeStamp = sec*1000000+usec;
      capturedBytes= new byte[(int)captureLength];
      captureStream.readFully(capturedBytes);
      //captureString = parsePacketStack(capturedBytes);
System.err.println("  starting to parsePacketStack-");
      parsePacketStack(capturedBytes);
    }//end parse of Capture

    public void parsePacketStack(byte[] capturedBytes){
PacketInputStream i;
Provider p;
String pTag;
String pKey;
int pLength;
//JDH reporting stuff String reportType;
//JDH reporting stuff     StringBuffer b;
//JDH reporting stuff      b = new StringBuffer();

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
	providerList.add(p);
/* JDH reporting stuff
	reportType="regular";
	if (shortProviders.containsKey(pKey)){
	  reportType="short";
	}
	if (dropProviders.containsKey(pKey)){
	  reportType="drop";
	}
        b.append(p.toString("regular"));
reporting stuff	JDH*/

	pTag=p.getMessageTag();
	pLength=p.getMessageLength();
	/*JDH only for logging
cmdArgs.fine(
  "P:"+p.getTag()+"@"+p.getLength()+
  " becomes "+
  "P:"+p.getMessageTag()+"@"+p.getMessageLength()
);
JDH*/
      } //looping on pTag
      //turn providerList into an array
Provider[] sample=new Provider[0];
      providerArray = (Provider[])providerList. toArray(sample);
    }

    public String toString(){
      StringBuffer b=new StringBuffer();
      b.append("  CAPTURE-");
      b.append(DumpHex.decPrint(getNumber()));
      b.append(" ");
      b.append(captureLength);
      b.append("/");
      b.append(totalLength);
      b.append("\n");
      b.append("Number of providers is  ");
      b.append(providerArray.length);
      b.append("\n");
for (int j=0;j<providerArray.length;j++){
  //JDHb.append(providerArray[j]);
try{
  //b.append(providerArray[j].providerReport("regular"));
  //System.err.println((Provider)providerArray[j].toString());
  b.append(providerArray[j].toString());
}catch(Exception x){
  System.err.println("x");
}
  b.append("\n");
}
      b.append("\n");
      return b.toString();
    }
  }//end Capture definition
