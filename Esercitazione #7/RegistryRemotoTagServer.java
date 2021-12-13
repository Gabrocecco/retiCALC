import java.rmi.RemoteException;

public interface RegistryRemotoTagServer extends RegistryRemotoServer, RegistryRemotoTagClient {
	boolean associaTag(String nomeLogicoServer, String tag) throws RemoteException;
}
