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


package br.jabuti.probe;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;



/** <p>This class implements a class to create a regular file 
 * to store the registered execution sequence. At 
 * itinialization it registers an object to be notified when the 
 * program exits. At that point, that object call 
 * {@link DefaultProber#dump}.
 */
public class DefaultProber  {
    static public final String delimiter = "**********************";
    static public final String READER_CLASS = "class br.jabuti.probe.DefaultTraceReader";
    
 // usado para modo batch
    static protected Hashtable<String,PrintStream> classesAndFiles = new Hashtable<String,PrintStream>(); 
    static private PrintStream fp;
    static private boolean stdout = true, habilitado = false;
    static private String tcName = "";
    static private String batchModePrefix = "";
    static private boolean isBatch = false;
	
    static {
        // Gets the name of the file where the execution will be dumped
        fp = null;
        String fileProber = System.getProperty("DEFAULT_PROBER");
        String fileBatch = ( System.getProperty("BATCH_MODE") != null )? System.getProperty("BATCH_MODE"): System.getenv("BATCH_MODE");
        
        if (fileBatch != null)
        {
        	batchModePrefix = new String(fileBatch);
        	isBatch = true;
        	fp = System.out; // NAO SERA USADO MAS SINALIZA QUE O DUMP DEVE SER GRAVADO
        	stdout = false;
        	startTrace();
        }
        else
        if (fileProber != null) {
            if (fileProber.length() == 0) {
                fp = System.out;
                stdout = true;
            } else {
                try {
                	init(fileProber);
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ":");
                    System.err.println("\t" + e.getMessage());
                    System.err.println("\tExecution will be dumped to standard output");
                    fp = System.out;
                    stdout = true;
                }
            }
        }
        // register an object that will be notified when the program exits
        Runtime.getRuntime().addShutdownHook(new DefaultProberHook());
    }
	
	static synchronized public void init(String fileProber)
			throws Exception
	{
        RandomAccessFile raf = new RandomAccessFile(fileProber, "rw");

        raf.seek(raf.length());
        FileOutputStream fos = new FileOutputStream(raf.getFD());

        fp = new PrintStream(fos);
        stdout = false;
	}

	static synchronized public void finished()
	throws Exception
	{
		fp.close();
	}

	static synchronized public void startTrace()
	{
		tcName = "";
		habilitado = true;
	}
	
	static synchronized public void startTrace(String name)
	{
		tcName = name;
		habilitado = true;
	}

	static synchronized public void stopTrace()
	{
		habilitado = false;
		try {
			dump();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
    /** This method stores (for example, sending to a file) the 
     * registered execution up to that point 
     * @throws IOException */
    static synchronized public void dump() throws IOException {
        if (fp == null) {
            return;
        }
        if ( ! isBatch )
        {
            // write a delimiter 
//            fp.println(delimiter);
        	fp.println(delimiter);
            fp.flush(); // Inseri um flush para descarregar o buffer.
        }
        else 
        {   // precisa escrever delimitador para cada arquivo criado
        	for (String s : classesAndFiles.keySet() )
        	{
        		PrintStream ps = classesAndFiles.get(s);
                // write a delimiter 
//                ps.println(delimiter);
            	ps.println(delimiter);
                ps.flush(); // Inseri um flush para descarregar o buffer.
        	}
        }
        
    }

	/** This method registers the execution of a given node */
    static synchronized public void probe(Object o, 
    							String clazz, 
    							int metodo, 
    							long nest,
    							Object n) 
    	{    		
    	if ( fp == null || (! habilitado) )
    		return;
        Runnable tr = Thread.currentThread();
        String s = o == null ? "STATIC" : 
        						o.getClass().getName() + System.identityHashCode(o);
        ProbedNode pb = new ProbedNode(tr.toString(), s,
                clazz, metodo, "");
        
        // Faz o dump imediatamente, sem esperar o final do caso de teste
        try {
			dumpNow(pb, nest, n);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }
	
    private static void dumpNow(ProbedNode pb, long nest, Object n) throws IOException {
    	if (! isBatch )
    	{
    		dumpNodes(fp, pb, nest, n);
    	}
    	else
    	{
    		dumpBatchNodes(pb,nest,n);
    	}

	}

	/** This method registers the execution of a given node 
     of an static method */
    static public void probe(String clazz, int metodo, long nest, Object n) {
        probe(null, clazz, metodo, nest, n);
    }
	
    synchronized static void dumpNodes(PrintStream fp, ProbedNode pbdNode, long nest, Object n) {
        if (stdout) // dump in a text mode.
        {

            fp.println();
            fp.println("-------------------------------");
            fp.println(pbdNode + " " + nest +":" + n);
            fp.println("-------------------------------");
        } else // dump in a binary mode
        {
            try {
            	fp.print(tcName+":"); // nome do caso de teste
                fp.print(pbdNode.threadCode+":");
                fp.print(pbdNode.objectCode+":");
                fp.print(pbdNode.clazz+":");
                fp.print(pbdNode.metodo+":");
                fp.println(nest+":"+n);
            } catch (Exception e) {}
        }
    }


    private static void dumpBatchNodes(ProbedNode tr, long nest, Object n) throws IOException {
    	String className = tr.clazz;
    	PrintStream fp = (PrintStream) classesAndFiles.get(className);
    	if (fp == null)
    	{
            RandomAccessFile raf = new RandomAccessFile(batchModePrefix+className+".trc", "rw");
            raf.seek(raf.length());
            FileOutputStream fos = new FileOutputStream(raf.getFD());
            fp = new PrintStream(fos);
            classesAndFiles.put(className, fp);
    	}
    	dumpNodes(fp, tr, nest, n);
	}

    
    static private long nestlevel = 0;
    
    static synchronized public long getNest()
    {
    	return nestlevel++;
    }
	
}


class DefaultProberHook extends Thread {
    public void run() {
        try {
			DefaultProber.dump();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
