import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RegistryRemotoTagImpl extends UnicastRemoteObject implements RegistryRemotoTagServer {	
	
	private static final long serialVersionUID = 1L;
	
	private static final int tableSize = 100;
	private final static int tagNum = 10;
	
	private final static int registryRemotoPort = 1099;
	private final static String registryRemotoHost = "localhost";
	private final static String registryRemotoName = "RegistryRemoto";

	
	Object[][] table = new Object[tableSize][2];	/* (nomeLogicoServizio, riferimentoRemoto) Tabella che contiene le coppie nomeLogico, riferimento per ogni servizio senza tag*/
	String[][] tagTable = new String[tagNum][tableSize];	/* (numeroTag, ??) Tabella che contiene identificatore del tag, (numero totale di tag = 10), */
	
	ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private int indexRef;	/* Indice che punta a alla prima entry libera nella tabella table.*/
	private int[] indexTag = new int[tagNum];	/* Indice che punta alla prima entry libera nella tabella dei tag, (un indice per ogni tag).*/
	
	/* Costruttore azzera indexTag (tabella vuota). Porto a null tutta la tabella table. Porto a null anche tutti gli indexTag.*/
	public RegistryRemotoTagImpl() throws RemoteException {
		super();
		
		this.indexRef = 0;
		
		for (int i = 0; i < tableSize; i++) {
			table[i][0] = null;
			table[i][1] = null;
		}
		
		for (int i = 0; i < tagNum; i++) {
			indexTag[i] = 0;
		}
		
	}
	/* (SERVER) AGGIUNTA DI UN NUOVO SERVER REMOTO:
		Dopo i controlli su argomenti e tabella, locko in scrittura
		scrivo nella prima entry libera di tabella il nuovo nome logico e il rifeirimento del servizio che si vuole aggiungere.
		Sblocco la scrittura e ritorno un risultato booleano.
	 */
	public boolean aggiungi(String nomeLogico, Remote riferimento) throws RemoteException {
		boolean risultato = false;
		if(nomeLogico == null || riferimento == null) {
			return risultato;
		}
		if (indexRef == tableSize) {
			throw new RemoteException("La tabella � piena");
		}
		try {
			lock.writeLock().lock();
			table[indexRef][0] = nomeLogico;
			table[indexRef][1] = riferimento;
			indexRef++;
		} finally {
			lock.writeLock().unlock();
		}
		return risultato;
	}
	
	/* (CLIENT) RICERCA DEL PRIMO RIFERIEMENTO DEL SERVER REMOTO REGISTRATO CON IL NOMELOGICO INDICATO NELL'ARGOMENTO.
		Dopo il controllo sull'argomento, locko la lettura e inizio la ricerca 
		della prima entry corrispondente al nome logico indicato, partendo dal fondo di table.
		Quindi rilascio il lock e ritorno il riferimento remoto.
	*/
	public Remote cerca(String nomeLogico) throws RemoteException {
		Remote risultato = null;
		if( nomeLogico == null ) {
			return null;
		}
		
		try {
			lock.readLock().lock();
			for (int i = 0; i < tableSize; i++) {
				if (nomeLogico.equals((String) table[i][0])) {	/* Individuo la giusta entry con nomeLogico corrispondente*/
					risultato = (Remote) table[i][1];	/* Salvo il riferiemnto remoto da ritornare.*/
					break;
				}
			} 
		} finally {
			lock.readLock().unlock();
		}
		return risultato;
	}
	
	/* (CLIENT) RICERCA TUTTI I RIFERIMENTI REMOTI CHE HANNO UN NOMELOGICO UGUALE A QUELLO PASSATO COME ARGOMENTO.
		Dopo il controllo dell'argomento, preparo un array di riferimenti remoti da ritornare, quindi loccko la lettura,
		inizio a cercare le entry valide a partire dall'inizio di table, contando quante occorrenze vengono trovate.
		A questo punto inializzo l'array di riferimenti remoti con numero di elementi pari al numero di occorrenze trovate.
		Quindi riparto dall'inzio di table questa volta salvando i riferimenti nell'array.
		Rilascio il lock e ritorno l'array di riferiemnti.
	*/
	public Remote[] cercaTutti(String nomeLogico) throws RemoteException {
		int cont = 0;
		if(nomeLogico == null) {
			return new Remote[0];
		}
		
		Remote[] risultato;
		try {
			lock.readLock().lock();
			for (int i = 0; i < indexRef; i++) {
				if (nomeLogico.equals((String) table[i][0])) {
					cont++;
				}
			}
			risultato = new Remote[cont];
			cont = 0;
			for (int i = 0; i < indexRef; i++) {
				if (nomeLogico.equals((String) table[i][0])) {
					risultato[cont++] = (Remote) table[i][1];
				}
			} 
		} finally {
			lock.readLock().unlock();
		}
		return risultato;
	}
	
	/* (SERVER) OTTENIMENTO LISTA DI TUTTE LE COPPIE NOMELOGICO/RIFERIMENTO.
		Preparo una matrice di oggetti da ritornare.
		Loccko la lettura, quindi inizializzo la matrice con numero di righe (entry) pari al numero di entry riempite su table.
		Quindi partendo dall'inizio di table procedo col copiare tutte le entry nella matrice appena inizializzata.
		Rilascio il lock e ritorno la matrice.

	*/
	public Object[][] restituisciTutti() throws RemoteException {
		Object[][] risultato;
		try {
			lock.readLock().lock();
			risultato = new Object[indexRef][2];
			for (int i = 0; i < indexRef; i++) {
				risultato[i][0] = table[i][0];
				risultato[i][1] = table[i][1];
			} 
		} finally {
			lock.readLock().unlock();
		}
		return risultato;
	}
	/* (SERVER) ELIMANA DELLA PRIMA ENTRY CORRISPONDENTE AL NOME LOGICO DATO.
		Dopo avere controllato l'argomento, loccko in lettura controllo che sia presente una entry con nomeLogico uguale a quello nell'argormento.
		Se effettivamente è preente una entry da cancellare adesso è identificata nella entry i-esima, (in tutti i due casi rilascio il lock lettura) procedo locckando in scrittura
		diminuisco di 1 indexRef, quindi per eliminare l'i-esima entry (che è quella da eliminare) sovrascrivo l'ultima entry della yabella, (indexRef-1 esima entry), propio nella
		i-esima entry, in questo modo il buco viene immediatamente colmato, la entry elinata e l'indexRef già decrementato. Pongo a null la prima entry.
		Rilascio il lock e ritorno un risultato booleano. 
	*/
	public boolean eliminaPrimo(String nomeLogico) throws RemoteException {
			boolean risultato = false;
			int i = 0;
			if (nomeLogico == null) {
				return risultato;    
			}
			try {
				lock.readLock().lock();
				for (; i < indexRef; i++) {
					if (nomeLogico.equals((String) table[i][0])) {
						risultato = true;
						break;
					}
				}
			} finally {
				lock.readLock().unlock();
			}
			
			if (risultato) {
				try {
					lock.writeLock().lock();
					indexRef = indexRef - 1;
					table[i][0] = table[indexRef][0];
					table[i][1] = table[indexRef][1];
					table[indexRef][0] = null;
					table[indexRef][1] = null;
				} finally {
					lock.writeLock().unlock();
				}
			}
			
			return risultato;
	}
	
	/* (SERVER) ELIMINAZIONE DI TUTTE LE ENTRY REGISTRATE CON IL NOMELOGICO PASSATO COME ARGOMENTO.
		... inizio a controllare le corrispondenze dall'inizio della table, e ogni volta che trovo corrispondenza sovrascrivo l'ultima entry della tabella nella entry da 
		cancellare, pongo a null la prima entry. Procedo così fin quando ho consumato tutte le entry.
		Ritorno un risultato boolaeno.
	*/
	public boolean eliminaTutti(String nomeLogico) throws RemoteException {
		boolean risultato = false;
		if (nomeLogico == null) {
			return risultato;    
		}
		
		try {
			lock.readLock().lock();
			for (int i = 0; i < indexRef; i++) {
				if (nomeLogico.equals((String) table[i][0])) {
					risultato = true;
					try {
						lock.writeLock().lock();
						indexRef = indexRef - 1;
						table[i][0] = table[indexRef][0];
						table[i][1] = table[indexRef][1];
						table[indexRef][0] = null;
						table[indexRef][1] = null;
					} finally {
						lock.writeLock().unlock();
						lock.readLock().lock();
					}
				}
			} 
		} finally {
			lock.readLock().unlock();
		}
		return risultato;
	}
	/* (CLIENT) RICERCA PER TAG DEI NOMI LOGICI DEI SERVIZI CERCATI.
		Come argomento ho un Tag sotto forma di stringa, per prima cosa estraggo
		il corrispettivo intero che identifica quel Tag.

	*/
	public String[] cercaTag(String tag) throws RemoteException {
		try {
			lock.readLock().lock();
			int tagEq = Tag.valueOf(tag).getValue();																	/* */
			String result[] = new String[indexTag[tagEq]];
			for (int i = 0; i < indexTag[tagEq]; i++) {															/* Itero per il numero di indexTag relativo al corrispettivo Tag.*/
				result[i] = tagTable[tagEq][i];																	/* Salvo uno alla volta i nomiLogici presenti nella riga di tagTable riferita al tag desiderato.*/
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}
	

	
	/* (SERVER) ASSOCIAZIONE DI UN TAG AD UN NOME LOGICO GIA' REGISTRATO.
		Estraggo l'intero rappresentativo della stringa tag, 
		quindi associo il nome logico passato come parametro alla tagEq-esima riga di tableTab, 
		alla prima posizione libera di quella riga ovvero la indexTag[tagEq].
		Quindi maggioro di 1 l'indice indexTag[tagEq] e tirorno un booleano.

	*/
	public boolean associaTag(String nomeLogicoServer, String tag) throws RemoteException {
		if (nomeLogicoServer == null) {
			return false;
		}
		try {
			lock.writeLock().lock();
			int tagEq = Tag.valueOf(tag).getValue();
			tagTable[tagEq][indexTag[tagEq]] = nomeLogicoServer;
			indexTag[tagEq] = indexTag[tagEq] + 1;
		} finally {
			lock.writeLock().unlock();
		}
		return true;
	}
	
	/* Il main fa la bind su localhost porta 1099.*/
	public static void main(String args[]) {
		try {
			RegistryRemotoTagImpl serverRMI = new RegistryRemotoTagImpl();
			Naming.rebind("//" + registryRemotoHost + ":" + registryRemotoPort + "/" + registryRemotoName, serverRMI);
		} catch (Exception e) {
			System.out.println("Errore nell'inizializzazione");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
}
