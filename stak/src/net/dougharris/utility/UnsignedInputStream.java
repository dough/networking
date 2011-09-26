package net.dougharris.utility;

import java.io.DataInput;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.EOFException;

 /**
 * Used for reading Streams that have unsigned integers
 * @author  Douglas Harris, doug@kisadvice.com
 * @version 0.2, 05/27/2003
 * @see     java.io.DataInputStream
 * @see     java.io.DataInput
 */
public class UnsignedInputStream {
  protected DataInputStream iData;
  protected long cap=0;
  protected byte[] byteArray;

  public UnsignedInputStream(InputStream iData){
    this(new DataInputStream(iData));
  }

  public UnsignedInputStream(DataInputStream iData){
    this.iData=iData;
  }

  public UnsignedInputStream(byte[] data){
    this(new ByteArrayInputStream(data));
  }

  public final int read() throws IOException { 
    int result = iData.read();
    if (0>result){
      throw new EOFException();
    }
    return result;
  }
  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final boolean readBoolean() throws IOException { 
    return iData.readBoolean();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final byte readByte() throws IOException { 
    return iData.readByte();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final int readUnsignedByte() throws IOException { 
    return iData.readUnsignedByte();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final char readChar() throws IOException { 
    return iData.readChar();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final float readFloat() throws IOException { 
    return iData.readFloat();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final double readDouble() throws IOException { 
    return iData.readDouble();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final short readShort() throws IOException { 
    return iData.readShort();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final int readUnsignedShort() throws IOException { 
    return iData.readUnsignedShort();
  }

  /** 
    Just delegates to the DataInputStream
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
    result = (l3<<24)+(l2<<16)+(l1<<8)+l0;
    return result;
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final long readLong() throws IOException { 
    return iData.readLong();
  }

  /** 
    Just delegates to the DataInputStream
    @param b
    @exception IOException
  */
  public final void readFully(byte[] b) throws IOException { 
    iData.readFully(b);
  }

  /** 
    Just delegates to the DataInputStream
    @param b
    @param off
    @param len
    @exception IOException
   */
  public final void readFully(byte[] b, int off, int len) throws IOException { 
    iData.readFully(b,off,len);
  }

  /** 
    Just delegates to the DataInputStream
    @exception Exception
  */
  public final String readLine() throws IOException { 
    return iData.readLine();
  }

  /** 
    Just delegates to the DataInputStream
    @exception IOException
  */
  public final String readUTF() throws IOException { 
    return iData.readUTF();
  }

  /** 
    Just delegates to the DataInputStream
    @param in
    @exception IOException
  */
  public final String readUTF(DataInput in) throws IOException { 
    return iData.readUTF(in);
  }

  /** 
    Just delegates to the DataInputStream
    @param n
    @exception IOException
  */
  public final int skipBytes(int n) throws IOException { 
    return iData.skipBytes(n);     
  }

  public int readUnsignedShort(boolean bigEndian)
    throws IOException{
    int result;
    short l0= (short)read();
    short l1= (short)read();
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
    long l0= (long)read();
    long l1= (long)read();
    long l2= (long)read();
    long l3= (long)read();
    if (bigEndian){
      result = (l0<<24)+(l1<<16)+(l2<<8)+l3;
    } else {
      result = (l3<<24)+(l2<<16)+(l1<<8)+l0;
    }
    if (0>l3) throw new EOFException();
    return result;
  }
}
