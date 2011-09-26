package net.dougharris.utility;
 
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
 
public class DumpHex{
 
  public static char charPrint(int b){
    return (b<32||b>126)?(char)'.':(char)b;
  }

  public static String decPrint(long b){
    StringBuffer m=new StringBuffer("00000").append(Long.toString(b));
    return(m.substring(m.length()-5));
  }
 
  public static String hexPrint(int b){
    StringBuffer m=new StringBuffer("00").append(Integer.toHexString(b));
    return(m.substring(m.length()-2));
  }

  public static String hexPrintShort(int b){
    StringBuffer m=new StringBuffer("0000").append(Integer.toHexString(b));
    return(m.substring(m.length()-4));
  }

  public static String separatedHexPrint(byte[] a){
    StringBuffer b = new StringBuffer();
    for (int j=0;j<6;j++){
      if (j>0){
        b.append("-");
      }
      b.append(hexPrint(a[j]));
    }
    return b.toString();
  }

  public static String shortPrint(short a){
  //JDH do I need to take care of the possible sign
    StringBuffer b = new StringBuffer("0000");
    b.append(Integer.toHexString(a));
    return b.substring(b.length()-4);
  }

  public static String intPrint(int a){
    StringBuffer b = new StringBuffer("00000000");
    b.append(Integer.toHexString(a));
    return b.substring(b.length()-8);
  }

  public static String longPrint(long a){
    StringBuffer b = new StringBuffer("0000000000000000");
    b.append(Long.toHexString(a));
    return b.substring(b.length()-16);
  }

  public static String bytesPrint(byte[] a){
    StringBuffer b = new StringBuffer();
    for (int j=0;j<a.length;j++){
      b.append(hexPrint(a[j]));
    }
    return b.toString();
  }

  public static String dottedDecimalPrint(byte[] a){
    StringBuffer b = new StringBuffer();
    for (int j=0;j<4;j++){
      if (j>0){
        b.append(".");
      }
      b.append(((a[j]<0)?256:0)+a[j]);
    }
    return b.toString();
  }

  public static String dumpBytes(byte[] a) throws IOException{
    return dumpBytes(a, true);
  }

  public static String dumpBytes(byte[] a, boolean ascii) throws IOException{
    ByteArrayInputStream i = new ByteArrayInputStream(a);
    return dumpStream(i,i.available(), ascii);
  }

  public static String dumpBytes(byte[] a, int offset, int length)
    throws IOException{
    ByteArrayInputStream i = new ByteArrayInputStream(a, offset, length);
    return dumpStream(i,i.available());
  }

  public static String dumpStream(InputStream i, int length) throws IOException{
    return dumpStream(i, length, true);
  }

  public static String dumpStream(InputStream i, int length, boolean ascii)
    throws IOException{
    StringBuffer m;
    StringBuffer p = new StringBuffer();
    int c;
    int k;

    for (int j=0;j<length;j+=16){
      StringBuffer l = new StringBuffer();
      StringBuffer s = new StringBuffer("00000000").append(Integer.toHexString(j));
      if (j>0){
        p.append("\n");
      }
      p.append(s.substring(s.length()-8)+"  ");
      for (k=0;k<16;k++){
        if ((j+k)>=length){
          break;
        }
        p.append(" ").append(hexPrint(c=i.read()));
	if (ascii){
          l.append(charPrint(c));
	}
      }
      for (;k<16;k++){
        p.append("   ");
      }
      p.append("  ").append(l);
    }
    return p.toString();
  }

  public static String ttlPrint(int time){
    return ttlPrint((long)time);
  }

  public static String ttlPrint(long time){
  /*
  y=31449600
  w=604800
  d=86400
  h=3600
  m=60
  */
    long seconds=time%60;
    long minutes=time/60;
    long hours=minutes/60;
    minutes=minutes-60*hours;
    long days=hours/24;
    hours=hours-24*days;
    long weeks=days/7;
    days=days-weeks*7;
    long years=weeks/52;
    weeks=weeks-years*52;
    return(years+"y-"+weeks+"w-"+days+"d-"+hours+"h-"+minutes+"m-"+seconds+"s");
  }

  public static boolean bigEndian(){
    return(ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
  }
 
  public static void main(String[] args) throws java.io.IOException{
    boolean ascii=false;
    if (args.length > 1){
      ascii=true;
    }
    InputStream i = new FileInputStream(args[0]);
    int length = i.available();
    System.out.println(dumpStream(i,length, ascii));
  }
}
