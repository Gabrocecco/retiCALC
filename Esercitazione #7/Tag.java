
public enum Tag {
	Utility(0),
	Echo(1),
	FileTransfer(2),
	Help(3),
	Ping(4),
	Test(5),
	Time(6),
	Status(7),
	Rating(8),
	Query(9);
	
	private int value;
	
	private Tag(int tag) {
		this.value = tag;
	}
	
	public int getValue() {
		return value;
	}
}
