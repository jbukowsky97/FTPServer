import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

class FTPClient {

	public Socket createDataConnection(int controlPort, DataOutputStream outToServer, String sentence) {

		Socket dataSocket = null;

		try {
			int dataPort = controlPort + 2;
			ServerSocket welcomeData = new ServerSocket(dataPort);
			outToServer.writeBytes(dataPort + " " + sentence + " " + '\n');
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

	public FTPClient() {
		try {
			String sentence;
			String modifiedSentence;
			boolean isOpen = true;
			int number = 1;
			boolean notEnd = true;
			String statusCode;
			boolean clientgo = true;

			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			sentence = inFromUser.readLine();
			StringTokenizer tokens = new StringTokenizer(sentence);

			while (!sentence.startsWith("connect")) {
			}

			String serverName = tokens.nextToken(); // pass the connect command
			serverName = tokens.nextToken();
			int port = Integer.parseInt(tokens.nextToken());

			System.out.println("You are connected to " + serverName);

			Socket ControlSocket = new Socket(serverName, port);

			DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());

			DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

			while (isOpen) {

				sentence = inFromUser.readLine();

				if (sentence.toLowerCase().equals("list")) {
					outToServer.writeBytes(port + " " + "list" + '\n');
				} else if (sentence.toLowerCase().equals("retr")) {
					outToServer.writeBytes(port + " " + "retr" + '\n');
				} else if (sentence.toLowerCase().equals("stor")) {
					outToServer.writeBytes(port + " " + "stor" + '\n');
				} else if (sentence.toLowerCase().equals("quit")) {
					quit();
				} else {
					System.out.println("Unrecognized command!");
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void quit() {

	}

	public static void main(String argv[]) throws Exception {
		new FTPClient();
	}
}
