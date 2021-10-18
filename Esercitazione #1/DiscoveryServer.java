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
        if (args.length < 3 || args.length %2 == 0) {
            System.out.println("Inserire tre argomenti");
            System.exit(0);
        }

        int porte[] = new int[args.length/2 + 1];
        String files[] = new String[args.length/2 + 1];
        files[0] = null;
        int porta;

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
        
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
        	System.out.println("Errore: impossibile trovare il localhost");
        	System.exit(3);
        }
        
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
        int clientPort, i;
        boolean found;
        String fileName = null;
        
        try {
            DatagramSocket socket = new DatagramSocket(porte[0], address);
            DatagramPacket pacchetto = new DatagramPacket(new byte[255], 0, 255);
            while (true) {
                socket.receive(pacchetto);
                System.out.println("Pacchetto ricevuto");
                dati = pacchetto.getData();
                byteReader = new ByteArrayInputStream(dati);
                reader = new DataInputStream(byteReader);
                clientAddress = pacchetto.getAddress();
                clientPort = pacchetto.getPort();
                fileName = reader.readUTF();

                found = false;

                for (i = 1; i < files.length && !found; i++) {
                	if (fileName.compareTo(files[i]) == 0) {
                		found = true;
                	}
                }
                
                pacchetto.setAddress(clientAddress);
                pacchetto.setPort(clientPort);
                
                if (found) {
                	writer.writeInt(porte[i - 1]);
                } else {
                	writer.writeInt(-1);
                }

            	dati = byteWriter.toByteArray();
            	pacchetto.setData(dati);
            	socket.send(pacchetto);
            	
            	reader.close();

            }
        } catch (SocketException e) {
            System.out.println("DiscoveryServer: Errore nella socket");
            System.exit(4);
        } catch (IOException e) {
            System.out.println("DiscoveryServer: Errore di I/O");
            System.exit(5);
        }
    }
}