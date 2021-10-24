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
		Path dir = Paths.get("C:\\Users\\Andrea\\Desktop\\out");
		
		
		try {
			/* Creao una ServerSocket sulla porta 8000*/
			serverSocket = new ServerSocket(8000);
			serverSocket.setReuseAddress(true);	
			while (true) {
				/* La ServerSocket si mette in ascolto sulla porta 8000
				 in attesa di una richeista di connessione dal client.*/
				clientSocket = serverSocket.accept();	
				clientSocket.setSoTimeout(30000);
				/* Per ogni connessione e quindi per ogni cliente creo u
				 Thread LoadServer che riceve la ClientSocket e la directory dove il server salverà i file.*/
				loadServer = new LoadServer(clientSocket, dir);	
				loadServer.start();
			}
			
		} catch (IOException e) {
			System.out.println("Impossibile creare la socket");
			System.exit(0);
		}

	}
}