import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class JoinGroupService implements Runnable {
  private Socket socket;
  private ConcurrentHashMap<String, List<Account>> groupMemberMap;
  private Vector<Account> activeAccountList;
  private ConcurrentHashMap<String, String> groupInfoMap;
  private Request request;
  private Thread runningThread = null;

  public JoinGroupService(Socket socket,
      ConcurrentHashMap<String, List<Account>> groupMemberMap,
      ConcurrentHashMap<String, String> groupInfoMap,
      Vector<Account> activeAccountList, Request request) {
    this.socket = socket;
    this.groupMemberMap = groupMemberMap;
    this.groupInfoMap = groupInfoMap;
    this.activeAccountList = activeAccountList;
    this.request = request;
  }

  @Override
  public void run() {
    synchronized (this) {
      this.runningThread = Thread.currentThread();
    }
    String groupname = request.getHeader()[0];
    String groupPassword = request.getHeader()[1];
    String username = request.getHeader()[2];
    String password = request.getHeader()[3];
    String response;
    System.out.println("Received a request to join a group with name " + groupname);
    if (!groupInfoMap.containsKey(groupname)) {
      response = "NAME:The requested group name does not exist!";
    } else if (!groupInfoMap.get(groupname).equals(groupPassword)) {
      response = "PASSWORD:The provided group name and password does not match our record";
    } else {
      response = String.format("SUCCESS:Successfully joined group: %s", groupname);
      groupMemberMap.get(groupname).add(getUser(username, password));
      System.out.println("Join group request succeeded!");
    }
    try {
      OutputStream outputStream = socket.getOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      dataOutputStream.writeUTF(response);
      List<Account> groupMemberList = groupMemberMap.get(groupname);
      if (groupMemberList.size() > 0) {
        Integer oneActivePeerPort = groupMemberList.get(0).getPortNumber();
        dataOutputStream.writeUTF(String.valueOf(oneActivePeerPort));
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Account getUser(String username, String password) {
    for (Account user : activeAccountList) {
      if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
        return user;
      }
    }
    // cannot go to this block
    System.out.println("Illegal information!");
    return null;
  }
}
