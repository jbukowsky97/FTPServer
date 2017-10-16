import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

class FTPClient {

	private Socket createDataConnection(int controlPort, DataOutputStream outToServer, String sentence) {

		Socket dataSocket = null;

		try {
			int dataPort = controlPort + 2;
			ServerSocket welcomeData = new ServerSocket(dataPort);
			outToServer.writeBytes(dataPort + " " + sentence + "\n");
			dataSocket = welcomeData.accept();
			welcomeData.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return dataSocket;
		}
	}

	private void list(int controlPort, DataOutputStream outToServer, String sentence) {
		try {
			Socket dataSocket = createDataConnection(controlPort, outToServer, sentence);
			BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

			while (!inData.ready())
				;

			StringBuffer response = new StringBuffer();
			String msg;

			while (!((msg = inData.readLine()).equals("eof"))) {
				response.append(msg + "\n");
			}

			System.out.println(response.toString().trim());

			inData.close();
			dataSocket.close();
		} catch (Exception e) {
			System.out.println("ERROR: list failed");
		}
	}

	private void retr(int controlPort, DataOutputStream outToServer, String sentence) {
		try {
			Socket dataSocket = createDataConnection(controlPort, outToServer, sentence);
			BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

			while (!inData.ready())
				;

			StringBuffer response = new StringBuffer();
			String msg;

			while (!((msg = inData.readLine()).equals("eof"))) {
				response.append(msg + "\n");
			}

			File newFile = new File("retrieved.txt");
			newFile.createNewFile();

			try (PrintStream out = new PrintStream(new FileOutputStream("retrieved.txt"))) {
				out.print(response.toString());
			} finally {
				System.out.println("retr successfull");
			}

			inData.close();
			dataSocket.close();
		} catch (Exception e) {
			System.out.println("ERROR: retr failed");
		}
	}

	private void stor(int port, DataOutputStream outToServer, DataInputStream inFromServer, String sentence) {
		DataOutputStream dataOutToServer;
		DataOutputStream dataOutToServer;
		BufferedReader inputStream;

		try{
			Socket dataSocket(createDataConnection(port, outToServer, sentence));
			dataOutToServer = new DataOutputStream(dataSocket.getOutputStream());

			inputStream = new BufferedReader(new FileReader(fileName));

			String count;

			while ((count = inputStream.readLine()) != null) {
				dataOutToServer.writeBytes(count + "\n");
			}

			inputStream.close();

			dataOutToClient.writeBytes("eof");
			dataOutToClient.close();
			dataSocket.close();
			System.out.println("stor data socket closed");
		} catch (Exception e) {
			System.out.println("ERROR: stor failed");
		}
	}

	private void quit(DataOutputStream outToServer) {
		try {
			// Passing quit command and a dummy port to avoid server issues
			outToServer.writeBytes("0" + " " + "quit");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("exiting...");
		System.exit(0);
	}

	public FTPClient() {
		String modifiedSentence;
		boolean isOpen = true;

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String sentence = null;

		boolean connected = false;
		int port = 0;
		Socket controlSocket = null;

		while (!connected) {
			try {
				sentence = inFromUser.readLine().toLowerCase();
				if (sentence.startsWith("connect")) {
					StringTokenizer tokens = new StringTokenizer(sentence);

					// skip connect token
					tokens.nextToken();

					String serverName = tokens.nextToken();
					port = Integer.parseInt(tokens.nextToken());

					controlSocket = new Socket(serverName, port);

					System.out.println(serverName + " connected on port " + port);
					connected = controlSocket.isConnected();
				} else if (sentence.startsWith("quit")) {
					System.out.println("closing...\n");
					System.exit(0);
				} else {
					System.out.println(
							"You are not connected to a server, try\n\t'connect <ip/hostname> <port>' or 'quit' to exit");
				}
			} catch (IOException e) {
				System.out.println("ERROR: connection failed");
				System.exit(5);
			}
		}

		DataOutputStream outToServer = null;
		BufferedReader inFromServer = null;
		try {
			outToServer = new DataOutputStream(controlSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("ERROR: could not setup data streams");
			System.exit(5);
		}

		try {
			while (isOpen) {

				sentence = inFromUser.readLine();
				System.out.println("---------------");
				StringTokenizer tokens = new StringTokenizer(sentence);
				sentence = tokens.nextToken();

				if (sentence.toLowerCase().equals("list")) {
					list(port, outToServer, sentence);
				} else if (sentence.toLowerCase().startsWith("retr")) {
					retr(port, outToServer, sentence);
				} else if (sentence.toLowerCase().startsWith("stor")) {
					String fileName = tokens.nextToken();
					stor(port, outToServer, sentence, fileName);
				} else if (sentence.toLowerCase().equals("quit")) {
					quit(outToServer);
				} else {
					System.out.println(
							"unrecognized command!\nUsage:\n\tlist\n\t\tget list of files\n\tretr <filename>\n\t\tretrieve file with name provided\n\tstor <filename>\n\t\tstore file with name provided\n\tquit\n\t\tterminate connection");
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR: error occurred while connected");
			System.exit(5);
		}
	}

	public static void main(String argv[]) throws Exception {
		new FTPClient();
	}
}
