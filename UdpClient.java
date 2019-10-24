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
    sendRequest();
    DatagramPacket packet = receiveResponse();
    displayResults(packet);
    mSocket.close();
  }

  private static void sendRequest() throws IOException {
    InetAddress address = InetAddress.getByName(HOST_NAME);
    DatagramPacket packet = new DatagramPacket(mPacketBuffer, mPacketBuffer.length, address, PORT);
    mSocket.send(packet);
  }

  private static DatagramPacket receiveResponse() throws IOException {
    DatagramPacket packet = new DatagramPacket(mPacketBuffer, mPacketBuffer.length);
    mSocket.receive(packet);

    InetAddress serverAddress = packet.getAddress();
    int serverPort = packet.getPort();

    System.out.println("Host: " + serverAddress);
    System.out.println("Port: " + serverPort);

    return packet;
  }

  private static void displayResults(DatagramPacket packet) {
    String received = new String(packet.getData(), 0, packet.getLength());
    System.out.println("Here's what I got: " + received);
  }
}
