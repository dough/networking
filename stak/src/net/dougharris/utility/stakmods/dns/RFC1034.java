package net.dougharris.utility.stakmods.dns;

import net.dougharris.utility.P;
import net.dougharris.utility.PacketInputStream;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.lang.SecurityException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

public class RFC1034{
// serverHost serverPort recordType recordName
  
  static String usage= "DNSQuery serverHostName recordType domainName";

  public static void main(String[] args) throws Exception{
    int srvPort = 53;
    boolean recurse = true;

    if (args.length<3){
      P.usage(usage);
    }
    String srvName=(args[0]);
    String recType = args[1];
    String domName = args[2];
    RFC1035 r = getReply(srvName, srvPort, domName, recType, recurse, 10000);
    P.rintln(r.parse().toString());
  }

  public static RFC1035 getReply
    (String srvName, int srvPort, String domName, String recType, boolean recurse, int timeOut){
    RFC1035 reply=null;
    InetAddress srvAddress=null;
    byte[] bs=null;
    byte[] br=new byte[512];
    DatagramPacket ps=null;
    DatagramPacket pr=null;
    DatagramSocket s=null;
    ByteArrayOutputStream ob=new ByteArrayOutputStream();
    DataOutputStream o=new DataOutputStream(ob);
    try{
    /*
      Getting the socket ready
    */
      s=new DatagramSocket(0,InetAddress.getLocalHost());
    /*
      Finding out where the packet should go
    */
      srvAddress=InetAddress.getByName(srvName);
    /* 
      Getting the packet ready
    */
      bs = RFC1035.createQueryPacket(domName, recType, recurse);
    /*
      Getting the DatagramPacket ready for the server filled with the packet
    */
      ps=new DatagramPacket(bs,bs.length,srvAddress,srvPort);

      s.setSoTimeout(timeOut);
      s.send(ps);
      P.rint("Client "+s.getLocalAddress()+":"+s.getLocalPort());
      P.rint(" querying "+srvAddress.getHostName()+":"+srvPort);
      P.rintln(":"+ps.getLength());
      pr=new DatagramPacket(br, br.length);
      s.receive(pr);
      /*now to take pr apart*/
      byte[] bq;
      //JDH need to be sure the byte length is correct
      //JDH getData may still have the original length
      bq=pr.getData();
    } catch(SecurityException x){
      P.exception(x,2);
    } catch(NoRouteToHostException x){
      P.exception(x,3);
    } catch(BindException x){
      P.exception(x,4);
    } catch(ConnectException x){
      P.exception(x,5);
    } catch(SocketTimeoutException x){
      return reply;
    } catch(SocketException x){
      P.exception(x,6);
    } catch(UnknownHostException x){
      P.exception(x,7);
    } catch(IOException x){
      P.exception(x,8);
    } catch(Exception x){
      P.exception(x,9);
    }
    P.rintln("got a datagram reply");//JDH
    return new RFC1035(pr.getData(), 0, pr.getLength());
  }

  public String[] parseSegs(String seg){
    ArrayList segs=new ArrayList();
    if (!seg.endsWith(".")){seg+=".";}
    int nDot=0;
    segs.add(seg);
    while ((nDot=seg.indexOf("."))>0){
      seg=seg.substring(nDot+1);
      segs.add(seg);
    }
    return (String[])segs.toArray(new String[segs.size()]);
  }
  static String[] rootServerAddresses = new String[13];
  static{
    rootServerAddresses[0]="198.41.0.4";
    rootServerAddresses[1]="128.9.0.107";
    rootServerAddresses[2]="192.33.4.12";
    rootServerAddresses[3]="128.8.10.90";
    rootServerAddresses[4]="192.203.230.10";
    rootServerAddresses[5]="192.5.5.241";
    rootServerAddresses[6]="192.112.36.4";
    rootServerAddresses[7]="128.63.2.53";
    rootServerAddresses[8]="192.36.148.17";
    rootServerAddresses[9]="198.41.0.10";
    rootServerAddresses[10]="193.0.14.129";
    rootServerAddresses[11]="198.32.64.12";
    rootServerAddresses[12]="202.12.27.33";
  }
  public static void explode(String domName){
    String[] serverAddresses = rootServerAddresses;
    RFC1035 reply;
    ResourceRecord[] records;
    for(int j=0;j<serverAddresses.length;j++){
      reply=getReply(serverAddresses[j],53,domName,"NS",false, 12000);
      P.rintln(reply.toString());//JDH
      reply.parse();
      if (null==(records=reply.getAnswers())){
        P.rintln("Server "+serverAddresses[j]+" has no NS Records");
      } else {
        P.rintln("Server "+serverAddresses[j]+" has NS "+records.length);
      }
    }
  }
}
