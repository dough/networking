package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;

public class IPProvider extends GenericProvider{
  private int vers;
  public  int hlen;
  private int tos;
  private int totalLength;
  private int id;
  private int flagfrag;
  private int flag;
  private int frag;
  private boolean df;
  private boolean mf;
  private int ttl;
  public int prot;
  private int cksum;
  private byte[] src = new byte[4];
  private byte[] dst = new byte[4];
private int nAvailable;
  public Provider
    parse(PacketInputStream i, int length, String parserTag) throws Exception{
nAvailable=i.available();//JDH
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    int verhlen=(byte)i.readUnsignedByte();headerLength+=1;
    vers = (verhlen&0x000000f0)>>4;
    hlen = (verhlen&0x0000000f)*4;
    tos=(byte)i.readUnsignedByte();headerLength+=1;
    parsedLength=(short)i.readUnsignedShort();headerLength+=2;
    id=i.readUnsignedShort();headerLength+=2;
    flagfrag=i.readUnsignedShort();headerLength+=2;
    ttl=i.readUnsignedByte();headerLength+=1;
    prot=i.readUnsignedByte();headerLength+=1;
    cksum=i.readUnsignedShort();headerLength+=2;
    i.readFully(src);headerLength+=src.length;
    i.readFully(dst);headerLength+=dst.length;
//getMessageLength may be wrong - DIX Ethernet does not know how long it is!
    setLength(parsedLength);
    frag = flagfrag&0x00001fff;
    flag = flagfrag&0x0000e000>>13;
    df = (2==(flag&2));
    mf = (1==(flag&1));
    String pSrc=DumpHex.bytesPrint(src);
    String pDst=DumpHex.bytesPrint(dst);
    /**
     * Here is where we begin pulling off the numbers
     * This one has the value prot which tells the type
     * tag type-from-parse type-stak
     * icmp 1
     * igmp 2
     * tcp 6
     * udp 17
     */
    if (prot == 1){
      parsedTag="icmp";
    } else if (prot == 2){
      parsedTag="igmp";
    } else if (prot == 6){
      parsedTag="tcp";
    } else if (prot == 17){
      if (mf || (frag >0)){
      // no UDP header on the fragment
        parsedTag="raw udp";
      } else {
        parsedTag="udp";
      }
    } else {
      parsedTag="raw";
    }
    String messageTag = parsedTag+":"+pSrc+":"+pDst;
    setMessageTag(messageTag);
    setMessageLength(getLength()-headerLength);
    return this;
  }

  public String providerReport(String type) throws Exception{
    StringBuffer b=new StringBuffer();
b.append("Available bytes:");
b.append(nAvailable);
b.append("\n");
    b.append(vers);
    b.append("   ");
    b.append(" src:");
    b.append(DumpHex.dottedDecimalPrint(src));
    b.append(" dst:");
    b.append(DumpHex.dottedDecimalPrint(dst));
    b.append("   ");
    b.append("hlen:");
    b.append(hlen);
    b.append(" len:");
    //JDH replaced this b.append(getLength());
    //JDH maybe it should be parsedLength
   // b.append(getMessageLength());
    b.append(parsedLength-hlen);
    b.append(" tos:");
    b.append(tos);
    b.append(" id:");
    b.append(id);
    if (df) b.append(" DF ");
    if (mf) b.append(" MF");
    if ((frag>0)||mf){
      b.append("\nFrag:");
      b.append(frag);
    }
    b.append(" ttl:");
    b.append(ttl);
    b.append(" protocol:");
    b.append(prot);
    b.append(" CheckSum:");
    b.append(DumpHex.shortPrint((short)cksum));
    return b.toString();
  }
}
