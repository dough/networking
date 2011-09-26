package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.ArrayList;
import net.dougharris.utility.DumpHex;

public class RTCPProvider extends GenericProvider{
  byte[] data=null;
  String parsedTag;
  String messageTag;
  int parsedLength;
  int headerLength;
  int vpCnt;
  int rType;
  int rLength;
  ArrayList reportList;
private int nAvailable;//JDH

  /**
   * These are defined in RFC3550
   */
  static int SR=200;
  static int RR=201;
  static int SDES=202;
  static int BYE=203;
  static int APP=204;

  /**
   * These are defined in RFC3550
   */
  static String[] sdesTypeStrings = new String[9];
  static {
    sdesTypeStrings[0]="END";
    sdesTypeStrings[1]="CNAME";
    sdesTypeStrings[2]="NAME";
    sdesTypeStrings[3]="EMAIL";
    sdesTypeStrings[4]="PHONE";
    sdesTypeStrings[5]="LOC";
    sdesTypeStrings[6]="TOOL";
    sdesTypeStrings[7]="NOTE";
    sdesTypeStrings[8]="PRIV";
  }

  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
    nAvailable=i.available();
    byte[] b;
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    /*
    The parse should establish messageTag
    The parse should establish headerLength
    The parse should establish messageLength
    */
    try{
      /*
       * The messageLength will just be the length of the data just read
       * For RTCP there is nothing above so we should be able to read
       * it all off as reports. We will treat this as entirely a header,
       * that is an envelop with no message.
       */
	    /*
	     * what we really want here is to build an array
	     * of these counts and especially types and values
	     * that is a TLVArray which we can later report on
	     */
    reportList = new ArrayList();
	  nAvailable=i.available();
System.err.println("\nnew RTCPCompoundReport on "+nAvailable);
  data= new byte[nAvailable];
//JDH  i.readFully(data);
/*JDH start report JDH*/
	  while (0<(nAvailable=i.available())){
	    vpCnt = i.readUnsignedByte();headerLength+=1;
	    rType = i.readUnsignedByte();headerLength+=1;
System.err.print("rType: "+rType);
	    rLength=4*(i.readUnsignedShort());headerLength+=2;
System.err.print(" rLength: "+rLength);
	  nAvailable=i.available();
System.err.print(" available: "+nAvailable);
if (nAvailable<rLength){
  System.err.println(" SHORT");
  b = new byte[i.available()];
} else {
  b = new byte[rLength];
}
System.err.println();
	    i.readFully(b);headerLength+=rLength;
	    reportList.add(new RTCPReport(rType, rLength+4, b));
	  }
/*JDH Stop report JDH*/
System.err.println("Done with the RTCPPacket");
      /*
       * Fix up this packet.
       */
      setMessageTag(messageTag); // should be "none"
      setMessageLength(i.available()); // should be 0
      setLength(headerLength+messageLength);
    } catch(EOFException x){
      System.err.println("RTCP got "+x);
    } catch(IOException x){
      System.err.println(x);
    } finally{
      messageLength=-1;
      messageTag=null;
      return this;
    }
  }

  public String providerReport(String type) throws Exception{
	  RTCPReport report;
	  String typeTag;
 /*
  * Things have been arranged so this is BOL
  */
    StringBuffer b=new StringBuffer();
/*JDH Start print report JDH*/
    if (type.equals("drop")){
    } else
    if (type.equals("short")){
      b.append(" length ");
      b.append(data.length);
    } else
    if (type.equals("regular")){
b.append("Available bytes:");
b.append(nAvailable);
b.append("\n");
	    b.append("RTCPReportList:\n");
	    for (int j=0;j<reportList.size();j++){
	       report = (RTCPReport)reportList.get(j);
	    int k;
	    switch(report.type){
	    case 200:
	     k=0;
	     b.append("  SR: length ");
	     b.append(report.length);
	     b.append(" SSRC: ");
	     for (;k<4;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    NTPstamp: ");
	     for (;k<12;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    RTPstamp: ");
	     for (;k<16;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    sentPackets: ");
	     for (;k<20;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    sentBytes: ");
	     for (;k<24;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	    break;
	    case 201:
	     k=0;
	     b.append("  RR:length ");
	     b.append(report.length);
	     b.append(" SSRC: ");
	     for (;k<4;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     if (k<report.length-4){
	     b.append("    SRC 1: ");
	     for (;k<8;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    loss: ");
	     for (;k<12;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    hiseq: ");
	     for (;k<16;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    jitter: ");
	     for (;k<20;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    last SR: ");
	     for (;k<24;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     b.append("    delay: ");
	     for (;k<28;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     }
	    break;
	    case 202:
	     b.append("  SDES: length ");
	     b.append(report.length);
	     b.append(" SSRC: ");
	     for (k=0;k<4;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	      int l;
	      int t;
	      while ((t=report.report[k])!=0){
	        l=report.report[++k];
		b.append("    ");
		b.append(sdesTypeStrings[t]);
		b.append(": ");
		b.append(new String(report.report, ++k, l));
	        b.append("\n");
	        k+=l;
	      }
	     b.append("\n");
	    break;
	    case 203:
	     b.append("  BYE: length ");
	     b.append(report.length);
	     b.append(" SSRC: ");
	     for (k=0;k<4;k++){
	       b.append(DumpHex.hexPrint(report.report[k]));
	     }
	     b.append("\n");
	     if (k<report.length){
	       l=report.report[k];
	       b.append("    REASON: ");
  	       b.append(new String(report.report, ++k, l));
	       b.append("\n");
	     }
	    break;
	    case 204:
	     b.append("APP: length ");
	     b.append(report.length);
	     b.append("\n");
	    break;
      default:
      b.append("Unknown report type");
      break;
	    }
      }
    }
/*JDH Stop print report JDH*/
//JDH     b.append(DumpHex.dumpBytes(data));
    return b.toString();
  }

  class RTCPReport{
	  int type;
	  int length;
	  byte[] report;

	  RTCPReport(int type, int length, byte[] report){
	    this.type=type;
	    this.length=length;
	    this.report=report;
	  }
  }
}
