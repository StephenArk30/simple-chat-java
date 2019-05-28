package Net;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

class Handler extends Thread{
	Socket socket;
	Socket send;
	PrintWriter pwtoclien = null;
	Scanner inScanner = null;
	
	Handler(Socket mys, Socket sends) {
		socket = mys;
		send = sends;
		try {
			pwtoclien = new PrintWriter(socket.getOutputStream());
			pwtoclien.println("已成功连接到远程服务器！");
			pwtoclien.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void setSend(Socket sends) { send = sends; }
	
	@Override
	public void run() {
		try {
			inScanner = new Scanner(socket.getInputStream());
			//阻塞等待客户端发送消息过来
			while(inScanner.hasNextLine()){
				String indata = inScanner.nextLine();
				System.out.println(indata);
				if(send != null) {
					pwtoclien = new PrintWriter(send.getOutputStream());
					pwtoclien.println(indata);
					pwtoclien.flush();
				}
				else {
					pwtoclien = new PrintWriter(socket.getOutputStream());
					pwtoclien.println("对方未上线！");
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
	public static final int SERVICE_PORT = 8080;
	static Handler c1 = null;
	static Handler c2 = null;
	
	static void runServer() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(SERVICE_PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("服务器已打开");
		while(true) {
			try {
				Socket socket = ss.accept();
				System.out.println(socket.getInetAddress()+"已成功连接到此台服务器上。");
				if (c1 == null) {
					c1 = new Handler(socket, null);
					c1.start();
				}
				else if(c2 == null) {
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
