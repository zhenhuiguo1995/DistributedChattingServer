import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

public class LoginService implements Runnable {
  private Socket socket;
  private Vector<Account> accountList;
  private Vector<Account> activeAccountList;
  private Request request;
  private Thread runningThread;

  // request.type = "login", request.header = {"username", "password", "ip"}
  public LoginService(Socket socket, Vector<Account> accountList,
      Vector<Account> activeAccountList, Request request) {
    this.socket = socket;
    this.accountList = accountList;
    this.activeAccountList = activeAccountList;
    this.request = request;
  }

  // send the response to client via socket
  @Override
  public void run() {
    synchronized (this) {
      this.runningThread = Thread.currentThread();
    }
    String username = request.getHeader()[0];
    String password = request.getHeader()[1];
    int portNumber = Integer.parseInt(request.getHeader()[2]);
    String response = "";
    Account tmp = new Account(username, password);
    boolean hasRecord = false;
    System.out.println("Received a request to login with name " + username);
    for (Account user : accountList) {
      if (user.equals(tmp)) {
        hasRecord = true;
        response = "Login successfully";
        tmp.setPortNumber(portNumber);
        activeAccountList.add(tmp);
        System.out.println(response);
      }
    }
    if (!hasRecord) {
      response = "Login failed. The provided credentials does not match out record";
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
}
