package net.dougharris.utility.stakmods.dns;

public class PTRResourceRecord extends ResourceRecord{
  private String name;

  public void setPTRName(String name){
    this.name = name;
  }
  
  public String getPTRName(){
    return name;
  }

  public String dataToString(){
    return getPTRName()+"\n";
  }
}
