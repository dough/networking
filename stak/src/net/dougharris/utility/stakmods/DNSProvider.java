package net.dougharris.utility.stakmods;

import net.dougharris.utility.stakmods.dns.RFC1035;
import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;

public class DNSProvider extends GenericProvider{
  private RFC1035 rfc1035;

  public Provider
  parse(PacketInputStream i, int length, String parserTag) throws Exception{
    setLength(length);
    setTag(parserTag);
    byte[] b=new byte[i.available()];
    i.readFully(b);
System.err.println("Before the rfc1035 parse of size "+b.length);
    rfc1035 = new RFC1035(b, 0, b.length);
    rfc1035.parse();
System.err.println("After the rfc1035 parse");//JDHE never gets here on 4th
    setMessageLength(0);
    setMessageTag(null);
    return this;
  }

  public String toString(StringBuffer b){
    b.append(rfc1035.toString());
    return b.toString();
  }
}
