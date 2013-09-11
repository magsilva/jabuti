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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import br.jabuti.util.Debug;

/** This class reads a file stored by the {@link DefaultProber} class.
 */

public class DefaultTraceReader implements Serializable, TraceReader {
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -782971586725069021L;
	protected Hashtable<ProbedNode, String[][]> paths;
    protected File fp;
    protected BufferedReader br;
    protected String tcName;
	
    public DefaultTraceReader(File f) throws IOException, FileNotFoundException {
        fp = f.getAbsoluteFile();
        br = new BufferedReader(new FileReader(fp));
    }
	
    protected DefaultTraceReader() {
        br = null;
    }
	
    protected int readPaths() {
        paths = null;
        if (br == null) {
            return 0;
        }
        paths = new Hashtable<ProbedNode, String[][]>();
        int k = 0;

        ProbedNode lastNode = null;
        String lastTcName = null;
        String lastNest = null;
        ArrayList<String> caminho = null; 
        ArrayList<String[]> curMeth = null; 
        
        do { // Le cada uma das linhas do arquivo de trace
        	try 
        	{
		       	String registro = br.readLine();
		       	if (registro == null || registro.equals(DefaultProber.delimiter)) {
       	           break; // termina se chegou no fim do arquivo ou se achou o delimitador
    		   	}
		       	tcName = getNameFromRegistro(registro); // le o nome do caso de teste
		       	ProbedNode pdn = getNodeFromRegistro(registro); // le dados sobre o no executado
		       	String nest = getNestFromRegister(registro);
		       	String noh = getNoFromRegistro(registro);
		       	if ( ! tcName.equals(lastTcName) || ! pdn.equals(lastNode) )
		       	{ // precisa criar uma nova entrada na hashtable pois o registro eh
		       	  // de um novo metodo
		       		if (lastNode != null )
		       		{
	       				curMeth.add(caminho.toArray(new String[0]));
		       			paths.put(lastNode, curMeth.toArray(new String[curMeth.size()][]));
		       		}
		       		curMeth = new ArrayList<String[]>();
		       		lastNest = null;
		       		lastTcName = tcName;
		       		lastNode = pdn;
		       	}
	       		if ( ! nest.equals(lastNest) )
	       		{ // novo caminho no metodo corrente
	       			if (lastNest != null )
	       			{
	       				curMeth.add(caminho.toArray(new String[0]));
	       			}
	       			caminho = new ArrayList<String>();
	       			lastNest = nest;
	       		}
	       		caminho.add(noh);
		       	
            } catch (Exception e) { 
                Debug.D("FINAL TRACE: (" + k + ") " + e + ""); 
                paths = null;
                return 0;
            }
        } while ( true );
		
        if ( lastNest != null )
        {
        	curMeth.add(caminho.toArray(new String[0]));
        	paths.put(lastNode, curMeth.toArray(new String[curMeth.size()][]));
        }
        
        return paths.size();
    }
    
    protected String getNoFromRegistro(String registro) {
		// No aparece como ultimo campo no registro
		int k = registro.lastIndexOf(':');
		
		return registro.substring(k+1);
	}

    protected String getNestFromRegister(String registro) {
		// Aninhamento aparece como penultimo campo no registro (5)
		String s = new String(registro);
		int k = s.indexOf(':'); // 1a ocorrencia
		
		s = s.substring(k+1);
		k = s.indexOf(':'); // 2a
		
		s = s.substring(k+1);
		k = s.indexOf(':'); // 3a

		s = s.substring(k+1);
		k = s.indexOf(':'); // 4a
		
		s = s.substring(k+1);
		k = s.indexOf(':'); // 5a

		s = s.substring(k+1);
		k = s.indexOf(':'); // 6a

		return s.substring(0, k);
	}

    protected ProbedNode getNodeFromRegistro(String registro) {
		int k = registro.indexOf(':'); // 1a ocorrencia
		String s = registro.substring(k+1);
		k = s.indexOf(':');
		String tredi = s.substring(0, k);
		
		s = s.substring(k+1);
		k = s.indexOf(':');
		String objeto = s.substring(0, k);

		s = s.substring(k+1);
		k = s.indexOf(':');
		String claz = s.substring(0, k);

		s = s.substring(k+1);
		k = s.indexOf(':');
		String metodo = s.substring(0, k);
		
		return new ProbedNode(tredi, objeto, claz, Integer.parseInt(metodo), "");
	}

	/**
	 * Pega o nome do caso de teste do registro lido do arquivo de trace
	 * @param registro - string com informacoes do arquivo de traca
	 * @return Nome do caso de teste
	 */
    protected String getNameFromRegistro(String registro) {
		// Nome aparece como primeiro campo do registro
		int k = registro.indexOf(':');
		
		return registro.substring(0, k);
	}


    
	

    public Hashtable<ProbedNode, String[][]> getPaths() {
        readPaths();
        return (paths == null || paths.size()== 0 ) ? null : paths;
    }
	
    public void reset() throws IOException, FileNotFoundException {
        br.close();
        br = new BufferedReader(new FileReader(fp));
    }

    public String toString() {
        String str = "";
        Enumeration en = paths.keys();

        while (en.hasMoreElements()) {
            ProbedNode met = (ProbedNode) en.nextElement();

            str += "==========================================\n";
            str += met + "\n";
            String nodes[][] = (String[][]) paths.get(met);

            str += "Paths: " + nodes.length + "\n";
            for (int i = 0; i < nodes.length; i++) {
                str += "Path len: " + nodes[i].length + "\n";
                for (int j = 0; j < nodes[i].length; j++) {
                    String n = nodes[i][j];

                    str += " " + n;
                }
                str += "\n";
            }
            str += "\n";
        }
        return str;
    }

	public String getName() {
		return tcName;
	}
    
}


