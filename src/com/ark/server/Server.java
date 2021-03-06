package com.ark.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

class Handler extends Thread{
	Socket socket;
	private Socket send;
	private PrintWriter pwtoclien = null;
	private Scanner inScanner = null;
	
	Handler(Socket mys, Socket sends) {
		socket = mys;
		send = sends;
		try {
			pwtoclien = new PrintWriter(socket.getOutputStream());
			pwtoclien.println("connected to server");
			pwtoclien.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void setSend(Socket sends) { send = sends; }

	void sendFile() {
		try {
			BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
			OutputStream out = send.getOutputStream();
			int len = 0;
			while (true) {
				byte[] buf = new byte[1024];
				if ((len = bin.read(buf)) == -1) break;
				String line = new String(buf);
				line = line.replaceAll("\n", " ");
				line = line.replaceAll("\r", " ");
				System.out.println("||" + line + "||");
				out.write(buf, 0, len);
				out.flush();
				if (line.matches(".*\\$EOF\\$.*")) break;
			}
			System.out.println("file send complete");
		} catch (IOException except) {
			return;
		}
	}
	
	@Override
	public void run() {
		try {
			inScanner = new Scanner(socket.getInputStream());
			while(inScanner.hasNextLine()){
				String indata = inScanner.nextLine();
				System.out.println(indata);
				if(send != null) {
					if (indata.startsWith("$FILE$")) {
						pwtoclien = new PrintWriter(send.getOutputStream());
						pwtoclien.println(indata);
						pwtoclien.flush();
						sendFile();
					} else {
						pwtoclien = new PrintWriter(send.getOutputStream());
						pwtoclien.println(indata);
						pwtoclien.flush();
					}
				} else {
					pwtoclien = new PrintWriter(socket.getOutputStream());
					pwtoclien.println("your friend is offline!");
					pwtoclien.flush();
				}
			}
		} catch(IOException e1) {
			e1.printStackTrace();
		} finally {
			try{
				pwtoclien.close();
				inScanner.close();
			} catch(Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
}

public class Server {
	private static final int SERVICE_PORT = 8080;
	private static Handler c1 = null;
	private static Handler c2 = null;

	private static void runServer() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(SERVICE_PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("server start failed");
			return;
		}
		System.out.println("server start");
		while (true) {
			try {
				Socket socket = ss.accept();
				System.out.println(socket.getInetAddress() + " connected");
				if (c1 == null) {
					c1 = new Handler(socket, null);
					c1.start();
				} else if (c2 == null) {
					c2 = new Handler(socket, c1.socket);
					c1.setSend(socket);
					c2.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		runServer();
	}

}
