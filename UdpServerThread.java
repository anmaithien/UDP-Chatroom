import java.io.*;
import java.net.*;
import java.util.*;

public class UdpServerThread extends Thread {
  private static final int PORT = 4445;
  private static final int BUFFER_SIZE = 256;

  protected DatagramSocket mSocket;

  public UdpServerThread() throws IOException {
    this("UdpServerThread");
  }

  public UdpServerThread(String name) throws IOException {
    super(name);
    mSocket = new DatagramSocket(PORT);
  }

  public void run() {
    byte[] byteBuffer = new byte[BUFFER_SIZE];
    DatagramPacket clientPacket = new DatagramPacket(byteBuffer, byteBuffer.length);

    try {
      System.out.println("Listening for a client...");
      mSocket.receive(clientPacket);
      System.out.println("Received packet from client");

      String responseString = "Well hello!";
      byteBuffer = responseString.getBytes();

      InetAddress clientAddress = clientPacket.getAddress();
      int clientPort = clientPacket.getPort();

      System.out.println("Host: " + clientAddress);
      System.out.println("Port: " + clientPort);

      DatagramPacket serverPacket = new DatagramPacket(byteBuffer, byteBuffer.length, clientAddress, clientPort);
      mSocket.send(serverPacket);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    mSocket.close();
  }
}
