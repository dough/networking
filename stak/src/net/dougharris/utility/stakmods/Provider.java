package net.dougharris.utility.stakmods;

import net.dougharris.utility.PacketInputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.EOFException;

public interface Provider extends Serializable{
  public Provider
    parse(PacketInputStream i, int length, String parserTag) throws Exception;
  public String toString();
  public String toString(String type);
  public String providerReport(String type) throws Exception;
/*
 * The length would have been determined at the sender by the provider as it created the envelope
 * The length is determined at the receiver by the agent which delivers it
 * It should then be compared against any length that the provider can parse off of the envelope.
 */
  public void setLength(int l);
  public int getLength();

  public void setTag(String pTag);
  public String getTag();

  public void setMessageLength(int l);
  public int getMessageLength();

  public void setMessageTag(String pTag);
  public String getMessageTag();
}
