import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class CheckGroupActiveUserService implements Runnable {
  private Socket socket;
  private Vector<Account> activeUserList;
  private ConcurrentHashMap<String, List<Account>> groupMemberMap;
  private Request request;
  private Thread runningThread;
  private Utils utils;

  public CheckGroupActiveUserService(Socket socket,
      Vector<Account> activeUserList,
      ConcurrentHashMap<String, List<Account>> groupMemberMap, Request request) {
    this.socket = socket;
    this.activeUserList = activeUserList;
    this.groupMemberMap = groupMemberMap;
    this.request = request;
    this.utils = new Utils();
  }

  @Override
  public void run() {
    synchronized (this) {
      this.runningThread = Thread.currentThread();
    }
    String groupname = request.getHeader()[0];
    List<Account> groupMemberList = groupMemberMap.get(groupname);
    List<Integer> activeMemberPortNumberList = new ArrayList<>();
    for (Account account : groupMemberList) {
      if (activeUserList.contains(account)) {
        activeMemberPortNumberList.add(account.getPortNumber());
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < activeMemberPortNumberList.size(); i ++) {
      if (i < activeMemberPortNumberList.size() - 1) {
        sb.append(activeMemberPortNumberList.get(i)).append(utils.SEPARATOR);
      } else {
        sb.append(activeMemberPortNumberList.get(i));
      }
    }
    try {
      OutputStream outputStream = socket.getOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      dataOutputStream.writeUTF(sb.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
