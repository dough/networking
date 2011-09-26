package net.dougharris.utility.jockmods;

import java.io.InputStream;
import java.io.OutputStream;

public interface Processor{
  public int process(InputStream i, OutputStream o) throws ProcessorException;
}
