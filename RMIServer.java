import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RMIServer extends UnicastRemoteObject implements Services {
	
	private static final long serialVersionUID = 1L;

	private final static int BLOCK_SIZE = 8196;
	
	private Map<String, ReadWriteLock> locks = new HashMap<String, ReadWriteLock>();
	
	private ReadWriteLock addLock = new ReentrantReadWriteLock();
	private Lock addLockRead = addLock.readLock();
	private Lock addLockWrite = addLock.writeLock();
	
	
	public RMIServer() throws java.rmi.RemoteException {
		super();
	}

	@Override
	public Response getDelete(String name, int row) throws RemoteException {
		
		FileInputStream fs = null;
		FileOutputStream fo = null;
		File newFile = null;
		File file = null;
		int letti = 0;
		int righe = 1;
		int pos;
		int mark;
		int fineLinea;
		
		byte[] buffer = new byte[BLOCK_SIZE];
		
		file = new File(name);
		ReadWriteLock fileLock = null;
		
		try {
			fs = new FileInputStream(name);
		} catch (FileNotFoundException e) {
			System.out.println("Errore nell'apertura del file");
			e.printStackTrace();
			throw new RemoteException("File non trovato");
		}
		
		newFile = new File(name + Thread.currentThread().getId());
		
		try {
			if (!newFile.createNewFile()) {
				System.out.println("Impossibile creare il file d'appoggio");
				fs.close();
				throw new RemoteException("Errore nel server");
			}
			fo = new FileOutputStream(newFile);
		} catch (IOException e) {
			System.out.println("Errore di I/O");
			try {
				fs.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new RemoteException("Errore nel server");
		}


		try {
			addLockRead.lock();
			while ((fileLock = locks.get(name)) == null) {
				addLockRead.unlock();
				try {
					addLockWrite.lock();
					locks.put(name, new ReentrantReadWriteLock());
				} finally {
					addLockWrite.unlock();
					addLockRead.lock();
				}
			} 
		} finally {
			addLockRead.unlock();
		}
		
		try {
			fileLock.readLock().lock();
			while ((letti = (buffer = fs.readNBytes(BLOCK_SIZE)).length) > 0) {
				pos = 0;
				mark = 0;
				fineLinea = 0;
				while (pos < letti) {
					if ((char) buffer[pos] == '\n') {
						righe++;
					}
					pos++;
					if (righe == row) {
						mark = pos;
						while (pos < letti && (char) buffer[pos] != '\n') {
							pos++;
						}
						if (pos < letti) {
							righe++;
							fineLinea = pos + 1;
						}
						fo.write(buffer, 0, mark);
					}
				}
				fo.write(buffer, fineLinea, letti - fineLinea);
			}
			file.delete();
			newFile.renameTo(file);
		} catch (IOException e) {
			System.out.println("Errore nella lettura");
			e.printStackTrace();
			throw new RemoteException("Errore nel server");
		} finally {
			fileLock.readLock().unlock();
		}
		
		try {
			fs.close();
			fo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (righe < row) {
			throw new RemoteException("Il file ha meno righe di quelle indicate");
		} else {
			return new Response(name, righe - 1);
		}
		
	}

	@Override
	public int getCount(String name, int num) throws RemoteException {
		
		int count = 0;
		int words = 0;
		int pos;
		int letti;
		byte[] buffer = new byte[BLOCK_SIZE];
		FileInputStream fs = null;
		char lastChar = ' ';
		
		ReadWriteLock fileLock = null;
		
		try {
			fs = new FileInputStream(name);
		} catch (FileNotFoundException e) {
			System.out.println("Errore nell'apertura del file");
			e.printStackTrace();
			throw new RemoteException("File inesistente");
		}
		
		try {
			addLockRead.lock();
			while ((fileLock = locks.get(name)) == null) {
				addLockRead.unlock();
				try {
					addLockWrite.lock();
					locks.put(name, new ReentrantReadWriteLock());
				} finally {
					addLockWrite.unlock();
					addLockRead.lock();
				}
			} 
		} finally {
			addLockRead.unlock();
		}
		
		try {
			fileLock.readLock().lock();
			while ((letti = (buffer = fs.readNBytes(BLOCK_SIZE)).length) > 0) {
				pos = 0;
				while (pos < letti) {
					if ((char) buffer[pos] == '\n') {
						if (pos != 0 && (char) buffer[pos - 1] != ' ' && (char) buffer[pos - 1] != '\n') {
							words++;
						}
						if (words > num) {
							count++;
						}
						words = 0;
					} else if ((char) buffer[pos] == ' ') {
						if (pos == 0) {
							if (lastChar != ' ') {
								words++;
							}
						} else if ((char) buffer[pos - 1] != ' ' && (char) buffer[pos - 1] != '\n') {
							words++;
						}
					}
					pos++;
				}
				lastChar = (char) buffer[letti - 1];
			}
		} catch (IOException e) {
			System.out.println("Errore nella lettura");
			e.printStackTrace();
			throw new RemoteException("Errore nel server");
		} finally {
			fileLock.readLock().unlock();
		}
		
		
		try {
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return count;
	}
	
	public static void main(String[] args) {
		try {
			RMIServer server = new RMIServer();
			Naming.rebind("//localhost:1099/Services", server);
		} catch (Exception e) {
			System.out.println("Errore nell'inizializzazione del server");
			e.printStackTrace();
			System.exit(0);
		}
	}


}