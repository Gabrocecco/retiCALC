import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client {

    private final static int BLOCK_SIZE = 262144;
    private final static String host = "localhost";
    private final static int port = 8000;
    private static final int ATTIVA = 1;
    
    public static void main(String args[]) {
        
        if (args.length != 2) {
            System.out.println("Inserire due argomenti");
            System.exit(0);
        }
        /* Il primo argomento letto è la cartella da dove trasferire i file.*/
        File dir = new File(args[0]);
        int minLen = 0;
        
        /* Il secondo argomento è la lunghezza MINIMA in byte per il quale un file deve essere trasferito. */
        try {
            minLen = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Il secondo argomento deve esser un numero");
            System.exit(1);
        }
        
        DataOutputStream out;
        DataInputStream in;
        DataInputStream fileIn = null;
        byte buffer[] = new byte[BLOCK_SIZE];   /* Lunghezza blocco di lettura/scrittura sui file.*/
        int state, read, len;
        
        try {
            /* Creiamo una socket con indirizzo IP locale e porta 8000, 
            quindi agganciamo dei DataInputStream e DataInputStream per leggere e scrivere su essa.*/
            Socket socket = new Socket(InetAddress.getByName(host), port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            socket.setSoTimeout(30000); 
            /* Itero per ogni file/cartella nella directory indicata dall'utente.*/
            for (File entry : dir.listFiles()) {
                /* Se la entry corrente è un file abbastanza grande per essere strasferito,
                invio il nome del file in questione al server. Il server risponde inviando un intero
                (ATTIVA = 1) se il file non è già presente, in caso contrario non si procede al trasferimento.*/
                if (entry.isFile() && (len = (int) entry.length()) >= minLen) {
                    out.writeUTF(entry.getName());
                    state = in.readInt();
                    state = ATTIVA;
                    /* Dopo avere ricevuto (ATTIVA = 1) dal server inizio il processo di lettura e scrittura a blocchi verso il server.*/
                    if (state == ATTIVA) {
                        out.writeInt(len);
                        fileIn = new DataInputStream(new FileInputStream(entry));
                        do {
                            read = fileIn.readNBytes(buffer, 0, BLOCK_SIZE);    /* Leggo a blocchi dal file, per poi inviarli al server.*/
                            out.write(buffer);
                        } while (read == BLOCK_SIZE);
                        fileIn.close();
                    }
                }
            }
            
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            
        } catch (SocketTimeoutException e) {
        	System.out.println("Timeout scaduto");
        } catch (IOException e) {
        	System.out.println("Errore di I/O");
            e.printStackTrace();
        }
        
    }

}