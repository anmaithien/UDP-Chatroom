import java.io.*;
import java.net.*;
import java.util.*;

public class UdpClient {
  private static final int PORT = 4445;
  private static final int BUFFER_SIZE = 256;
  private static final String HOST_NAME = "localhost";

  private static DatagramSocket mSocket;
  private static byte[] mPacketBuffer = new byte[BUFFER_SIZE];

  public static void main(String[] args) throws IOException {
    mSocket = new DatagramSocket();

    System.out.print("Username: ");
    Scanner scnr = new Scanner(System.in);

    String name = scnr.nextLine();
    name = name.trim();
    String mes = name;    
    String response = "TYPE=JOIN;USERNAME=" + name;
    mPacketBuffer = response.getBytes();
    sendRequest();

    // response
    DatagramPacket packet = receiveResponse();
    String ServerMes = new String (packet.getData(), 0, packet.getLength());
    
    // Print test
    // System.out.println(ServerMes);
    String[] messageServer = ServerMes.split(";");

    String status = messageServer[1];
    // System.out.println(status);
    while (status.equals("STATUS=1")) {
      // re-promt
      System.out.print("Username: ");
      name = scnr.nextLine();
      response = "TYPE=JOIN;USERNAME=" + name;
      mPacketBuffer = response.getBytes();
      sendRequest();

      // receive from server
      // mPacketBuffer = new byte[BUFFER_SIZE]; IMPORTANT TO BE NEW BYTE
      packet = receiveResponse();

      ServerMes = new String (packet.getData(), 0, packet.getLength());
      messageServer = ServerMes.split(";");
  
      status = messageServer[1];      
    }

    // make final for thread
    final String name2 = name;
  
    // thread to receive response from server
    new Thread(
      new Runnable(){
        @Override
        public void run() {
          // receive response
          while (true) {
            try {
              mPacketBuffer = new byte[BUFFER_SIZE];
              DatagramPacket receive = receiveResponse();
              String ServerMes = new String (receive.getData(), 0, receive.getLength());

              // LOgging to help debug
              // System.out.println(ServerMes);
              String[] messageServer = ServerMes.split(";");
    
              // System.out.println("RECEIVED");
              String type = messageServer[0];
              int equalInd = type.indexOf("=");
              String op = type.substring(equalInd + 1);
      
              if (op.equals("NEWMESSAGE")) {
                String username = messageServer[1].substring(messageServer[1].indexOf("=") + 1);
                // System.out.println(username);
                if (!username.equals(name2)) {
      
                  System.out.println(username + ": " + messageServer[2].substring(messageServer[2].indexOf("=") + 1));
                }
              }
            }catch(IOException e) {
                e.printStackTrace();
            }
          }
        }
      }
    ).start();

    // sending packets
    while (!mes.equals("q")) {
      mes = scnr.nextLine();

      // send a post 
      response = "TYPE=POST;TOKEN=" + name + ";MESSAGE=" + mes;
      mPacketBuffer = response.getBytes();
      sendRequest();
    }

    // leave
    response = "TYPE=LEAVE;TOKEN=" + name;
    mPacketBuffer = response.getBytes();
    sendRequest();
    mSocket.close();
  }

  private static void sendRequest() throws IOException {
    InetAddress address = InetAddress.getByName(HOST_NAME);
    DatagramPacket packet = new DatagramPacket(mPacketBuffer, mPacketBuffer.length, address, PORT);
    mSocket.send(packet);
  }

  private static DatagramPacket receiveResponse() throws IOException {
    mPacketBuffer = new byte[BUFFER_SIZE];
    DatagramPacket packet = new DatagramPacket(mPacketBuffer, mPacketBuffer.length);
    mSocket.receive(packet);

    // InetAddress serverAddress = packet.getAddress();
    // int serverPort = packet.getPort();

    // System.out.println("Host: " + serverAddress);
    // System.out.println("Port: " + serverPort);

    return packet;
  }

  // private static void displayResults(DatagramPacket packet) {
  //   String received = new String(packet.getData(), 0, packet.getLength());
  //   System.out.println("Here's what I got: " + received);
  // }
}
