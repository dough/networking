package net.dougharris.utility;
/** @ (#)P 1.01 02/01/2003
  * Copyright 2002 Keep It Simple Advice, all rights reserved
  */

import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.io.IOException;

/** 
  * <pre><code>
  * A class used for handling command line arguments, errors, printing,
  * and other tasks common in any program.  An instance of it extends
  * HashMap, and is created in a program very early on, providing
  * information about both command line arguments provided, and command
  * line arguments expected, classified as flags, properties, and parameters.
  * These are defined in the "original Unix" style.
  * 
  * You tell it to parse the arguments with the function P.arseArgs(...),
  * which takes an "option" string and an "arguments" string.
  * 
  * A flag is an argument like "-a", with a single letter after a hyphen,
  * and is intended to act like a boolean saying whether to follow a
  * particular behavior, or not.
  *
  * A property is an argument like "-p SOMEVALUE", which starts with a 
  * a flag-like argument and treats the following argument as a value,
  * so in that sense can be thought of as "setting a property" in your
  * program.
  *
  * A parameter is just a single string argument that is not a flag.
  * It typically represents the name of some file that your program
  * will operate upon.
  *
  * The option string specified with P.arseArgs(...) describes
  * the flags and properties that might be expected, stating for each
  * whether it is optional or required.  It is expected that flags and
  * properties are specified first on the command line, and parameters
  * are anything left over.
  *
  * Thus the args "-z +a -v SomeValue -u Another Value this that the other
  * has two flags, two properties, and four parameters.
  *
  * The option string "-z+a.v_u" states that -z is an optional flag,
  * -a is a required flag (which some would think is unneeded, but it
  * can be useful), -v is a property (so the next argument is its value),
  * which must be present, and -u is a property which is optional.
  *
  * The argument "--" on the command line turns off this parsing, so
  * everything that follows is a parameter.
  *
  * P.arseArgs() can also be called with an additional "usage" string
  * which describes the message that should be printed if there is a
  * problem in parsing arguments.
  *
  * There is also an assortment of P.rint(...) and P.rintln(...)
  * functions that this author has found useful, and perhaps more
  * helpful there is an assortment of (...), P.error(...),
  * and P.exit(...) routines that create specific printouts and
  * actions when desired.
  * 
  * It is intended in the next version of this program (due "real soon
  * now") to provide hooks into the JDK1.4 logging routines.</pre>
  * version 1.1 does this
  * </code></pre>
  * 
  * @author  Douglas Harris
  * @version 1.1
  */

public class P extends java.util.HashMap{
  private static long startTime=System.currentTimeMillis();
  private static Random random = new Random();
  public Logger logger;
  private Handler handler;
  private Level defaultLogLevel = Level.ALL;
  private String state;
  
  public P(){
    logger = Logger.getLogger("");
    logger.setLevel(defaultLogLevel);
    handler = logger.getHandlers()[0];
    handler.setLevel(defaultLogLevel);
    handler.setFormatter(new PFormatter());
  }

/**
  *  just delegate to this logger most of the time, refine it later
  */
  public void setDefaultLogLevel(Level level){
    this.defaultLogLevel=level;
    logger.setLevel(defaultLogLevel);
  }
  public void setLogLevel(Level level){
    logger.setLevel(level);
    handler.setLevel(level);
  }

  public void setLogFile(String fileName){
    Handler fileHandler;
    try{
      fileHandler=new FileHandler(fileName);
      fileHandler.setFormatter (handler.getFormatter());
      fileHandler.setLevel(handler.getLevel());
      logger.addHandler(fileHandler);   
      handler.setLevel(Level.OFF);
    }catch(IOException x){
    }
  }

/**
  *  just delegate to this logger most of the time, refine it later
  */
  public void log(String msg){
    logger.log(defaultLogLevel, msg);
  }

  public void log(Level level, String msg){
    logger.log(level, msg);
  }

  public void severe(String msg){
    logger.severe(msg);
  }

  public void warning(String msg){
    logger.warning(msg);
  }

  public void config(String msg){
    logger.config(msg);
  }

  public void info(String msg){
    logger.info(msg);
  }

  public void fine(String msg){
    logger.fine(msg);
  }

  public void finer(String msg){
    logger.finer(msg);
  }

  public void finest(String msg){
    logger.finest(msg);
  }

  public String getState(){
    return state;
  }

  public void setState(String state){
    this.state=state;
  }

/**
  *
  */
  static public void rint(boolean b, boolean yes){
   if (yes)rint(b);
  }
  static public void rint(boolean b){
    System.out.print(b);
  }

/**
  *
  */
  static public void rintln(boolean b, boolean yes){
   if (yes)rintln(b);
  }
  static public void rintln(boolean b){
    rint(b);
    rintln();
  }

/**
  *
  */
  static public void rint(char c, boolean yes){
   if (yes)rint(c);
  }
  static public void rint(char c){
    System.out.print(c);
  }

/**
  * JDH problem here, I have this defined for printing a boolean!
  */
  /*
  static public void rintln(boolean yes){
   if (yes)rintln();
  }
  */
  static public void rintln(){
    rint('\n');
  }

/**
  *
  */
  static public void rint(long n, boolean yes){
    if (yes)rint(n);
  }
  static public void rint(long n){
    System.out.print(""+n);
  }
  
/**
  *
  */
  static public void rintln(long n, boolean yes){
    if (yes)rintln(n);
  }
  static public void rintln(long n){
    rint(n);
    rintln();
  }

/**
  *
  */
  static public void rint(Object o, boolean yes){
    if (yes) rint(o);
  }
  static public void rint(Object o){
    System.out.print(o.toString());
  }

/**
  *
  */
  static public void rintln(Object o, boolean yes){
    if (yes) rintln(o);
  }
  static public void rintln(Object o){
    rint(o);
    rintln();
  }

/**
  *
  */
  static public void rintln(Object[] o, boolean yes){
    if (yes) rintln(o);
  }
  static public void rintln(Object[] a){
    for (int j=0;j<a.length;j++){
      rintln(a[j]);
    }
  }


/**
  *
  */
  static public void error(Object o){
    System.err.println(o.toString());
  }

/**
  * Prints name of exception and its message
  */
  static public void exception(Exception x){
    error(x.getClass().getName()+ " says " + x.getMessage());
  }

/**
  * Prints user message, then exception and message
  */
  static public void exception(String s, Exception x){
    rint(s+": ");
    exception(x);
  }

/**
  * Prints name of exception and message, then exits with a code
  */
  static public void exception(Exception x, int code){
    exception(x);
    System.exit(code);
  }

/**
  * Prints user message, then exception and message, then exits with a code
  */
  static public void exception(String s,Exception x, int code){
    exception(s, x);
    P.exit(code);
  }

/**
  * Exits with code 0
  */
  static public void exit(){
    exit(0);
  }

/**
  * Exits with specified code
  */
  static public void exit(int code){
    System.exit(code);
  }

/**
  * Exits
  */
  static public void exit(Exception x){
    exit(x, 1);
  }

/**
  * Prints exception name and message, the exits with a code
  */
  static public void exit(Exception x, int code){
    if (null!=x){
      exception(x);
    }
    exit(code);
  }

/**
 *  Timing code
    long t = -P.now();
    t     += P.now();
 */
  static public long now(){
    return System.currentTimeMillis();
  }
/**
  * Pauses the thread in which it is called for a time specified in ms
  */
  static public void ause(long delay){
    if (delay>0){
      long doneTime=System.currentTimeMillis()+delay;
      long sleepTime;
      while(0<(sleepTime=doneTime-System.currentTimeMillis())){
      try{
        Thread.currentThread().sleep(sleepTime);
      } catch(java.lang.InterruptedException x){
      }
      }
    }
  }

  static public void ause(long minDelay, long varDelay ){
    if (0==varDelay){
      ause(minDelay);
    } else {
      int randomDelay=random.nextInt((int)varDelay);
      try{
        Thread.currentThread().sleep(minDelay+randomDelay);
      } catch(java.lang.InterruptedException x){
      }
    }
  }

  HashMap markTable = new HashMap();

  static public long mark(){
    return System.currentTimeMillis();
  }

  static public long markTime(long start){
    return System.currentTimeMillis()-start;
  }

  public long mark(String name){
    long result = System.currentTimeMillis();
    markTable.put(name, new Long(result));
    return result;
  }

/**
  I have to think over the Exception here - maybe something better
  negative will not work, since you could get negative anyway!
  I take that back, you cannot get negative, since you cannot run
  markTime on a name until you have run mark for it.
  So a negative value is good.
  */
  public long markTime(String name){
    Long storedValue=(Long)markTable.get(name);
    if (null==storedValue) return -1;
    return System.currentTimeMillis()-storedValue.longValue();
  }

  public void markOut(String name){
    markTable.remove(name);
  }

/**
  * Prints the given usage message.
  */
  static public void usage(String usage){
    rint("Usage: ");
    rintln(usage);
    exit(1);
  }

/**
  * Prints the given  error message, then the given usage message
  */
  static public void usage(String error, String usage){
    rintln(error);
    rintln("Usage: "+usage);
    exit(1);
  }


/**
  * This is the major routine, which should be called early in
  * your program.  It creates an instance of this class, to
  * hold the information from parsing, and then does the parse.
  * The options argument shows what flags and properties to expect.
  * The args array shows the arguments to parse, which would
  * normally be all arguments given on the command line.
  * 
  * When called in this way, the program will continue even if
  * errors are found in the options, and will let the user test
  * for a particular option, and obtain all parameters.
  * 
  * If it is desired to fail when errors are found, use the form
  * of this call that provides a usage String.
  */
  static public P arseArgs(String[] args){
    return arseArgs("",args);
  }
  static public P arseArgs(String options, String[] args){
    return (new P()).parse(options, args);
  }

/**
  * This is the major routine, which should be called early in
  * your program.  It creates an instance of this class, to
  * hold the information from parsing, and then does the parse.
  * The options argument shows what flags and properties to expect.
  * It also provides a usage message to be shown if the parse fails.
  * The args array shows the arguments to parse, which would
  * normally be all arguments given on the command line.
  *
  * Called in this form if there is an error in the options
  * the program will exit with code a, printing the usage String.
  */
  static public P arseArgs(String options, String[] args, String usage){
    P p = (new P()).parse(options, args);
    if (0!= p.getErrors().length()){
      P.rintln(p.getErrors());
      P.usage(usage);
    }
    return p;
  }
/**
 *  This is used to construct instances from a className string
 */
  static public Object getInstance(String className){
    Object result = null;
      try{
        result= Class.forName(className).newInstance();
      } catch(ClassNotFoundException x){
         P.exception(x);
      } catch(InstantiationException x){
         P.exception(x);
      } catch(IllegalAccessException x){
         P.exception(x);
      }
      return result;
    }
/*
  ================================================================
  The methods above are basically static utility methods
  The methods below deal with this instance and its variables
  ================================================================
*/

/**
  * After arseArgs(...) has been run, lets you ask if a particular
  * flag was specified as an argument.
  */
  public boolean getFlag(String flag){
    return null != this.get(flag);
  }

/**
  * After arseARgs(...) has been run, lets you retrieve the value
  * of a particular property, returning null if that property
  * was not specified as an argument.
  */
  public String getProperty(String flag){
    return (String)this.get(flag);
  }

  public String getProperty(String flag, String defaultValue){
    String result;
    result = (String)this.get(flag);
    if (null==result){
      result=defaultValue;
    }
    return result;
  }

  public int getIntProperty(String flag){
    return getIntProperty(flag, -1);
  }

  public int getIntProperty(String flag, int defaultValue){
    int result = defaultValue;
    String value = (String)this.get(flag);
    if (null!=value){
      result=Integer.parseInt((String)this.get(flag));
    }
    return result;
  }

/**
  * After arseArgs(...) has been run, lets you retrieve the
  * arguments that are to be treated as parameters.
  * It returns a String array whose length tells how many
  * parameters are present, including 0 length if none are present.
  */
  public String[] getParams(){ //OK
    return (String[])this.get(" ");
  }

  public String getParamsAsString(){ //OK
    StringBuffer b = new StringBuffer();
    String[] params = getParams();
    for (int j = 0; j< params.length; j++){
      b.append(params[j]);
      b.append(" ");
    }
    return b.toString();
  }

/**
  * After arseArgs(...) has been run, tells the details of
  * any parsing errors.  Returns null if there were no errors.
  */
  public String getErrors(){
    return (String)this.get("*");
  }

/**
  * The actual routing to parse arguments, called by arseArgs(...).
  */
  protected P parse(String options, String[] args){
    // a- means optional flag
    // a+ means required flag
    // a_ means optional property, must be followed by a value
    // a. means required property, must be followed by a value
    HashMap cmdFlags = new HashMap();
    String flag;
    String nextFlag=null;
    StringBuffer errors=new StringBuffer();
    /**
      First go through options to see what should be in args
    */
    for(int which=0;which<options.length();which++){
      flag = "-"+options.substring(which,which+1);
      if(which+1<options.length()){
        nextFlag=options.substring(which+1,which+2);
        if (nextFlag.equals("-")){
          cmdFlags.put(flag,nextFlag);
        } else
        if (nextFlag.equals("+")){
          cmdFlags.put(flag,nextFlag);
          /*
            mark that it is required
            if found this will be overwritten by -
          */
          this.put(flag,nextFlag);
        } else
        if (nextFlag.equals("_")){
          cmdFlags.put(flag,nextFlag);
        } else
        if (nextFlag.equals(".")){
          cmdFlags.put(flag," "); //JDH changed this from ":"
          /*
            mark that it is required using " "
	    so it cannot be the same as a value.
            if found this will be overwritten by the value.
          */
          this.put(flag," "); // mark that it is required
        } else {
          System.out.println("Bad symbol "+nextFlag+"in option string");
        }
        which++;
      } else {
        System.out.println("Missing symbol in option string at "+which);
      }
    }

    int arg=0;
    for(;arg<args.length;arg++){
      if (!args[arg].startsWith("-")){
        break;
      }
      flag = args[arg];
      /*
        This should tell it to quit looking for flags or options
      */
      if (flag.equals("--")){
        arg++;
        break;
      }
      if (!(cmdFlags.containsKey(flag))){
        errors.append("\nbad flag "+flag);
        continue;
      }
      if (((String)cmdFlags.get(flag)).equals("-")){
      this.put(flag,"-");
        continue;
      }
      if (((String)cmdFlags.get(flag)).equals("+")){
      this.put(flag,"-");// turns off the + because it was found
        continue;
      }
      if (!(arg+1<args.length)){
        errors.append("\nMissing value for "+flag);
        continue;
      }
      arg++;
      this.put(flag,args[arg]);
    }
    String[] params=null;
    params = new String[args.length - arg];

    int n=0;
    // reverse these so they come back in the right order!
    for(;arg<args.length;arg++){
      params[n++] = args[arg];
    }
    Iterator k = null;
    Map.Entry e = null;
    if (this.containsValue("+")){
      // can iterate through to see which ones
      k = this.entrySet().iterator();
      while (k.hasNext()){
        if ("+".equals((String)(e=(Map.Entry)k.next()).getValue())){
          errors.append("\nThe required flag "+(String)e.getKey()+" was not supplied.");
        };
      }
    } 
    /*
      Should change this to " " in accordance with remark above
    */
      //JDH changed to " " from ":" in both spots below
    if (this.containsValue(" ")){
      // can iterate through to see which ones
      k = this.entrySet().iterator();
      while (k.hasNext()){
        if (" ".equals((String)(e=(Map.Entry)k.next()).getValue())){
          errors.append("\nThe required property "+(String)e.getKey()+" was not supplied.");
        }
      }
    }
    this.put(" ",params);
    this.put("*",errors.toString());
    return this;
  }

/**
  * This demonstrates how the argument parsing methods might be used,
  * including specifying the options and a usage string,
  * and testing to see if a particular flag or property is present,
  * and obtaining the list of parameters.
  * Notice that it can be run again, perhaps after having added or
  * removed some parameters.
  */

  static public String[] shiftArgs(String[] args, int shift){
    String[] result = new String[args.length -shift];
    for (int j=0;j<result.length;j++){
      result[j]=args[j+shift];
    }
    return result;
  }

  static class PFormatter extends Formatter{
  // Cumulative time is the best
    public String format(LogRecord r){
      StringBuffer b=new StringBuffer(100);
      b.append(string12(r.getLevel().toString()));
      b.append(' ');
      b.append(time13(r.getMillis()-startTime));
      b.append(' ');
      b.append(formatMessage(r));
      b.append('\n');
      return b.toString();
    }

    static public String time13(long time){
      StringBuffer b = new StringBuffer(26);
      b.append("             ");
      b.append(time);
      return b.substring(b.length()-13);
    }

    static public String string12(String s){
      StringBuffer b = new StringBuffer(12);
      b.append(s);
      int l=12-b.length();
      for(int j=0;j<l;j++){
        b.append(' ');
      }
      return b.toString();
    }
  }

  static public class Tester{

    static public void main(String[] args){
      long startTime=System.currentTimeMillis();
      String options=args[0];
      String[] newargs=P.shiftArgs(args,1);
      P.rintln("Expecting "+options);
      P cmdArgs=P.arseArgs(options, newargs);
      P.rintln(cmdArgs.getErrors());
      String property; 
      P.rintln("flag a is "+cmdArgs.getFlag("-a"));
      P.rintln("flag b is "+cmdArgs.getFlag("-b"));
      property = cmdArgs.getProperty("-c");   
      P.rintln((property==null)?"No property c":"c value is "+property);
      property = cmdArgs.getProperty("-d");   
      P.rintln((property==null)?"No property d":"d value is "+property);
      String[] params = cmdArgs.getParams();
      if (0 != params.length){
        P.rintln("  === Parameters ===  ");
        P.rintln(params);
      }
      /*
        Do it with a usage message, which will exit 
        after printing that message if there is a parse error
      */
      P.rintln("\n\n Trying it with a usage message");
      options="a-b+c_d.";
      String usage = "P options [-a] -b [-c Value] -d Value";
      cmdArgs=P.arseArgs(options, newargs, usage);
    }
  }
}
