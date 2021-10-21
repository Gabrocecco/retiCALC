import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FrontServer {
	public static void main(String args[]) {
		
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		LoadServer loadServer = null;
		Path dir = Paths.get(args[0]);
		
		
		try {
			serverSocket = new ServerSocket(8000);
			while (true) {
				clientSocket = serverSocket.accept();
				clientSocket.setSoTimeout(30000);
				loadServer = new LoadServer(clientSocket, dir);
				loadServer.start();
			}
			
		} catch (IOException e) {
			System.out.println("Impossibile creare la socket");
			System.exit(0);
		}
		

	}
}