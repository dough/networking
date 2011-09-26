package net.dougharris.utility.jockmods;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class EchoProcessor implements Processor{
  public int process(InputStream i, OutputStream o) {
    int status = 0;
    int nRead;
    int nWrite;
// should really make this buffer bigger, or get its size
// from parameters of jock
    byte[] bRead=new byte[1024];
    try{
      while(-1!=(nRead=i.read(bRead))){
//log received nRead
// for now just put it on stderr
System.err.println("received "+nRead+" bytes to be echoed.");
//Cannot ask how many bytes written - will send them all
        o.write(bRead, 0, nRead);
        o.flush();
      }
// Was getting an exception for one of these closes
// that it was already closed. Sort this out JDH.
//       i.close();
//       o.close();
    } catch(IOException x){
       status=-1;
    }
    return status;
  }
}
