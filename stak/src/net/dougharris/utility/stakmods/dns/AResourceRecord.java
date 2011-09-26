package net.dougharris.utility.stakmods.dns;

import net.dougharris.utility.DumpHex;

public class AResourceRecord extends ResourceRecord{
  private byte[] b;

  public String dataToString(){
    return DumpHex.dottedDecimalPrint(getData())+"\n";
  }

  public void setData(byte[] b){
    this.b=b;
  }

  public byte[] getData(){
    return b;
  }
}
