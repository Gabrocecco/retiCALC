import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class Client {
	
	public static void main(String[] args) {
		
		Services server = null;
		int response;
		Response r;
		
		try {
			server = (Services) Naming.lookup("//localhost:1099/Services");
		} catch (Exception e) {
			System.out.println("Errore nella richiesta");
			e.printStackTrace();
			System.exit(0);
		}
		
		char tipo;
		String nomeFile;
		String num;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			try {
				System.out.println("Quale servizio vuoi? (D per cancellazione righe, C per conteggio righe");
				tipo = (char) in.read();
				in.readLine();
				if (tipo == 'D') {
					System.out.println("Inserisci il file:");
					nomeFile = in.readLine();
					System.out.println("Inserisci la riga da cancellare:");
					num = in.readLine();
					try {
						r = server.getDelete(nomeFile, Integer.parseInt(num));
					} catch (RemoteException e1) {
						System.out.println("Errore nella chiamata");
						e1.toString();
						continue;
					} catch (NumberFormatException e2) {
						System.out.println("Inserire un numero");
						continue;
					}
					System.out.println(r.name + " " + r.num);
				} else if (tipo == 'C') {
					System.out.println("Inserisci il file:");
					nomeFile = in.readLine();
					System.out.println("Inserisci il numero di parole:");
					num = in.readLine();
					in.readLine();
					try {
						response = server.getCount(nomeFile, Integer.parseInt(num));
					} catch (RemoteException e1) {
						System.out.println("Errore nella chiamata");
						e1.toString();
						continue;
					} catch (NumberFormatException e2) {
						System.out.println("Inserisci un numero");
						continue;
					}
					System.out.println(response);
				} else {
					System.out.println("Sbagliato");
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
		
	}
}