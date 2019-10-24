import java.io.*;

public class UdpServer {
  public static void main(String[] args) throws IOException {
    new UdpServerThread().start();
  }
}
