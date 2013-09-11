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
 * A single def-use pair used by the all-uses criterion
 *
 *
 * @version: 1.0
 * @author: Marcio Delamaro  
 * @author: Auri Vincenzi  
 *
 **/
public class DefUse extends Requirement {
	
    String var;
    String def;
    String useFrom, useTo;
	
    public DefUse(String v, String f, String t1, String t2) {
        var = v;
        def = f;
        useFrom = t1;
        useTo = t2;
    }

    public String getVar() { 
        return var; 
    } 
   
    public String getDef() { 
        return def; 
    } 
   
    public String getUseFrom() { 
        return useFrom; 
    } 
   
    public String getUseTo() { 
        return useTo; 
    } 
	
    public String toString() {
        return (useTo == null)
                ? ("<" + var + ", " + def + "," + useFrom + ">")		 
                : ("<" + var + ", " + def + "," + "(" + useFrom + ", " + useTo
                + ")" + ">");
    }
	
    public boolean equals(Object y) {
        if (!(y instanceof DefUse)) {
            return false;
        }
        DefUse x = (DefUse) y;

        if (!var.equals(x.var)) {
            return false;
        }
				
        if (! def.equals(x.def) ) {
            return false;
        }

        if (! useFrom.equals(x.useFrom) ) {
            return false;
        }

		if (useTo == null)
			return x.useTo == null;
			
        return useTo.equals(x.useTo);
    }			
				
    public int hashCode() {
        return var.hashCode() + def.hashCode() + useFrom.hashCode();
    }

	/**
	 * Compare two DefUse objects. 
	 */
    public int compareTo(Object other) {
    	if ( other instanceof DefUse ) {
    		DefUse x = (DefUse) other;
    		
    		if ( this.equals( x ) )
    			return 0;
    		else {
    			if (!var.equals(x.var))
    				return var.compareTo(x.var);
    			else {
	    			if (!useFrom.equals(x.useFrom))
	    				return useFrom.compareTo(x.useFrom);
	    			else {
	    				if ( useTo != null ) {
		    				if (!useTo.equals(x.useTo))
		    					return useTo.compareTo(x.useTo);
		    				else
		    					return 0;
		    			} else {
		    				return -1;
		    			}
	    			}
    			}
    		}
    	}
    	else
    		return 0;
    }
}
