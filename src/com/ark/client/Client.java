package com.ark.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client extends Thread{
    private static int SERVICE_PORT = 8080;
    private static String IP = "localhost";
    private static String username = "";

    private Socket socket = null;
    private static PrintWriter pwtoserver = null;

    private static JTextArea messages = new JTextArea();
    private JTextField text = new JTextField();
    private ActionListener sendListener = new ActionListener() {

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
    private ActionListener sendFileListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                String FilePath = chooser.getSelectedFile().getPath();
                String FileName = FilePath.substring(FilePath.lastIndexOf("\\") + 1);
                System.out.println(FilePath);
                messages.append(FileName + "\n");
                try {
                    pwtoserver.println("$FILE$" + FileName);
                    pwtoserver.flush();
                    BufferedInputStream bin = new BufferedInputStream(new FileInputStream(FilePath));
                    OutputStream out = socket.getOutputStream();
                    byte[] buf = new byte[1024];
                    int len = 0;
                    while ((len = bin.read(buf)) != -1)
                        out.write(buf, 0, len);

                    socket.shutdownOutput(); // upload complete
                } catch (Exception err) {
                    messages.append("network error!\n");
                }
            }
        }
    };

    Client(String ip, int port, String name) {
		IP = ip;
		SERVICE_PORT = port;
		username = name;
	}

    public void run() {
        System.out.println("connecting to server " + IP + ":" + SERVICE_PORT + "...");
		Scanner inScanner = null;
		try {
			socket = new Socket(IP, SERVICE_PORT);
            inScanner = new Scanner(socket.getInputStream());
			System.out.println(inScanner.nextLine());
			pwtoserver = new PrintWriter(socket.getOutputStream());

            while (true) {
                try {
                    String indata = inScanner.nextLine();
                    if (indata.startsWith("$FILE$")) {
                        boolean res = recieveFile(indata.substring(5));
                    }
                    messages.append(indata + "\n");
                } catch (NoSuchElementException exception) {
                }
			}
		} catch (UnknownHostException e) {
			System.out.println("connection failed!");
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    boolean recieveFile(String fileName) {
        BufferedInputStream bin = null;
        try {
            bin = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File dir = new File("C:\\java-chat-download");

        if (!dir.exists()) dir.mkdir(); // create folder if not exist

        // read filename
        InputStreamReader insr = new InputStreamReader(bin);

        int count = 1;
        //我觉得这里的后缀名，需要通过发送方也发过来的
        File file = new File(dir, fileName);
        if (file.exists()) file = new File(dir, fileName); //带号的文件名

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        // read file from socket
        byte[] buf = new byte[1024];
        int len = 0;
        try {
            bin.read(buf, 0, 8);
            while ((len = bin.read(buf)) != -1) {
                fout.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void initComponent() {
		JFrame frame = new JFrame(username);
		frame.getContentPane().setLayout(new BorderLayout());
		messages.setEditable(false);

        JPanel textarea = new JPanel();
		textarea.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
		JButton sendButton = new JButton("Send");
        JButton fileButton = new JButton("Send File");
		sendButton.addActionListener(sendListener);
        fileButton.addActionListener(sendFileListener);
        buttonPanel.add(fileButton, BorderLayout.WEST);
        buttonPanel.add(sendButton, BorderLayout.EAST);

		textarea.add(text, BorderLayout.CENTER);
        textarea.add(buttonPanel, BorderLayout.EAST);

        frame.add(messages, BorderLayout.CENTER);
		frame.add(textarea, BorderLayout.SOUTH);
		frame.setSize(280, 400);

        frame.setVisible(true);
	}
}
