package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import net.dougharris.utility.DumpHex;
import java.io.IOException;
import java.io.EOFException;

public class ARPProvider extends GenericProvider{

//  private byte[] data;
  private int linkType;
  private int netType;
  private int linkLen;
  private int netLen;
  private int opCode;
  private byte[]sndLinkAddress;
  private byte[]sndNetAddress;
  private byte[]tgtLinkAddress;
  private byte[]tgtNetAddress;

  public Provider parse(PacketInputStream i, int length, String parserTag) throws Exception{
    setLength(length);
    setTag(parserTag);
    headerLength=0;
    linkType = i.readUnsignedShort();headerLength+=2;
    netType  = i.readUnsignedShort();headerLength+=2;
    linkLen  = i.readUnsignedByte();headerLength+=1;
    netLen   = i.readUnsignedByte();headerLength+=1;
    opCode   = i.readUnsignedShort();headerLength+=2;
    sndLinkAddress = new byte[linkLen];headerLength+=linkLen;
    sndNetAddress = new byte[netLen];headerLength+=netLen;
    tgtLinkAddress = new byte[linkLen];headerLength+=linkLen;
    tgtNetAddress = new byte[netLen];headerLength+=netLen;
    i.readFully(sndLinkAddress);headerLength+=sndLinkAddress.length;
    i.readFully(sndNetAddress);headerLength+=sndNetAddress.length;
    i.readFully(tgtLinkAddress);headerLength+=tgtLinkAddress.length;
    i.readFully(tgtNetAddress);headerLength+=tgtNetAddress.length;
    setMessageLength(-1);
    setMessageTag(null);
    return this;
  }

  public String providerReport(String type) throws Exception{
    StringBuffer b = new StringBuffer();
    b.append("opType:");
    b.append(opCode);
    b.append("\n");
    b.append("src-");
    b.append("(link:");
    b.append(DumpHex.separatedHexPrint(sndLinkAddress));
    b.append(" ");
    b.append("net");
    b.append(":");
    b.append(DumpHex.dottedDecimalPrint(sndNetAddress));
    b.append(")");
    b.append("\n");
    b.append("tgt-");
    b.append("(link:");
    b.append(DumpHex.separatedHexPrint(tgtLinkAddress));
    b.append(" ");
    b.append("net");
    b.append(":");
    b.append(DumpHex.dottedDecimalPrint(tgtNetAddress));
    b.append(")");
    return b.toString();
  }
}
