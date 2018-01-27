# **Dependencies** 

## MySql

We are using the NTNU database service to save information in a MySQL database, which only works on an NTNU network. If you want to try the program, you'll have to set up your own database and replace the DB connection code. 
Currently username and passwords are saved to the database.

### Drivers

We downloaded extra drivers for mysql.
To get the project to work with these drivers the mysql .jar needs to be added in the Java Build Path under project properties. Unless something has gone wrong with the pull, this should already be done in the downloaded project, so no need to do it yourself.

# **The Code**

## Main methods
The main method for the client application is located in the Client.java file. For server it is located in the Server.java file.

## Data storage
![How the server stores data in memory and permanently](https://bitbucket.org/repo/daRbxoA/images/4161267439-DataStorage.png)

### Memory

While the server is running, it stores in its memory three arraylists containing the classes called ClientInfo, ChatInfo, and GameInfo. The ClientInfo array holds the information the server requires to remember for every currently logged in client. The GameInfo does the same for every ongoing game, and the ChatInfo for every ongoing chatroom. Note that GameInfo and ChatInfo also keeps their own ArrayList of connected clients, and that there is always 1 ChatInfo, which is that of the global main chat that all connected users are logged in to.

### Permanent

The permanent storage only consists of text files whose names coincide with the name of chatroom. Game chats are logged by ID. Within each file is a line for every chat message sent across the network through the given chat room.

## Network Communication

Our server and clients communicates by sending packets preceded by tags. These are used so the server/client can know what kind of content to expect from the packet and how to handle it. Each tag consists of a major tag (User, Chat, Ludo), and is followed by a subtag within that group, and then followed by the content of the message (if any).

### Server to client tag content
* User
    * Login: The result of an attempt by a client to connect to the server
    * Register: The result of an attempt by a client to register a new user
    * Logout: The result of an attempt by a client to log out
    * List: A name to add to the challenge list
    * Wins: Number of wins to add to win counter

* Ludo
    * Dice: The resulting dice from a successful dice throw by active player
    * Piece: The new location of a piece that was successfully moved by active player
    * Player: The new state of a player whose state changed
    * Name: The name of a player in the given ludo game (Sent at start of game, when someone joins, leaves or wins)
    * Challenge: The username of the challenging player that challenge thee
    * ChallengeConfirm: The result of a challenge game validation
    * ChallengeTimedOut: Empty message that signals that challenge timed out
    * JoinRandom: The game data for a random game created after the queue filled up
    * Join: The game data for a game the client requested to join
    * RandomSuccess: Empty message that signals the client was added to the random game queue
    * Chat: The content of a say message
* Chat
    * Say: The content of a message sent through a chat room
    * Join: The data for a chat room that client was able to join
    * Name: The name of a user in the chat room
    * RemoveName: The name of a user that left the chat room
    * ListName: The name of a chat room to add to chat list
    * Create: The result of creating a chat and its attributes

### Client to server tag content
* User
    * Login: A username and password a client attempted to login with
    * Register: A username and password a client attempted to register
    * Logout: Empty packet from a client that explicitly wants to log out
    * List: Request to recieve usernames of all connected clients
* Ludo
    * Throw: The ID of a game for which a player tried to throw a dice in
    * Move: The gameID, playerindex, from tile and to tile for a ludo piece move
    * JoinRandom: Empty packet from a client that wants to be added to the random game queue
    * Challenge: Request from player to challenge other players
    * ChallengeConfirm: The result from a client confirm
    * ChallengeValidation: Whether game has enough players to start and player names of accepting challengers  
    * Init: A request from a client to resend all relevant game info to all the clients of an unstarted game
    * Leave: Game ID of game the client wants to leave
    * Chat: The game ID of game to chat with and its message content
* Chat
    * Join: The name of a chat channel a user wants to join
    * Create: The name of a chat channel a user wants to create
    * List: An empty packet from a client that wants the name of all current chat rooms
    * Say: The ID of a chat room and the content the client wants to send to it
    * Leave: The ID of a chat room the client wants to leave
    * Init: The ID of a chat room the client wants to receive all relevant info for
    * InitGlobal: Request to join global chat

## Structure

### Server
![The servers multithreading structure](https://bitbucket.org/repo/daRbxoA/images/2828161834-ServerStructure.png)

**The GUI Thread**
is handled through JavaFX and is spoken to by the rest of the program through its controller. Whenever one of the other threads changes something about the information the GUI displays, it performs a runLater update on the GUI.

**The Connection Thread**
simply uses the serverSocket to continually listen for connection requests. Since clients always send their username and password directly after a connection attempt, the thread listens for a packet immediately after the a connection request happens. It then responds to the client with a message telling of how the result of the SQL query and depending on the result potentially adds the client as a new ClientInfo to the servers list of connected users.

**The Client Input Thread**
loops through the connected clients list, and, if their input stream has bytes available to read, reads the incoming bytes. It then splits it into separate packets, and for each packet it checks if its major tag is a valid one. If it is, it queues the packet up for handling in the corresponding handler threads.

**The User, Ludo and Chat Threads**
block themselves until they have work to do, and when they have work to do they handle their packets by checking for valid subtags and then handling the packets accordingly. For some packets, they will also queue up a reply in the Send thread.

**The Challenge Timout Thread**
blocks until a challenge has timed out. A challenge notifies this thread if it has lived longer than 30 seconds. Before the challenge is removed all the users are notified with packets that tells them to close their local challenge windows. Then the challenge is removed from the ludotask challenges list. 

**The Send Thread**
blocks until it has work to do. When it has work to do, it simply parses the clientID off of the message and sends the rest of the message to the connected client corresponding with the found ID, if any. Whenever it gets a Logout packet (originating from the User Thread), it will after sending the message also queue up work for the Cleanup Thread to remove the user.

**When the Cleanup Thread**
has work it waits until all other threads are finished reading from the list of connected clients, and then removes from the connected clients list the client that it has been tasked with removing.

### Client
![The client multithreading structure](https://bitbucket.org/repo/daRbxoA/images/913381503-ClientStructure.png)

**The GUI Thread**
is handled through JavaFX and is spoken to by the rest of the program through its various controllers. As the GUI will display several tabs and windows over its lifetime, there are plenty of controllers and functions for getting the correct one when the other threads needs to communicate with the GUI. Whenever something communicates with the GUI, it does so by doing a runLater on functions in the relevant controller. This thread does most of the clients packet sending to server, and it does this through a generic static sending function located in the Client class itself.

**The Network Thread**
simply blocks until it finds something to read from its input stream. When it finds something, it splits whatever bytes it read into separate packets and then checks for a valid major tag for each one. If it finds one it queues up the rest of the message as work for the corresponding thread.

**The User, Ludo and Chat Threads**
block until they are handed some work to do through the Network Thread. When they get work, they check for a valid subtag and if they find one they handle it with accordingly with separate methods. Sometimes it is necessary to reply back to server after handling a received packet, and if so this is done via the static send function in the Client class just like with the GUI thread.
