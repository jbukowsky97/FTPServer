import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerThread extends Thread {

	private Socket connectionSocket;
	private DataOutputStream outToClient;
	private BufferedReader inFromClient;

	public ServerThread(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
		outToClient = null;
		inFromClient = null;
		try {
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			inFromClient = new BufferedReader(
			new InputStreamReader(connectionSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("ERROR: could not set up data streams");
		}
	}

	public void run() {
		boolean running = true;
		try {
			while (running) {
				while (!inFromClient.ready()) {

				}
				String fromClient = inFromClient.readLine();
				System.out.println("From Client:\t" + fromClient);
				StringTokenizer tokens = new StringTokenizer(fromClient);

				int port = Integer.parseInt(tokens.nextToken());
				String clientCommand = tokens.nextToken();

				if (clientCommand.equals("list")) {
					list(connectionSocket, port);
				} else if (clientCommand.equals("retr")) {
					port = Integer.parseInt(tokens.nextToken());
					Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

					System.out.println("-retr logic-");

					dataOutToClient.close();
					dataSocket.close();
					System.out.println("Data Socket closed");
				} else if (clientCommand.equals("stor")) {
					port = Integer.parseInt(tokens.nextToken());
					Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

					System.out.println("-stor logic-");

					dataOutToClient.close();
					dataSocket.close();
					System.out.println("Data Socket closed");
				} else if (clientCommand.equals("quit")) {
					quit(connectionSocket);
					running = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void list(Socket connectionSocket, int port) throws IOException {
		Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
		DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

		File folder = new File(System.getProperty("user.dir"));
		File[] listOfFiles = folder.listFiles();

		String files = "";
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				files += listOfFiles[i].getName() + "\n";
			}
		}

		dataOutToClient.writeBytes(files);

		dataOutToClient.close();
		dataSocket.close();
	}

	private void quit(Socket connectionSocket) {
		System.out.println("Client " + connectionSocket.getInetAddress() + " disconnected\n");

		try {
			connectionSocket.close();
		} catch (Exception e) {
			
		}
	}
}
