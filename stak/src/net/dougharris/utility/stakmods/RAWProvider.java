package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;
import java.util.StringTokenizer;

public class RAWProvider extends GenericProvider{
  private byte[] data;

  public Provider parse(PacketInputStream i, int length, String parserTag){
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    data=new byte[i.available()]; 
//    data=new byte[Math.min(totalLength,i.available())]; //JDHfix
    try{
      i.readFully(data);headerLength+=data.length;
      /*
       * The messageLength will just be the length of the data just read
       * For ICMP the length is just whatever it turns out to be.
       */
    } catch(EOFException x){
      System.err.println(x);
    } catch(IOException x){
      System.err.println(x);
    } finally{
      setMessageLength(-1);
      setMessageTag(null);
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
      if((data!=null)&&(data.length!=0)){
       b.append(DumpHex.dumpBytes(data));
      }
    }
    return b.toString();
  }
}
