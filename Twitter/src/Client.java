import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class Client {
    private Integer selfPort;
    private String selfAddress;
    private String username;
    private String userpass;
    private Map<String, List<Account>> allConnections;
    private Utils utils;

    public Client(String selfAddress, Integer selfPort) {
        this.selfPort = selfPort;
        this.selfAddress = selfAddress;
        this.utils = new Utils();
    }
/**
 * Whenever a client is started, it logs in to the central server
 * - logs in to central server
 * - gives the group number and group id to the central server
 * - if the group does not exist, start a group and store it's portnumber in a set
 * - if the group exist, connect to all the servers in the server port set the central server maintains
 *
 */

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserpass(String userpass) {
        this.userpass = userpass;
    }

    public String getUsername() {
        return this.username;
    }

    public String getUserpass() {
        return this.userpass;
    }

    public Connection connectToCentralServer(String centralAddress, Integer centralPort) {
        try {
            Socket socket = new Socket(centralAddress, centralPort);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            Connection connection = new Connection(dataInputStream, outputStream);
            return connection;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean login(DataOutputStream outputStream, DataInputStream inputStream, String username, String userpass) {
        try {
            //send request to server
            //login:username:password:portnumber
            String req = "login" + utils.SEPARATOR + username +  utils.SEPARATOR +
                userpass + utils.SEPARATOR + selfPort;
            outputStream.writeUTF(req);
            outputStream.flush();
            //read respond from server
            String res = inputStream.readUTF();
            if (res.equals("Login successfully")) {
                setUsername(username);
                setUserpass(userpass);
                System.out.println(res);
                // check to which group this client belongs
                // establish connection with members in the group (if there are any) and get all posts
                return true;
            }
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something wrong with your input");
            return false;
        }
    }

    public boolean joinGroup(DataOutputStream output, DataInputStream input, String groupName, String groupPass,
                             Map<String, List<String>> twitterPostMap) {
        // join_group:groupname:grouppassword:username:user_password
        String sendToCentral = "join_group" + utils.SEPARATOR + groupName + utils.SEPARATOR +
                groupPass + utils.SEPARATOR + username + utils.SEPARATOR + userpass;
        try {
            output.writeUTF(sendToCentral);
            String responseFromCentral = input.readUTF();
            if (!"SUCCESS".equalsIgnoreCase(responseFromCentral.split(":")[0])) {
                return false;
            } else {
                System.out.println("You have successfully joined a group");
                //central return active account list
                System.out.println("Pulling posts in this group...");
                Integer oneActivePeerPort = Integer.valueOf(input.readUTF());
                String[] allPosts = getPostsFromPeer(oneActivePeerPort, groupName);
                List<String> posts = new LinkedList<>();
                if (allPosts != null) {
                    for (String post : allPosts) {
                        posts.add(post);
                    }
                    twitterPostMap.put(groupName, posts);
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String[] getPostsFromPeer(Integer port, String groupName) {
            if (port != this.selfPort) {
                try {
                    Socket socket = new Socket("localhost", port);
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    String prompt = "GET_ALL_POSTS:" + groupName;
                    output.writeUTF(prompt);
                    String result =input.readUTF();
                    if (!"FAILED".equalsIgnoreCase(result)) {
                        String[] allPosts = result.split(":");
                        return allPosts;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return null;
    }

    /**
     * todo: create group should return a boolean indicating if this attempt succeeds
     *
     * Create a new group
     * and then call join the group method to join the group
     * @param groupName
     * @param groupPass
     */
    public boolean createGroup(DataOutputStream outputStream, DataInputStream inputStream, String groupName, String groupPass) {
        //report group name, group id, and self port number to the central server
        // create server port for others to connect

        //format: create:groupname:grouppass:username:userpass
        StringBuilder reportToCentral = new StringBuilder();
        reportToCentral.append(utils.CREATE_GROUP);
        reportToCentral.append(":");
        reportToCentral.append(groupName);
        reportToCentral.append(":");
        reportToCentral.append(groupPass);
        reportToCentral.append(":");
        reportToCentral.append(this.username);
        reportToCentral.append(":");
        reportToCentral.append(this.userpass);
        // System.out.println("current username" + this.username);

        try {
            outputStream.writeUTF(reportToCentral.toString());
            String res = inputStream.readUTF();
            String expected = String.format(
                    "The request to create a group with name %s succeeds!", groupName);
            if (expected.equalsIgnoreCase(res)) {
                return true;
            } else {
                return false;
            }

        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(DataOutputStream outputStream, DataInputStream inputStream,
                            String username, String userpass) {
        try {
            //send request to server
            String req = "register" + utils.SEPARATOR + username +  utils.SEPARATOR + userpass;
            outputStream.writeUTF(req);
            // red respond from server
            String res = inputStream.readUTF();
            if (res.equals("Successfully created user!")) {
                setUsername(username);
                setUserpass(userpass);
                return true;
            }
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something wrong with your input");
            return false;
        }
    }

    public boolean logout(DataOutputStream outputStream, DataInputStream inputStream) {
        try {
            //send request to server
            String req = "logout" + utils.SEPARATOR + this.username + utils.SEPARATOR + this.userpass;
            outputStream.writeUTF(req);
            // red respond from server
            String res = inputStream.readUTF();
            if (res.equals("Successfully logged user out")) {
                setUsername(null);
                setUserpass(null);
                return true;
            }
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something wrong with your input");
            return false;
        }
    }

    public void sendPost(DataOutputStream outputStream, DataInputStream inputStream, String groupName, String postInformation) {
        // step 1: get active members
        // step 2: send post to all active members in the group
        String[] activePeers = getActivePeers(outputStream, inputStream, groupName);
        for (String port: activePeers) {
            Integer portToConnect = Integer.parseInt(port);
            try {
                if (!portToConnect.equals(this.selfPort)) {
                    // open socket for peers and send post information
                    Socket socket = new Socket("localhost", portToConnect);
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF("POST:" + groupName + ":" + postInformation);
                    System.out.println("This post: " + postInformation);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getActivePeers(DataOutputStream outputStream, DataInputStream inputStream, String groupName) {
        String requestActiveMem = "check_active_user_list:" + groupName;
        try {
            outputStream.writeUTF(requestActiveMem);
            String allActive = inputStream.readUTF();
            return allActive.split(":");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    // get all posts within a group
    // first ask the central server to get the list of all current active users
    // then send one user a message to get all the posts in this group
    public List<String> getPosts(String groupName, Map<String, List<String>> twitterPostMap) {
        return twitterPostMap.get(groupName);
    }
}
