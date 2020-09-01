import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javafx.util.Pair;

import java.io.IOException;

public class ClientMain {

    private static final String centralAddress = "localhost";
    private static final Integer centralPort = 1234;
    // key is the name of the group, value is a Pair
    // the key in the pair is the hostname, value is the portnumber
    private static Map<String, Pair<String, Integer>> allMemberConnections;
    private static Map<String, List<String>> twitterPostMap = new HashMap<>();
    // private static Set<TwitterPost> twitterPostSet = new HashSet<>();
    private static Set<String> groupList = new HashSet<>();

    public ClientMain() {

    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please enter a address and a port number. ");
        }

        // port and address for server port (for connection of other clients)
        String address = args[0];
        Integer port = Integer.parseInt(args[1]);

        //create server socket for other clients to connect
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        new Thread(new WorkRunnable(serverSocket, twitterPostMap)).start();

        new Thread(new ConnectWithClient(serverSocket, twitterPostMap)).start();
        //Get options from client
        //5 options available:
        // - register
        // - login
        // - logout
        // - create group
        // - join group

        Client client = new Client(address, port);
        boolean loggedIn = false;

        Scanner inputScanner = new Scanner(System.in);
        while (true) {

            // connect to central server
            if (!loggedIn) {
              System.out.println("You have not logged in! What do you want to do? Action list: \n" +
                  "- REGISTER\n" +
                  "- LOGIN\n");
            } else {
              System.out.println("You have logged in! What do you want to do? Action list: \n" +
                  "- CREATE_GROUP\n" +
                  "- JOIN_GROUP\n" +
                  "- POST\n" +
                  "- PULL\n" +
                  "- LOGOUT\n");
            }

            String action = inputScanner.nextLine();
            if (!loggedIn) {
              if (ClientAction.REGISTER.toString().equalsIgnoreCase(action)) {
                System.out.println("Please enter a username: ");
                String username = inputScanner.nextLine();
                System.out.println("Please enter a user password: ");
                String userpass = inputScanner.nextLine();
                Connection connToCentral = client.connectToCentralServer(centralAddress, centralPort);
                // send user name and password to central
                boolean registerStatus = client.register(connToCentral.getOutputStream(), connToCentral.getInputStream(),
                    username, userpass);
                if (registerStatus) {
                  System.out.println("You have successfully registered an account!");
                } else {
                  System.out.println("The provided username has already been registered!");
                }
              } else if (ClientAction.LOGIN.toString().equalsIgnoreCase(action)) {
                System.out.println("Please enter a username: ");
                String username = inputScanner.nextLine();
                System.out.println("Please enter a user password: ");
                String userpass = inputScanner.nextLine();
                Connection connToCentral = client.connectToCentralServer(centralAddress, centralPort);
                boolean loginStatus = client.login(connToCentral.getOutputStream(), connToCentral.getInputStream(), username, userpass);
                if (loginStatus) {
                  loggedIn = true;
                  System.out.println("You have successfully logged in!");
                } else {
                  System.out.println("The provided credentials does not match our record. "
                      + "Please try again!");
                }
              } else {
                System.out.println("Please create an account or login first");
              }
            } else {
              if (ClientAction.CREATE_GROUP.toString().equalsIgnoreCase(action)) {
                System.out.println("Please enter the group name you want to create: ");
                String groupName = inputScanner.nextLine();

                System.out.println("Please create a password for your group ");
                String groupPass = inputScanner.nextLine();

                Connection connToCentral = client.connectToCentralServer(centralAddress, centralPort);
                // send request in the format of create:groupname:grouppass:username:userpass to the central server
                boolean success = client.createGroup(
                    connToCentral.getOutputStream(), connToCentral.getInputStream(), groupName, groupPass);
                // Then call reportSelfInfo to report its server port and address to central server
                if (success) {
                    groupList.add(groupName);
                    twitterPostMap.put(groupName, new ArrayList<>());
                } else {
                  System.out.println("Failed to create a group with name " + groupName);
                }
              } else if (ClientAction.LOGOUT.toString().equalsIgnoreCase(action)) {
                Connection connToCentral = client.connectToCentralServer(centralAddress, centralPort);
                boolean logoutStatus = client.logout(
                    connToCentral.getOutputStream(), connToCentral.getInputStream());
                if (logoutStatus) {
                  loggedIn = false;
                  System.out.println("You have successfully logged out!");
                } else {
                  System.out.println("Illegal Action!");
                }
              } else if (ClientAction.JOIN_GROUP.toString().equalsIgnoreCase(action)) {
                System.out.println("Please enter the group name you want to join: ");
                String groupName = inputScanner.nextLine();
                System.out.println("Please enter the password of the group you want to join: ");
                String groupPass = inputScanner.nextLine();
                //send group name and group password to the central server, central server return boolean to accept the member
                Connection connToCentral = client.connectToCentralServer(centralAddress, centralPort);
                boolean acceptNewMember = client
                    .joinGroup(connToCentral.getOutputStream(), connToCentral.getInputStream(),
                            groupName, groupPass, twitterPostMap);
                if (acceptNewMember) {
                  groupList.add(groupName);
                } else {
                  System.out.println("You have entered a wrong group name or password.");
                }
              } else if (ClientAction.POST.toString().equalsIgnoreCase((action))) {
                // get active users from the central server
                // send twitter post to twitterPostSet
                if (groupList.size() > 0) {
                    System.out.println("Please enter the group name which you want to make a post in:");
                    String groupName = inputScanner.nextLine();
                    while (!groupList.contains(groupName)) {
                    System.out.println("It seems that you are not the member of the group, "
                        + "please choose from the following groups");
                    for (String name : groupList) {
                      System.out.println(name);
                    }
                    groupName = inputScanner.nextLine();
                  }
                  System.out.println("Please enter the post information:");
                  String postInformation = inputScanner.nextLine();
                  Connection connToCentral = client.connectToCentralServer(centralAddress, centralPort);
                  client.sendPost(connToCentral.getOutputStream(), connToCentral.getInputStream(), groupName, postInformation);
                  twitterPostMap.get(groupName).add(postInformation);
                }else { System.out.println("No group yet");}

              } else if (ClientAction.PULL.toString().equalsIgnoreCase(action)) {
                if (groupList.size() == 0) {
                  System.out.println("You are not members of any group! Please join a group first!");
                } else {
                  System.out.println("Please enter the group name you want to pull all post: ");
                  String groupName = inputScanner.nextLine();
                  while (!groupList.contains(groupName)) {
                    System.out.println("It seems that you are not the member of the group, "
                        + "please choose from the following groups");
                    for (String name : groupList) {
                      System.out.println(name);
                    }
                    groupName = inputScanner.nextLine();
                  }
                  List<String> allPosts = client.getPosts(groupName, twitterPostMap);
                  if (allPosts.size() == 0) {
                    System.out.println("There are no posts in this group!");
                  } else {
                    System.out.println("All the posts from group " + groupName);
                    for (int i = 0; i < allPosts.size(); i++) {
                      System.out.println(i + ". " + allPosts.get(i));
                    }
                    System.out.println();
                  }
                }
              } else {
                  System.out.println("You have entered a wrong action, please enter again!");
              }
            }
        }
    }
}
