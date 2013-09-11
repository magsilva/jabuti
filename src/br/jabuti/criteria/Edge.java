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


package br.jabuti.criteria;

/** 
 * A single edge used by the all-edges criterion
 *
 *
 * @version: 1.0
 * @author: Marcio Delamaro  
 * @author: Auri Vincenzi  
 *
 **/
public class Edge extends Requirement {
	
    String from;
    String to;
	
    public Edge(String f, String t) {
        from = f;
        to = t;
    }
	
    public String toString() {
        return "(" + from + "," + to + ")";
    }
	
    public String getFrom() { 
        return from; 
    } 

    public String getTo() { 
        return to; 
    } 

    public boolean equals(Object y) {
        if (!(y instanceof Edge)) {
            return false;
        }
        Edge x = (Edge) y;

        if (!from.equals(x.from)) {
            return false;
        }
				
        return to.equals(x.to);
    }			
				
    public int hashCode() {
        return from.hashCode() + to.hashCode();
    }

	/**
	 * Compare two edges objects. 
	 */
    public int compareTo(Object other) {
    	if ( other instanceof Edge ) {
    		if ( this.equals( other ) )
    			return 0;
    		else
    			if (!from.equals(((Edge)other).from))
    				return this.from.compareTo(((Edge)other).from);
    			else
    				return this.to.compareTo(((Edge)other).to);
    	}
    	else
    		return 0;
    }
}
