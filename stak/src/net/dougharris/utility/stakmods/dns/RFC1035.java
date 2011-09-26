package net.dougharris.utility.stakmods.dns;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class RFC1035{
  static protected HashMap typeNames = new HashMap();
  static{
    typeNames.put(new Integer(1), "A");
    typeNames.put(new Integer(2),"NS");
    typeNames.put(new Integer(5),"CNAME");
    typeNames.put(new Integer(6),"SOA");
    typeNames.put(new Integer(11),"WKS");
    typeNames.put(new Integer(12),"PTR");
    typeNames.put(new Integer(13),"HINFO");
    typeNames.put(new Integer(15),"MX");
    typeNames.put(new Integer(16),"TXT");
  }
  private PacketInputStream inputStream = null;
  protected QueryRecord[]    queryArray = new QueryRecord[0];
  protected ResourceRecord[] ansArray   = new ResourceRecord[0];
  protected ResourceRecord[] autArray   = new ResourceRecord[0];
  protected ResourceRecord[] addArray   = new ResourceRecord[0];
  StringBuffer reportBuffer=new StringBuffer();
  short id;
  boolean qr;
  int opcode;
  boolean aa;
  boolean tc;
  boolean rd;
  boolean ra;
  int rcode;
  short flags;
  short queryCount;
  short answerCount;
  short authorityCount;
  short additionalCount;

  public RFC1035(byte[] b){
    this(b, 0, b.length);
  }

  public RFC1035(byte[] b, int offset, int length){
    try{
      this.inputStream = new PacketInputStream(b, offset, length);
    }catch(Exception x){
      System.exit(2);
    }
  }

  public ResourceRecord[] getAnswers(){
    return ansArray;
  }

  public ResourceRecord[] getAuthorities(){
    return autArray;
  }

  public ResourceRecord[] getAdditional(){
    return addArray;
  }

  public static byte[] createQueryPacket(String name, String type, boolean recurse){
    int intType;
    try{
      intType = Integer.parseInt(type);
    } catch(NumberFormatException x){
      intType=RFC1035.getTypeNumber(type);
      if (intType == -1){
        System.err.println("No type "+type);
        System.exit(2);
      }
    }
    return createQueryPacket(intType, name, recurse);
  }

  public static byte[] createQueryPacket
    (int type, String name, boolean recurse){
    ByteArrayOutputStream ob = new ByteArrayOutputStream();
    DataOutputStream o = new DataOutputStream(ob);
    try{
      String n;
      o.writeShort(1);     // Id
      if (recurse){
        o.writeShort(256);   // Recursion Desired
      } else {
        o.writeShort(0);     // No Recursion Desired
      }
      o.writeShort(1);     // Questions
      o.writeShort(0);     // Answers
      o.writeShort(0);     // Authority
      o.writeShort(0);     // Additional
      StringTokenizer k = new StringTokenizer(name,".");
      while(k.hasMoreTokens()){
        n=k.nextToken();
        o.writeByte(n.length());
        for(int j=0;j<n.length();j++){
        o.writeByte(n.charAt(j));
        }
      }
      o.writeByte(0);     // Root name
      o.writeShort(type);
      o.writeShort(1);     // class IN
      for (int j=o.size();j<512;j++){
        o.writeByte(0);
      }
      o.close();
    }catch(IOException x){
      System.exit(69);
    }
    return ob.toByteArray();
  }

  public RFC1035 parse(){
System.err.println("  Entering RFC1035.parse");//JDHE
    ArrayList recordList = new ArrayList();
    String name;
    int type;
    int rClass;
    try{
      id=inputStream.readShort();
      flags=inputStream.readShort();
      queryCount=inputStream.readShort();
      answerCount=inputStream.readShort();
      authorityCount=inputStream.readShort();
      additionalCount=inputStream.readShort();
      for (int j=1;j<=queryCount;j++){
        recordList.add(readQueryRecord(inputStream));
      }
      recordList.toArray(queryArray=new QueryRecord[recordList.size()]);
      recordList.clear();
System.err.println("    Finished queries");//JDHE
        
      reportBuffer.append(answerCount+" answer records--\n");
      for (int j=1;j<=answerCount;j++){
        recordList.add(readResourceRecord(inputStream));
      }
      recordList.toArray(ansArray=new ResourceRecord[recordList.size()]);
      recordList.clear();
System.err.println("    Finished answers");//JDHE
  
      reportBuffer.append(authorityCount+" authority records--\n");
System.err.println("      authorities "+authorityCount);//JDHE
      for (int j=1;j<=authorityCount;j++){
System.err.println("        have "+inputStream.available());//JDHE
        recordList.add(readResourceRecord(inputStream));
System.err.println("got RR");//JDHE
      }
      recordList.toArray(autArray=new ResourceRecord[recordList.size()]);
      recordList.clear();
System.err.println("Finished authorities");//JDHE
//JDHE Could it be that there is a short query
//JDHE Not this EOFException
  
      reportBuffer.append(additionalCount+" additional records--\n");
      for (int j=1;j<=additionalCount;j++){
        recordList.add(readResourceRecord(inputStream));
      }
      recordList.toArray(addArray=new ResourceRecord[recordList.size()]);
      recordList.clear();
System.err.println("  Exiting RFC1035.parse");//JDHE
    }catch(EOFException x){
       System.out.println("Short record");
    }catch(Exception x){
       System.out.println(x.getClass().getName()+" in parse says "+x.getMessage());
    } finally{
      return this;
    }
  }

  private final static String[] classNames = new String[]{"","IN"};
   
  public static String getRclassString(int cls){
    return classNames[cls];
  }

  public static String getTypeString(int type){
    String t = (String)typeNames.get(new Integer(type));
    if (t==null){
      System.exit(69);
    }
    return t;
  }

  public static ResourceRecord getTypeInstance(int type){
    String className="net.dougharris.utility.stakmods.dns."
     +getTypeString(type)+"ResourceRecord";
    ResourceRecord instance=null;
    try{
      instance = (ResourceRecord)Class.forName(className).newInstance();
    } catch(ClassNotFoundException x){
    System.err.println(x.getClass().getName()+":"+x.getMessage());
    } catch(InstantiationException x){
    System.err.println(x.getClass().getName()+":"+x.getMessage());
    } catch(IllegalAccessException x){
    System.err.println(x.getClass().getName()+":"+x.getMessage());
    }
    return instance;
  }

  public static int getTypeNumber(String typeName){
    int reply = -1;
    typeName=typeName.toUpperCase();
    Map.Entry entry;
    String entryName;
    Integer entryNumber;
    Iterator j = typeNames.entrySet().iterator();
    while (j.hasNext()){
      entry=(Map.Entry)j.next();
      entryName = (String)(entry.getValue());
      entryNumber= (Integer)(entry.getKey());
      if (entryName.equals(typeName)){
        reply = entryNumber.intValue();
      }
    }
    return reply;
  }

    QueryRecord readQueryRecord(PacketInputStream i)
      throws IOException{
      QueryRecord result=new QueryRecord();
      String name = decompressRFC1035Name(i);
      int type = i.readUnsignedShort();
      int rClass = i.readUnsignedShort();
      result.setName(name);
      result.setType(type);
      result.setRClass(rClass);
      return result;
    }

    ResourceRecord readResourceRecord(PacketInputStream i) {
      ResourceRecord result = null;
      try{
        String name = decompressRFC1035Name(i);
        int type = i.readUnsignedShort();
        int rClass = i.readUnsignedShort();
        int ttl = i.readInt();
        int length = i.readUnsignedShort();
        result = getTypeInstance(type);
        result.setName(name);
        result.setType(type);
        result.setRclass(rClass);
        result.setTTL(ttl);
        result.setLength(length);
      /*
        Now switch on the type number and fill the packets
      */
      //byte[] b = readData(length);
      //result.setData(b);
System.err.println("ready to deal with type");//JDHE
        switch(type){
        case 2: /* NS */
System.err.println("type2");//JDHE
	  ((NSResourceRecord)result).setNSName(decompressRFC1035Name(i));
	break;

        case 5: /* CNAME */
System.err.println("type5");//JDHE
	  ((CNAMEResourceRecord)result).setCNAME(decompressRFC1035Name(i));
	break;

        case 6: /* SOA */
System.err.println("type6");//JDHE
	  SOAResourceRecord rr = (SOAResourceRecord)result;
	  rr.setMname(decompressRFC1035Name(i));
	  rr.setRname(decompressRFC1035Name(i));
	  rr.setSerial(i.readUnsignedInt());
	  rr.setRefresh(i.readUnsignedInt());
	  rr.setRetry(i.readUnsignedInt());
	  rr.setExpire(i.readUnsignedInt());
	  rr.setMinimum(i.readUnsignedInt());
	break;

        case 12: /* PTR */
System.err.println("type12");//JDHE
	  ((PTRResourceRecord)result).setPTRName(decompressRFC1035Name(i));
	break;

        case 15: /* MX */
System.err.println("type15");//JDHE
	  ((MXResourceRecord)result).setPreference(i.readUnsignedShort());
	  ((MXResourceRecord)result).setExchanger(decompressRFC1035Name(i));
	break;

        case 1: /* A */
	default:
System.err.println("type default");//JDHE
          byte[] b = readData(i,length);
          result.setData(b);
	break;
        }
System.err.println("Finished dealing with type");//JDHE
      } catch(EOFException x){
        System.out.println("short read of ResourceRecord");
      } catch(Exception x){
        System.out.println(x.getClass().getName()+" in readResourceRecord says "+x.getMessage());
      } finally {
        System.out.println("return from readResourceRecord");
        return result;
      }
    }

    public byte[] readData(PacketInputStream i, int n) throws IOException{
       byte[] data=new byte[n];
       i.readFully(data);
       return data;
    }

  String decompressRFC1035Name(PacketInputStream i)
    throws IOException{
    String name=new String();
    try{
    int n=0;
    while(true){
      try{
        n=i.readUnsignedByte();
      } catch(Exception x){
      System.out.println(x.getClass().getName()+" says "+x.getMessage());
        break;
      }
      if (n==0){
        break;
      }
      if (n<64){
        name+=plainSegment(i,n);
        continue;
      } else if (n>191){
        n=((n&63)<<8)+i.readUnsignedByte();
        name+=compressedSegment(i,n);
        break;
      } else {
        throw new IOException("bad encoding of name with n= "+n);
      }
    }
    }catch(Exception x){
      System.out.println(x.getClass().getName()+" in decompress says "+x.getMessage());
    } finally {
      return name;
    }
  }

  String plainSegment(PacketInputStream i,int n)
    throws IOException{
    byte[] b = new byte[n];
    i.readFully(b);
    return new String(b,"ASCII7")+".";
  }

  String compressedSegment(PacketInputStream i,int n)
    throws IOException{
    String s="";
    try{
    long where=i.getPosition();
    i.setPosition(n);
    s=decompressRFC1035Name(i);
    i.setPosition(where);
    }catch(Exception x){
      System.out.println(x.getClass().getName()+" at "+n);
      System.exit(69);
    } finally {
    return s;
    }
  }

  public String showFlags(short flags){
    StringBuffer b = new StringBuffer();
    qr = (0==(flags&(short)0x8000)>>15);
    opcode = (flags&(short)0x7800)>>11;
    aa = (0!=((flags&(short)0x0400)>>10));
    tc = (0!=((flags&(short)0x0200)>>9));
    rd = (0!=((flags&(short)0x0100)>>8));
    ra = (0!=((flags&(short)0x0080)>>7));
    rcode  = (flags&(short)0x000f);
    return b.toString();
  }
  public String toString(){
    StringBuffer b = new StringBuffer();
    int n;
    b.append("id"+id);
    b.append(": ");
    if (opcode==0){
      b.append("QUERY");
    }
    b.append(" "+showFlags(flags));
    b.append(qr?"Q":"R");
    b.append(":");
    b.append(aa?"AA":"-A");
    b.append(":");
    b.append(tc?"TC":"-T");
    b.append(":");
    b.append(rd?"RD":"-D");
    b.append(ra?"RA":"-A");
    b.append(": ");
    switch(rcode){
    case(0):
    b.append("OK"); 
    break;
    case(1):
    b.append("FormatErr");
    break;
    case(2):
    b.append("SrvFail");
    break;
    case(3):
    if(aa){
      b.append("NxName");
    }
    break;
    case(4):
    b.append("NotImpl");
    break;
    case(5):
    b.append("WillNot");
    break;
    }
    b.append(":");
    b.append(" queries "+queryCount);
    b.append(" ");
    b.append(" ans "+answerCount);
    b.append(" ");
    b.append(" auth "+authorityCount);
    b.append(" ");
    b.append(" addl "+additionalCount);
    b.append("\n");

    n=0;
    for (int j=0;j<queryCount;j++){
      b.append("     Query "+(++n));
      b.append("\n");
      b.append("Name:"+queryArray[j].toString());
      b.append("\n");
    }
    if (queryCount>0){
      b.append("\n");
    }
    n=0;
    for (int j=0;j<answerCount;j++){
      b.append("     Answer "+(++n));
      b.append("\n");
      b.append(ansArray[j].toString());
    }
    if (answerCount>0){
      b.append("\n");
    }
    n=0;
    for (int j=0;j<authorityCount;j++){
      b.append("     Authority "+(++n));
      b.append("\n");
      b.append(autArray[j].toString());
    }
    if (authorityCount>0){
      b.append("\n");
    }
    n=0;
    for (int j=0;j<additionalCount;j++){
      b.append("     Additional "+(++n));
      b.append("\n");
      b.append(addArray[j].toString());
    }
    if (additionalCount >0){
      b.append("\n");
    }

    return b.toString();
  }
}
