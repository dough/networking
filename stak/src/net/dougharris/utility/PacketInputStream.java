/**
  * There is a big problem.
  * If you get -1 from any of the integer reads
  * it does not turn into an EOFException!
  */
package net.dougharris.utility;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;

 /**
 * Used for reading Internet primitives from a byte array
 * @author  Douglas Harris, doug@kisadvice.com
 * @version 0.1, 07/25/01
 * @see     java.io.DataInputStream
 * @see     java.io.DataInput
 * @see     java.io.ByteArrayInputStream
 */
public class PacketInputStream extends ByteArrayInputStream
  implements DataInput{
  protected DataInputStream iData;
  protected long cap=0;
  protected byte[] byteArray;

  public PacketInputStream(byte[] b){
    this(b, 0, b.length);
  }

  public PacketInputStream(byte[] h, byte[] b){
    this(mergeArrays(h,b), 0, h.length+b.length);
  }

  static byte[] mergeArrays(byte[] h, byte[] b){
     byte[] returnValue = new byte[h.length+b.length];
     for (int j=0;j<h.length;j++){
       returnValue[j]=h[j];
     }
     for (int j=0;j<b.length;j++){
       returnValue[h.length+j]=b[j];
     }
     return returnValue;
  }

  /**
     Because of the way constructors work,
     it cannot extend DataInputStream,
     although that would be a little cleaner for the method calls.
     That constructor must wrap a stream, so my constructor
     would have to have the ByteArrayInputStream available when called!
     And as for exceptions BAIS read does not throw IOException,
     so my read cannot, even though an IS read is supposed to!
     On the other hand it is doing iData read and that does throw
     an IOException!
     So I will just leave it out!
     @param b    the byte array
     @param off  offset into <code>b</code>
     @param len  length from the offset
  */
  public PacketInputStream(byte[] b, int off, int len){
    super(b, off, len);
    this.iData=new DataInputStream(this);
    /* just in case somebody changes BAIS!*/
    this.cap=(long)super.available();
    this.byteArray=b;
  }

  public byte[] getByteArray(){
    return this.byteArray;
  }

  /**
    @param n where the position should be, an offset from the beginning
    @exception throws IOException when the extended stream does
  */
  public void setPosition(long n) throws IOException{
    if (n>cap){
      throw new IOException("setPosition to "+n+" beyond end of stream");
    }
    iData.reset();
    skip(n);
  }

  /**
    Nothing "should" go wrong
  */
  public long getPosition(){
    long result = cap-available();
    if (result <0){
      // impossible?
    }
    return result;
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final boolean readBoolean() throws IOException { 
    return iData.readBoolean();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final byte readByte() throws IOException { 
    return iData.readByte();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final int readUnsignedByte() throws IOException { 
    return iData.readUnsignedByte();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final char readChar() throws IOException { 
    return iData.readChar();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final float readFloat() throws IOException { 
    return iData.readFloat();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final double readDouble() throws IOException { 
    return iData.readDouble();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final short readShort() throws IOException { 
    return iData.readShort();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final int readUnsignedShort() throws IOException { 
    return iData.readUnsignedShort();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final int readInt() throws IOException { 
    return iData.readInt();
  }

  /** 
    Gets it as two UnsignedShorts glued together
    @exception IOException
  */
  public final long readUnsignedInt() throws IOException { 
    long result;
    long l3= (long)read();
    long l2= (long)read();
    long l1= (long)read();
    long l0= (long)read();
    if (0>l0) throw new EOFException();
    result = (l3<<24)+(l2<<16)+(l1<<8)+l0;
    return result;
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final long readLong() throws IOException { 
    return iData.readLong();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param b
    @exception IOException
  */
  public final void readFully(byte[] b) throws IOException { 
    iData.readFully(b);
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param b
    @param off
    @param len
    @exception IOException
  */
  public final void readFully(byte[] b, int off, int len) throws IOException { 
    iData.readFully(b,off,len);
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception Exception
  */
  public final String readLine() throws IOException { 
    return iData.readLine();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception IOException
  */
  public final String readUTF() throws IOException { 
    return iData.readUTF();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param in
    @exception IOException
  */
  public final String readUTF(DataInput in) throws IOException { 
    return iData.readUTF(in);
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param n
    @exception IOException
  */
  public final int skipBytes(int n) throws IOException { 
    return iData.skipBytes(n);     
  }
  /*JDH Added June 15 2003JDH*/
  public static PacketInputStream fromFile(String fileName){
    PacketInputStream returnValue=null;
    byte[] b= null;
    try{
      FileInputStream iFile = new FileInputStream(fileName);
      b= new byte[iFile.available()];
      iFile.read(b, 0, iFile.available());
      returnValue = new PacketInputStream(b, 0, b.length);
    } catch(FileNotFoundException x){
      System.err.println("No such file");
      System.exit(1);
    } catch(IOException x){
      System.err.println("Cannnot construct the PIS");
      System.exit(2);
    }
    return returnValue;
  }
  //=================================
  public int readUnsignedShort(boolean bigEndian)
    throws IOException{
    int result;
    short l0= (short)readByte();
    short l1= (short)readByte();
    if (bigEndian){
      result = (l0<<8)+l1;
    } else {
      result = (l1<<8)+l0;
    }
    if (0>l1) throw new EOFException();
    return result;
  }

  public long readUnsignedInt(boolean bigEndian)
    throws IOException{
    long result;
    long l0= (long)readInt();
    long l1= (long)readInt();
    long l2= (long)readInt();
    long l3= (long)readInt();
    if (bigEndian){
      result = (l0<<24)+(l1<<16)+(l2<<8)+l3;
    } else {
      result = (l3<<24)+(l2<<16)+(l1<<8)+l0;
    }
    if (0>l3) throw new EOFException();
    return result;
  }
  //=================================
}
