package net.dougharris.utility.jockmods;

import java.io.InputStream;

public class Chargen extends InputStream{
  static String orderedChars =" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
  static byte[] orderedBytes = orderedChars.getBytes();
  static String orderedChars2 =orderedChars+orderedChars;
  static byte[] orderedBytes2 = orderedChars2.getBytes();

  public static byte[] getBytes(int offset){
    byte[] resultBytes=new byte[72];
    System.arraycopy(orderedBytes2, offset, resultBytes, 0, 72);
    return resultBytes;
  }

  public static byte[] getBytes(int offset, int length){
    offset%=orderedBytes2.length;
    byte[] resultBytes = new byte[length];
    int supply    = orderedBytes2.length;

    int available=supply-offset;
    int resultOffset=0;
    while(length>available){
      System.arraycopy
        (orderedBytes2, offset, resultBytes, resultOffset, available);
      offset=0;
      length-=available;
      resultOffset+=available;
      available=supply;
    }
    System.arraycopy(orderedBytes2, offset, resultBytes, resultOffset, length);
    return resultBytes;
  }

  public static byte[] getLine(int offset){
    offset%=orderedChars.length();
    byte[] resultBytes=new byte[74];
    byte[] crlf=new byte[]{13,10};
    System.arraycopy(orderedBytes2, offset, resultBytes, 0, 72);
    System.arraycopy(crlf, 0, resultBytes, 72, 2);
    return resultBytes;
  }

  public static byte[] getLine(int offset, int width){
    byte[] lineBytes=getBytes(offset, width);
    byte[] resultBytes=new byte[width+2];
    byte[] crlf=new byte[]{13,10};
    System.arraycopy(lineBytes, 0, resultBytes, 0, width);
    System.arraycopy(crlf, 0, resultBytes, width, 2);
    return resultBytes;
  }

  private int currentOffset =0;
  public int read(){
    int result;
    if (currentOffset>=orderedBytes.length){
      currentOffset=0;
    }
    return (int)orderedBytes[currentOffset++];
  }

  static class TesterBytes{
    public static void main(String[] args){
      int offset=Integer.parseInt(args[0]);
      int length=Integer.parseInt(args[1]);
      System.out.println(new String(getBytes(offset, length)));
    }
  }

  static class TesterLines{
    public static void main(String[] args){
      int offset=Integer.parseInt(args[0]);
      int length=Integer.parseInt(args[1]);
      StringBuffer b=new StringBuffer();
      for (int j=0;j<length;j++){
        b.append(new String(getLine(offset+j)));
      }
      System.out.println(b.toString());
    }
  }

  static class TesterWideLines{
    public static void main(String[] args){
      int offset=Integer.parseInt(args[0]);
      int length=Integer.parseInt(args[1]);
      int width=Integer.parseInt(args[2]);
      StringBuffer b=new StringBuffer();
      for (int j=0;j<length;j++){
        b.append(new String(getLine(offset+j,width)));
      }
      System.out.println(b.toString());
    }
  }
}
