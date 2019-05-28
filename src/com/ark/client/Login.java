package com.ark.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Scanner;

public class Login extends JFrame implements ActionListener {

	protected static Thread thread = null;
	protected JLabel lbUserName, lbHost;
	protected JTextField txtUserName;
	protected JTextField txtHost;
	protected JButton btnLogin, btnReset;

	public Login() {
		super("Login");
		initComponent();
	}

	public static void main(String[] args) {
		new Login();
	}

	public void initComponent() {
		lbUserName = new JLabel("username");
		lbHost = new JLabel("host");

		txtUserName = new JTextField(10);
		txtHost = new JTextField(10);

		btnLogin = new JButton("Sign in");
		btnReset = new JButton("Reset");

		btnLogin.addActionListener(this);
		btnReset.addActionListener(this);

		this.setLayout(new GridLayout(3, 2));
		this.add(lbUserName);
		this.add(txtUserName);
		this.add(lbHost);
		this.add(txtHost);
		this.add(btnLogin);
		this.add(btnReset);

		txtUserName.setFocusable(true);

		this.setSize(300, 200);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		JButton btn = (JButton) e.getSource();

		if (btn == btnLogin) {
			if (txtUserName.getText().equals("") || txtUserName.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(this, "please input username", "empty username",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (txtHost.getText().equals("")) {
				JOptionPane.showMessageDialog(this, "please input host", "empty host", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String userName;
			String url;
			userName = txtUserName.getText().trim();
			url = txtHost.getText();

			String[] ip = url.split(":");
			String host = ip[0];
			int port = 8080;
			if (ip.length > 1) port = Integer.valueOf(ip[1]);

			this.dispose();
			Client client = new Client(host, port, userName);
			client.initComponent();
			JOptionPane.showMessageDialog(this, "welcome, " + userName, "welcome", JOptionPane.INFORMATION_MESSAGE);
			client.start();
		} else if (btn == btnReset) {
			txtUserName.setText("ark");
			txtHost.setText("127.0.0.1:8080");
			txtUserName.setFocusable(true);
		}
	}
}
