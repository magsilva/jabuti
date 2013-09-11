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


import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 <p>Class to help emit some debugging message. To activate the class
 run your program whith <BR></p>

 <p>java -DDEBUG <your Prog><BR></p>

 <p>and the messages will go to standard output<BR>
 Run with <BR></p>

 <p>java -DDEBUG=xxxx <your Prog><BR></p>

 <p>and the messages will go to file xxxx<BR></p>

 @version: 0.00001
 @author: Marcio Delamaro


 */
public class Debug {
	
    private static PrintStream out;
	
    static {
        try {
            String dout = System.getProperty("DEBUG");
            if (dout != null) {
                if (dout.length() == 0) {
                    out = System.out;
                } else {
                    out = new PrintStream(
                            new FileOutputStream(
                            new File(dout)
                            )
                            );
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
	
    /** Prints a message to the "debug device"

     @param x Sends <code>x.toString()</code> to the debug device
     */
    static public void D(Object x) {
        if (out != null) {
            out.println(x);
        }
    }
	 
    static public long freeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
	
}
