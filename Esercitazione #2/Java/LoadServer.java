import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoadServer extends Thread {
    
    private Socket clientSocket;
    private Path dir;
    private static final int ATTIVA = 1;
    private static final int SALTA = 0;
    private static final int BLOCK_SIZE = 65536;
    
    
    public LoadServer(Socket clientSocket, Path dir) {
        this.clientSocket = clientSocket;
        this.dir = dir;
    }
    
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            DataInputStream reader = new DataInputStream(input);
            DataOutputStream writer = new DataOutputStream(output);
            FileOutputStream fileWriter;
            int size, letti;
            String fileName;
            File file;
            byte block[] = new byte[BLOCK_SIZE];
            
            while ((fileName = reader.readUTF()) != null) {
                
                file = Paths.get(dir.toString() + "\\" + fileName).toFile();
                
                if (file.createNewFile()) {
                    
                    writer.writeInt(ATTIVA);
                    size = reader.readInt();
                    fileWriter = new FileOutputStream(file);
                    letti = 0;
                    
                    while (letti < size) {
                        if (size - letti >= BLOCK_SIZE) {
                            letti += reader.readNBytes(block, 0, BLOCK_SIZE);
                        } else {
                            reader.readNBytes(block, 0, size - letti);
                        }
                        fileWriter.write(block);
                    }
                    
                    fileWriter.close();
                    
                } else {
                    writer.writeInt(SALTA);
                }
            }
            
            clientSocket.shutdownInput();
            clientSocket.shutdownOutput();
            clientSocket.close();
            
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Errore di input");
            e.printStackTrace();
        }
    }
    
}