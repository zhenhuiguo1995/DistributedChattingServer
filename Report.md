# Report 

**Introduction**

Our group implemented a peer-to-peer, anonymous twitter project. The core functions of our project are listed as follows:

- A user can register an account by providing a user-name and password
- A user can login by providing a user name and password
- After logged in, a user can:
  - create a new group of topic by setting the group name and password
  - join an existing group by providing the group name and password
  - sending post in a group which the user has joined by providing the group name and the post information
  - viewing all posts in a group which the user has joined.

Further, one feature this twitter project has is that it is highly anonymous. 

- Once there's no active user within a group, the group will be dismissed, and no one else can retrieve any posts in this group
- Posts(messages) within a group does not come with the name tag, meaning a user does not have any idea which user posted that message



**Component level design:** 

we mainly have two components in the project: a CentralServer and a Client

- CentralServer: the central server is responsible to provide centralized services, including registering an account, logging into an account, a user creating a group, a user joining a group, logging out of an account and checking active members of a groups. Further, the central server essentially provides interfaces to micro-services, since each service is provided in a thread. 



- Client: client mainly has two functions

  - interacting with the user: the client will continuously ask the user to provide an operation through the command line. The client will also verify the correctness of the input, and if an input is illegal, will ask the user to provide a legal input

  - interacting with other users, this include two parts:

    - when a user sends a post, it first gets the active user list from the central server, then directly sends the message to all other clients within the specified group

    

    - when a user joins a group, or tries to get all posts within a group, it asks the central server for an active group member information, and then sends the request to an active member of the group. The receiver, upon receiving the request, will reply all the posts in the group to the sender



**Algorithms involved**

Here are some core algorithms which we have implemented in our project:

- Time and clocks: We made an abstraction and view the posts as events in the distributed system. Since posts in real twitter have a time attribute, we also want to make sure all posts are correctly ordered. We assign a timestamp to each posts within a group, and we arrange the order of posts in a group by the timestamp.
- Group communication: group communication happens when a client is sending a post within a specified group. The client first gets a list of all active users within the group from the central server, and the do a broadcasting to send posts to all members of the group
- Peer to peer network: our client works both in group communication and peer to peer network. When a client tries to get all posts in a group, it communicates with one active user within the group to get all the posts, which is peer to peer communication
- Managing replicated data: Since this is a peer to peer, decentralized twitter system, we are using clients to store the posts information. Each client maintains a database of all posts of the groups the client has joined. Besides, for each group, all clients have the same data about the posts. We are making data consistent by forcing the client to sync whenever it joins a group 
- Mutual exclusion: high concurrency may occur on the central server, when a lot of clients are making requests to the server. We handle this by routing the requests to services which are initialized in a new thread, which is sort of a micro-service. Also, we are using locks and thread-safe data structures to make sure requests to modify the same data structure will succeed: for example, we use a Vector(which is thread safe) to record all registered user information, this can handle the situation where two users registers at the same time.



**How to run our code**

Start the server:

java -jar server.jar  

Start the client(change the local port number if you want to start more than 3 clients): 

java -jar client.jar localhost 1111

java -jar client.jar localhost 2222

java -jar client.jar localhost 3333


[Test video demo link](https://drive.google.com/file/d/18VJaLSm7Fn1tx2EZ4gtkAmO6of9mXlXr/view?usp=sharing)

Testing cases:

- Start the server and 3 clients
- Register 3 clients and login 3 clients
- Client 1(wanjia) create a group Twitter, and post 1 message.
- Client 2(zhenhui) join the group Twitter and pull all messages, he can now see the first message client 1 posted prior to him joining the group.
- Client 2 post another message to the group Twitter, client 1 pull all messages, and can see both messages. This shows the replication works well.
- Client 3(yue) create another group Facebook, and post 1 message to the Facebook group.
- Client 2 joins the Facebook group and could see all the messages in the group. He then post a message to the Facebook group. Now client 2 belongs to both Twitter group and Facebook group. d
- Client 1 join the Facebook group and pull all messages. Now Cilent 1 belongs to both twitter group an facebook group. We then run pull opertions on both groups and can see they both work well. This shows the replication works across all clients and groups and they maintain a serparation of messages according to group name.
- Clients 1, 2 ,3 log out. 
- Using the same terminal and log in a new user named testuser and it was also successful.
- Also tested cases when commands the users gave contains typos. The commandline could recognize them and prompt users to enter again. 


