package es1_proposta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
	// > IPDS portDS filename
	public static void main(String[] args) {
		
		InetAddress addr = null;
		int port = -1;
		String nomeFile = null;
		
		try {
			if (args.length == 3) {	//controllo argomenti e salvataggio IPDS porta e nomeFile.
				addr = InetAddress.getByName(args[1]);
				port = Integer.parseInt(args[2]);
				nomeFile = args[3];
			} else {
				System.out.println("Numero argomenti sbaglaiti!");
				System.out.println("Client: interrompo...");
				System.exit(1);
			}
		} catch (UnknownHostException e) {
			System.out.println("Problemi con la determinazione del discovery server!");
			e.printStackTrace();
			System.out.println("Client: interrompo...");
			System.exit(2);
		}
		
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];
		
		try {	//creo il pacchetto e la socket per parlare con il DS 
			socket = new DatagramSocket();
			packet = new DatagramPacket(buf, buf.length, addr, port);	//apro la socket verso il DS con addr e port date da argoemento.
			System.out.println("\nClient: avviato");
			System.out.println("Creata la socket: " + socket);
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.out.println("Client: interrompo...");
			System.exit(1);
		}
		
		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		byte[] data = null;
		String richiestaDS = null;
		String rispostaDS = null;
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		int portaRW = -1;
		
		// riempimento e invio del pacchetto verso il DS.
		try {
			richiestaDS = nomeFile;	//il messaggio verso il DS è semplicemente il nome del file.
			
			boStream = new ByteArrayOutputStream();
			doStream = new DataOutputStream(boStream);
			doStream.writeUTF(richiestaDS);
			data = boStream.toByteArray();	//preparo i dati del pacchetto 
			packet.setData(data);
			socket.send(packet);	//invio il pacchetto verso il DS.
			System.out.println("Richiesta inviata a " + addr + ", " + port);
		} catch (IOException e) {
			System.out.println("Problemi nell'invio della richiesta al DS: ");
			e.printStackTrace();
			System.exit(3);
		}
		//ricezione della risposta dal DS contenente la porta del RawSwapServer che ha il file in questione. 
		try {
			// settaggio del buffer di ricezione
			packet.setData(buf);
			socket.receive(packet);
		} catch (IOException e) {
			System.out.println("Problemi nella ricezione del datagramma del DS: ");
			e.printStackTrace();
			System.exit(4);
		}
		
		try {
			biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
			diStream = new DataInputStream(biStream);
			rispostaDS = diStream.readUTF();	//leggo la risposta ovvero la porta e la salvo come intero.
			portaRW = Integer.parseInt(rispostaDS);
			System.out.println("Risposta DS: " + rispostaDS);
		} catch (IOException e) {
			System.out.println("Problemi nella lettura della risposta DS: ");
			e.printStackTrace();
		}
		
		//System.out.println("LineClient: termino...");
		//socket.close();
	}
}
