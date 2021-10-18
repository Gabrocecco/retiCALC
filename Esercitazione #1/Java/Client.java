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

public class Client {

	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("Inserire tre argomenti");
			System.exit(0);
		}
		
		try {
			int porta = Integer.parseInt(args[1]);
			InetAddress address = InetAddress.getLocalHost();//InetAddress.getByName(args[0]);
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(30000);
			DatagramPacket pacchetto = new DatagramPacket(new byte[255], 255, address, porta);
			ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();
			DataOutputStream writer = new DataOutputStream(byteWriter);
			writer.writeUTF(args[2]);
			byte dati[] = new byte[255];
			dati = byteWriter.toByteArray();
			pacchetto.setData(dati);
			socket.send(pacchetto);
			
			socket.receive(pacchetto);
			dati = pacchetto.getData();
			ByteArrayInputStream byteReader = new ByteArrayInputStream(dati);
	        DataInputStream reader = new DataInputStream(byteReader);
	        int res = reader.readInt();
	        if (res < 0) {
	        	System.out.println("Errore, nessun TextSwapServer � disponibile per il file indicato");
	        	System.exit(1);
	        } else {
	        	int riga1 = (int) (Math.random() * 100);
	        	int riga2 = (int) (Math.random() * 100);
	        	byteWriter.reset();
	        	writer.flush();
	        	writer.writeInt(riga1);
	        	writer.writeInt(riga2);
	        	dati = byteWriter.toByteArray();
	        	pacchetto.setPort(res);
	        	pacchetto.setLength(8);
	        	pacchetto.setData(dati);
	        	socket.send(pacchetto);
	        	System.out.println("Richiesto lo scambio delle righe " + riga1 + " e " + riga2);
	        	socket.close();
	        }
		} catch (UnknownHostException e) {
			System.out.println("Impossibile trovare l'Host specificato");
		} catch (NumberFormatException e) {
			System.out.println("La porta deve essere un intero");
		} catch (SocketException e) {
			System.out.println("Errore nella gestione del socket");
		} catch (IOException e) {
			System.out.println("Errore di IO");
		}
	}
}
