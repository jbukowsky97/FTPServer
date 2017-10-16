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
		} finally {
			System.out.println("Client " + connectionSocket.getInetAddress() + " disconnected\n");
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
			System.out.println("ERROR: list failed");
		}
	}
	
	public void stor(Socket connectionSocket, int port, String fileName) {
		
	}

	private void retr(Socket connectionSocket, int port, String fileName) {
		Socket dataSocket;
		DataOutputStream dataOutToClient;
		BufferedReader inputStream;
		
		try {
			dataSocket = new Socket(connectionSocket.getInetAddress(), port);
			dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

			inputStream = new BufferedReader(new FileReader(fileName));
			
	        String count;
	        
	        while ((count = inputStream.readLine()) != null) {
	            dataOutToClient.writeBytes(count + "\n");
	        }
	        
	        inputStream.close();

			dataOutToClient.writeBytes("eof");
			dataOutToClient.close();
			dataSocket.close();
		} catch (Exception e) {
			
		}
	}

	private void quit(Socket connectionSocket) {
		try {
			connectionSocket.close();
		} catch (Exception e) {
			
		}
	}
}
