public interface Services extends java.rmi.Remote {
	
	Response getDelete(String name, int row) throws java.rmi.RemoteException;
	
	int getCount(String name, int num) throws java.rmi.RemoteException;
	
}
