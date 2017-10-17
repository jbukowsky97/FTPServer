import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

class FTPClient {

    private static final String ROOT_PATH = System.getProperty("user.dir") + "/root/";


	/* Method name: createDataConnection */
	/* Arguments:	controlPort (int): control port for the TCP data connection */
 	/*		outToServer (DataOutputStream): data output stream for the TCP control connection */
	/*		inFromServer (BufferedReader): data input stream for the TCP control connect */
	/*		sentence (String): message to be sent to the server */
	/* Returns: dataSocket (Socket) */
	/* Usage: creates a TCP data connection on the given port number (controlPort) and */
	/* 		returns a socket */
	private Socket createDataConnection(int controlPort, DataOutputStream outToServer, BufferedReader inFromServer, String sentence) {

		Socket dataSocket = null;

		try {
			int dataPort = controlPort + 2;
			outToServer.writeBytes(dataPort + " " + sentence + "\n");

			String statusCode = "";
			while ((statusCode = inFromServer.readLine()).equals("")) {
				System.out.println();
			}

			if (!statusCode.contains("200")) {
				System.out.println("File not present on remote server");
			} else {
				System.out.println("Working");
				ServerSocket welcomeData = new ServerSocket(dataPort);
				dataSocket = welcomeData.accept();
				welcomeData.close();
			}

		} catch (Exception e) {
			System.out.println("Data connection cannot be established");
		} finally {
			return dataSocket;
		}
	}


	/* Method name: list
	/* Arguments:	controlPort (int): control port for the TCP data connection */
 	/*		outToServer (DataOutputStream): data output stream for the TCP control connection */
	/*		inFromServer (BufferedReader): data input stream for the TCP control connect */
	/*		sentence (String): message to be sent to the server */
	/* Usage: 		 */
	private void list(int controlPort, DataOutputStream outToServer, BufferedReader inFromServer, String sentence) {
		try {
			Socket dataSocket = createDataConnection(controlPort, outToServer, inFromServer, sentence);
			BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

			/* Wait while the data is not ready for transmission */
			while (!inData.ready())
				;

			StringBuffer response = new StringBuffer();
			String msg;

			/* Receive the message until encountering the end of the file "eof" */
			while (!((msg = inData.readLine()).equals("eof"))) {
				response.append(msg + "\n");
			}

			/* Print the response (the list of files) on the client side */
			System.out.println(response.toString().trim());

			/* Close the data sockets*/
			inData.close();
			dataSocket.close();
		} catch (Exception e) {
			System.out.println("ERROR: list failed");
		}
	}

	/* Method name: retr
	/* Arguments:	controlPort (int): control port for the TCP data connection */
 	/*		outToServer (DataOutputStream): data output stream for the TCP control connection */
	/* 		inFromServer (BufferedReader): data input stream for the TCP control connect */
	/*		sentence (String): message to be sent to the server */
	/* Usage: retrieve a file from a server and store it on the client */
	private void retr(int controlPort, DataOutputStream outToServer, BufferedReader inFromServer, String sentence) {
        StringTokenizer tokens = new StringTokenizer(sentence);
        tokens.nextToken();
		String fileName = tokens.nextToken();
		try {
			Socket dataSocket = createDataConnection(controlPort, outToServer, inFromServer, sentence);	
			BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

			/* Wait while the data is not ready for transmission */
			while (!inData.ready())
				;

			StringBuffer response = new StringBuffer();
			String msg;

			/* Receive the message until encountering the end of the file "eof" */
			while (!((msg = inData.readLine()).equals("eof"))) {
				response.append(msg + "\n");
			}

			/* Create a new file if it does NOT exist based on user input */
			File newFile = new File(ROOT_PATH + fileName);
			newFile.createNewFile();

			/* Move the data from the servers response onto the new file */
			PrintStream out = new PrintStream(new FileOutputStream(ROOT_PATH + fileName));
			out.print(response.toString());

			/* Close the data sockets*/
			inData.close();
			dataSocket.close();
			System.out.println("retr successful");
		} catch (Exception e) {
			System.out.println("ERROR: retr failed");
		}
	}
	/* Method name: stor */
	/* Arguments:	controlPort (int): control port for the TCP data connection */
 	/*		outToServer (DataOutputStream): data output stream for the TCP data connection */
	/* 		inFromServer (BufferedReader): data input stream for the TCP control connect */
	/*		sentence (String): message to be sent to the server */
	/* Usage: 		store a file from the client onto the server */
	private void stor(int controlPort, DataOutputStream outToServer, BufferedReader inFromServer, String sentence) {
		StringTokenizer tokens = new StringTokenizer(sentence);
		tokens.nextToken();
		String fileName = tokens.nextToken();

		DataOutputStream dataOutToServer;
		BufferedReader inputStream;

		try{
			Socket dataSocket = createDataConnection(controlPort, outToServer, inFromServer, sentence);
			dataOutToServer = new DataOutputStream(dataSocket.getOutputStream());

			//read the file
			inputStream = new BufferedReader(new FileReader(ROOT_PATH + fileName));

			String count;

			//send to the server
			while ((count = inputStream.readLine()) != null) {
				dataOutToServer.writeBytes(count + "\n");
			}

			//close input stream
			inputStream.close();

			dataOutToServer.writeBytes("eof");
			dataOutToServer.close();
			dataSocket.close();
			System.out.println("stor succesful");
		} catch (Exception e) {
			System.out.println("ERROR: stor failed");
		}
	}

	/* Method name: quit */
	/* Arguments:	controlPort (int): control port for the TCP data connection */
 	/*				inFromServer (DataInputStream): data input stream for the TCP data connection */
	/*				sentence (String): message to be sent to the server */
	/* Usage: 		safely exit the client */
	private void quit(DataOutputStream outToServer) {
		try {
			/* Passing quit command and a dummy port to avoid server issues */
			outToServer.writeBytes("0" + " " + "quit");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("exiting...");
		System.exit(0);
	}

	/* Method name: FTPClient */
	/* Usage: Interface for the FTP client commands */
	public FTPClient() {
		String modifiedSentence;
		boolean isOpen = true;

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String sentence = null;

		boolean connected = false;
		int port = 0;
		Socket controlSocket = null;

		/* While we are NOT connected to a server*/
		while (!connected) {
			try {
				sentence = inFromUser.readLine().toLowerCase();
				/* If the client sends the command "connect" */
				if (sentence.startsWith("connect")) {
					StringTokenizer tokens = new StringTokenizer(sentence);

					if (tokens.countTokens() != 3) {
					    System.out.println("wrong number of parameters\n\t'connect <ip/hostname> <port>' or 'quit' to exit");
					    continue;
                    }
					/* Skip the connect token */
					tokens.nextToken();

					/* Receive the server name and port number and 
					 * load them into separate identifiers */
					String serverName = tokens.nextToken();
					port = Integer.parseInt(tokens.nextToken());

					/* Create a control socket based on that server name and port */
					controlSocket = new Socket(serverName, port);

					/* <Server name> is connected on port <port number> */
					System.out.println(serverName + " connected on port " + port);
					connected = controlSocket.isConnected();
					/* If user enters the "quit" command */
				} else if (sentence.startsWith("quit")) {
					System.out.println("exit...\n");
					System.exit(0);
				} else {
					/* If user is not connected to a server and is entering input */
					System.out.println(
							"You are not connected to a server, try\n\t'connect <ip/hostname> <port>' or 'quit' to exit");
				}
			} catch (IOException e) {
				System.out.println("ERROR: connection failed");
				System.exit(5);
			}
			System.out.println();
		}

		DataOutputStream outToServer = null;
		BufferedReader inFromServer = null;
		
		/* Attempt to set up data streams for the server to connect on */
		try {
			outToServer = new DataOutputStream(controlSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("ERROR: could not setup data streams");
			System.exit(5);
		}

		try {
			/* While the connection is open */
			while (isOpen) {

				/* Reads commands from the client until "quit" is entered */
				sentence = inFromUser.readLine();
				System.out.println("");

				if (sentence.toLowerCase().equals("list")) {
					list(port, outToServer, inFromServer, sentence);
				} else if (sentence.toLowerCase().startsWith("retr ") && sentence.split(" ").length == 2) {
					retr(port, outToServer, inFromServer, sentence);
				} else if (sentence.toLowerCase().startsWith("stor ") && sentence.split(" ").length == 2) {
					stor(port, outToServer, inFromServer, sentence);
				} else if (sentence.toLowerCase().equals("quit")) {
					quit(outToServer);
				} else {
					System.out.println("unrecognized command or wrong number of parameters\nusage:\n\tlist\n\t\tget list of files\n\tretr <filename>\n\t\tretrieve file with name provided\n\tstor <filename>\n\t\tstore file with name provided\n\tquit\n\t\tterminate connection");
				}

				System.out.println();
			}
		} catch (IOException e) {
			System.out.println("ERROR: error occurred while connected");
			System.exit(5);
		}
	}

	/* Main Method */
	public static void main(String argv[]) throws Exception {
		new FTPClient();
	}
}
