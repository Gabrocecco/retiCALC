import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Consumatore {
	public static void main(String[] args) {
		BufferedReader r = null;
		char ch;
		int x;
		boolean[] check = new boolean[256]; //Inizializzato a false
		
		if (args.length != 1 && args.length != 2){
			System.out.println("Numero di argomenti errato");
			System.exit(0);
		}
		
		if (args.length == 1) {
			r = new BufferedReader(new InputStreamReader(System.in));
		} else {
			try {
				r = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
			} catch (FileNotFoundException e) {
				System.out.println("File non trovato");
				System.exit(1);
			}
		}
		
		//Come nell'esercizio in C, per evitare di iterare ogni volta su args[0], ovvero il prefisso
		
		for (int i = 0; i < args[0].length(); i++) {
			check[(int) args[0].charAt(i)] = true;
		}
		
		try {
			while ((x = r.read()) >= 0) { 
				ch = (char) x;
				if (!check[x]) {
					System.out.print(ch);
				}
			}
			r.close();
		} catch(IOException ex){
			System.out.println("Errore di input");
			System.exit(2);
		}
}}
