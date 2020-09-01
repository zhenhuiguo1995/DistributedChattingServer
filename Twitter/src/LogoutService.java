import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class LogoutService implements Runnable {
  private Socket socket;
  private Vector<Account> activeAccountList;
  private ConcurrentHashMap<String, List<Account>> groupMemberMap;
  private ConcurrentHashMap<String, String> groupInfoMap;
  private Request request;
  private Thread runningThread = null;

  public LogoutService(Socket socket,
      ConcurrentHashMap<String, List<Account>> groupMemberMap,
      ConcurrentHashMap<String, String> groupInfoMap,
      Vector<Account> activeAccountList,
      Request request) {
    this.socket = socket;
    this.activeAccountList = activeAccountList;
    this.groupMemberMap = groupMemberMap;
    this.groupInfoMap = groupInfoMap;
    this.request = request;
  }

  @Override
  public void run() {
    synchronized (this) {
      this.runningThread = Thread.currentThread();
    }
    String username = request.getHeader()[0];
    String password = request.getHeader()[1];
    System.out.println("Received a request to logout user:" + username);
    Account account = getUser(username, password);
    // remove user from data structures
    removeActiveUser(account);
    List<String> groupToBeRemoved = new ArrayList<>();
    for (String groupName : groupMemberMap.keySet()) {
      List<Account> groupMemberList = groupMemberMap.get(groupName);
      if (groupMemberList.contains(account)) {
        groupMemberList.remove(account);
        if (groupMemberList.size() == 0) {
          // if after one user logs out, the whole group does not have any active user
          // then we should remove all information about the group
          groupToBeRemoved.add(groupName);
        }
      }
    }
    // update two maps
    for (String groupName : groupToBeRemoved) {
      groupMemberMap.remove(groupName);
      groupInfoMap.remove(groupName);
    }

    try {
      OutputStream outputStream = socket.getOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      dataOutputStream.writeUTF("Successfully logged user out");
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

  private void removeActiveUser(Account account) {
    Account accountToRemove = null;
    for (Account tmp : activeAccountList) {
      if (tmp.equals(account)) {
        accountToRemove = account;
        break;
      }
    }
    // there will always be an account to remove, no need to check for null
    activeAccountList.remove(accountToRemove);
  }
}
