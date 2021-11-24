import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = 1;
	
	public String name;
	public int num;

	public Response(String name, int num) {
		this.name = name;
		this.num = num;
	}
	
}
