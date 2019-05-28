package Net;

import java.util.Scanner;

public class Test {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("请输入用户名");
		String name = sc.next();
		System.out.println("请输入连接地址");
		String[] ip = sc.next().split(":");
		String host = ip[0];
		int port = 80;
		if(ip.length > 1) port = Integer.valueOf(ip[1]);
		Client client = new Client(host, port, name);
		client.run();
		sc.close();
	}

}
