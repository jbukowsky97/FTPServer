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
			outToServer.writeBytes(sentence + " " + dataPort + " " + '\n');
			dataSocket = welcomeData.accept();
			welcomeData.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return dataSocket;
		}

		// TODO Need this
		// DataInputStream inData = new DataInputStream(new
		// BufferedInputStream(dataSocket.getInputStream()));
		// while (notEnd) {
		// modifiedSentence = inData.readUTF();
		// //........................................
		// //........................................

		// welcomeData.close();
		// dataSocket.close();
		// System.out.println("\nWhat would you like to do next: \n retr: file.txt ||
		// stor: file.txt || close");
	}

	private void list(int controlPort, DataOutputStream outToServer, String sentence) throws IOException {
		Socket dataSocket = createDataConnection(controlPort, outToServer, sentence);
		BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
		while (!inData.ready()) {

		}
		StringBuffer response = new StringBuffer();
		int c = 0;
		while ((c = inData.read()) != -1) {
			response.append((char) c);
		}
		System.out.println(response.toString());
		inData.close();
		dataSocket.close();
	}

	public void quit(DataOutputStream outToServer) {
		try {
			outToServer.writeBytes("quit");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Exiting");
		System.exit(0);
	}

	public FTPClient() {
		String modifiedSentence;
		boolean isOpen = true;
		String statusCode;

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

					//skip connect token
					tokens.nextToken();

					String serverName = tokens.nextToken();
					port = Integer.parseInt(tokens.nextToken());

					controlSocket = new Socket(serverName, port);

					System.out.println("Connected: " + serverName + ":" + port);
					connected = controlSocket.isConnected();
				} else if (sentence.startsWith("quit")) {
					System.out.println("Closing\n");
					System.exit(0);
				} else {
					System.out.println("You are not connected to a server, try\n\t'connect <ip/hostname> <port>' or 'quit' to exit");
				}
			} catch (IOException e) {
				System.out.println("ERROR: Connection failed");
				System.exit(5);
			}
		}

		DataOutputStream outToServer = null;
		DataInputStream inFromServer = null;
		try {
			outToServer = new DataOutputStream(controlSocket.getOutputStream());
			inFromServer = new DataInputStream(new BufferedInputStream(controlSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("ERROR: Could not setup data streams");
			System.exit(5);
		}

		try {
			while (isOpen) {

				sentence = inFromUser.readLine().toLowerCase();
				System.out.println("---------------");

				if (sentence.toLowerCase().equals("list")) {
					list(port, outToServer, sentence);
				} else if (sentence.equals("retr")) {
					outToServer.writeBytes(port + " " + "retr" + '\n');
				} else if (sentence.equals("stor")) {
					outToServer.writeBytes(port + " " + "stor" + '\n');
				} else if (sentence.equals("quit")) {
					quit(outToServer);
				} else {
					System.out.println("Unrecognized command!\nUsage:\n\tlist\n\t\tget list of files\n\tretr <filename>\n\t\tretrieve file with name provided\n\tstor <filename>\n\t\tstore file with name provided\n\tquit\n\t\tterminate connection");
				}
			}
		}catch (IOException e) {
			System.out.println("ERROR: error occurred while connected");
			System.exit(5);
		}
	}

	public static void main(String argv[]) throws Exception {
		new FTPClient();
	}
}
