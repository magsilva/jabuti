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


// GraphViz.java - a simple API to call dot from Java programs

/*$Id$*/
/*
 ******************************************************************************
 *                                                                            *
 *              (c) Copyright 2003 Laszlo Szathmary                           *
 *                                                                            *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms of the GNU Lesser General Public License as published by   *
 * the Free Software Foundation; either version 2.1 of the License, or        *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY *
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public    *
 * License for more details.                                                  *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public License   *
 * along with this program; if not, write to the Free Software Foundation,    *
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.                              *
 *                                                                            *
 ******************************************************************************
 */
package br.jabuti.graph.layout.graphviz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.swing.JOptionPane;

/**
 * <dl>
 * <dt>Purpose: GraphViz Java API
 * <dd>
 *
 * <dt>Description:
 * <dd> With this Java class you can simply call dot
 *      from your Java programs
 * <dt>Example usage:
 * <dd>
 * <pre>
 *    GraphViz gv = new GraphViz();
 *    gv.addln(gv.start_graph());
 *    gv.addln("A -> B;");
 *    gv.addln("A -> C;");
 *    gv.addln(gv.end_graph());
 *    System.out.println(gv.getDotSource());
 *
 *    File out = new File("out.png");
 *    gv.writeGraphToFile(gv.getGraph(gv.getDotSource()), out);
 * </pre>
 * </dd>
 *
 * </dl>
 *
 * @version v0.1, 2003/12/04 (Decembre)
 * @author  Laszlo Szathmary (<a href="szathml@delfin.unideb.hu">szathml@delfin.unideb.hu</a>)
 */
public class GraphViz
{
   /**
    * The dir where temporary files will be created.
    */
   private static String TEMP_DIR   = System.getProperty("java.io.tmpdir");

   /**
    * Where is your dot program located? It will be called externally.
    */
   private final static String DOT_W        = "C:\\Program Files\\Graphviz2.20\\bin\\dot.exe";
   private final static String DOT_L        = "/usr/bin/dot";
   private static String DOT = null;

   /**
    * The source of the graph written in dot language.
    */
	private StringBuffer graph = new StringBuffer();

   /**
    * Constructor: creates a new GraphViz object that will contain
    * a graph.
 * @throws FileNotFoundException 
    */
   public GraphViz() throws FileNotFoundException {
	   if ( DOT == null )
	   {
		   String s = System.getProperty("os.name").toUpperCase();
		   if ( "LINUX".equals(s) )
		   {
			   DOT = DOT_L;
		   }
		   else
		   if ( s != null && s.startsWith("WINDOWS") )
		   {
			   DOT = windowsFindDot();
			   // DOT = DOT_W;
			   System.out.println(DOT);
		   }
		   else
		   {
			   DOT = JOptionPane.showInputDialog(null, "Please enter path:", 
					   "Cannot find GraphViz layouter (dot).", JOptionPane.ERROR_MESSAGE);
			   
		   }
		   while ( DOT != null )
		   {
			   File f = new File(DOT);
			   if ( f.isFile() && f.canRead() )
				   break;
			   DOT = JOptionPane.showInputDialog(null, "Please enter path:", 
					   "Cannot find GraphViz layouter at " + DOT, JOptionPane.ERROR_MESSAGE);
			   
		   }
		   if ( DOT == null )
		   {
			   DOT = "";
			   throw new FileNotFoundException("Cannot find GraphViz.");
		   }
	   }
   }

   private String windowsFindDot() {
	String s = getGraphVizInstallPath();
	if (s == null)
		return DOT_W;
	else
		return s += File.separator + "bin" + File.separator + "dot.exe";
}
   
   private static final String REGQUERY_UTIL = "reg query ";
   private static final String REGSTR_TOKEN = "REG_EXPAND_SZ";
   private static final String COMPUTER_WINDOWS_GRAPHVIZ_FOLDER = REGQUERY_UTIL + "\"HKLM\\SOFTWARE\\AT&T Research Labs\\Graphviz\" /v InstallPath";


   public static String getGraphVizInstallPath() {
		try {
			Process process = Runtime.getRuntime().exec(
					COMPUTER_WINDOWS_GRAPHVIZ_FOLDER);
			StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			String result = reader.getResult();
			int p = result.indexOf(REGSTR_TOKEN);
			if (p == -1)
				return null;
			return result.substring(p + REGSTR_TOKEN.length()).trim();
		} catch (Exception e) {
			return null;
		}
	}   

/**
 * Returns the graph's source description in dot language.
 * 
 * @return Source of the graph in dot language.
 */
   public String getDotSource() {
      return graph.toString();
   }

   /**
    * Adds a string to the graph's source (without newline).
    */
   public void add(String line) {
      graph.append(line);
   }

   /**
    * Adds a string to the graph's source (with newline).
    */
   public void addln(String line) {
      graph.append(line+"\n");
   }

   /**
    * Adds a newline to the graph's source.
    */
   public void addln() {
      graph.append('\n');
   }

   /**
    * Returns the graph as an image in binary format.
    * @param dot_source Source of the graph to be drawn.
    * @return A byte array containing the image of the graph.
    */
   public byte[] getGraph(String dot_source)
   {
      File dot;
      byte[] img_stream = null;
   
      try {
         dot = writeDotSourceToFile(dot_source);
         if (dot != null)
         {
            img_stream = get_img_stream(dot);
            if (dot.delete() == false) 
               System.err.println("Warning: "+dot.getAbsolutePath()+" could not be deleted!");
            return img_stream;
         }
         return null;
      } catch (java.io.IOException ioe) { return null; }
   }

   public String getDotGraph(String dot_source)
   {
      File dot;
      String img_stream = null;
   
      try {
         dot = writeDotSourceToFile(dot_source);
         if (dot != null)
         {
            img_stream = get_dot_stream(dot);
            if (dot.delete() == false) 
               System.err.println("Warning: "+dot.getAbsolutePath()+" could not be deleted!");
         }
         return img_stream;
      } catch (Exception ioe) { return null; }
   }
   
   /**
    * Writes the graph's image in a file.
    * @param img   A byte array containing the image of the graph.
    * @param file  Name of the file to where we want to write.
    * @return Success: 1, Failure: -1
    */
   public int writeGraphToFile(byte[] img, String file)
   {
      File to = new File(file);
      return writeGraphToFile(img, to);
   }

   /**
    * Writes the graph's image in a file.
    * @param img   A byte array containing the image of the graph.
    * @param to    A File object to where we want to write.
    * @return Success: 1, Failure: -1
    */
   public int writeGraphToFile(byte[] img, File to)
   {
      try {
         FileOutputStream fos = new FileOutputStream(to);
         fos.write(img);
         fos.close();
      } catch (java.io.IOException ioe) { return -1; }
      return 1;
   }

   /**
    * It will call the external dot program, and return the image in
    * binary format.
    * @param dot Source of the graph (in dot language).
    * @return The image of the graph in .png format.
    */
   private byte[] get_img_stream(File dot)
   {
      File img;
      byte[] img_stream = null;

      try {
         img = File.createTempFile("graph_", ".png", new File(this.TEMP_DIR));
         String temp = img.getAbsolutePath();

         Runtime rt = Runtime.getRuntime();
         String cmd = DOT + " -Tpng "+dot.getAbsolutePath()+" -o"+img.getAbsolutePath();
         Process p = rt.exec(cmd);
         p.waitFor();

         FileInputStream in = new FileInputStream(img.getAbsolutePath());
         img_stream = new byte[in.available()];
         in.read(img_stream);
         // Close it if we need to
         if( in != null ) in.close();

         if (img.delete() == false) 
            System.err.println("Warning: "+img.getAbsolutePath()+" could not be deleted!");
      }
      catch (java.io.IOException ioe) {
         System.err.println("Error:    in I/O processing of tempfile in dir "+this.TEMP_DIR+"\n");
         System.err.println("       or in calling external command");
         ioe.printStackTrace();
      }
      catch (java.lang.InterruptedException ie) {
         System.err.println("Error: the execution of the external program was interrupted");
         ie.printStackTrace();
      }

      return img_stream;
   }

   private String get_dot_stream(File dot) throws IOException, InterruptedException
   {
      File img;
 
     img = File.createTempFile("graph_", ".dot", new File(TEMP_DIR));

     Runtime rt = Runtime.getRuntime();
     String cmd = DOT + " -Tdot "+dot.getAbsolutePath()+" -o"+img.getAbsolutePath();
     Process p = rt.exec(cmd);
     p.waitFor();
      return img.getAbsolutePath();
   }

    
   /**
    * Writes the source of the graph in a file, and returns the written file
    * as a File object.
    * @param str Source of the graph (in dot language).
    * @return The file (as a File object) that contains the source of the graph.
    */
   private File writeDotSourceToFile(String str) throws java.io.IOException
   {
      File temp;
      try {
         temp = File.createTempFile("graph_", ".dot.tmp", new File(this.TEMP_DIR));
         FileWriter fout = new FileWriter(temp);
         fout.write(str);
         fout.close();
      }
      catch (Exception e) {
         System.err.println("Error: I/O error while writing the dot source to temp file!");
         return null;
      }
      return temp;
   }

   /**
    * Returns a string that is used to start a graph.
    * @return A string to open a graph.
    */
   public String start_graph() {
      return "digraph G {";
   }

   /**
    * Returns a string that is used to end a graph.
    * @return A string to close a graph.
    */
   public String end_graph() {
      return "}";
   }
}

class StreamReader extends Thread {
	private InputStream is;
	private StringWriter sw;

	StreamReader(InputStream is) {
	this.is = is;
	sw = new StringWriter();
	}

	public void run() {
	try {
	int c;
	while ((c = is.read()) != -1)
	sw.write(c);
	}
	catch (IOException e) { ; }
	}

	String getResult() {
	return sw.toString();
	}
}
