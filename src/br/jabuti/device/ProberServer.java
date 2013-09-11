/*  Copyright 2003  Auri Marcelo Rizzo Vicenzi, Marcio Eduardo Delamaro, 			    Jose Carlos Maldonado

    This file is part of Jabuti.

    Jabuti is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as 
    published by the Free Software Foundation, either version 3 of the      
    License, or (at your option) any later version.

    Jabuti is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Jabuti.  If not, see <http://www.gnu.org/licenses/>.
*/


/*
 * Created on 14/09/2005
 *
 * 
 */
package br.jabuti.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;

import br.jabuti.probe.ProbedNode;
import br.jabuti.util.ToolConstants;

public class ProberServer extends Thread {

	private static Hashtable devices, probes;
    static public final String delimiter = "**********************";
    static public final String READER_CLASS = "class br.jabuti.probe.DefaultTraceReader";

	ServerSocket serverSocket;

	public ProberServer(ServerSocket sk) {
		serverSocket = sk;
	}

	public void run()
	{
		System.out.println("Listening to " + serverSocket.getLocalPort());
		System.out.println("Local address: " + serverSocket.getLocalSocketAddress());
		while (true)
		{
			Socket s;
			try {
				s = serverSocket.accept();
				System.out.println("Incoming connection " + s.getRemoteSocketAddress());
				DeviceConnection conn = new DeviceConnection(s);
				conn.start();
			} catch (IOException e) {
				System.err.println("I/O error on server socket accept.");
			}
		}
	}
	/**
	 * @param args - 0 - porta a acoplar; 1 - nome arq. de config.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if ( "TestPort".equals(args[0]) )
		{
			testPort(args[1], Integer.parseInt(args[2]));
			return;
		}
		if ( args.length != 2)
		{
			usage();
			return;
		}
		int port = Integer.parseInt(args[0]);
		startServer(port, args[1]);
	}
	
	/**
	 * @param string
	 */
	private static void testPort(String string, int port) {
		try {
			Socket sk = new Socket(string, port);
			OutputStream os = sk.getOutputStream();
			PrintStream ps = new PrintStream(os);
			ps.println("Teste sucedido.");
			ps.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private static void usage() {
        System.out.println(ToolConstants.toolName + " v. " + ToolConstants.toolVersion);
        System.out.println("\nProberServer usage:");
        System.out.println("-------------------\n");
        System.out.println("java probe.ProberServer <port> <filename> \n");
        System.out.println("    <port>              Port to which the server will listen");
		System.out.println("    <filename>          Specifies the name of the configuration file .");
	}

	public static void startServer(int port, String configFile) throws IOException
	{
		devices = new Hashtable();
		probes = new Hashtable();
	   	File f = new File(configFile);
		BufferedReader br = new BufferedReader(new FileReader(f)); 
		for (String s = br.readLine(); s != null; s = br.readLine())
		{
			if (s.trim().length() <= 0)
				continue;
			String x = br.readLine();
			if ( x == null)
				break;
			devices.put(s.trim(), x.trim());
			System.out.println("\tProject: " + s.trim() + " file: " + x.trim());
		}
		br.close();
		ServerSocket sk = new ServerSocket(port);
		ProberServer ps = new ProberServer(sk);
		ps.start();
	}

	
	class DeviceConnection extends Thread 
	{
		BufferedReader br;
		PrintStream ps = null;
		Hashtable threadsAndProbes = null;
		
		public DeviceConnection(Socket s) throws IOException {
			InputStream is = s.getInputStream();
			br = new BufferedReader(new InputStreamReader(is)); 
		}
		
		public void run()
		{
			try {
				String line = br.readLine();
				System.out.println(line);
				if ( line == null )
					return;
				line = line.trim();
				if (! line.startsWith("Id:"))
					return;
				String nome = line.substring(3).trim();
				String outFile = (String) devices.get(nome);
				threadsAndProbes = (Hashtable) probes.get(nome);
				if ( threadsAndProbes == null)
				{
					threadsAndProbes = new Hashtable();
					probes.put(nome, threadsAndProbes);
				}
				if ( outFile == null )
				{
					System.out.println("Not found program: " + nome);
					System.out.println("Will write to stdout.");
					ps = System.out;
				}
				else
				{
			        RandomAccessFile raf = new RandomAccessFile(outFile, "rw");

			        raf.seek(raf.length());
			        FileOutputStream fos = new FileOutputStream(raf.getFD());
			        ps = new PrintStream(fos);
				}
				do 
				{
					String thread = br.readLine();
					System.out.println("Thread " + thread);
					if ( thread == null ) break;
					thread = thread.trim();
					if ( ("EndTestCase "+ nome).equals(thread))
					{
						dump();
						probes.remove(nome);
						break;
					}
					String obj = br.readLine();
					System.out.println(obj);
					if ( obj == null ) break;
					obj = obj.trim();
					String clazz = br.readLine();
					System.out.println(clazz);
					if ( clazz == null ) break;
					clazz = clazz.trim();
					String metodo = br.readLine();
					System.out.println(metodo);
					if ( metodo == null ) break;
					metodo = metodo.trim();
					int m = Integer.parseInt(metodo);
					String nest = br.readLine().trim();
					while ( nest != null && ! "-1".equals(nest) )
					{
						probe(thread, obj, clazz, m, nest.trim());
						nest = br.readLine();
					}
				} while (true);
				ps.close();
				ps = null;
			}
			catch (Exception e)
			{
				e.printStackTrace(); // termina o procedimento
			}
			finally {
				try {
					br.close();
				} catch (IOException e) {
					;
				}
				if ( ps != null ) ps.close();
			}
		}
		
	   public void dump() {
	        if (ps == null) {
	            return;
	        }
	        System.out.println("dump " + threadsAndProbes.size());
	        Enumeration en = threadsAndProbes.keys();

	        while (en.hasMoreElements()) {
	            ProbedNode tr = (ProbedNode) en.nextElement();

	            dumpNodes(tr, (ArrayList) threadsAndProbes.get(tr));
	        }
	        // write a delimiter 
	        ps.println(delimiter);
	        ps.flush(); // Inseri um flush para descarregar o buffer.
	    }
		
	   /** This method registers the execution of a given node */
	    public void probe(String tr,
	    				  String o, 
	    				  String clazz, 
	    				  int metodo, 
	    				  String nest) 
	    {    		
	        ProbedNode pb = new ProbedNode(tr, o, clazz, metodo, "");
	        ArrayList probedNodes;

	        if (threadsAndProbes.containsKey(pb)) {
	            probedNodes = (ArrayList) threadsAndProbes.get(pb);
	        } else {
	            probedNodes = new ArrayList();
	            threadsAndProbes.put(pb, probedNodes);
	        }
	        probedNodes.add(nest);
	    }	

	    public void dumpNodes(ProbedNode pbdNode, ArrayList probedNodes) {
	        try {
	        	ps.println(READER_CLASS);
	        	ps.println(); // linha em branco que representa o nome
	            ps.println(pbdNode.threadCode);
	            ps.println(pbdNode.objectCode);
	            ps.println(pbdNode.clazz);
	            ps.println(pbdNode.metodo);
	            ListIterator li = probedNodes.listIterator();

	            while (li.hasNext()) {
	                Object o = li.next();

	                ps.println(o);
	            }
	            ps.println("-1");
	        } catch (Exception e) {}
	    }

	
	}
	
    	 
}
