import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class CentralServer {
  public static void main(String[] args) throws Exception {
    int serverPortNumber = 1234;
    // initialize server socket
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(serverPortNumber);
    } catch (IOException ignored) {
      return;
    }

    // stores information about user login credentails
    // this vector should be updated when a user regsiters account
    // this vector will be needed when a user tries to login
    Vector<Account> accountList = new Vector<>();

    // stores information about an active user.
    // when a user logs-in, an Account object will be created and put into this vector
    // we will set the port number of the Account object
    Vector<Account> activeAccountList = new Vector<>();

    // stores information about groups members:
    // key is the name of the group, value is a list of all accounts within this group
    // this map is needed when a new group is created, or when a user tries to join a group
    // or when a user is trying to get the active users within a group
    ConcurrentHashMap<String, List<Account>> groupMemberMap = new ConcurrentHashMap<>();

    // key is the name of a group, value is the password to the group
    // this map is updated when a group is created, or when a user tries to join a group
    // or when all users within the same group logs(the group will be removed!)
    ConcurrentHashMap<String, String> groupInfoMap = new ConcurrentHashMap<>();


    Utils utils = new Utils();
    System.out.println("Server Started at port" + serverPortNumber);

    while (true) {
      Socket socket = serverSocket.accept();
      InputStream inputStream = socket.getInputStream();
      DataInputStream dataInputStream = new DataInputStream(inputStream);
      String message = dataInputStream.readUTF();
      Request request = Request.parseRequest(message);
      if (request.getType().equals(utils.REGISTER)) {
        // register:username:password
        new Thread(new RegisterService(socket, accountList, request)).start();
      } else if (request.getType().equals(utils.LOGIN)) {
        // login:username:password:portnumber
        new Thread(new LoginService(socket, accountList, activeAccountList, request)).start();
      } else if (request.getType().equals(utils.CREATE_GROUP)) {
        // create_group:groupname:group_password:username:user_password
        new Thread(new CreateGroupService(socket, groupMemberMap, groupInfoMap, activeAccountList, request)).start();
      } else if (request.getType().equals(utils.JOIN_GROUP)) {
        // join_group:groupname:grouppassword:username:user_password
        new Thread(new JoinGroupService(socket,
            groupMemberMap, groupInfoMap, activeAccountList, request)).start();
      } else if (request.getType().equals(utils.LOGOUT)) {
        // logout:username:password
        new Thread(new LogoutService(socket, groupMemberMap,
            groupInfoMap, activeAccountList,request)).start();
      } else if (request.getType().equals(utils.CHECK_ACTIVE_USER_LIST)) {
        // check_group_active_user:groupname
        new Thread(new CheckGroupActiveUserService(
            socket, activeAccountList, groupMemberMap, request)).start();
      } else {
        System.out.println("cannot enter this block!!!");
      }
    }

  }
}
