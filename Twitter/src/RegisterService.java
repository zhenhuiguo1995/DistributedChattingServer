import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

public class RegisterService implements Runnable {
  private Socket socket;
  private Vector<Account> accountList;
  private Request request;
  private Thread runningThread;

  // request.type = "register", request.header = {"username", "password"};
  public RegisterService(Socket socket, Vector<Account> accountList, Request request) {
    this.socket = socket;
    this.accountList = accountList;
    this.request = request;
  }

  // put user information into the accountList
  @Override
  public void run() {
    synchronized (this) {
      this.runningThread = Thread.currentThread();
    }
    String username = request.getHeader()[0];
    String password = request.getHeader()[1];
    String response = "";
    System.out.println("Received a request to create an account with name " + username);
    // check if username is already register
    for (Account existingAccount : accountList) {
      if (existingAccount.getUsername().equals(username)) {
        response = "The username has already been registered!";
        System.out.println(response);
      }
    }
    if (response.equals("")) {
      Account user = new Account(username, password);
      accountList.add(user);
      response = "Successfully created user!";
      System.out.println("Successfully created user with name " + username);
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
