/**
 * ClientCongresso.java
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;

class ClientCongresso {

  // Avvio del Client RMI
	public static void main(String[] args) {
		int registryRemotoPort = 1099;
		String registryRemotoHost = null;
		String registryRemotoName = "RegistryRemoto";
		String serviceName = "ServerCongresso";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Controllo dei parametri della riga di comando
		if (args.length != 1 && args.length != 2) {
			System.out.println("Sintassi: ClientCongresso NomeHostRegistryRemoto [registryPort], registryPort intero");
			System.exit(1);
		}
		registryRemotoHost = args[0];
		if (args.length == 2) {
			try {
				registryRemotoPort = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.out
					.println("Sintassi: ClientCongresso NomeHostRegistryRemoto [registryPort], registryPort intero");
				System.exit(1);
			}
		}
		
		// 	Impostazione del SecurityManager
		/*if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());
		*/
		// Connessione al servizio RMI remoto
		try {
			String completeRemoteRegistryName = "//" + registryRemotoHost + ":"
					+ registryRemotoPort + "/" + registryRemotoName;
			RegistryRemotoTagClient registryRemoto = 
					(RegistryRemotoTagClient) Naming.lookup(completeRemoteRegistryName);
			
			Remote server = null;
			Remote[] servers;
			ServerCongresso serverRMI = null;
			ServerCongresso[] possible = new ServerCongresso[100];
			
			System.out.println("Ricerca servizio (C=Cerca, A=Cerca tutti, T=Cerca per tag)");
			String req;
			String nome;
			String tag;
			String[] nomi;
			int index;
			int num;
			
			/* -----------------------------------------------------------------------------------------------------------*/
			while (serverRMI == null) {
				req = stdIn.readLine();
				if (req.equals("C")) {	//CERCA IL PRIMO SERVIZIO CON CON UN CERTO NOME LOGICO
					System.out.println("Inserisci il nome del servizio");
					nome = stdIn.readLine();	/* Richiedo di inserire il nomeLogico del servizio desiderato.*/
					server = registryRemoto.cerca(nome);	/* Viene ritornato un riferiemnto remoto (il primo della tabella con nomeLogico corrispondente.). */
					if (server == null) {
						System.out.println("Nessun servizio disponibile con quel nome");
					} else if (!(server instanceof ServerCongresso)) {
						System.out.println("Servizio non supportato");
					} else {
						serverRMI = (ServerCongresso) server;
					}
				} else if (req.equals("A")) {	//CERCA TUTTI I SERVIZI CON UN CERTO NOME LOGICO
					System.out.println("Inserisci il nome del servizio");
					nome = stdIn.readLine();
					servers = registryRemoto.cercaTutti(nome);	/* Ritorna un array di riferimenti remoti.*/
					if (servers.length == 0) {
						System.out.println("Nessun servizio disponibile con quel nome");
					} else {
						index = 0;
						for (int i = 0; i < servers.length; i++) {	/* Per ogni riferimento remoto ritornato che è istanza di ServerCongresso lo aggiungo a un array di ServerCongresso*/
							if (servers[i] instanceof ServerCongresso) {
								possible[index] = (ServerCongresso) servers[i];
								index++;
							}
						}
						if (index == 0) {
							System.out.println("Non ci sono servizi supportati");
						} else {	/* Chiedo di selezionare quale dei servizi individuati si vuole utilizzare.*/
							System.out.println("Trovati " + index + " servizi corrispondenti a quel nome");
							System.out.println("Quale vuoi? (1 - " + index + ")");
							num = Integer.parseInt(stdIn.readLine());
							if (num > index || num < 1) {
								System.out.println("Sbagliato");
							} else {
								serverRMI = (ServerCongresso) possible[num - 1];	/* Associo il server desiderato alla variabile serverRMI.*/
							}
						}
					}
				} else if (req.equals("T")) {	//CERCA PER TAG
					System.out.println("Inserisci il tag da cercare");
					tag = stdIn.readLine();
					nomi = registryRemoto.cercaTag(tag);	/* Ritorna un array di nomiLogici.*/
					if (nomi.length == 0) {
						System.out.println("Non ci sono servizi con quel tag");
					} else {
						System.out.println("Trovati i seguenti servizi:");
						for (int i = 0; i < nomi.length; i++) {
							System.out.println(nomi[i]);
						}
					}
				} else {
					System.out.println("Riprova");
				}
			}
			/* -----------------------------------------------------------------------------------------------------------*/
			
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");
			
			System.out.println("\nRichieste di servizio fino a fine file");
			
			String service;
			System.out.print("Servizio (R=Registrazione, P=Programma del congresso): ");
			
			while ((service = stdIn.readLine()) != null) {
				
				if (service.equals("R")) {
					
					boolean ok = false;
					int g = 0;
					System.out.print("Giornata (1-3)? ");
					while (ok != true) {
						g = Integer.parseInt(stdIn.readLine());
						if (g < 1 || g > 3) {
							System.out.println("Giornata non valida");
							System.out.print("Giornata (1-3)? ");
							continue;
						} else
							ok = true;
					}
					ok = false;
					String sess = null;
					System.out.print("Sessione (S1 - S12)? ");
					
					while (ok != true) {
						sess = stdIn.readLine();
						if (!sess.equals("S1") && !sess.equals("S2") && !sess.equals("S3")
								&& !sess.equals("S4") && !sess.equals("S5")
								&& !sess.equals("S6") && !sess.equals("S7")
								&& !sess.equals("S8") && !sess.equals("S9")
								&& !sess.equals("S10") && !sess.equals("S11")
								&& !sess.equals("S12")) {
							System.out.println("Sessione non valida");
							System.out.print("Sessione (S1 - S12)? ");
							continue;
						} else
							ok = true;
					}

					System.out.print("Speaker? ");
					String speak = stdIn.readLine();

					// Tutto corretto
					if (serverRMI.registrazione(g, sess, speak) == 0)
						System.out.println("Registrazione di " + speak
								+ " effettuata per giornata " + g + " sessione " + sess);
					else
						System.out.println("Sessione piena: giornata" + g + " sessione "+ sess);
				} // R

				else if (service.equals("P")) {
					int g = 0;
					boolean ok = false;
					System.out.print("Programma giornata (1-3)? ");
					
					while (ok != true) {
						// intercettare la NumberFormatException
						g = Integer.parseInt(stdIn.readLine());
						if (g < 1 || g > 3) {
							System.out.println("Giornata non valida");
							System.out.print("Programma giornata (1-3)? ");
							continue;
						} else
							ok = true;
					}
					System.out.println("Ecco il programma: ");
					serverRMI.programma(g).stampa();
					
				} // P

				else System.out.println("Servizio non disponibile");
				
				System.out.print("Servizio (R=Registrazione, P=Programma del congresso): ");
			} // !EOF richieste utente

		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
	}
}