package net.dougharris.utility.stakmods.dns;

public class QueryRecord{
  String name;
  int rType;
  int rClass;

  public String toString(){
    StringBuffer b=new StringBuffer();
    b.append(name);
    b.append(" type "+rType);
    b.append(" class "+rClass);
    return b.toString();
  }

  public void setName(String name){
    this.name=name;
  }

  public String getName(){
    return name;
  }

  public void setType(int rType){
    this.rType=rType;
  }

  public int getType(){
    return rType;
  }

  public void setRClass(int rClass){
    this.rClass=rClass;
  }

  public int getRClass(){
    return rClass;
  }
}
