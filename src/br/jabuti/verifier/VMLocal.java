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


package br.jabuti.verifier;


import org.apache.bcel.generic.Type;


/** <p>This class represents one configuration of the local
 variables. It is basicaly an array of 
 {@link de.fub.bytecode.generic.Type} objects.
 */

public class VMLocal {
	
    Type[] v;
	
    /** Creates an empty local variable configuration. Each element
     is initialized with <code>null</code> indicating that the 
     variables have not been initialized</p>

     @param l The number of spots in the configuration, i.e., the 
     number of local variables
     */
    public VMLocal(int l) {
        v = new Type[l];
    }

    /** Creates local variable configuration. Each element
     is initialized the correspondent element in the 
     argument array. The array is copied, so the original
     argument can be used as the programer wants</p>

     @param x The "model" for the configuration. 
     */
    public VMLocal(Type[] x) {
        v = new Type[x.length];
        System.arraycopy(x, 0, v, 0, x.length);
    }
	
    /** <p> Places a given type in a given spot </p>

     @param e The type to be placed in the spot
     @param l The spot to store the type
     */ 
    public void add(Type e, int l) {
        int t = e == null ? 1 : e.getSize();

        for (int i = 0; i < t; i++) {
            v[l++] = e;
        }
    }

    /** Gets the type stored in a given spot 

     @param i The number of the spot from where to retrieve the type
     @return The type stored in the given spot
     */
    public Type get(int i) {
        return v[i];
    }

    /** Makes a shalow copy of this {@link VMLocal} object.

     @return A new {@link VMLocal} object initialized with the same
     elements of this object
     */
    public Object clone() {
        return new VMLocal(v);
    }
	
    /** <p>Compares this object with another. The rules used in the comparisson
     are:<br></p>
     <p><UL>
     <LI> if the argument is not a {@link VMLocal} object return false
     <LI> if the {@link VMLocal#size} of this object is not the same of the
     arguments, return false
     <IL> if element i in this object is null and element i in the argument 
     is not, return false
     <IL> if for any the non-null element x store in spot i, the result of 
     <code>x.equals(y.get(i))</code> is false, returns false;
     <IL> returns true otherwise
     </UL><br>
     </p>

     @param y The object to be compared with
     @return true if all the elements 
     in corresponding spots compare the same acording to {@link Type#equals}
     */
    public boolean equals(Object y) {
        if (!(y instanceof VMLocal)) {
            return false;
        }
        VMLocal x = (VMLocal) y;

        if (x.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (get(i) != x.get(i)) {
                if (get(i) == null) {
                    return false;
                }
                if (!get(i).equals(x.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Gets the number of spots in this object 

     @return The number of spots in this object 
     */		
    public int size() {
        return v.length;
    }

    /** Return a string with information about this object.
     The string has the type descriptions of all the spots separated
     by a newline character

     @return The representation of this object as a colection of
     types
     */   	
    public String toString() {
        String str = "";

        for (int i = 0; i < v.length; i++) {
            str += v[i] + "\n";
        }
        return str;
    }
}
