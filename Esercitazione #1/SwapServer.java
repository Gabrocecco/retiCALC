import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SwapServer extends Thread {
    
    private int porta;
    private String file, tmpFile, ausFile1, ausFile2;
    private InetAddress address;
    
    public SwapServer(int porta, String file, InetAddress address) {
        this.porta = porta;
        this.file = file;
        this.address = address;
        tmpFile = "tmpFile" + porta + ".txt";
        ausFile1 = "ausFile1" + porta + ".txt";
        ausFile2 = "ausFile2" + porta + ".txt";
    }
    
    public void run() {
    	
        int linea1, linea2, tmp, i, k;
        boolean success;
        String riga1, riga2, riga;
        byte dati[] = new byte[8];
        ByteArrayInputStream byteReader = null;
        DataInputStream reader = null;
        BufferedReader fileReader, ausReader1, ausReader2;
        BufferedWriter tmpWriter, ausWriter1, ausWriter2;
        File srcFile, tmpFile, ausFile1, ausFile2;
        
        try {
            DatagramSocket socket = new DatagramSocket(porta, address);
            DatagramPacket packet = new DatagramPacket(new byte[8], 0, 8);
            while (true) {
                socket.receive(packet);
                dati = packet.getData();
                byteReader = new ByteArrayInputStream(dati);
                reader = new DataInputStream(byteReader);
                linea1 = reader.readInt();
                linea2 = reader.readInt();
                if (linea1 != linea2) {
                	success = false;
                	if (linea1 > linea2) {
                    	tmp = linea2;
                    	linea2 = linea1;
                    	linea2 = tmp;
                	}
                	
                	srcFile = new File(file);
                	fileReader = new BufferedReader(new FileReader(srcFile));
                	tmpFile = new File(this.tmpFile);
                	tmpWriter = new BufferedWriter(new FileWriter(tmpFile));
                	ausFile1 = new File(this.ausFile1);
                	ausWriter1 = new BufferedWriter(new FileWriter(ausFile1));
                	ausReader1 = new BufferedReader(new FileReader(ausFile1));
                	ausFile2 = new File(this.ausFile2);
                	ausWriter2 = new BufferedWriter(new FileWriter(ausFile2));
                	ausReader2 = new BufferedReader(new FileReader(ausFile2));
                	
                	for (i = 0; i < linea1; i++) {
                		tmpWriter.write(fileReader.readLine() + "\n");
                	}
                	riga1 = fileReader.readLine();
                	for (; i < linea2; i++) {
                    	ausWriter1.write(fileReader.readLine() + "\n");
                	}
                	riga2 = fileReader.readLine();
                	if (riga2 != null) {
                		while ((riga = fileReader.readLine()) != null) {
                        	ausWriter2.write(riga + "\n");
                			i++;
                    	}
                		ausWriter1.close();
                		ausWriter2.close();
                    	fileReader.close();
                    	tmpWriter.write(riga2 + "\n");
                    	for (k = 0; k < linea2 - linea1; k++) {
                    		tmpWriter.write(ausReader1.readLine() + "\n");
                    	}
                    	tmpWriter.write(riga1 + "\n");
                    	for (k = 0; k < i - linea2; k++) {
                    		tmpWriter.write(ausReader2.readLine() + "\n");
                    	}
                    	tmpWriter.close();
                    	ausWriter1.close();
                    	ausWriter2.close();
                    	srcFile.delete();
                    	success = tmpFile.renameTo(srcFile);
                	}
                } else {
                	success = true;
                }
                
                if (success) {
                	System.out.println("Operazione eseguita con successo");
                } else {
                	System.out.println("Errore nell'operazione");
                }
            }
        } catch (SocketException e) {
            System.out.println("Errore nel thread " + getId() + ": impossibile aprire la socket");
        } catch (IOException e) {
        	System.out.println("Errore nel thread " + getId() + ": errore di I/O");
        }
    }
}