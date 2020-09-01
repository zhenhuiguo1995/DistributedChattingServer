import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ConnectWithClient implements Runnable {
    ServerSocket serverSocket;
    Map<String, List<String>> twitterPostMap;

    public ConnectWithClient(ServerSocket serverSocket, Map<String, List<String>> twitterPostMap) {
        this.serverSocket = serverSocket;
        this.twitterPostMap = twitterPostMap;
    }

    public String getAllPostsForGroup(String groupName) {
        StringBuilder allPosts = new StringBuilder();
        List<String> posts = this.twitterPostMap.get(groupName);
        if (posts.size() == 0) {
          return "";
        }
        for (int i =0; i < posts.size()-1; i++) {
            allPosts.append(posts.get(i));
            allPosts.append(":");
        }
        allPosts.append(posts.get(posts.size()-1));
        return allPosts.toString();
    }

    public void run() {
      synchronized (this) {
        while (true) {
          try {
            // System.out.println("Waiting for clients to connect...");
            Socket socket = this.serverSocket.accept();

            // System.out.println("Connected with peer! port number: " + socket.getPort());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            String getRequest = input.readUTF();
            String action = getRequest.split(":")[0];
            String groupName = getRequest.split(":")[1];
            if ("GET_ALL_POSTS".equalsIgnoreCase(action)) {
              String toSend = getAllPostsForGroup(groupName);
              output.writeUTF(toSend);
            } else if ("POST".equalsIgnoreCase(action)) {
              String body = getRequest.split(":")[2];
              this.twitterPostMap.get(groupName).add(body);
              System.out.println("Received a message from group " + groupName);
              System.out.println("The post is: " + body);
            } else {
              output.writeUTF("FAILED");
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
}
