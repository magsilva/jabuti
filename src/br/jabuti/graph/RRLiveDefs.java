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


import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class RRLiveDefs implements RoundRobinExecutor {
    public final static String defaultLabel = RRLiveDefs.class.getName();
    public String label = defaultLabel;
    private static HashSet dummySet = new HashSet();
    private int which;
    static public final int PRIMARY = 1,
            SECONDARY = 2,
            ALL = 3;
	
    public RRLiveDefs(String x, int w) {
        label = x;
        which = w;
    }
	
    public RRLiveDefs(int w) { which = w;}
	
    public Object calcNewSet(GraphNode theNode, 
            Vector primary, 
            Vector secondary) {
        HashSet s1 = new HashSet();
			
		if ( which == ALL || which == PRIMARY)
		{
        	for (int i = 0; i < primary.size(); i++) {
        	    GraphNode gn = (GraphNode) primary.elementAt(i);
        	    HashSet s2 = computeBottom(gn);

        	    s1.addAll(s2);
        	}
    	}
    	
		if ( which == ALL || which == SECONDARY)
		{
	        for (int i = 0; i < secondary.size(); i++) {
	            GraphNode gn = (GraphNode) secondary.elementAt(i);
	            HashSet s2 = computeBottom(gn);
	
	            s1.addAll(s2);
	        }
	     }
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
	
    public void init(GraphNode theNode,
            Vector primary, 
            Vector secondary) {
        theNode.setUserData(label, dummySet);
    }
	
    private Vector toVector(String def, GraphNode x) {
        Vector v = new Vector(2);

        v.add(def);
        v.add(x);
        return v;
    }

    private HashSet computeBottom(GraphNode gn) {
        CFGNode gfcn = (CFGNode) gn;
        HashSet top = (HashSet) gfcn.getUserData(label);
        Iterator it = top.iterator();
        HashSet newPairs = new HashSet();

        while (it.hasNext()) {
            Vector pair = (Vector) it.next();
            String def = (String) pair.elementAt(0);

            if (!gfcn.definitions.containsKey(def)) {
                newPairs.add(pair);
            }
        } 
        Enumeration en = gfcn.definitions.keys();

        while (en.hasMoreElements()) {
            String def = (String) en.nextElement();

            newPairs.add(toVector(def, gn));
        }
        return newPairs;
    }		
	
}
