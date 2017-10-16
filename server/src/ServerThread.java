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

	private static final String ROOT_PATH = System.getProperty("user.dir") + "/root/";

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

	public void run() {
		boolean running = true;
		try {
			while (running) {
				while (!inFromClient.ready())
					;

				String fromClient = inFromClient.readLine();
				System.out.println("client request: " + fromClient);
				StringTokenizer tokens = new StringTokenizer(fromClient);

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
			e.printStackTrace();
		} finally {
			System.out.println("client " + connectionSocket.getInetAddress() + " disconnected\n");
		}
	}

	private void list(Socket connectionSocket, int port) {
		Socket dataSocket;
		DataOutputStream dataOutToClient;
		try {
			File folder = new File(ROOT_PATH);
			File[] listOfFiles = folder.listFiles();

			dataSocket = new Socket(connectionSocket.getInetAddress(), port);
			dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

			for (File f : listOfFiles) {
				if (f.isFile()) {
					dataOutToClient.writeBytes(f.getName() + "\n");
				}
			}

			dataOutToClient.writeBytes("eof");
			dataOutToClient.close();
			dataSocket.close();
			System.out.println("list data socket closed");
		} catch (Exception e) {
			System.out.println("ERROR: list failed");
		}
	}

	private void retr(Socket connectionSocket, int port, String fileName) {
		Socket dataSocket;
		DataOutputStream dataOutToClient;
		BufferedReader inputStream;

		try {
			dataSocket = new Socket(connectionSocket.getInetAddress(), port);
			dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

			System.out.println(ROOT_PATH + fileName);
			inputStream = new BufferedReader(new FileReader(ROOT_PATH + fileName));

			String count;

			while ((count = inputStream.readLine()) != null) {
				dataOutToClient.writeBytes(count + "\n");
			}

			inputStream.close();

			dataOutToClient.writeBytes("eof");
			dataOutToClient.close();
			dataSocket.close();
			System.out.println("retr data socket closed");
		} catch (Exception e) {
			System.out.println("ERROR: retr failed");
		}
	}

	public void stor(Socket connectionSocket, int port, String fileName) {

		try {
			Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
			BufferedReader inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

			while (!inData.ready())
				;

			StringBuffer response = new StringBuffer();
			String msg;

			while (!((msg = inData.readLine()).equals("eof"))) {
				response.append(msg + "\n");
			}
			System.out.println(ROOT_PATH + fileName);


			File newFile = new File(ROOT_PATH + fileName);
			newFile.createNewFile();

			inData.close();
			dataSocket.close();
			System.out.println("stor data socket closed");
		} catch (Exception e) {
			System.out.println("ERROR: stor failed");
		}
	}

	private void quit(Socket connectionSocket) {
		try {
			connectionSocket.close();
		} catch (Exception e) {
			System.out.println("ERROR: quit failed");
		}
	}
}
