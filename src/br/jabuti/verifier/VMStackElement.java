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


/** <p>This class represents one element store in the "execution"
 * stack of a {@link InstructionNode}. Each such element stores 
 * a {@link Type} object and a few more information:<br></p>
 *  
 * <p>
 * <UL>
 * <LI> the {@link InstructionNode} that stored the information in the
 * stack element
 * <LI> a string representing the object being stored in this stack element
 * </UL>
 */ 
public class VMStackElement {
	
    /** Type of the element */
    public Type type;

    /** Local, field, constant or unknown */

    /** The instruction that stored the information in element */
    public InstructionNode producer; // the instruction that pushed the element

    /** Representation of the element stored in this element */
    public String defuse;   // var being defined or used
	
    /** Creates and initializes the object.

     @param t The type of the information in this element
     @param d The representation of the data in this element
     @param x The instruction that stored information in this element
     */
    public VMStackElement(Type t, String d, InstructionNode x) {
        type = t;
        producer = x;
        defuse = d;
    }

    /** <p>The same as <br></p>
     <code> VMStackElement(x.type, x.defuse, x.producer);</code>
     */	
    public VMStackElement(VMStackElement x) {
        this(x.type, x.defuse, x.producer);
    }
	
    /** <p>Return some data on this object. Like<br></p>
     <text>
     Kind: Local     3
     Def/Use: $local$3
     Type: I
     Pushed by: 267: iload[2] 3
     </text>
     */
    public String toString() {
        String str = (type == null ? null : type.getSignature());

        str += " Def/Use: " + defuse;
        return str + " Pushed by " + (producer == null ? null : producer.ih);
    }

    /** <p>Compare this object with another one. The rules for comparison 
     are: <br></p>
     <UL>
     <LI> if the argument is not a {@link VMStackElement} object returns false;
     <LI> if this element's type is null and the argument's type is not null
     return false
     <LI> if <code>type.equals(y.type)</code> is false, return false
     <LI> if this element's producer is null and the argument's producer is not null
     return false
     <LI> if <code>producer.equals(y.producer)</code> is false, return false
     <LI> if <code>defuse.equals(y.defuse)</code> is false, return false
     <LI> return true otherwise
     </UL>


     @param x The object to be compared
     @return true If type, producer and def/use matches
     */ 	
    public boolean equals(Object x) {
        if (!(x instanceof VMStackElement)) {
            return false;
        }
        VMStackElement y = (VMStackElement) x;
        boolean t1, t2;

        if (type == null) {
            t1 = type == y.type;
        } else {
            t1 = type.equals(y.type);
        }
        if (producer == null) {
            t2 = producer == y.producer;
        } else {
            t2 = producer.equals(y.producer);
        }
        return t1 && t2 && defuse.equals(y.defuse);
    }
				
}
