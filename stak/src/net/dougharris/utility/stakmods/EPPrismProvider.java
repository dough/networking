package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;

public class EPPrismProvider extends GenericProvider{
  private byte[] prismHeader = new byte[144];

  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    i.readFully(prismHeader);headerLength+=prismHeader.length;
    setMessageLength(getLength()-headerLength);
    setMessageTag("ether11");
    return this;
  } 

  public String providerReport(String type) throws Exception{
    StringBuffer b = new StringBuffer();
    if (type.equals("drop")){
    }
    else
    if (type.equals("short")){
      b.append("Prism Header: ");
      b.append("144 bytes");
    }
    else
    if (type.equals("regular")){
      if((prismHeader!=null)&&(prismHeader.length!=0)){
        b.append(DumpHex.dumpBytes(prismHeader));
      }
    }
    b.append("\n");
    return b.toString();
  }
}
