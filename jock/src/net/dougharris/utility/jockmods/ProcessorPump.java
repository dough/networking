package net.dougharris.utility.jockmods;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessorPump implements Runnable{
  protected InputStream i;
  protected OutputStream o;
  protected Processor p;
  private int status=0;

  public int getStatus(){
    return this.status;
  }

  public ProcessorPump(InputStream i, Processor p, OutputStream o){
    this.i=i;
    this.o=o;
    this.p=p;
  }

  public void start(){
    (new Thread(this)).start();
  }

  public void run() {
    int status = 0;
    try{
System.err.println("ProcessorPump starting");
      status = p.process(i,o);
System.err.println("ProcessorPump ended with status "+status);
    } catch(ProcessorException x){
       this.status=-1;
       throw new RuntimeException(x);
    }
  }
}
