import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerCongressoImpl extends UnicastRemoteObject implements ServerCongresso {
	
	private static final long serialVersionUID = 1L;
	
	static Programma prog[];

	public ServerCongressoImpl() throws RemoteException {
		super();
	}

	public int registrazione(int giorno, String sessione, String speaker) throws RemoteException {
	  	int numSess = -1;
	  	System.out.println("Server RMI: richiesta registrazione con parametri");
	  	System.out.println("giorno   = " + giorno);
	  	System.out.println("sessione = " + sessione);
	  	System.out.println("speaker  = " + speaker);

	  	if (sessione.startsWith("S")) {
	  		try {
	  			numSess = Integer.parseInt(sessione.substring(1)) - 1;
	  		} catch (NumberFormatException e) {
	  		}
	  	}

    // Se i dati sono sbagliati significa che sono stati trasmessi male e quindi
    // solleva una eccezione
	  	if (numSess == -1)
	  		throw new RemoteException();
	  	if (giorno < 1 || giorno > 3)
	  		throw new RemoteException();

    	return prog[giorno - 1].registra(numSess, speaker);
    }


    public Programma programma(int giorno) throws RemoteException {
    	System.out.println("Server RMI: richiesto programma del giorno " + giorno);
    	return prog[giorno - 1];
    }

  
   public static void main(String[] args) {

	   prog = new Programma[3];
	   for (int i = 0; i < 3; i++)
		   prog[i] = new Programma();
	   int registryRemotoPort = 1099;
	   String registryRemotoName = "RegistryRemoto";
	   String serviceName = "ServerCongresso";
	   
	   String[] tags = null;

    // Controllo dei parametri della riga di comando
	   if (args.length == 0) {
		   System.out.println("Inserire il nome dell'host remoto e, opzionalmente, la porta e i tag");
		   System.exit(1);
	   }
	   String registryRemotoHost = args[0];
	   if (args.length >= 2) {
		   try {
			   registryRemotoPort = Integer.parseInt(args[1]);
		   } catch (Exception e) {
			   System.out.println("Inserire il nome dell'host remoto e, opzionalmente, la porta e i tag");
			   System.exit(2);
		   }
		   /* -----------------------------------------------------------------------------------------------------------*/
		   if (args.length >= 3) {
			   tags = new String[args.length - 2];
			   for (int i = 2; i < args.length; i++) {
				   tags[i - 2] = args[i];
			   }
		   }
	   }	/* -----------------------------------------------------------------------------------------------------------*/

	   // Impostazione del SecurityManager
	  /* if (System.getSecurityManager() == null) {
		   System.setSecurityManager(new RMISecurityManager());
	   }*/

	   // Registrazione del servizio RMI
	   String completeRemoteRegistryName = "//" + registryRemotoHost + ":" + registryRemotoPort + "/" + registryRemotoName;

	   try {
		   RegistryRemotoTagServer registryRemoto = (RegistryRemotoTagServer) Naming.lookup(completeRemoteRegistryName);
		   ServerCongressoImpl serverRMI = new ServerCongressoImpl();
		   registryRemoto.aggiungi(serviceName, serverRMI);
		   if (args.length >= 3) {
			   for (int i = 0; i < tags.length; i++) {
				   registryRemoto.associaTag(serviceName, tags[i]);
			   }
		   }
		   System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
	   } catch (Exception e) {
		   System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
		   e.printStackTrace();
		   System.exit(1);
	   }
    }
}