package com.ark.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.*;

public class Client extends Thread{
	public static int SERVICE_PORT = 8080;
	public static String IP = "localhost";
	static String username = "";
	
	static PrintWriter pwtoserver = null;
	
	static JTextArea messages = new JTextArea();
	JTextField text = new JTextField();
	ActionListener sendListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = username + ": " + text.getText();
			messages.append(message + "\n");
			try{
				pwtoserver.println(message);
				pwtoserver.flush();
			} catch(Exception err) {
				messages.append("network error!\n");
			}
		}
	};
	
	public Client(String ip, int port, String name) {
		IP = ip;
		SERVICE_PORT = port;
		username = name;
	}

    public void run() {
        System.out.println("connecting to server " + IP + ":" + SERVICE_PORT + "...");
		Socket socket = null;
		Scanner inScanner = null;
		try {
			socket = new Socket(IP, SERVICE_PORT);
            inScanner = new Scanner(socket.getInputStream());
			System.out.println(inScanner.nextLine());
			pwtoserver = new PrintWriter(socket.getOutputStream());
			
			while(true) {
				String indata = inScanner.nextLine();
				messages.append(indata + "\n");
			}
		} catch (UnknownHostException e) {
			System.out.println("connection failed!");
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public void initComponent() {
		JFrame frame = new JFrame(username);
		frame.getContentPane().setLayout(new BorderLayout());
		messages.setEditable(false);

        JPanel textarea = new JPanel();
		textarea.setLayout(new BorderLayout());
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(sendListener);
		textarea.add(text, BorderLayout.CENTER);
		textarea.add(sendButton, BorderLayout.EAST);

        frame.add(messages, BorderLayout.CENTER);
		frame.add(textarea, BorderLayout.SOUTH);
		frame.setSize(280, 400);

        frame.setVisible(true);
	}
}
