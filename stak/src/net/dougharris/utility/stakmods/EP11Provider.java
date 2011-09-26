package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;

public class EP11Provider extends GenericProvider{
	/*
/* Be very very careful about littleEndian/bigEndian
 * Apparently IEEE uses littleEndian in headers when they are sent
 * so when reading shorts or ints they have to be read as littleEndian
 * even if we are using bigEndian (network byte order) for everything else
 * Yyeccchhhh!
 */
static private int cn=0;//JDH
  private int controlField;
  private int version;
  private int fieldType;
  private int subType;
  private int flagField;
  private int durationField;
  private byte[] addr1 = new byte[6];   
  private byte[] addr2 = new byte[6];   
  private byte[] addr3 = new byte[6];   
  private byte[] addr4 = new byte[6];   
  private int sqCtl;
  private int etherType;
  private int dsap;
  private int ssap;
  private int snapCtl;
  private byte[] ctlBytes = new byte[3];   

  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    controlField = i.readUnsignedShort();headerLength+=2;
    /*
     * Because I am using an int to hold it the mask must be 32 bits
     * We only use the right 16 bits of the int however
     */
    flagField  = (controlField&0x000000ff);
    version = (controlField&0x00000300)>>8;
    fieldType = (controlField&0x00000c00)>>10;
    subType = (controlField&0x0000f000)>>12;

    durationField = i.readUnsignedShort();headerLength+=2;
    parsedTag="raw";
    if (!(fieldType==2)){
	    /*
	     * For now do nothing if it is not data
	     */
    } else {
if (subType==0){
      i.readFully(addr1);headerLength+=addr1.length;
      i.readFully(addr2);headerLength+=addr2.length;
      i.readFully(addr3);headerLength+=addr3.length;
      sqCtl = i.readUnsignedShort();headerLength+=2;
      dsap=i.readUnsignedByte();headerLength+=1;
      ssap=i.readUnsignedByte();headerLength+=1;
      snapCtl = i.readUnsignedByte();headerLength+=1;
      byte[] ctlBytes = new byte[3];
      i.readFully(ctlBytes);headerLength+=ctlBytes.length;
      etherType=i.readUnsignedShort();headerLength+=2;
      if (etherType == 2048){
        parsedTag="ip";
      } else if (etherType == 2054){
        parsedTag="arp";
      } else if (etherType == 33079){
        parsedTag="IPX";
      } else {
        parsedTag="raw";
      }
}
    }
    setMessageLength(getLength()-headerLength);
    setMessageTag(parsedTag);
    return this;
  } 

  public String providerReport(String type) throws Exception{
    StringBuffer b = new StringBuffer();
    if (type.equals("drop")){
    } else {
      if (type.equals("short")){
        b.append("Passed through EP11 on the way to raw");
      } else {
        if (type.equals("regular")){
          b.append("Showing the controlfield:");//JDH
          b.append(DumpHex.shortPrint((short)controlField));
          b.append("  Capture ");
          b.append(cn++);
          b.append("\n");
          b.append("type:"+fieldType);
          b.append(" ");
          b.append("subtype:");
          b.append(subType);
          b.append(" ");
          b.append("flags:");
          b.append(DumpHex.shortPrint((short)flagField));
          b.append(" ");
          b.append("duration:");
          b.append(durationField);
          //JDHb.append("\n");
	  if ((fieldType==2)&&(subType==0)){
            b.append(DumpHex.bytesPrint(addr1));
            b.append(" ");
            b.append(DumpHex.bytesPrint(addr2));
            b.append(" ");
            b.append(DumpHex.bytesPrint(addr3));
            b.append(" ");
	    b.append(DumpHex.hexPrintShort(sqCtl));
            b.append(" ");
            b.append("ssap/dsap:");
	    b.append(ssap);
            b.append("/");
	    b.append(dsap);
            b.append(" ");
            b.append("ctlBytes:");
            b.append(DumpHex.bytesPrint(ctlBytes));
            b.append(" ");
            b.append("etherType:");
	    b.append(DumpHex.hexPrintShort(etherType));
	  }
        }
      }
    }
    b.append("\n");
    return b.toString();
  }
  /*
  class AssociationRequest{
  2  2   6  6  6   2  2   2    var  var
  fc|dur|da|sa|bss|sc|cap|lint|ssid|rates|
  }
  */
}
