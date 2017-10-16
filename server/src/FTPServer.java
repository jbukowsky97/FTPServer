import java.io.*;
import java.net.*;

class FTPServer {

	public static void main(String[] args) {
		try {
			/* Create new welcome socket on port 12000 */
			ServerSocket welcomeSocket = new ServerSocket(5338);

			/* Loop for continued client usage */
			while (true) {
				/* Accept the welcome socket */
				Socket connectionSocket = welcomeSocket.accept();

				/* <IP address> connected */
				System.out.println(connectionSocket.getInetAddress().toString().substring(1) + " connected");

				/* Create a new thread to handle each client */
				ServerThread serverThread = new ServerThread(connectionSocket);

				/* Start the thread */
				serverThread.start();
			}
		} catch (IOException e) {
			/* If exceptions are caught, print the stack trace */
			e.printStackTrace();
		}
	}
}
