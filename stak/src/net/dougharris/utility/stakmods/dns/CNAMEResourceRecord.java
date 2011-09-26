package net.dougharris.utility.stakmods.dns;

public class CNAMEResourceRecord extends ResourceRecord{
  private String name;

  public void setCNAME(String name){
    this.name = name;
  }
  
  public String getCNAME(){
    return name;
  }

  public String dataToString(){
    return getCNAME()+"\n";
  }
}
