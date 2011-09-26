package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class HTTPProvider extends GenericProvider{
  private byte[] data;
  private String qrLine;
  transient private HashMap qHeadersMap = new HashMap();

  public Provider parse(PacketInputStream i, int length, String parserTag){
    String qHeaderLine;
    int separator;
    String name;
    String previousValue;
    String value;
    int CRLF=2;

//JDH Appears to be wrong length, always shows 0
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    if (length>0){
    data=new byte[i.available()]; 
    try{
      qrLine = i.readLine(); headerLength+=qrLine.length()+CRLF;
System.err.println("query of length "+qrLine.length()+ " gives headerLength "+headerLength);
// get lines until there is a blank line (line of zero length)
// they should all be headers
      while (0<(qHeaderLine=i.readLine()).length()){
        headerLength+=qHeaderLine.length()+CRLF;
System.err.println();
// if this is -1 it is not a header, and we have an error
// for now we should perhaps ignore it, or put "ERROR"
        separator = qHeaderLine.indexOf(":");
        name  = qHeaderLine.substring(0, separator).toUpperCase();;
System.err.println("header "+name+" of length "+qHeaderLine.length()+" gives headerLength "+headerLength);
        value = qHeaderLine.substring(separator+2);
        previousValue = (String)qHeadersMap.get(name);
        if (null != previousValue){
          value=previousValue += ", " + value;
        }
        qHeadersMap.put(name, value);
      }
      headerLength+=CRLF;
     //JDH  i.readFully(data);headerLength+=data.length;
    } catch(EOFException x){
      System.err.println(x);
    } catch(IOException x){
      System.err.println(x);
    }
}
      parsedTag = "raw";
      setMessageLength(getLength()-headerLength);
      setMessageTag(parsedTag);
      return this;
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
      if((data!=null)&&(data.length!=0)){
        b.append("  ");
        b.append("qrLine: ");
        b.append(qrLine);
        b.append("\n");
        Map.Entry entry;
        String key;
        for (Iterator i = qHeadersMap.entrySet().iterator(); i.hasNext();){
          entry = (Map.Entry)i.next();
          key = (String)entry.getKey();
          b.append("  ");
          b.append(key);
          b.append(": ");
          b.append(entry);
          b.append("\n");
        }
      }
    }
    return b.toString();
  }
}
