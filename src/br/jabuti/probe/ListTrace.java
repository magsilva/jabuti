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


import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * 
 * @author delamaro
 *
 * Essa classe eh uma classe auxiliar para visualizar o conteudo de um
 * arquivo de trace. Passe como argumento o nome do arquivo de trace e
 * ela mostra o seu conteudo, de modo formatado. Chamada da linha
 * de comando
 */
public class ListTrace {
    public static void main(String args[]) throws Throwable {
        TraceReader dtr = null;

        try {
            String filename = args[0];

            System.out.println("TRACE FILE: " + filename);
			
            dtr = new DefaultTraceReader(new File(filename));
        	
            Hashtable trace = (Hashtable) dtr.getPaths();
            int cont = 0;        	

            while (trace != null && trace.size() > 0 ) {
                System.out.println("**************************************");
                System.out.println("Path number " + (++cont));
                System.out.println("**************************************");
                Iterator it = trace.keySet().iterator();

                while (it.hasNext()) {
                    ProbedNode pn = (ProbedNode) it.next();

                    System.out.println(pn.toString());
        			
                    String[][] nodes = (String[][]) trace.get(pn);
        			
                    for (int i = 0; i < nodes.length; i++) {
                        System.out.println("Path len: " + nodes[i].length + "\n");
                        for (int j = 0; j < nodes[i].length; j++) {
                            String n = nodes[i][j];

                            System.out.print(" " + n);
                        }
                        System.out.println();
                    }
                    System.out.println();
                }
                trace = (Hashtable) dtr.getPaths();
            }
        } catch (IOException ioe) {
            System.out.println("Cannot find trace file");
            return;
        }
    }
}
