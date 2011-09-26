package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.ArrayList;

public class TCPConnectionProvider extends GenericProvider{
  private static Hashtable synseq = new Hashtable();
  private String srcHost;
  private String dstHost;
  private int sPort;
  private int dPort;
  private String srcPort; //in hex
  private String dstPort; //in hex
  private long seqN;
  private long ackN;
  private int offset; // how many 4-byte in header
  private int optionLength;
  private byte[] options;
  private int reserved;
  private int flagword;
  private int flags;
  private boolean urg;
  private boolean ack;
  private boolean psh;
  private boolean rst;
  private boolean syn;
  private boolean fin;
  private int window;
  private int chksum;
  private int urgent;
  private int messageLength;
  private boolean showOptions=false;
  private String connection;
  private String connectrev;
  boolean connected;

  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    sPort = i.readUnsignedShort();headerLength+=2;
    dPort = i.readUnsignedShort();headerLength+=2;
    seqN  = i.readUnsignedInt();headerLength+=4;
    ackN  = i.readUnsignedInt();headerLength+=4;
    flagword = i.readUnsignedShort();headerLength+=2;
    offset =   (flagword&0x0000f000)>>12;
    optionLength = (offset*4) - 20;
    window = i.readUnsignedShort();headerLength+=2;
    chksum = i.readUnsignedShort();headerLength+=2;
    urgent = i.readUnsignedShort();headerLength+=2;
    if (optionLength >0){
      options = new byte[optionLength];
      i.readFully(options);headerLength+=options.length;
    }
    setMessageLength(getLength()-headerLength);

    reserved = (flagword&0x00000fc0)>>06;
    flags    = (flagword&0x00000c3f)>>00;
    urg = (32==(flags&0x00000020));
    ack = (16==(flags&0x00000010));
    psh = (8==(flags&0x00000008));
    rst = (4==(flags&0x00000004));
    syn = (2==(flags&0x00000002));
    fin = (1==(flags&0x00000001));

    ArrayList tags = new ArrayList();
    tags = GenericProvider.parseTags(getTag());
    srcHost = (String)tags.get(1);
    dstHost = (String)tags.get(2);
    srcPort = DumpHex.shortPrint((short)sPort);
    dstPort = DumpHex.shortPrint((short)dPort);
    connection = srcHost+":"+srcPort+"-"+dstHost+":"+dstPort;
    connectrev = dstHost+":"+dstPort+"-"+srcHost+":"+srcPort;
    Object isn;
    long offset;
    Connection current;
    if (syn){
      if (null!=(current=(Connection)synseq.get(connectrev))){
      // then this syn is from the passive peer
        current.setPassiveName(connection);
        current.setPassiveSeq(seqN);
	current.halfOpen=false;
      } else {
        synseq.put(connection, new Connection(seqN, connection));
      }
    } 
    if (null!=(current=(Connection)synseq.get(connection))){
    //I am active
      offset = current.getActiveSeq();
      seqN -= offset;
      offset = current.getPassiveSeq();
      ackN -= offset;
    } else
    //I am passive
    if (null!=(current=(Connection)synseq.get(connectrev))){
      offset = current.getPassiveSeq();
      seqN -= offset;
      offset = current.getActiveSeq();
      ackN -= offset;
    }
//JDH need to think this through - you need both of them before removing
// probably set something to null or 0 and only remove if both are gone!
    if (fin){
      if (null==(current=(Connection)synseq.get(connection))){
        if (null!=(current=(Connection)synseq.get(connectrev))){
	  current.halfClosed=true;
	}
      }
      if (current!=null){
        if (current.halfClosed){
          synseq.remove(connection);
	}
      }
    }

    // At the moment we recognize only HTTP and SMTP on top of TCP
    /**
     *  Here are what it can parse
     *  SMTP server - delivers to SMTP server
     *  SMTP client - not sure what to do
     *  HTTP server - delivers to HTTP server
     *  HTTP client - not sure what to do
     */
    parsedTag=null;
    if(sPort == 25){
      parsedTag="SMTP:Server ";
    } else
    if(dPort == 25){
      parsedTag="SMTP:Client ";
    } else
    if(sPort == 80){
      parsedTag="http:Server ";
    } else
    if(dPort == 80){
      parsedTag="http:Client ";
    } else {
      parsedTag="raw";
    }
    String messageTag = parsedTag+":"+sPort+":"+dPort;

    setMessageTag(messageTag);
    return this;
  }

  public String providerReport(String type) throws Exception{
    StringBuffer b=new StringBuffer();
    b.append("     ");
    b.append("src:");
    b.append(sPort);
    b.append(" dst:");
    b.append(dPort);
    b.append(" ");
    b.append(psh?"P":" ");
    b.append(urg?"U":" ");
    b.append(syn?"S":" ");
    b.append(ack?"A":" ");
    b.append(fin?"F":" ");
    b.append(rst?"R":" ");
    b.append(" ");
    b.append("s:");
    b.append(seqN);
    b.append(" l:");
    b.append(getMessageLength());
    b.append(" a:");
    b.append(ackN);
    b.append(" w:");
    b.append(window);
    b.append(" ");
    if (urg) b.append("U ");
    if (psh) b.append("P ");
    /*
    b.append("\nchecksum: ");
    b.append(DumpHex.shortPrint((short)chksum));
    if (urg){
      b.append("\nurgent: ");
      b.append(urgent);
    }
    */
    if (showOptions&&optionLength>0){
      b.append("\n");
      b.append("Options of length ");
      b.append(optionLength);
        for (int j=0;j<optionLength;j++){
        int kind = options[j];
        b.append(":kind-");
        b.append(kind);
        b.append(" ");
        switch(kind){
        case 0:
          j=optionLength;
        break;
        case 1:
          b.append("NOP");
        break;
        case 2:
          j++;
          j++;
          int mss = 256*(options[j]+((options[j]<0)?256:0)); 
          j++;
          mss += options[j]+((options[j]<0)?256:0); 
          b.append("MSS=");
          b.append(mss);
        break;
        case 4:
          b.append("SACK");
          j++; // skip the length byte
        break;
        default:
          j++;
          int skip = options[j];
          for(int k = 0;k<skip;k++){
            j++;
          }
          b.append("skipped length "+skip);
        }
      }
    }
    return b.toString();
  }

  class Connection{
    private long[]   initialSeqs  = new long[2];
    private String[] connectNames = new String[2];
    private boolean halfOpen;
    private boolean halfClosed;

    Connection(long initialSeq, String connectName){
      initialSeqs[0]  = initialSeq;
      connectNames[0] = connectName;
      halfOpen=true;
      halfClosed=false;
    }

    private long getActiveSeq(){
      return initialSeqs[0];
    }

    private long getPassiveSeq(){
      return initialSeqs[1];
    }

    private void setPassiveSeq(long seq){
      initialSeqs[1]=seq;
    }

    private String getActiveName(){
      return connectNames[0];
    }

    private String getPassiveName(){
      return connectNames[1];
    }

    private void setPassiveName(String name){
      connectNames[1]=name;
    }
  }
}
