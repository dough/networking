package net.dougharris.utility.jockmods;

import java.net.DatagramPacket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DatagramProcessorPump implements Runnable{
  protected DatagramPacket iPacket;
  protected DatagramPacket oPacket;
  protected Processor p;
  private int status=0;

  public int getStatus(){
    return this.status;
  }

  public DatagramProcessorPump
    (DatagramPacket iPacket, Processor p, DatagramPacket oPacket){
    this.iPacket=iPacket;
    this.oPacket=oPacket;
    this.p=p;
  }

  public void start(){
    (new Thread(this)).start();
  }

  public void run() {
    int status = 0;
    try{
      InputStream i =
        new ByteArrayInputStream(iPacket.getData());
      ByteArrayOutputStream o =
        new ByteArrayOutputStream();
      status = p.process(i,o);
      oPacket.setData(o.toByteArray());
    } catch(ProcessorException x){
       this.status=-1;
       throw new RuntimeException(x);
    }
  }
}
