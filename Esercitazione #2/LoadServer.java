import java.io.DataInputStream; 
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadServer extends Thread {
    
	private final static int BLOCK_SIZE = 262144;
	
    private Socket clientSocket;
    private Path dir;
    private static final int ATTIVA = 1;
    private static final int SALTA = 0;
    
    /* Costruttore che riceve la clientSocket e la directory dove salvare i file trasferiti.*/
    public LoadServer(Socket clientSocket, Path dir) {
        this.clientSocket = clientSocket;
        this.dir = dir;
    }
    
    private String UTFReader(DataInputStream reader) {
    	try {
    		return reader.readUTF();
    	} catch (IOException e) {
    		return null;
    	}
    }
    
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            DataInputStream reader = new DataInputStream(input);
            DataOutputStream writer = new DataOutputStream(output);
            FileOutputStream fileWriter;
            int size, letti, lettiTmp;
            String fileName;
            File file;
            byte block[] = new byte[BLOCK_SIZE];    /* Blocco di lettura e scrittura da file.*/
            
            while ((fileName = UTFReader(reader)) != null) {    /* Leggo dal cliente il nome del file.*/
                
                file = Paths.get(dir.toString() + "\\" + fileName).toFile();    
                
                if (file.createNewFile()) { /* Crea il file SOLO SE NON è già presente nella directory. */
                    
                    writer.writeInt(ATTIVA);    /* Invio l'intero (ATTIVA=1) per dare l'ok al trasferimento del file.*/
                    size = reader.readInt();    /* Leggo la lunghezza del file inviata dal cliente.*/
                    fileWriter = new FileOutputStream(file);
                    letti = 0;
                    
                    /* Leggo i blocchi trasmessi dal cliente per poi scriverli nel nuovo file.*/
                    while (letti < size) {
                        if (size - letti >= BLOCK_SIZE) {
                        	lettiTmp = reader.readNBytes(block, 0, BLOCK_SIZE);
                            letti += lettiTmp;
                        } else {    /* L'ultimo frazione di file letta potrebbe essere più piccola di un blocco, in questo caso leggo il rimanente.*/
                        	lettiTmp = reader.readNBytes(block, 0, size - letti);
                            letti += lettiTmp;
                        }
                        fileWriter.write(block, 0, lettiTmp);   /* Scrivo il blocco appena letto nel file.*/
                    }
                    
                    fileWriter.close();
                    writer.flush();
                    output.flush();
                    
                } else {    /* Se il file è già presente invio l'intero (SALTA=0) al cliente.*/
                    writer.writeInt(SALTA);
                }
            }
            
            
            clientSocket.shutdownInput();
            clientSocket.shutdownOutput();
            clientSocket.close();
            
        } catch (SocketTimeoutException e) {
        	System.out.println("Timeout scaduto");
        } catch (IOException e) {
            System.out.println("Errore di I/O");
            e.printStackTrace();
        }
    }
    
}