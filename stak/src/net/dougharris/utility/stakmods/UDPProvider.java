package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import java.io.IOException;
import java.io.EOFException;
import net.dougharris.utility.DumpHex;

public class UDPProvider extends GenericProvider{
  private int sPort;
  private int dPort;
  private int checkSum;
private int nAvailable;//JDH
  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
nAvailable=i.available();
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    sPort = i.readUnsignedShort();headerLength+=2;
    dPort = i.readUnsignedShort();headerLength+=2;
    /*
     * I guess this would also be the length
     */
    parsedLength = i.readUnsignedShort();headerLength+=2;
    checkSum = i.readUnsignedShort();headerLength+=2;
      /*
       *   At this point i has been moved right 8 bytes
       *   Also sPort and dPort have been extracted
       *   and need to be passed to the next Provider somehow
       *   Perhaps just by adding it to the pTag
       */
    /**
     * Here begins the parse.
     * For now it only knows
     * dns server deliver to DNS server parser
     * dns client
     */
      parsedTag="raw";
      if ((sPort==(short)53)||(dPort == (short)53)){
        parsedTag="dns";
      }
      if (dPort == (short)3300){
        parsedTag="rtp";
      }
      if (dPort == (short)3301){
        parsedTag="rtcp";
      }
      if (dPort == (short)3302){
        parsedTag="rtp";
      }
      if (dPort == (short)3303){
        parsedTag="rtcp";
      }
      if (dPort == 42050){
        parsedTag="rtp";
      }
      if (dPort == 42051){
        parsedTag="rtcp";
      }
      if (dPort == 42052){
        parsedTag="rtp";
      }
      if (dPort == 42053){
        parsedTag="rtcp";
      }

//JDHE      String messageTag = parsedTag+":"+sPort+" "+dPort;
      String newMessageTag = parsedTag+":"+sPort+" "+dPort;
      setMessageTag(newMessageTag);
      setMessageLength(parsedLength-headerLength);
      return this;
  }

  public String providerReport(String type) throws Exception{
    StringBuffer b = new StringBuffer();
b.append("Available bytes:");
b.append(nAvailable);
b.append("\n");
    b.append("src:");
    b.append(sPort);
    b.append(" dst:");
    b.append(dPort);
    b.append(" length=");
    b.append(parsedLength);
    b.append(" cksum=");
    b.append(DumpHex.shortPrint((short)checkSum));
    return b.toString();
  }
}
