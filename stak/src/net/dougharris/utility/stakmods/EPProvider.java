package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;

public class EPProvider extends GenericProvider{
  private byte[] src = new byte[6];   
  private byte[] dst = new byte[6];   
  private int tylen;
  private boolean isIEEE;
  private int dsap;
  private int ssap;
  private int control;
  private byte[] oui = new byte[3];
private int nAvailable;//JDH

  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
nAvailable=i.available();
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    i.readFully(dst);headerLength+=dst.length;
    i.readFully(src);headerLength+=src.length;
    tylen=i.readUnsignedShort();headerLength+=2;
    /**
     * Here is where we decide the user
    */
    if (isIEEE=(tylen <1537)){
      parsedLength=tylen;
      dsap = i.readUnsignedByte();headerLength+=1;
      ssap = i.readUnsignedByte();headerLength+=1;
      control = i.readUnsignedByte();headerLength+=1;
      i.readFully(oui);headerLength+=oui.length;
      if ((dsap== 66)&&(ssap==66)){
       parsedTag="raw";       
      }
    } else {
      parsedLength=getLength()-headerLength;//JDH not 14, think of 802.11
      if (tylen == 2048){
        parsedTag="ip";
      } else if (tylen == 2054){
        parsedTag="arp";
      } else if (tylen == 33079){
        parsedTag="IPX";
      } else parsedTag="raw";
    }
    String uSrc=DumpHex.bytesPrint(src);
    String dSrc=DumpHex.bytesPrint(dst);
    String messageTag = parsedTag+":"+uSrc+":"+dSrc;
    setMessageLength(parsedLength);
    setMessageTag(messageTag);
    return this;
  } 

  public String providerReport(String type) throws Exception{
    StringBuffer b = new StringBuffer();
b.append("Available bytes:");
b.append(nAvailable);
b.append("\n");
    b.append("src:");
    b.append(DumpHex.separatedHexPrint(src));
    b.append(" dst:");
    b.append(DumpHex.separatedHexPrint(dst));
    b.append(" ");
    b.append(isIEEE?"length: ":"type: ");
    b.append(tylen);
    return b.toString();
  }
}
