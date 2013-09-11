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


package br.jabuti.lookup;


import java.util.Vector;


/**
 <p>This class is used to store information about a given class
 in a program. A program is represented by {@link Program} object.
 A {@link RClass} object stores information about which subclasses
 extends the class and which classes implements it (if it is 
 an interface).<BR>
 </p>

 <p>This class is used in the context of a program to represent those 
 classes that are out of the scope of interest. For example, if 
 the program is built over a set of 5 classes <BR>

 <UL>
 <LI> MyClass1
 <LI> MyClass2
 <LI> MyClass3
 <LI> MyClass4
 <LI> MyClassFive
 </UL> <BR>

 and <code>MyClass1</code> extends {@link java.util.Vector} and 
 implements {@link java.util.Comparator} the these two classes
 are represented with {@link RClass} objects. Both pointing to
 <code>MyClass1</code> one indicating that it is extended by
 <code>MyClass1</code> and the other indicating that it is implemented
 by <code>MyClass1</code>.<BR>
 </p>

 <p>
 On the other hand, the classes that are part of the program 
 are represented by {@link RClassCode}.
 </p>

 @version 0.00001
 @author Marcio Delamaro
 @see RClassCode
 @see Program

 */
public class RClass {

    /** The set of classes that extend the class represented by the
     current object. Each object in the vector is a string with the 
     name of the subclass */	
    Vector subclasses, 

            /** The set of classes that implement the interface represented by the
             current object. Each object in the vector is a string with the 
             name of the implementing class */	
            implementations;

    /** The name of this class. Complete in the form package.subpack.class */
    String name;

    /** The name used for the default package */
    static public final String DEFAULT_PACKAGE = "";

    /** Creates an object representing a class.

     @param x The name of the class. No check is done, for example whether the
     class can be found in the current classpath.
     */

    public RClass(String x) {
        name = x; 
        subclasses = new Vector();
        implementations = new Vector();
    }

    /** Gets the name of this class.

     @return The name of the class, assigned on its creation 
     */
    public String getName() {
        return name;
    }
	
    /** Adds a class in the list of classes that extend this
     one. If the class is already there, it is not inserted
     again.

     @param s The name of the subclass
     */
    public void setSubClass(String s) {
        if (!subclasses.contains(s)) {
            subclasses.add(s);
        }
    }
	
    /** Gets the list of subclasses of this one.

     @return An array of strings that contains the names of the subclasses.
     It is never <code>null</code>. If no subclass, an array of size 0 
     is returned.
     */
    public String[] getSubClasses() {
        return (String[]) subclasses.toArray(new String[0]);
    }
	
    /** Gets the size of the list of subclasses.

     @return The number of subclasses of this one
     */
    public int countSubClasses() {
        return subclasses.size();
    }
	
    /** Adds a class in the list of classes that implements this
     one. If the class is already there, it is not inserted
     again.

     @param s The name of the implementing class
     */
    public void setImplementation(String s) {
        implementations.add(s);
    }

    /** Gets the list of classes that implement this one.

     @return An array of strings that contains the names of the implementing classes.
     It is never <code>null</code>. If no subclass, an array of size 0 
     is returned.
     */
    public String[] getImplementations() {
        return (String[]) implementations.toArray(new String[0]);
    }

    /** Gets the size of the list of implementing classes.

     @return The number of classes that implement this one
     */
    public int countImplementations() {
        return implementations.size();
    }

    /** Return whether this class is an interface. The
     information calculated based on the 
     {@link RClass#countImplementations}. If it is 0 it is assumed the
     object does not refer to an interface

     @return True if {@link RClass#countImplementations} > 0.
     */
    public boolean isInterface() {
        return countImplementations() > 0;
    }

    /** Sends to standard output some information about this class:
     <BR>
     <UL>
     <LI> Its name
     <LI> Its subclasses
     <LI> Its implementations
     </UL>

     */		
    public void print() {
        System.out.println();
        String s = isInterface() ? "Interface" : "Class";

        System.out.println("******* Sys " + s + " " + getName() + " *********");
			
        String[] subs = getSubClasses();

        System.out.println("Extended by: ");
        for (int i = 0; i < subs.length; i++) {
            System.out.println(subs[i] + " ");
        }

        subs = getImplementations();
        System.out.println("Implemented by: ");
        for (int i = 0; i < subs.length; i++) {
            System.out.println(subs[i] + " ");
        }
    }
	
    static public String getPackName(String x) {
        int k = x.lastIndexOf(".");

        if (k >= 0) {
            return x.substring(0, k);
        }
        return DEFAULT_PACKAGE;
    }

    static public String getClassName(String x) {
        int k = x.lastIndexOf(".");

        if (k >= 0) {
            return x.substring(k + 1);
        }
        return x;
    }
	
    public String getPackageName() {
        String s = getName();

        return getPackName(s);
    }
}

