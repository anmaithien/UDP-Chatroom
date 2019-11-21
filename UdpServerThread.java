import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;

class User {
  public InetAddress mIP;
  public int mPort;

  public User(InetAddress ip, int port) {
    mIP = ip;
    mPort = port;
  }
}

public class UdpServerThread extends Thread {
  private static final int PORT = 4445;
  private static final int BUFFER_SIZE = 256;

  // map to store token as name and IP/port combo
  private Map<String, User> str = new HashMap<>();

  protected DatagramSocket mSocket;

  public UdpServerThread() throws IOException {
    this("UdpServerThread");
  }

  public UdpServerThread(String name) throws IOException {
    super(name);
    mSocket = new DatagramSocket(PORT);
  }

  public void run() {
    while(true) {
      byte[] byteBuffer = new byte[BUFFER_SIZE];
      DatagramPacket clientPacket = new DatagramPacket(byteBuffer, byteBuffer.length);

      try {
        System.out.println("Listening for a client...");
        mSocket.receive(clientPacket);
        System.out.println("Received packet from client");

        String clientMes = new String (clientPacket.getData(), 0, clientPacket.getLength());

        String[] mes = clientMes.split(";");
        
        String type = mes[0];
        int equalInd = type.indexOf("=");

        String op = type.substring(equalInd + 1).trim();
        //FIXME
        System.out.println(op);
        String responsString = "";

        if (op.equals("JOIN")) {
          String name = mes[1].substring(mes[1].indexOf("=") + 1);

          if (str.containsKey(name)) {
            responsString = "TYPE=JOINRESPONSE;STATUS=1;MESSAGE=error"; 
          }else {
            str.put(name, new User(clientPacket.getAddress(), clientPacket.getPort()));
            responsString = "TYPE=JOINRESPONSE;STATUS=0;MESSAGE=success"; 
          }
        }else if (op.equals("POST")) {
          String name = mes[1].substring(mes[1].indexOf("=") + 1);
          String message = mes[2].substring(mes[2].indexOf("=") + 1);
          responsString = "TYPE=NEWMESSAGE;USERNAME=" + name + ";MESSAGE=" + message;
          byte[] bBuffer = responsString.getBytes(); 

          str.forEach((key, value) -> {

            InetAddress clientAddress = value.mIP;
            int clientPort = value.mPort;

            DatagramPacket serverPacket = new DatagramPacket(bBuffer, bBuffer.length, clientAddress, clientPort);
            try {
              mSocket.send(serverPacket);
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
        }else if (op.equals("LEAVE")) {
          // remove from map
          String name = mes[1].substring(mes[1].indexOf("=") + 1);
          str.remove(name);

          responsString = "TYPE=BYE";
        }
        
        // print out string to send to client
        System.out.println(responsString);

        // send message back to client
        if (!op.equals("POST")) {
          // System.out.println("SENT");
          byteBuffer = responsString.getBytes();

          InetAddress clientAddress = clientPacket.getAddress();
          int clientPort = clientPacket.getPort();
    
          DatagramPacket serverPacket = new DatagramPacket(byteBuffer, byteBuffer.length, clientAddress, clientPort);
          mSocket.send(serverPacket);
        }
      }
      catch (IOException e) {
        mSocket.close();
        e.printStackTrace();
      }
    }
  }
}
