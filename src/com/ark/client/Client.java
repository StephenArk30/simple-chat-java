package com.ark.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

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
                    while ((len = bin.read(buf)) != -1) {
                        System.out.println(new String(buf));
                        out.write(buf, 0, len);
                    }
                    String eof = "$EOF$";
                    buf = eof.getBytes();
                    out.write(buf, 0, buf.length);
                    out.flush();
                    System.out.println("file upload succeed");
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
                        boolean res = recieveFile(indata.substring(6));
                        if (res) messages.append(indata.substring(6) + "\n");
                        else messages.append(indata.substring(6) + " RECEIVE FAILED！\n");
                    } else messages.append(indata + "\n");
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

        File file = new File(dir, fileName);
        if (file.exists()) file = new File(dir, fileName);

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
            while (true) {
                buf = new byte[1024];
                if ((len = bin.read(buf)) == -1) break;
                String line = new String(buf);
                line = line.replaceAll("\n", " ");
                line = line.replaceAll("\r", " ");
                System.out.println("||" + line + "||");
                if (line.matches(".*\\$EOF\\$.*")) break;
                fout.write(buf, 0, len);
            }
            fout.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                fout.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }
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

        JScrollPane messagewin = new JScrollPane
                (messages, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(messagewin, BorderLayout.CENTER);
		frame.add(textarea, BorderLayout.SOUTH);
		frame.setSize(280, 400);

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        frame.setVisible(true);
	}
}
