package net.dougharris.utility.stakmods;
//Tue Aug 24 12:54:44 CDT 2010

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.EOFException;
import java.io.IOException;

public class ICMPProvider extends GenericProvider{
  static String[] icmpTypeStrings = new String[255];
  static String[] unreachableCodeStrings = new String[16];
  static {
    // These values are from
    // http://www.iana.org/assignments/icmp-parameters
    icmpTypeStrings[0]="Echo Reply";
    icmpTypeStrings[1]="Unassigned";
    icmpTypeStrings[2]="Unassigned";
    icmpTypeStrings[3]="Destination Unreachable";
    icmpTypeStrings[4]="Source Quench";
    icmpTypeStrings[5]="Redirect ";
    icmpTypeStrings[6]="Alternate Host Address";
    icmpTypeStrings[7]="Unassigned";
    icmpTypeStrings[8]="Echo ";
    icmpTypeStrings[9]="Router Advertisement";
    icmpTypeStrings[10]="Router Solicitation";
    icmpTypeStrings[11]="Time Exceeded";
    icmpTypeStrings[12]="Parameter Problem";
    icmpTypeStrings[13]="Timestamp";
    icmpTypeStrings[14]="Timestamp Reply";
    icmpTypeStrings[15]="Information Request";
    icmpTypeStrings[16]="Information Reply";
    icmpTypeStrings[17]="Address Mask Request";
    icmpTypeStrings[18]="Address Mask Reply";
    icmpTypeStrings[19]="";
    icmpTypeStrings[20]="";
    icmpTypeStrings[21]="";
    icmpTypeStrings[22]="";
    icmpTypeStrings[23]="";
    icmpTypeStrings[24]="";
    icmpTypeStrings[25]="";
    icmpTypeStrings[26]="";
    icmpTypeStrings[27]="";
    icmpTypeStrings[28]="";
    icmpTypeStrings[29]="";
    icmpTypeStrings[30]="Traceroute";
    icmpTypeStrings[31]="Traceroute";
    icmpTypeStrings[32]="Traceroute";
    icmpTypeStrings[33]="Traceroute";
    icmpTypeStrings[34]="Traceroute";
    icmpTypeStrings[35]="Traceroute";
    icmpTypeStrings[36]="Traceroute";
    icmpTypeStrings[37]="Domain Name Request";
    icmpTypeStrings[38]="Domain Name Reply";
    icmpTypeStrings[39]="SKIP";
    icmpTypeStrings[40]="Photuris";
    unreachableCodeStrings[0]="Net Unreachable";
    unreachableCodeStrings[1]="Host Unreachable";
    unreachableCodeStrings[2]="Protocol Unreachable";
    unreachableCodeStrings[3]="Port Unreachable";
    unreachableCodeStrings[4]="Fragmentation Needed and Don't Fragment";
    unreachableCodeStrings[5]="Source Route Failed";
    unreachableCodeStrings[6]="Destination Network Unknown";
    unreachableCodeStrings[7]="Destination Host Unknown";
    unreachableCodeStrings[8]="Source Host Isolated";
    unreachableCodeStrings[10]="";
    unreachableCodeStrings[11]="";
    unreachableCodeStrings[12]="";
    unreachableCodeStrings[13]="";
    unreachableCodeStrings[14]="";
    unreachableCodeStrings[15]="";
  }
  private int icmpType;
  private int icmpCode;
  private int cksum;
  private long dAddress;
  private long dPreference;
  private long unused;
  private byte[] data;
  private int id  = -1;
  private int seq = -1;
  private long ip;

  public Provider 
  parse(PacketInputStream i, int length, String parserTag) throws Exception{
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    /*
     * The default header size is 8 bytes
     */
    icmpType = i.readUnsignedByte();headerLength+=1;
    icmpCode = i.readUnsignedByte();headerLength+=1;
    cksum = i.readUnsignedShort();headerLength+=2;
    if (icmpType==3){
      unused=i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
    } else
    if (icmpType==9){
i.readUnsignedInt();headerLength+=4;
//This is Router Discovery and requires some work to learn the length
i.readUnsignedInt();headerLength+=4;
    }else 
    if (icmpType==10){
      dAddress=i.readUnsignedInt();headerLength+=4;
      dPreference=i.readUnsignedInt();headerLength+=4;
    }else 
    if (icmpType==17){
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
    }else 
    if (icmpType==18){
i.readUnsignedInt();headerLength+=4;
i.readUnsignedInt();headerLength+=4;
    }else 
    if ((icmpType==0)||(icmpType==8)||(icmpType==13)||(icmpType==14)||(icmpType==17)||(icmpType==18)){
      id  = i.readUnsignedShort();headerLength+=2;
      seq = i.readUnsignedShort();headerLength+=2;
    }
    int messageLength=getLength()-headerLength;
    data = new byte[messageLength];
    i.readFully(data);
// Presumably the messageLength is 0 //JDHI
    setMessageLength(0);
    setMessageTag(null);
    return this;
  }

  public String providerReport(String type) throws Exception{
    StringBuffer b=new StringBuffer();
    b.append("icmpType:");
    if (icmpType < icmpTypeStrings.length){
      b.append(icmpTypeStrings[icmpType]);
    } else {
      b.append(icmpType);
    }
    b.append(" ");
    b.append("icmpCode:");
    if ((icmpType==3)&(icmpCode < unreachableCodeStrings.length)){
      b.append(unreachableCodeStrings[icmpCode]);
    } else {
      b.append(icmpCode);
    }
    if ((icmpType==0)||(icmpType==8)||(icmpType==13)||(icmpType==14)||(icmpType==17)||(icmpType==18)){
      b.append(" ");
      b.append("id:");
      b.append(id);
      b.append(" ");
      b.append("seq:");
      b.append(seq);
      b.append("\n");
    }
    if (data.length>0){
      b.append(" length:"+data.length+"\n");
      b.append(DumpHex.dumpBytes(data));
    }
    return b.toString();
  }
}
