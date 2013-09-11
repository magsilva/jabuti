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
 * Created on 12/09/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.jabuti.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import br.jabuti.util.ToolConstants;

public final class Preverify {
static public final String WMA_JAR = "wma11.jar",
						   CLDC_JAR = "cldcapi11.jar",
						   MIDP_JAR = "midpapi20.jar";
	
    public static void usage() {
        System.out.println(ToolConstants.toolName + " v. " + ToolConstants.toolVersion);
        System.out.println("\nPreverify usage:");
        System.out.println("-------------------\n");
        System.out.println("java br.jabuti.device.Preverify -WTK <dirname> -source <filename> -instr <filename> [-o <filename>] [-jad <filename>] \n");
        System.out.println("      -source <filename>      The name of the jar file where all the original " +
        				   "                              preverified classes are.");
        System.out.println("      -instr <filename>     The name of the jar file where all the instrumented " +
		   				   "                              non-preverified classes are.");
        System.out.println("      -WTK <dirname>          The directory where to find the Sun-WTK.");
        System.out.println("      -o <filename>           Name of the jar file where to write to. If not supplied," +
        		           "                              the \"source\" jar file is overwritten.");
        System.out.println("      [-jad <filename>]       Specifies the name of the jad file to create. Optional");
        System.out.println("\nCopyright (c) 2002\n");
    }

	/**
	 * @param args - ver usage()
	 */
	public static void main(String[] args) {
		String WTKdir = null;
		String sourceFile = null;
		String instrFile = null;
		String outFile = null;
        String jadFile = null;
        Manifest mnf;

        if (args.length == 0) 
        {
        	usage();
        	return;
        }

        int i = 0;
		
        while (i < args.length && args[i].startsWith("-")) {
            // -CP: Class path
			if (("-WTK".equals(args[i])) && (i < args.length - 1)) {
                i++;
                WTKdir = args[i];
            } // -P: project name
            else if (("-source".equals(args[i])) && (i < args.length - 1)) {
                i++;
                sourceFile = args[i];
            } 
            else if (("-instr".equals(args[i])) && (i < args.length - 1)) {
                i++;
                instrFile = args[i];
            } 
            else if (("-o".equals(args[i])) && (i < args.length - 1)) {
                i++;
                outFile = args[i];
            }
            else if (("-jad".equals(args[i])) && (i < args.length - 1)) {
                i++;
                jadFile = args[i];
            } 
            else {
                System.out.println("Error: Unrecognized option: " + args[i]);
                System.exit(0);
            }
            i++;
        }
	    // Checking if all essential parameters are not null
	    if ( WTKdir == null || sourceFile == null || 
	    	 instrFile == null ) 
	    {
	        System.out.println("Error: Missing parameter!!!");
	        usage();
	        return;
	    }
	    if ( outFile == null) outFile = sourceFile;
	    
	    FileInputStream fisJar;
		try {
			fisJar = new FileInputStream(sourceFile);
		} catch (FileNotFoundException e1) {
			System.out.println("Cannot find source jar file: " + sourceFile);
			e1.printStackTrace();
			return;
		}

	    FileOutputStream fosJar;
	    File aux = null;
		try {
			aux  = File.createTempFile("predef", "aux");
			fosJar = new FileOutputStream(aux);
		} catch (IOException e1) {
			System.out.println("Cannot find temporary jar file: " + aux);
			e1.printStackTrace();
			return;
		}

	    JarFile instrJar = null;
	    Enumeration en = null;
	    File tempDir = null;
	    try {
			instrJar = new JarFile(instrFile);
			en = instrJar.entries();
			tempDir = File.createTempFile("jbtp", "");
			tempDir.delete();
			System.out.println("Create directory: " + tempDir.mkdirs());
			tempDir.deleteOnExit();
		} catch (IOException e) {
			System.out.println("Cannot open instrumented file: " + instrFile);
			e.printStackTrace();
			return;
		}
		
		String[] wtklib = new java.io.File(WTKdir+ File.separator + "lib").list(new OnlyJar());

		String preverifyCmd = WTKdir + File.separator + "bin" + File.separator
	    		+ "preverify -classpath " + 
	             WTKdir + File.separator + "lib" + File.separator + CLDC_JAR + 
	             File.pathSeparator + 
	             WTKdir + File.separator + "lib" + File.separator + MIDP_JAR + 
	             File.pathSeparator + 
	             WTKdir + File.separator + "lib" + File.separator + WMA_JAR + 
	             File.pathSeparator + 
	             instrFile;
		
		// Adding additional JAR files available at WTK lib directory.
		for (int k=0; k < wtklib.length; k++) {
			preverifyCmd += File.pathSeparator + WTKdir + File.separator + "lib" + wtklib[k];
		}
		
		preverifyCmd += " " + "-d " + tempDir.getAbsolutePath() + " ";
	    
	    while (en.hasMoreElements())
	    {
	    	JarEntry je = (JarEntry) en.nextElement();
	    	String jeName = je.getName();
	    	
	    	if ( jeName.endsWith(".class") )
	    		jeName = jeName.substring(0, jeName.length() - 6);
	    	preverifyCmd +=  jeName + " ";
	    }
	    
	    try {
			Process p = Runtime.getRuntime().exec(preverifyCmd);
			if (p.waitFor() != 0) {
				BufferedReader in
				   = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				System.out.println("Error calling the preverify command.");
				while (in.ready()) {
					System.out.print(""+in.readLine());
				}
				System.out.println();
				in.close();
				return;
			}
		} catch (Exception e) {
			System.out.println("Cannot execute preverify command");
			e.printStackTrace();
			return;
		}
		File[] listOfFiles = computeFiles(tempDir);
		
		System.out.println("-------------------------------\n" +
				"Files to insert: ");
		String[] strFiles = new String[listOfFiles.length];
		int l = tempDir.toString().length()+1;
		for (int j = 0; j < listOfFiles.length; j++)
		{
			strFiles[j] = listOfFiles[j].toString().substring(l);
			strFiles[j] = strFiles[j].replace(File.separatorChar, '/');
			System.out.println(strFiles[j]);
		}
		System.out.println("-------------------------------");
		try {
			JarInputStream jis = new JarInputStream(fisJar);
			mnf = jis.getManifest();

			JarOutputStream jos = new JarOutputStream(fosJar, mnf);
			nextJar:
			for (JarEntry je = jis.getNextJarEntry(); je != null; je = jis.getNextJarEntry())
			{
				String s = je.getName();
				for (int k = 0; k < strFiles.length; k++)
				{
					if (strFiles[k].equals(s))
						continue nextJar;
				}
				jos.putNextEntry(je);
				byte[] b = new byte[512];
				for (int k = jis.read(b, 0, 512); k >= 0; k = jis.read(b, 0, 512))
				{
					jos.write(b, 0, k);
				}
			}
			jis.close();
			for (int j = 0; j < strFiles.length; j++)
			{
				FileInputStream fis = new FileInputStream(listOfFiles[j]);
				JarEntry je = new JarEntry(strFiles[j]);
				jos.putNextEntry(je);
				byte[] b = new byte[512];
				while ( fis.available() > 0 )
				{
					int k = fis.read(b, 0, 512);
					jos.write(b, 0, k);
				}
				fis.close();
			}
			jos.close();
			fisJar.close();
			fosJar.close();
		} catch (IOException e) {
			System.out.println("Cannot read/write jar file.");
			e.printStackTrace();
			return;
		}
		try
		{
			FileOutputStream fos = new FileOutputStream(outFile);
			FileInputStream fis = new FileInputStream(aux);
			byte[] b = new byte[512];
			while (fis.available() > 0 )
			{
				int k = fis.read(b, 0, 512);
				fos.write(b, 0, k);
			}
			fis.close();
			fos.close();
		}
		catch (IOException e)
		{
			System.out.println("Cannot write output jar file: " + outFile);
			e.printStackTrace();
		}

		Iterator it;
		Attributes atr;
		atr = mnf.getMainAttributes();
		it = atr.keySet().iterator();
		if ( jadFile != null )
		{
			FileOutputStream fos;
			try
			{
				File outJarFile = new File(outFile);
				fos = new FileOutputStream(jadFile);
				PrintStream psjad = new PrintStream(fos);
				while (it.hasNext())
				{
					Object ats =  it.next();
//					if ( ! ats.toString().toUpperCase().startsWith("MIDLET"))
//						continue;
					psjad.println(ats + ": " + atr.get(ats) );
				}
				psjad.println("MIDlet-Jar-URL: " + outFile);
				psjad.println("MIDlet-Jar-Size: " + outJarFile.length());
				fos.close();
			}
			catch (IOException eio)
			{
				System.out.println("Cannot create jad file.");
				eio.printStackTrace();
			}
		}

	}

	private static File[] computeFiles(File tempDir) {
		Vector<File> v = new Vector<File>();
		xcomputeFiles(v, tempDir);
		return (File[]) v.toArray(new File[0]);
	}

	private static void xcomputeFiles(Vector<File> v, File tempDir) {
		if ( ! tempDir.isDirectory() )
		{
			v.add(tempDir);
		}
		else
		{
			File[] list = tempDir.listFiles();
			for (int i = 0; i < list.length; i++)
			{
				xcomputeFiles(v, list[i]);
			}
		}
	}
}

class OnlyJar implements FilenameFilter {
	  public boolean accept(File dir, String s) {
	    if (s.endsWith(".jar") && (!s.startsWith("cldcapi") && !s.startsWith("midpapi") && !s.startsWith("wma")))
	      return true;
	    // others: projects, ... ?
	    return false;
	  }
	} 