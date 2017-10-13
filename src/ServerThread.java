import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerThread extends Thread {

	private Socket connectionSocket;

	public ServerThread(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}

	public void run() {
		try {
			while (true) {
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				BufferedReader inFromClient = new BufferedReader(
						new InputStreamReader(connectionSocket.getInputStream()));

				String fromClient = null;
				try {
					fromClient = inFromClient.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}

				StringTokenizer tokens = new StringTokenizer(fromClient);

				String frstln = tokens.nextToken();
				int port = Integer.parseInt(frstln);
				String clientCommand = tokens.nextToken();

				if (clientCommand.equals("list")) {

					Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());			
					
					File folder = new File(System.getProperty("user.dir"));
					File[] listOfFiles = folder.listFiles();

				    for (int i = 0; i < listOfFiles.length; i++) {
				      if (listOfFiles[i].isFile()) {
				        System.out.println("file " + listOfFiles[i].getName());
				      }
				    }

					dataOutToClient.close();
					dataSocket.close();
					System.out.println("Data Socket closed");
				}

				if (clientCommand.equals("retr")) {

					Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

					System.out.println("-retr logic-");

					dataOutToClient.close();
					dataSocket.close();
					System.out.println("Data Socket closed");
				}

				if (clientCommand.equals("stor")) {

					Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());

					System.out.println("-stor logic-");

					dataOutToClient.close();
					dataSocket.close();
					System.out.println("Data Socket closed");
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}