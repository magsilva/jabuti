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


package br.jabuti.util;


/**************************************************
 * This class store some static constants used by
 * the graphical interface.
 **************************************************/

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import javax.swing.JButton;

import br.jabuti.gui.JabutiGUI;



public class ToolConstants {
    static public final String toolName = "JaBUTi";
    static public final String toolDescription = "Java Bytecode Understanding and Testing";
    static public final String toolVersion = "1.0";
   	
    static public final String mainLogo = "jabuti.gif";

    static public final String aboutLogo = "jabuti-about128x128.gif";

    static public final String blankSpace = "blank.gif";
            
    static public final String projectExtension = ".jbt";
    static public final String traceExtension = ".trc";
    static public final String instExtension = "_instr.jar";

    static public final Font normalFont = (new JButton()).getFont();
    static public final Font titleFont = normalFont.deriveFont(normalFont.getSize2D() + 1.0F);

	static public PrintStream STDERR = System.err; // To report exceptions
	static public int sourceFontSize = 14;
	
    // Labels used to store information on each CFGNode...
    static public final String LABEL_COLOR = new String("Color");
    static public final String LABEL_DOMINATOR = new String("Dominator");
    static public final String LABEL_IDOMINATOR = new String("IDominator");
    static public final String LABEL_LIVE_DEFINITIONS = new String("Alive definitions");
    static public final String LABEL_WEIGHT = new String("Weight");
   
    // Colors used to show the CFG
    static public final int COLOR_0 = 0;
    static public final int COLOR_1 = 1;
    static public final int COLOR_2 = 2;
    static public final int COLOR_3 = 3;
    static public final int COLOR_4 = 4;
    static public final int COLOR_5 = 5;
    static public final int COLOR_6 = 6;
    static public final int COLOR_7 = 7;
    static public final int COLOR_8 = 8;
    static public final int COLOR_9 = 9;
   	
    static public final int SELECTED = 30;
   	
    static public final int NUM_COLORS = 10;
   
    static public final Color[] CFGColors = new Color[NUM_COLORS];
	
    // Color of the selected node (definition or decision)
    static private final Color selectedColor = new Color(128, 128, 255); // Light roxo
  	
    static public Color getColor(int c) {
        if (CFGColors[0] == null) {
            // Background color
            CFGColors[COLOR_0] = new Color(192, 192, 192); // Light Grey
  			
            // CFGNode colors
            CFGColors[COLOR_1] = new Color(255, 255, 255); // White
            CFGColors[COLOR_2] = new Color(000, 255, 255); // Cyan
            CFGColors[COLOR_3] = new Color(064, 224, 208); // Dark Cyan
            CFGColors[COLOR_4] = new Color(000, 255, 127); // Green
            CFGColors[COLOR_5] = new Color(173, 255, 047); // Olive
            CFGColors[COLOR_6] = new Color(255, 255, 000); // Yellow
            CFGColors[COLOR_7] = new Color(255, 215, 000); // Dark Yellow
            CFGColors[COLOR_8] = new Color(255, 140, 000); // Orange
            CFGColors[COLOR_9] = new Color(255, 000, 000); // Red
        }
        if (c == SELECTED) {
            return selectedColor;
        }
        if (c >= 1 && c < NUM_COLORS) {
            return CFGColors[c];
        }
        return CFGColors[COLOR_0];
    }
  	
    static public String getFourDigitNumber(int num) {
        DecimalFormat formatter = new DecimalFormat("0000");
  		  		
        return formatter.format(num);
    }

	static public void setSTDERR( PrintStream ps ) {
		STDERR = ps;
	}

	static public void setSTDERR( String name ) {
		try {
    		STDERR = new PrintStream( new FileOutputStream( name ) );
    	} catch( FileNotFoundException fnfe ) {
    		STDERR = System.err;
    	}
	}
	
    static public void reportException(Throwable  e, PrintStream out) {
    	System.err.println( "AN EXCEPTIONS WAS GENERETED..." );
        out.println("==> " + e.getClass().getName());
        out.println("Message:   " + e.getMessage());
        out.println("Stack trace: ");
        e.printStackTrace( out );
        out.println("");
        out.flush();
    }
	
    static public URL getToolBaseResource(String filename) {
        URL toolBaseDirectory = JabutiGUI.class.getResource(filename);
        return toolBaseDirectory;
    }
    
    static public Class getClassFromClasspath(String cName, boolean initialize, String classpath) throws MalformedURLException, ClassNotFoundException {
		StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);

		URL[] arrUrl = new URL[st.countTokens()];

		for (int i = 0; st.hasMoreTokens(); i++) {
			arrUrl[i] = new File(st.nextToken()).toURI().toURL();
		}

		ClassLoader cLoader = new URLClassLoader(arrUrl);
		return Class.forName(cName, initialize, cLoader);
    }
}
