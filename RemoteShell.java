package wrz;



import java.io.BufferedReader;

import java.io.BufferedWriter;

import java.io.IOException;

import java.io.InputStreamReader;

import java.io.InputStream;

import java.io.PrintWriter;

import java.io.OutputStream;

import java.io.OutputStreamWriter;

import java.net.ServerSocket;

import java.net.Socket;

import java.net.SocketException;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;



import javax.swing.plaf.basic.BasicBorders.SplitPaneBorder;



public class RemoteShell {

	

	public int port;

	public ServerSocket server;

	private static String[] signals = { "ls", "pwd", "nc" };

	public static Socket socket;

	private static String username = "semaphore";

	private static String password = "signalperson";



	RemoteShell(int port) throws SocketException {

		this.port = port;

		if (!createServer())

			System.out.println("Cannot start the server");

		else

			System.out.println("Server running on port " + port);

	}



	public boolean createServer() throws SocketException {

		try {

			server = new ServerSocket(port,200);

		} catch (IOException e) {

			e.printStackTrace();

			return false;

		}



		return true;

	}



	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {



		RemoteShell tcp = new RemoteShell(1337);

		

		while (true) {



			try {

		    socket = tcp.server.accept();

			

			System.out.println("A client has connected");

			System.out.println(token("semaphore" + "signalperson"));

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			try {

			out.write("# WARZONE 2 # WARZONE 2 # WARZONE 2 #\n");

			out.write("{SECRET SYSTEM REMOTE ACCESS}\n");

			out.flush();

			} catch (SocketException e) {

				   socket.getOutputStream().close();

				   socket = tcp.server.accept();

				}



			

			boolean login = login(out, in);

			

			

			while(!login) {

				

			 login = login(out, in);

			}



			if(login) {



				out.write("Success Login\n");

				out.write("[SIGNALS] { ls, pwd, nc}\n");

				out.write("[semaphore] > ");

				out.flush();



				try {

					String cmd;

					while (!(cmd = in.readLine()).equals("END")) {



						System.out.println("Recieved: " + cmd);



						boolean cmdCheck = cmdChecker(cmd);



						if (cmdCheck){



							out.write("[+] Recognized signal\n");

							out.flush();

							out.write("[+] sending......\n");

							out.flush();

							Process p = Runtime.getRuntime().exec(cmd);

							BufferedReader pRead = new BufferedReader(new InputStreamReader(p.getInputStream()));



							String line;

							while ((line = pRead.readLine()) != null) {

								System.out.println(line);

								out.write(line + "\n");

								out.flush();

							}

							out.write("[semaphore] > ");

							out.flush();

						}

						else {

							

							try {

								

							out.write("Wrong signal\n");

							out.write("[semaphore] > ");

							out.flush();

							

							} catch (SocketException e) {

							   socket.getOutputStream().close();

							}

						}

					}

				} catch (IOException ex) {

					

					try {

					out.write("[!] IOException : Please use a valid signal from list\n");

					out.write("[semaphore] > ");

					out.flush();

					} catch (SocketException e) {

						   socket.getOutputStream().close();

						}

					

				}

				catch (NullPointerException n) {

					

					socket.getInputStream().close();

				}

			

			}

			

			} catch (SocketException e) {

				   socket.getOutputStream().close();

				}

			catch (NullPointerException n) {

				

				socket.getInputStream().close();

			}

		

		}

			

	

	}



	static boolean cmdChecker(String userInput) {



	



		for (String s : signals) {

			if(!s.equals("nc")) {

			if(userInput.equals(s)) {

				

				return true;

			}

			}

			else if(userInput.contains(s)) {

			

				return true;

			}

		}



		

		return false;

	}



	static boolean login(BufferedWriter out, BufferedReader in) throws IOException, NoSuchAlgorithmException {



		out.write("Username :");

		out.flush();

		String user = in.readLine();

		out.write("Password :");

		out.flush();

		String pass = in.readLine();

		out.write("Token :");

		out.flush();

		String token = in.readLine();



		return auth(user, pass, token);



	}



	static boolean auth(String u, String p, String token) throws NoSuchAlgorithmException {



		String systemToken = token("semaphore" + "signalperson");



		if (u.equals(username) && p.equals(password) && token.equals(systemToken)) {



			return true;

		}



		return false;



	}



	static String token(String originalString) throws NoSuchAlgorithmException {



		MessageDigest digest = MessageDigest.getInstance("SHA-256");

		byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));



		StringBuilder hexString = new StringBuilder(2 * encodedhash.length);

		for (int i = 0; i < encodedhash.length; i++) {

			String hex = Integer.toHexString(0xff & encodedhash[i]);

			if (hex.length() == 1) {

				hexString.append('0');

			}

			hexString.append(hex);

		}

		return hexString.toString();



	}



}
