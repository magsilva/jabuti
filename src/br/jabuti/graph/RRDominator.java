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


package br.jabuti.graph;


import java.util.HashSet;
import java.util.Vector;

public class RRDominator implements RoundRobinExecutor {
    public final static String defaultLabel = RRDominator.class.getName();
    public String label = defaultLabel;
    private HashSet auxInit = null;
    private GraphNode entryNode = null;
	
    public RRDominator(String x) {
        label = x;
    }
	
    public RRDominator() {}
	
    public Object calcNewSet(GraphNode theNode, 
            Vector primary, 
            Vector secondary) {
        HashSet s1 = null;
        int ip = 0, is = 0;

        // Inicializa o conjunto s1
        if (primary.size() > 0 && theNode != entryNode ) {
            GraphNode gn = (GraphNode) primary.elementAt(0);

			if ( gn.getUserData(label) == null)
			{
				System.out.println("Null data: " + gn);
			}
			s1 = (HashSet) gn.getUserData(label);
            if ( s1 != null)
            	s1 = new HashSet(s1);
            else
            	s1 = new HashSet(1);
            ip++;
        } else
        if (secondary.size() > 0 && theNode != entryNode ) {
            GraphNode gn = (GraphNode) secondary.elementAt(0);

			s1 = (HashSet) gn.getUserData(label);
            if ( s1 != null)
            	s1 = new HashSet(s1);
            else
            	s1 = new HashSet(1);
            is++;
        } else {
            s1 = new HashSet(1);
        }
			
        // Faz a interseccao de todos os dominadores
        // dos antecessores.
        for (int i = ip; i < primary.size(); i++) {
            GraphNode gn = (GraphNode) primary.elementAt(i);
            HashSet s2 = (HashSet) gn.getUserData(label);

            if (s2 != null) {
                s1.retainAll(s2);
            }
        }
        for (int i = is; i < secondary.size(); i++) {
            GraphNode gn = (GraphNode) secondary.elementAt(i);
            HashSet s2 = (HashSet) gn.getUserData(label);

            if (s2 != null) {
                s1.retainAll(s2);
            }
        }
        s1.add(theNode);
        return s1;
    }

    public boolean compareEQ(GraphNode theNode, Object theNewSet) {
        HashSet s1 = (HashSet) theNode.getUserData(label),
                s2 = (HashSet) theNewSet;

        if (s1 == null) {
            return s2 == s1;
        }
        return s1.equals(s2);
    }

    public void setNewSet(GraphNode theNode, Object theNewSet) {
        theNode.setUserData(label, theNewSet);
    }
	

    /** Para inicializar os conjuntos de dominadores, o metodo abaixo
     * adiciona todos os nos que estao no conjunto de dominadores dos
     * seus predecessores, primarios ou secundários.
     */
    public void init(GraphNode theNode,
            Vector primary, 
            Vector secondary) {
            	
        //System.out.println("Iniciando: " + theNode);
        //System.out.println("PRIMARY: " + primary);
        //System.out.println("SECONDARY: " + secondary);
    	
    	/* o conjunto auxInit contem a lista de todos os nos que
    	 * jah foram inicializados
    	 */
        if (auxInit == null) {
            auxInit = new HashSet();
            HashSet s1 = new HashSet();

            s1.add(theNode);
            theNode.setUserData(label, s1);
            auxInit.add(theNode);
            entryNode = theNode;
            return;
        }

        HashSet s1 = null;
			
        for (int i = 0; primary != null && i < primary.size(); i++) {
            GraphNode gn = (GraphNode) primary.elementAt(i);

            // se o no nao foi inicializado ainda, entao nao eh levado
            // em consideracao para inicializar o noh corrente
            if (!auxInit.contains(gn)) {
                continue;
            }
            HashSet s2 = (HashSet) gn.getUserData(label);

            if (s2 != null) {
                if (s1 == null) {
                    s1 = new HashSet(s2.size());
                    s1.addAll(s2);
                } else {
                    s1.retainAll(s2);
                }
            }
        }
        for (int i = 0; secondary != null && i < secondary.size(); i++) {
            GraphNode gn = (GraphNode) secondary.elementAt(i);

            if (!auxInit.contains(gn)) {
                continue;
            }
            HashSet s2 = (HashSet) gn.getUserData(label);

            if (s2 != null) {
                if (s1 == null) {
                    s1 = new HashSet(s2.size());
                    s1.addAll(s2);
                } else {
                    s1.retainAll(s2);
                }
            }
        }
        // se nenhum dominador foi identificado, adiciona
        // conjunto vazio
        if (s1 == null) {
            s1 = new HashSet(1);
        }
        // adiciona o proprio noh
        s1.add(theNode);
        theNode.setUserData(label, s1);
        auxInit.add(theNode);		
    }
	
    public void reset() {
        auxInit = null;
    }
}
