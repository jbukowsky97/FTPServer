import java.io.BufferedReader;
import java.io.FileReader;
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
					String fileName = tokens.nextToken();
					retr(connectionSocket, port, fileName);
				} else if (clientCommand.equals("stor")) {
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

	private void list(Socket connectionSocket, int port) {
		Socket dataSocket;
		DataOutputStream dataOutToClient;
		try {
			File folder = new File(System.getProperty("user.dir"));
			File[] listOfFiles = folder.listFiles();

			dataSocket = new Socket(connectionSocket.getInetAddress(), port);
			dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

			for (File f: listOfFiles) {
				if (f.isFile()) {
					dataOutToClient.writeBytes(f.getName() + "\n");
				}
			}
			dataOutToClient.writeBytes("eof");
			dataOutToClient.close();
			dataSocket.close();
		} catch (Exception e) {
			System.out.println("!!!list failed!!!");
		}
	}

	private void retr(Socket connectionSocket, int port, String fileName) {
		File folder = new File(System.getProperty("user.dir"));
		File[] listOfFiles = folder.listFiles();

		File send = null;
		for (File f: listOfFiles) {
			if (f.isFile() && f.getName().equals(fileName)) {
				send = f;
				break;
			}
		}

		try {
			if (send == null) {
				outToClient.writeBytes("550 file not found");
			} else {
				outToClient.writeBytes("200 command ok");

				Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
				DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
				BufferedReader br = new BufferedReader(new FileReader(send));
				String line;
				while ((line = br.readLine()) != null) {
					dataOutToClient.writeBytes(line);
				}
				dataOutToClient.writeBytes("eof");
				dataOutToClient.close();
				dataSocket.close();
			}
		} catch (Exception e) {
			System.out.println("!!!retr failed!!!");
		}
	}

	private void quit(Socket connectionSocket) {
		System.out.println("Client " + connectionSocket.getInetAddress() + " disconnected\n");

		try {
			connectionSocket.close();
		} catch (Exception e) {
			
		}
	}
}
