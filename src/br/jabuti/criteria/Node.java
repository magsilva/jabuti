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
 * Created on 18/10/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.jabuti.criteria;

public class Node extends Requirement {

	   String from;
		
	    public Node(String f) {
	        from = f;
	    }
		
	    public String toString() {
	        return new String(from);
	    }
		
	    public String getLabel() { 
	        return from; 
	    } 

	    public boolean equals(Object y) {
	        if (!(y instanceof Node)) {
	            return false;
	        }
	        Node x = (Node) y;

					
	        return from.equals(x.from);
	    }			
					
	    public int hashCode() {
	        return from.hashCode();
	    }

		/**
		 * Compare two nodes objects. 
		 */
	    public int compareTo(Object other) {
	    	if ( other instanceof Node ) {
	    		if ( this.equals( other ) )
	    			return 0;
	    		else
	    			return this.from.compareTo(((Node)other).from);
	    	}
	    	else
	    		return 0;
	    }	
}
