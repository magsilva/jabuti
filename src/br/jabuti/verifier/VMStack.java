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


/** <p>This class represents a configuration of the execution stack
 for a given instruction. The stack may have empty and empty
 spots. The first are indicated with the value <code>null</code>.
 The second is represented with a {@link VMStackElement}<br></p>

 Each taken spot in the 

 */

public class VMStack {
    VMStackElement[] v;
    int topc;
	
    /** Creates an empty stack 

     @param l The capacity of this stack
     */ 
    public VMStack(int l) {
        v = new VMStackElement[l];
        topc = -1;
    }
	
    /** Pushs al element in the stack. Each element may take
     more than one spot in the stack, depending on the type 
     stored in the element, exactly like is done in the  VM.
     For example an element that stores a double element will take
     the two top spots in the stack

     @param e The element to be inserted in the top of the stack
     */
 	
    public void push(VMStackElement e) {
        Type t = e.type;

        for (int i = 0; i < t.getSize(); i++) {
            v[++topc] = e;
        }
    }

    /** Removes the element from the top of the stack. It may 
     required to remove more than one spot from the stack if the
     top element occupies more than one spot.

     */	
    public void pop() {
        Type t = top().type;

        for (int i = 0; i < t.getSize(); i++) {
            v[topc--] = null;
        }
    }
	
    /** The same of calling {@link VMStack#pop} k times.

     @param The number of elements to remove from the top of the
     stack
     */		
    public void pop(int k) {
        for (int i = 0; i < k; i++) {
            pop();
        }
    }

    /** Removes k spots (not elements) from the top of the stack 

     @param k The number of spots to remove from the stack
     */
    public void pop2(int k) { 
        while (k-- > 0) {
            v[topc--] = null;
        }
    }
	
    /** Gets the elemnt in the top of the stack. Does not pop it.

     @return The element in the top of the stack
     */
    public VMStackElement top() {
        return top(0);
    }
		
    /** Gets the kth element from the top of the stack. 
     <code>top(0)</code> corresponds to the top element

     @param k The element (from the top) that one wants to retrive
     @return The kth element from the top
     */
    public VMStackElement top(int k) {
        int j = topc;
        VMStackElement e = null;

        for (int i = 0; i <= k; i++) {
            e = v[j];
            j -= e.type.getSize();
        }
        return e;
    }
	
    /** Return spot (not element) k in the stack

     @param k The spot (from the bottom!) to be retrieved
     @return The kth element from the base of the stack
     */
    public VMStackElement get(int k) {
        return v[k];
    }

    /** Gets the capacity of the stack (number of spots)

     @return The capacity (number of spots) of the stack
     */	
    public int length() {
        return v.length;
    }

    /** Gets the number of taken spots (not elements) in the stack.

     @return The number of taken spots (not elements) in the stack.
     */	public int size() {
        return topc + 1;
    }

    /** Removes all the elements from the stack

     */
    public void reset() {
        pop2(size()); 
    }
	
    /** Makes a deep copy of this object, i.e., not only the
     stack itself is copied, but also the elements are re-ccreated using
     {@link VMStackElement#clone}.

     @return The new stack
     */ 
    public Object clone() {
        VMStack x = new VMStack(v.length);

        for (int i = 0; i < v.length; i++) {
            if (v[i] != null) {
                x.v[i] = new VMStackElement(v[i]);
            }
        }
        x.topc = topc;
        return x;
    }
	
    /** Answers whether the stack is empty

     @return true if <code>size() == 0</code>
     */	
    public boolean empty() {
        return size() == 0;
    }
	
    /** <p>Returns in the form of a string the following information:</p>

     <UL>
     <LI> the value of the top (0 to length - 1)
     <LI> the string returned from {@link VMStackElement#toString()} for
     each element
     </UL>

     @return The string with information about each element of the stack
     */

    public String toString() {
        String str = "";

        str += "topc: " + topc + "\n";
        for (int i = v.length - 1; i >= 0; i--) {
            str += "\t" + i + ") ";
            if (v[i] == null) {
                str += "null\n";
            } else {
                str += v[i].toString() + "\n";
            }
        }
        return str;
    }

    /** <p> Compare this object with another one. The rulles for the
     comparison are: <br></p>

     <UL>
     <LI> if the argument is not a {@link VMStack} object, return false
     <LI> if the <code>size()</code> of the stacks are not the same, returns false;
     <LI> if element i of this object is null and the corresponding object
     in the argument is not null return false
     <LI> if calling {@link VMStackElement#equals} on element i of this stack 
     with element i of argument returns false, then return false
     <LI> return true otherwise
     </UL>

     @return true If all the elements of bith stacks match
     */
    public boolean equals(Object y) {
        if (!(y instanceof VMStack)) {
            return false;
        }
        VMStack x = (VMStack) y;

        if (size() != x.size()) {
            return false;
        }
        for (int i = 0; i <= topc; i++) {
            if (v[i] == x.v[i]) {
                continue;
            }
            if (v[i] == null || x.v[i] == null) {
                return false;
            }
            if (!v[i].equals(x.v[i])) {
                return false;
            }
        }
        return true;
    }
}
