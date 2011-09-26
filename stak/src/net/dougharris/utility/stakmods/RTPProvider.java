package net.dougharris.utility.stakmods;
/**
 * It is worth your life to deal with all these encodings.
 * Start with RFC3551 Tables 4 and 5, pages 28 and 29.
 * Our capture example seems to have type 5, which is DVI4
 * which is audio type A, 8000Hz, one channel.
 * So we start off interpreting that, and use it as a base.
 *
 */

import net.dougharris.utility.PacketInputStream;
import java.io.IOException;
import java.io.EOFException;
import net.dougharris.utility.DumpHex;

public class RTPProvider extends GenericProvider{
  String parsedTag;
  String messageTag;
  int parsedLength;
  int headerLength;
  int vPadField;
  int version;
  int isPadded;
  int isMarked;
  int isExtended;
  int nCsrc;
  long ssrc;
  int pTypeField;
  int pMarked;
  int pType;
  int seqNbr;
  long tStamp;
  byte[] data;
  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    /*
    The parse should establish messageTag
    The parse should establish headerLength
    The parse should establish messageLength
    */
    /*
     *
2 version
1 padded
1 extended
4  ncsrc
1 marked
7 payloadType
16 sequence
32 timeStamp
32 ssrc
     */
    vPadField=i.readUnsignedByte();headerLength+=1;
    version=(vPadField&0x000000c0)>>6;
    isPadded=(vPadField&0x00000020)>>5;
    isExtended=(vPadField&0x00000000)>>4;
    nCsrc=(vPadField&0x0000000f);
    pTypeField=i.readUnsignedByte();headerLength+=1;
    pMarked=(pTypeField&0x00000080)>>7;
    pType=pTypeField&0x0000007f;
    seqNbr=i.readUnsignedShort();headerLength+=2;
    tStamp=i.readUnsignedInt();headerLength+=4;
    ssrc=i.readUnsignedInt();headerLength+=4;//JDH
    data=new byte[i.available()];
    try{
      i.readFully(data);headerLength+=data.length;
    } catch(EOFException x){
      System.err.println("RTPProvider 70 -"+x);
    } catch(IOException x){
      System.err.println("RTPProvider 72 -"+x);
    } finally{
      messageLength=-1;
      messageTag=null;
      setMessageTag(messageTag);
      setMessageLength(messageLength);
      setLength(headerLength+messageLength);
      return this;
    }
  }

  public String providerReport(String type) throws Exception{
 /*
  * Things have been arranged so this is BOL
  */

    StringBuffer b=new StringBuffer();
    if (type.equals("drop")){
    } else
    if (type.equals("short")){
      b.append(" length ");
      b.append(data.length);
    } else
    if (type.equals("regular")){
      b.append("version:");
      b.append(version);
      b.append("\n");
      b.append("pMarked:");
      b.append(pMarked);
      b.append("\n");
      b.append("pType:");
      b.append(pType);
      b.append(" nCsrc:");
      b.append(nCsrc);
      b.append("\n");
      b.append("seqNbr:");
      b.append(seqNbr);
      b.append("\n");
      b.append("tStamp:");
      b.append(tStamp);
      b.append("\n");
      b.append("SSRC:");
      b.append(ssrc);
      b.append("\n");
      /*
      if (pType==5){
        i.readUnsignedShort();//predictValue
     	  i.readUnsignedByte();//stepIndex
	      i.readUnsignedByte();//MBZ
      }
      */
      if((data!=null)&&(data.length!=0)){
       b.append(DumpHex.dumpBytes(data));
      }
    }
    return b.toString();
  }
}
