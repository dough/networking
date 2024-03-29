package net.dougharris.utility.stakmods.dns;

public class MXResourceRecord extends ResourceRecord{
  private int preference;
  private String exchanger;

  public void setPreference(int preference){
    this.preference=preference;
  }

  public int getPreference(){
    return preference;
  }

  public void setExchanger(String name){
    this.exchanger=name;
  }

  public String getExchanger(){
    return exchanger;
  }

  public String dataToString(){
    StringBuffer b = new StringBuffer();
    b.append("Preference: ");
    b.append(""+getPreference());
    b.append(" Exchanger: ");
    b.append(getExchanger());
    b.append("\n");
    return b.toString();
  }
}
