import java.io.BufferedReader;
import java.io.FileReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;
import java.io.*;


public class ServerThread extends Thread {

	private Socket connectionSocket;
	private DataOutputStream outToClient;
	private BufferedReader inFromClient;

	private static final String OK = "200 command ok";
	private static final String NO_FILE = "550 command failed";

	private static final String ROOT_PATH = System.getProperty("user.dir") + "/root/";

	/* Method name: ServerThread */
	/* Arguments: 	connectionSocket (Socket): the socket on which the client will connect */
	/* Usage: set up data streams for data transfer between server and client on a thread */
	public ServerThread(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
		outToClient = null;
		inFromClient = null;
		try {
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("ERROR: could not set up data streams");
		}
	}

	/*
 	* Method name: createDataConnection
 	* Arguments:	dataPort (int): port to create data connection on
 	* 		sendOK (boolean): whether requested command successful and OK message should
 	* 			be sent.
 	* Usage: responds to client with proper message and creates data connection
 	* Return: Newly create data Socket or null if sendOK false
  	*/
	private Socket createDataConnection(int dataPort, boolean sendOK) {
		Socket dataSocket = null;

		// Tell the client if the command was successful
		try {
			if (!sendOK) {
				outToClient.writeBytes(NO_FILE);
				return null;
			} else {
				outToClient.writeBytes(OK);
			}
		} catch (Exception e) {
			return null;
		}

		// Establish data connection with client
		while (dataSocket == null || !dataSocket.isConnected()) {
			try {
				dataSocket = new Socket(connectionSocket.getInetAddress(), dataPort);
			} catch (Exception e) {
				System.out.println("Not connected");
			}
		}

		// Return the new data connection
		return dataSocket;
	}

	/* Method name: Run */
	/* Usage: Thread handler, contains logic for commands */
	public void run() {
		boolean running = true;
		try {
			/* While the thread is running */
			while (running) {
				/* Wait until client data stream is ready */
				while (!inFromClient.ready())
					;

				/* Continue to read client commands until "quit" is entered */
				String fromClient = inFromClient.readLine();
				System.out.println("client request: " + fromClient);
				StringTokenizer tokens = new StringTokenizer(fromClient);

				/* Extract the port and client command from the message sent from the client */
				int port = Integer.parseInt(tokens.nextToken());
				String clientCommand = tokens.nextToken();

				if (clientCommand.equals("list")) {
					list(connectionSocket, port);
				} else if (clientCommand.equals("retr")) {
					String fileName = tokens.nextToken();
					retr(connectionSocket, port, fileName);
				} else if (clientCommand.equals("stor")) {
					String fileName = tokens.nextToken();
					stor(connectionSocket, port, fileName);
				} else if (clientCommand.equals("quit")) {
					quit(connectionSocket);
					running = false;
				}
			}
		} catch (Exception e) {
			/* If an exception is caught print the stack trace */
			e.printStackTrace();
		} finally {
			/* client <IP address> disconnected */
			System.out.println("client " + connectionSocket.getInetAddress() + " disconnected\n");
		}
	}

	/* Method name: list */
	/* Arguments: 	connectionSocket (Socket): the socket over which we will send data */
	/*				port (int): the port over which we will connect */
	/* Usage: send a list of files on the server to the client */
	private void list(Socket connectionSocket, int port) {
		Socket dataSocket;
		DataOutputStream dataOutToClient;
		try {
			/* the user's working directory is the root path */
			File folder = new File(ROOT_PATH);
			File[] listOfFiles = folder.listFiles();

System.out.println("Test1");
			dataSocket = createDataConnection(port, true);

			// Return if no data connection to work with
			if (dataSocket == null) {
				return;
			}

			dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
System.out.println("Test2");
			/* Transmit the list of file names */
			for (File f : listOfFiles) {
				if (f.isFile()) {
					dataOutToClient.writeBytes(f.getName() + "\n");
				}
			}
System.out.println("Test3");
			/* End of the file "eof" */
			dataOutToClient.writeBytes("eof");
			dataOutToClient.close();
			dataSocket.close();
		} catch (Exception e) {
			System.out.println("ERROR: list failed");
		}
	}

	/* Method name: retr */
	/* Arguments: 	connectionSocket (Socket): the socket over which we will send data */
	/*				port (int): the port over which we will connect*/
	/*				fileName (String): the desired file name */
	/* Usage: retrieve a file from the server and send it to the client */
	private void retr(Socket connectionSocket, int port, String fileName) {
		Socket dataSocket;
		DataOutputStream dataOutToClient;
		BufferedReader inputStream;

		try {
			File folder = new File(ROOT_PATH);
			File[] listOfFiles = folder.listFiles();

			boolean fileExists = false;
			for (File f: listOfFiles) {
				if (f.getName().equals(fileName)) {
					fileExists = true;
					break;
				}
			}

			if (!fileExists) {
				System.out.println("Test no file");
				outToClient.writeBytes(NO_FILE);
			} else {
				System.out.println("Test file found");
				outToClient.writeBytes(OK);
				dataSocket = new Socket(connectionSocket.getInetAddress(), port);
				dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
			
				/* Create an input stream based on the given file path */
				inputStream = new BufferedReader(new FileReader(ROOT_PATH + fileName));

				String count;

				/* While the stream does not equal null, send each line to the client */
				while ((count = inputStream.readLine()) != null) {
					dataOutToClient.writeBytes(count + "\n");
				}

				/* Close the input stream */
				inputStream.close();

				dataOutToClient.writeBytes("eof");
				dataOutToClient.close();
				dataSocket.close();
			}
		} catch (Exception e) {
			System.out.println("ERROR: retr failed");
		}
	}

	/* Method name: stor */
	/* Arguments: 	connectionSocket (Socket): the socket over which we will send data */
	/*				port (int): the port over which we will connect*/
	/*				fileName (String): the desired file name */
	/* Usage: store a file from the client onto the server */
	public void stor(Socket connectionSocket, int port, String fileName) {

		try {
			Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
			BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

			//wait for the data to be ready
			while (!inData.ready())
				;

			StringBuffer response = new StringBuffer();
			String msg;

			while (!((msg = inData.readLine()).equals("eof"))) {
				response.append(msg + "\n");
			}

			//create a new file
			File newFile = new File(ROOT_PATH + fileName);
			newFile.createNewFile();

			//move data from client to server file
			PrintStream out = new PrintStream(new FileOutputStream(ROOT_PATH + fileName));
			out.print(response.toString());

			inData.close();
			dataSocket.close();
			System.out.println("File stored from: " + connectionSocket.getInetAddress());
		} catch (Exception e) {
			System.out.println("ERROR: stor failed");
		}
	}

	/* Method name: quit */
	/* Arguments: 	connectionSocket (Socket): the socket over which we will send data */
	/* Usage: quit the server thread safely */
	private void quit(Socket connectionSocket) {
		try {
			connectionSocket.close();
		} catch (Exception e) {
			System.out.println("ERROR: quit failed");
		}
	}
}
