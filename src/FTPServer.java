import java.io.*;
import java.net.*;

class FTPServer {

	public static void main(String[] args) {

		try {
			ServerSocket welcomeSocket = new ServerSocket(12000);

			while (true) {
				Socket connectionSocket = welcomeSocket.accept();

				System.out.println(connectionSocket.getInetAddress().toString().substring(1) + " connected");

				ServerThread serverThread = new ServerThread(connectionSocket);

				serverThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
