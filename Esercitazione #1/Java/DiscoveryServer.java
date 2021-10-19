import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DiscoveryServer {

    public static void main(String args[]) {
        /* Gli argomenti devono essere non meno di 3, e devono essere dispari. (Nome programma e coppie di file/porta). */
        if (args.length < 3 || args.length %2 == 0) {
            System.out.println("Inserire tre argomenti");
            System.exit(0);
        }
        /* Creo un array di stringhe per i nomi dei file e uno di interi per contenere le porte associate ai file.*/
        int porte[] = new int[args.length/2 + 1];       
        String files[] = new String[args.length/2 + 1];
        files[0] = null;    //Fisso il nome del file in posizione 0 nullo, per uniformità.
        int porta;

        /* Itero sui nomi dei file controllando che ogni porta sia lecita e non ripetuta.
           Salvo le porte sul rispettivo array.
        */
        try {
            for (int i = 0; i < args.length; i++) {
                if (i % 2 == 0) {
                    porta = Integer.parseInt(args[i]);
                    if (porta < 1024 || porta > 65535) {
                        System.out.println("La porta deve essere compresa tra 1024 e 65535");
                        System.exit(1);
                    }
                    for (int k = 0; k < porte.length; k++) {
                        if (porta == porte[k]) {
                            System.out.println("Non ripetere le porte");
                            System.exit(2);
                        }
                    }
                    porte[i/2] = porta;
                } else {
                    files[i/2 + 1] = args[i];
                }
            }
        } catch (NumberFormatException e1) {
        	System.out.println("Le porte devono esser degli interi");
        	System.exit(2);
        } 
        SwapServer servers[] = new SwapServer[files.length - 1];
        InetAddress address = null;
        
        /* Recupero l'indirizzo IP della macchina locale, lo stesso che avranno gli SwapServer*/
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
        	System.out.println("Errore: impossibile trovare il localhost");
        	System.exit(3);
        }
        /* Inizializzo un server per ogni file.*/
        for (int i = 0; i < servers.length; i++) {
        	servers[i] = new SwapServer(porte[i + 1], files[i + 1], address);
        }
        for (int i = 0; i < servers.length; i++) {
        	servers[i].start();
        }
        
        byte dati[] = new byte[255];
        ByteArrayInputStream byteReader = null;
        ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();
        DataInputStream reader = null;
        DataOutputStream writer = new DataOutputStream(byteWriter);
        InetAddress clientAddress;
        int clientPort, i = 0;
        boolean found;
        String fileName = null;
        
        /* In questa sezione il DiscoveryServer deve ricevere un pacchetto dal Client, 
           quindi deve rispondere */
        try {
            DatagramSocket socket = new DatagramSocket(porte[0], address);
            DatagramPacket pacchetto = new DatagramPacket(new byte[255], 0, 255);
            while (true) {
            	
				socket.receive(pacchetto);
                System.out.println("Pacchetto ricevuto");
                byteReader = new ByteArrayInputStream(pacchetto.getData(), 0, pacchetto.getLength());
                reader = new DataInputStream(byteReader);
                
                clientAddress = pacchetto.getAddress();
                clientPort = pacchetto.getPort();
                fileName = reader.readUTF();

                System.out.println(fileName);
                
                found = false;
                /* Ricerco il file richiesto dal cliente.*/
                for (i = 1; i < files.length && !found; i++) {
                	if (fileName.compareTo(files[i]) == 0) {
                		found = true;
                	}
                }
                /* Preparo il pacchetto di risposta con l'indirizzo e la porta del cliente mittente.*/
                pacchetto.setAddress(clientAddress);
                pacchetto.setPort(clientPort);
                /* Se ho effettivamente trovato il file, scrivo nel pacchetto di risposta la porta corrispondente.
                   In caso contrario scrivo -1 come messaggio di errore.*/ 
                if (found) {
                	writer.writeInt(porte[i - 1]);
                } else {
                	writer.writeInt(-1);
                }
                /* Aggiungo i dati nel pacchetto, quindi invio.*/
            	dati = byteWriter.toByteArray();
            	pacchetto.setData(dati, 0, dati.length);
            	socket.send(pacchetto);
            	
            	
            	reader.flush();

            }
        } catch (SocketException e) {
            System.out.println("DiscoveryServer: Errore nella socket");
            System.exit(4);
        } catch (IOException e) {
            System.out.println("DiscoveryServer: Errore di I/O");
            e.printStackTrace();
            System.exit(5);
        }
    }
}