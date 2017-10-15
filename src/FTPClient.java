import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

class FTPClient {

	private Socket createDataConnection(int controlPort, DataOutputStream outToServer, String sentence) {

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

	public FTPClient() {
        String modifiedSentence;
        boolean isOpen = true;
        int number = 1;
        boolean notEnd = true;
        String statusCode;
        boolean clientgo = true;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        String sentence = null;
        try {
            sentence = inFromUser.readLine();
            while (!sentence.startsWith("connect")) {
                System.out.println("You are not connected to a server, try\n\tconnect <ip/hostname> <port>");
                sentence = inFromUser.readLine();
            }
        } catch (IOException e) {
            System.out.println("ERROR: Could not read from System in");
            System.exit(5);
        }
        StringTokenizer tokens = new StringTokenizer(sentence);

        //skip connect token
        tokens.nextToken();

        String serverName = tokens.nextToken();
        int port = Integer.parseInt(tokens.nextToken());

        Socket ControlSocket = null;
        try {
            ControlSocket = new Socket(serverName, port);
        } catch (IOException e) {
            System.out.println("ERROR: Could not connect");
            System.exit(5);
        }

        DataOutputStream outToServer = null;
        DataInputStream inFromServer = null;
        try {
            outToServer = new DataOutputStream(ControlSocket.getOutputStream());
            inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("ERROR: Could not setup data streams");
            System.exit(5);
        }

        System.out.println("You are connected to " + serverName);

        try {
            while (isOpen) {

                sentence = inFromUser.readLine();
                System.out.println("---------------");

                if (sentence.toLowerCase().equals("list")) {
                    list(port, outToServer, sentence);
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
        }catch (IOException e) {
            System.out.println("ERROR: error occurred while connected");
            System.exit(5);
        }
	}

	public void quit() {

	}

	public static void main(String argv[]) throws Exception {
		new FTPClient();
	}
}
