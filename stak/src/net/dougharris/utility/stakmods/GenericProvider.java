package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.EOFException;

public abstract class GenericProvider implements Provider{
  protected int length;      // learned from the delivering agent
  protected int parsedLength;      // learned from the delivering agent
  protected String tag=null; //learned from the delivering agent
  protected String parsedTag=null; //learned from the delivering agent
  protected int messageLength;       // learned from the parse
  //protected String messageTag=null; //learned from the parse
  protected String messageTag=null; //learned from the parse
  protected String truncated=null;
  protected String aborted=null;
  protected StringBuffer b;
  protected int headerLength;

  public String parseKey(){
    String key=null;
    String providedTag=getTag();
    int sp;
    if (null!=providedTag){
      sp=providedTag.indexOf(":");
      if (sp == -1){
        sp=providedTag.length();
      }
      key= providedTag.substring(0,sp);
    }
    return key;
  }

  static public ArrayList parseTags(String pTag){
    ArrayList l =  new ArrayList();
    StringTokenizer t= new StringTokenizer(pTag,":");
    while (t.hasMoreTokens()){
      l.add(t.nextToken());//JDH
    }
    l.trimToSize();
    return l;
  }

  public void setLength(int l){
    this.length=l;
  }

  public int getLength(){
    return length;
  }

  public void setTag(String providedTag){
    this.tag=providedTag;
  }

  public String getTag(){
    return tag;
  }

  public void setMessageLength(int l){
    this.messageLength=l;
  }

  public int getMessageLength(){
    return messageLength;
  }

  public void setMessageTag(String p){
    this.messageTag=p;
  }

  public String getMessageTag(){
    return messageTag;
  }

  final public String toString(String type){
   StringBuffer b = new StringBuffer();
   StringBuffer report= new StringBuffer();
   try{
     b.append("PROVIDER:");
     b.append((String)(GenericProvider.parseTags(getTag())).get(0));
     b.append(" ");
     b.append("USER:");
String jdh="none";
if (null!=getMessageTag()){
   jdh=(String)(GenericProvider.parseTags(getMessageTag())).get(0);
     }
	//JDHLOGSystem.err.println("key is "+jdh);
     b.append(jdh);
     if (null!=aborted){
       b.append("\n"+aborted);
     } else if (null!=truncated){
       b.append("\n"+truncated);
     }
//JDH should compare this int headerLength=getLength()-getMessageLength();
     b.append(" <"+headerLength+"+"+getMessageLength()+">\n");
     b.append(providerReport(type));
   } catch(Exception x){
     b.append("Ended by exception "+x.getClass().getName()+" "+x.getMessage());
   } finally{
     b.append("\n");
     return b.toString();
   }
  }

  public String providerReport(String type) throws Exception{
    StringBuffer b = new StringBuffer();
    b.append("   --- <<< GenericProvider >>> ---");
    return b.toString();
  }
}
