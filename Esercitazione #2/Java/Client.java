import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {

    private final static int BLOCK_SIZE = 65536;
    private final static String host = "localhost";
    private final static int port = 8000;
    private static final int ATTIVA = 1;
    private static final int SALTA = 0;
    
    public static void main(String[] args) {
        
        if (args.length != 2) {
            System.out.println("Inserire due argomenti");
            System.exit(0);
        }
        
        //Path dir = Paths.get(args[0]);
        File dir = new File(args[0]);
        int minLen;
        
        try {
            minLen = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Il secondo argomento deve esser un numero");
            System.exit(1);
        }
        
        File file;
        DataOutputStream out;
        DataInputStream in;
        DataInputStream fileIn = null;
        byte buffer[] = new byte[BLOCK_SIZE];
        int state, read;
        
        try {
            Socket socket = new Socket(InetAddress.getByName(host), port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            for (File entry : dir.listFiles()) {
                System.out.println(entry.getName());
                if (entry.isFile()) {
                    out.writeUTF(entry.getName());
                    state = in.readInt();
                    if (state == ATTIVA) {
                        out.writeInt((int) entry.length());
                        fileIn = new DataInputStream(new FileInputStream(entry));
                        do {
                            read = fileIn.readNBytes(buffer, 0, BLOCK_SIZE);
                            out.write(buffer);
                        } while (read == BLOCK_SIZE);
                        fileIn.close();
                    }
                }
            }
            
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}