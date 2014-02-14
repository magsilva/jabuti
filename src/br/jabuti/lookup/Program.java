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


import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import br.jabuti.util.Debug;


/**
 * <p>This class is an abstraction of a program, composed of several 
 * .class files. The .class files may have subclassing and implementation
 * relationship between them and this is represented in this structure.
 * </p>
 *  
 * <p>
 * On the other hand, it is considered that other classes like the Java
 * API or other libraries are not of direct interest to this abstraction.
 * So they are represented in the "borders" of the program if one of
 * the classes in the program has something to do with them. For example
 * if the class <code>MyClass1</code> extends the {@link java.util.Vector}
 * class, both should appear in the program structure but <code>MyClass1</code>
 * appears as a first class object and {@link java.util.Vector} only as
 * an auxiliary object.
 * </p>
 *  
 * <p> The classes of interest are represented by {@link RClassCode} objects.
 * In opposition, peripheral classes appears as {@link RClass} objects. For
 * the first a complete description of the class is avaiable, like superclass,
 * interfaces, subclasses, code, fields, etc. For the second only the 
 * essential information can be obtained, i.e., the classes that extend or
 * implements it.
 * </p>
 *  
 */
public class Program implements Serializable {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -4329914975067335936L;
	/** The set of classes in the program. The elements will be
     of class {@link RClass} (or one of its subclasses) */
    private Hashtable classes;		// classes used in the program

    /**
     *     Creates the structure os a program. Begining at a given class name,
     *     this constructor calculates all the referenced classes and includes
     *     in the structure. See the description of the parameters for a more
     *     complete explanation
     *      
     *     @param className The name of the starting class. From it all the 
     *     referenced classes are found and included in the structure. The 
     *     class should be found in the classpath
     * 
     *     @param noSys This param tells whether "system" classes should be part
     *     of the main program structure or just as peripheral classes. If
     *     <code>true</code>, classes with the following prefix are kept out
     *     of the main structure:<BR>
     *     <UL>
     *         <LI> java.
     *         <LI> javax.lang
     *         <LI> org.omg
     *     </UL> <BR>
     *     In addition, any referenced class for which the code (a .class) file
     *     can not be found is considered out of the main structure.
     *      
     *     @param toAvoid This is a string that indicates other classes that should be
     *     avoided in the main structure of the program. For example, if the
     *     program uses library packages <code>org.dummy</code> and 
     *     <code>br.din.foo</code> the use of "org.dummy br.din.foo" as the third
     *     argument will keep the classes in these packages out of the program
     *     structure, even if their class files can be found in the classpath
     *      
     *     @throws ClassNotFoundException If the root class is not found in the classpath
     *     @throws FileNotFoundException  If the root class file is not found
     *     @throws IOException Error reading root class file
     *      
     *     */
    public Program(String className, boolean noSys, String toAvoid, String classPath)
            throws ClassNotFoundException, FileNotFoundException, IOException {
        classes = new Hashtable();
        ClassClosure cc = null;


        if (classPath == null) {
            cc = new ClassClosure();
        } else {
            cc = new ClassClosure(classPath);
        }
        String[] closure = cc.getClosure(className, noSys, toAvoid);

        for (int i = 0; i < closure.length; i++) {
            String s = cc.findFile(closure[i]);
            RClass mc = null;

            if (s != null &&	// do I have the code for the class?
                    !cc.doMatch(closure[i], noSys, toAvoid)) {
                JavaClass javaClass = new ClassParser(s).parse();

                mc = new RClassCode(javaClass, closure[i]);
            } else {
                mc = new RClass(closure[i]);
            }
            classes.put(closure[i], mc);
        }
        updateSubSuper();	// links super and subclasses
    }

    /** The same of <BR>
     *     <center> <code>Program( className, true, null ) </code></center>
     *      
     *     @param className The name of the starting class
     *      
     **/
    public Program(String className)
            throws ClassNotFoundException, FileNotFoundException, IOException {
        this(className, true, null, System.getProperty("java.class.path"));
        System.out.println("Property: " + System.getProperty("java.class.path"));
    }

	/** this constructor uses a Map where the keys have the names of the 
	* classes and the elements their bytecodes. */
	public Program(Map hs)
            throws ClassNotFoundException, IOException
	{
        ClassClosure cc = new ClassClosure();
		classes = new Hashtable();
    	Iterator keys = hs.keySet().iterator();
    	while (keys.hasNext()) 
    	{
      		String keyName = (String) keys.next();
      		byte[] b = (byte[]) hs.get(keyName);
      		JavaClass jv = (new ClassParser(
      					new ByteArrayInputStream(b), keyName)).parse();
      		RClassCode rc = new RClassCode(jv, keyName);
      		classes.put(keyName, rc);
            String[]closure = cc.accessedClasses(jv);
            for (int i = 0; i < closure.length; i++) 
            {
                if (!classes.containsKey(closure[i])) 
                {
                    Debug.D("Closure " + closure[i]);
                    // javaClass = new ClassParser ( closure[i] ).parse (  );                    
                    // mc = new RClassCode ( javaClass, closure[i] );
                    RClass mc = new RClass(closure[i]);                    
                    classes.put(closure[i], mc);
                }
            }
}
        updateSubSuper();	// links super and subclasses
	}

    /**
     *     The same of {@link Program#Program(String, boolean, String, String)} but 
     *     all the classes in the {@link ZipFile} are included in the 
     *     structure of the program, as well as the classes they reference..
     *      
     *     */
    public Program(ZipFile zippedFile, boolean noSys, String toAvoid)
            throws ClassNotFoundException, IOException {
        classes = new Hashtable();
        ClassClosure cc = new ClassClosure();
        Enumeration en = zippedFile.entries();
        ZipEntry zippedEntry = null;

        while (en.hasMoreElements()) {
            zippedEntry = (ZipEntry) en.nextElement();
            String className = zippedEntry.getName();

            if (!className.endsWith(".class")) {
                continue;
            }
            JavaClass javaClass;

            javaClass = new ClassParser(zippedFile.getInputStream(zippedEntry), className).parse();	// May throw IOException
            className =
                    cc.toPoint(className.substring(0, className.length() - 6));
            
            if (cc.doMatch(className, noSys, toAvoid))
            	continue;

            RClass mc = new RClassCode(javaClass, className);

            classes.put(className, mc);
            Debug.D("Class " + className);
            String[]closure = cc.getJCClosure(javaClass, noSys, toAvoid);

            for (int i = 0; i < closure.length; i++) {
                if (!classes.containsKey(closure[i])) {
                    Debug.D("Closure " + closure[i]);
                    // javaClass = new ClassParser ( closure[i] ).parse (  );                    
                    // mc = new RClassCode ( javaClass, closure[i] );
                    mc = new RClass(closure[i]);                    
                    classes.put(closure[i], mc);
                }
            }
        }
        updateSubSuper();	// links super and subclasses
    }

    /** A private method used to update the links (extends/implements)
     *     between the 
     *     {@link RClass} objects in the program
     *     */
    private void updateSubSuper() throws ClassNotFoundException {
        Enumeration en = classes.elements();

        while (en.hasMoreElements()) {
            RClass dc = (RClass) en.nextElement();

            if (!(dc instanceof RClassCode)) {
                continue;
            }		// this class is not of interest
            RClassCode mc = (RClassCode) dc;
            String s = mc.getSuperClass();
            RClass auz = (RClass) classes.get(s);

            if (auz == null) {
                throw new ClassNotFoundException(s);
            }
            String name = null;

            auz.setSubClass(name = mc.getName());
            String[] ints = mc.getInterfaces();

            for (int i = 0; i < ints.length; i++) {
                auz = (RClass) classes.get(ints[i]);
                if (auz == null) {
                    throw new ClassNotFoundException(ints[i]);
                }
                auz.setImplementation(name);
            }
        }
        
        // elimina aquelas classes que nao pertencem ao escopo
        // e que n�o possuem sub-classe ou implementa��o
		en = classes.elements();
        while (en.hasMoreElements())
        {
			RClass dc = (RClass) en.nextElement();

			if ( dc instanceof RClassCode ) {
				continue;
			}		// this class is not of interest
			if ( dc.implementations.size() == dc.subclasses.size())
			{ // se s�o iguais, ambos s�o 0
				classes.remove(dc.name);
			}
        }
    }

    /**
     *     Gets the {@link RClass} object for a given class name
     *      
     *     @param s The name of the class for which the information is
     *     required 
     *     */
    public RClass get(String s) {
        return (RClass) classes.get(s);
    }

    /**
     *     Gets the complete list with the names of the classes
     *      
     *     @return An array of strings representing the names of the classes
     *     in the program 
     *     */
    public String[] getClasses() {
        String[]x = new String[classes.size()];
        Enumeration en = classes.elements();
        int i = 0;

        while (en.hasMoreElements()) {
            RClass root = (RClass) en.nextElement();

            x[i++] = root.getName();
        }
        return x;
    }
    
    /**
     *     Gets the name of the kth class returned by {@link Program#getClasses}
     *      
     *     @return A strings representing the name of the kth class returned 
     *     by a call to {@link Program#getClasses}
     * 	  @param k - the order of the class to be accessed; 0 based
     *     */
    public String getClass(int k) {
        String[] x = getClasses();

        if (x == null || x.length <= k) {
            return null;
        }
        return x[k];
    }
    
    /**
     *     Gets the list with the names of the classes in the main structure
     *     of this program. Only objects of type {@link RClassCode} are 
     *     included in the list. The peripheral classes are not.
     *      
     *     @return An array of strings representing the names of the classes
     *     in the main program structure
     *     */
    public String[] getCodeClasses() {
        Vector x = new Vector();
        Enumeration en = classes.elements();

        while (en.hasMoreElements()) {
            RClass root = (RClass) en.nextElement();

            if (root instanceof RClassCode) {
                x.add(root.getName());
            }
        }
        
        
        String[] ret = (String[]) x.toArray(new String[0]);
        Arrays.sort(ret);
        return ret;
    }

    /**
     *     Gets the name of the kth class returned by {@link Program#getCodeClasses}
     *      
     *     @return A strings representing the name of the kth class returned 
     *     by a call to {@link Program#getCodeClasses}
     * 	  @param k - the order of the class to be accessed; 0 based
     *     */
    public String getCodeClass(int k) {
        String[] x = getCodeClasses();

        if (x == null || x.length <= k) {
            return null;
        }
        return x[k];
    }

    /**
     *     Gets the list with the names of the classes in the main structure
     *     of this program. Only objects of type {@link RClassCode} are 
     *     included in the list. The peripheral classes are not.
     *      
     *	  @param packName A package name used as filter to select the classes
     *     @return An array of strings representing the names of the classes
     *     in the main program structure
     *     */
    public String[] getCodeClasses(String packName) {
        Vector x = new Vector();
        Enumeration en = classes.elements();

        while (en.hasMoreElements()) {
            RClass root = (RClass) en.nextElement();

            if (root instanceof RClassCode) {
                if (root.getPackageName().equals(packName)) {
                    x.add(root.getName());
                }
            }
        }
        return (String[]) x.toArray(new String[0]);
    }

    /**
     *     Gets the list with the names of the packages in the main structure
     *     of this program. Only packages corresponding to 
     *	  objects of type {@link RClassCode} are 
     *     included in the list. The peripheral classes are not.
     *     
     *     @return An array of strings representing the names of the packages
     *     in the main program structure
     *     */
    public String[] getCodePackages() {
        String[] cls = getCodeClasses();
        HashSet hs = new HashSet();

        for (int i = 0; i < cls.length; i++) {
            hs.add(RClass.getPackName(cls[i]));
        }
        String[] v = (String[]) hs.toArray(new String[0]);

        Arrays.sort(v);
        return v;
    }

    /**
     *     The opposite of {@link Program#getCodeClasses}.
     *     Gets the list with the names of the classes not in the main structure
     *     of this program. Only objects of type {@link RClass} are 
     *     included in the list.
     *      
     *     @return An array of strings representing the names of the classes
     *     not in the main program structure
     *     */
    public String[] getSysClasses() {
        Vector x = new Vector();
        Enumeration en = classes.elements();

        while (en.hasMoreElements()) {
            RClass root = (RClass) en.nextElement();

            if (!(root instanceof RClassCode)) {
                x.add(root.getName());
            }
        }
        return (String[]) x.toArray(new String[0]);
    }

    /**
     *     Gets the name of the kth class returned by {@link Program#getSysClasses}
     *      
     *     @return A strings representing the name of the kth class returned 
     *     by a call to {@link Program#getSysClasses}
     * 	  @param k - the order of the class to be accessed; 0 based
     *     */
    public String getSysClass(int k) {
        String[] x = getSysClasses();

        if (x == null || x.length <= k) {
            return null;
        }
        return x[k];
    }

    /**
     *     The opposite of {@link Program#getCodeClasses}.
     *     Gets the list with the names of the classes not in the main structure
     *     of this program. Only objects of type {@link RClass} are 
     *     included in the list.
     *      
     *	  @param packName A package name used as filter to select the classes
     *     @return An array of strings representing the names of the classes
     *     not in the main program structure
     *     */
    public String[] getSysClasses(String packName) {
        Vector x = new Vector();
        Enumeration en = classes.elements();

        while (en.hasMoreElements()) {
            RClass root = (RClass) en.nextElement();

            if (!(root instanceof RClassCode)) {
                if (root.getPackageName().equals(packName)) {
                    x.add(root.getName());
                }
            }
        }
        return (String[]) x.toArray(new String[0]);
    }

    /**
     *     Gets the list with the names of the packages not in the main structure
     *     of this program. Only packages corresponding to 
     *	  objects of type {@link RClass} are 
     *     included in the list. 
     *      
     *     @return An array of strings representing the names of the packages
     *     not in the main program structure
     *     */
    public String[] getSysPackages() {
        String[] cls = getCodeClasses();
        HashSet hs = new HashSet();

        for (int i = 0; i < cls.length; i++) {
            hs.add(RClass.getPackName(cls[i]));
        }
        String v[] = (String[]) hs.toArray(new String[0]);

        Arrays.sort(v);
        return v;
    }

    /**
     *     Computes the level (depth) of a class in the hierarchical
     *     structure of the program. 
     *      
     *     @param s The name of the class
     *     @return The level of the class in the hierarchical structure. If
     *     the class is not in the prgram the value is -1. If it is a peripheral
     *     class (type {@link RClass}) the value is 0. Otherwise the value is
     *     <code> 1 + levelOf(its superclass) </code>.
     *     */

    public int levelOf(String s) {
        RClass r = get(s);

        if (r == null) {
            return -1;
        }
        if (!(r instanceof RClassCode)) {
            return 0;
        }
        return levelOf(((RClassCode) r).getSuperClass()) + 1;
    }

    /** <p>Send to the standard output a few informations about this program like: <BR>
     *     </p>
     *     <p>
     *     <UL>
     *         <IL> The list of classes and their level
     *         <IL> The number of code classes
     *         <IL> The number of "system"classes
     *         <IL> The classe whith highest depth
     *         <IL> The classe with highest number of subclasses
     *         <IL> The interface with highest number of implementations
     *     </UL>
     *     </p>
     *     */
    public void print() {
        int codecount = 0, nocodecount = 0, max = -1;
        int impmax = -1, sbmax = -1;
        String smax = "", ssb = "", simp = "";
        Enumeration en = classes.elements();

        while (en.hasMoreElements()) {
            int k;
            RClass root = (RClass) en.nextElement();

            root.print();
            System.out.println("Level: " + (k = levelOf(root.getName())));
            if (root instanceof RClassCode) {
                codecount++;
            } else {
                nocodecount++;
            }
            if (k > max) {
                max = k;
                smax = root.getName();
            }
            if ((k = root.countImplementations()) > impmax) {
                impmax = k;
                simp = root.getName();
            }
            if ((k = root.countSubClasses()) > sbmax) {
                sbmax = k;
                ssb = root.getName();
            }
        }
        System.out.println();
        String[] ccl = getCodeClasses();
        for (int i = 0; i < ccl.length; i++)
        {
        	System.out.print(i + ") " + ccl[i] + " ");
        }
        System.out.println();
        System.out.println("Summary: " + codecount + " code class(es) and " + nocodecount + " system class(es)");
        System.out.println("Maximum level: " + max + " " + smax);
        System.out.println("Maximum number of Subclasses: " + sbmax + " " + ssb);
        System.out.println("Maximum number of Implementations: " + impmax + " " + simp);
    }


    /** Gets the list of subclasses of this one and their subclasses....

     @return An array of strings that contains the names of the subclasses.
     It is never <code>null</code>. If no subclass, an array of size 0 
     is returned.
     */
    public String[] getSubClassClosure(String cl)
    {
    	HashSet s = new HashSet();
    	auxGetSub(cl, s);
        return (String[]) s.toArray(new String[0]);
    }
    
    private void auxGetSub(String cl, HashSet hs)
    {
    	RClass rc = get(cl);
    	if ( rc == null )
    		return;
    	String[] v = rc.getSubClasses();
    	for (int i = 0; i < v.length; i++)
    	{
    		hs.add(v[i]);
    		auxGetSub(v[i], hs);
    	}
    }

    /**
     *     <p>A test driver. Can be called as: <BR></p>
     *     <p>
     *     java program.Program classname [avoid-name-list]
     *     </p>
     *     or
     *     <p>
     *     java program.Program zipfilename
     *     </p>
     *      
     *     <p>
     *     In both cases the system classes are not included in the 
     *     program structure
     *     </p>
     *     */
    static public void main(String args[]) throws Exception {
        Program p = null;

        if (args.length >= 3) {
            p = new Program(args[0], true, args[2], args[1]);
        } else {
            ZipFile zippedFile = null;

            if (args[0].endsWith(".jar")) {
                zippedFile = new JarFile(args[0]);
            } else if (args[0].endsWith(".zip")) {
                zippedFile = new ZipFile(args[0]);
            }

            if (zippedFile == null) {
                p = new Program(args[0]);
            } else {
                p = new Program(zippedFile, true, null);
            }
        }
        // p.print();
        String[] classes = p.getCodeClasses();
        for (int i = 0; i < classes.length; i++)
        {
        	String rcc = p.getCodeClass(i);
        	RClassCode rc = (RClassCode) p.get(rcc);
        	try {
        	String[][] m = rc.getCalledMethods();
        	for (int j = 0; j < m.length; j++)
        	{
        		System.out.println(m[j][0]);
        		for (int k = 1; k < m[j].length; k++)
        		{
            		System.out.println("\t\t" + m[j][k]);
        		}
        	}
        	} catch(Exception e) {
        		System.err.println("IGNORING CLASS DUE EXCEPTION: " + rc.getName());
        		e.printStackTrace();
        		//Silent ignoring exceptions
        	}
        }
    }

}
