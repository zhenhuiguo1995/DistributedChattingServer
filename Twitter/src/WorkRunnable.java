// import com.sun.security.ntlm.Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * Handles client to client communication messages
 */
public class WorkRunnable implements Runnable {
  private ServerSocket serverSocket;
  private Thread runningThread;
  private Map<String, List<String>> twitterPostMap;

  public WorkRunnable(ServerSocket serverSocket,
      Map<String, List<String>> twitterPostMap) {
    this.serverSocket = serverSocket;
    this.twitterPostMap = twitterPostMap;
  }

  @Override
  public void run() {
    synchronized (this) {
      runningThread = Thread.currentThread();
    }
    while (true) {
      Socket socket = null;
      try {
        socket = serverSocket.accept();
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      // get input message
      InputStream inputStream = null;
      try {
        inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        String message = dataInputStream.readUTF();
        // todo: handle message
        String response = this.handle(message);
        if (message.contains(ClientAction.PULL.toString())) {
          OutputStream outputStream = socket.getOutputStream();
          DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
          dataOutputStream.writeUTF(response);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // todo: generate response!
  private String handle(String message) {
    // message can be 'pull' or 'post'
    // pull:groupname or post:groupname:postInformation
    String[] strings = message.split(":");
    String operation = strings[0];
    String groupName = strings[1];
    if (operation.equalsIgnoreCase(ClientAction.PULL.toString())) {
      // if this is a pull request, then this client will pack all the posts
      // and send it back
      List<String> postList = twitterPostMap.get(groupName);
      StringBuilder sb = new StringBuilder();
      for (String post : postList) {
        sb.append(post).append(":");
      }
      return sb.toString();
    } else {
      // if this is just a post request, then this client will display the post
      // and add the post information into groupPost hashmap
      String post = strings[2];
      System.out.println("New post from group " + groupName);
      System.out.println(post);
      twitterPostMap.get(groupName).add(post);
      return "";
    }
  }
}
