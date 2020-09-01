import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class CreateGroupService implements Runnable {
  private Socket socket;
  private ConcurrentHashMap<String, List<Account>> groupMemberMap;
  private ConcurrentHashMap<String, String> groupInfoMap;
  private Vector<Account> activeAccountList;
  private Request request;
  private Thread runningThread = null;

  public CreateGroupService(Socket socket,
      ConcurrentHashMap<String, List<Account>> groupMemberMap,
      ConcurrentHashMap<String, String> groupInfoMap,
      Vector<Account> activeAccountList, Request request) {
    this.socket = socket;
    this.groupMemberMap = groupMemberMap;
    this.groupInfoMap = groupInfoMap;
    this.activeAccountList = activeAccountList;
    this.request = request;
  }

  // request.type = "create_group" request.header = {"group_name", "group_password"};
  @Override
  public void run() {
    synchronized (this) {
      this.runningThread = Thread.currentThread();
    }
    String groupName = request.getHeader()[0];
    String groupPassword = request.getHeader()[1];
    String username = request.getHeader()[2];
    String password = request.getHeader()[3];
    String response = "";
    System.out.println("Received a response to create a group with name" + groupName);
    if (groupInfoMap.containsKey(groupName)) {
      // the group name already exists1
      response = "The request to create a group failed! The group name already exists!";
      System.out.println(response);
    } else {
      response = String.format(
          "The request to create a group with name %s succeeds!", groupName);
      // put the information into group info map
      groupInfoMap.put(groupName, groupPassword);
      // update group member map
      List<Account> groupUserList = new ArrayList<>();
      groupUserList.add(getUser(username, password));
      groupMemberMap.put(groupName, groupUserList);
      System.out.println(response);
    }

    try {
      OutputStream outputStream = socket.getOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      dataOutputStream.writeUTF(response);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Account getUser(String username, String password) {
    for (Account account: activeAccountList) {
      if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
        return account;
      }
    }
    System.out.println("There is no active account with this username! Please login before creating a group.");
    return null;
  }
}
