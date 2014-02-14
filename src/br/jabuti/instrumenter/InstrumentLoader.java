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


package br.jabuti.instrumenter;


// import de.fub.bytecode.classfile.*;
// import de.fub.bytecode.generic.*;
// import de.fub.bytecode.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.bcel.util.ClassPath;

import br.jabuti.util.Debug;


/** This is the class that implements the functionality of a
 JVM code instrumenter. Using such object it is possible
 to insert JVM code in a given JVM method.

 @version: 0.00001
 @author: Auri Marcelo Rizzo Vincenzi
 M�rcio Eduardo Delamaro

 */

public class InstrumentLoader extends ClassLoader {
	
    // Classes instrumented
    private Map classes;
    private Hashtable loaded;
    private Vector dontLookup;
    ClassPath cp = null;
    
    public InstrumentLoader(Map cl, Map init, String clasPath) {
        super();
        classes = cl;
        loaded = new Hashtable(init);
        dontLookup = new Vector();
        cp = new ClassPath(clasPath);
    }

    public InstrumentLoader(Map cl, String clasPath) {
        this(cl, new Hashtable(), clasPath);
    }

    public void addDontLookup(String x)
    {
    	dontLookup.add(x);
    }
    
    protected URL findResource(String name) {
        String cf = null;

        try {
            cf = cp.getPath(name);
        } catch (IOException e) {
            return null;
        }
    	
        try {
            return new URL("file://" + cf);
        } catch (MalformedURLException e) { 
            System.err.println("Invalid URL: " + cf); 
            return null;
        }
    }

    public synchronized Class loadClass(String name, boolean resolve) 
            throws ClassNotFoundException {
      		
        Class cl = (Class) loaded.get(name);
      	
        Debug.D("Loading:" + name);
        if (cl != null) {
        	Debug.D("Jah existe " + cl);
            return cl;
        }
      	
        boolean lookup = true;
        for (int i = 0; i < dontLookup.size(); i++)
        {
        	String pack = (String) dontLookup.elementAt(i);
        	if ( name.startsWith(pack) )
        	{
        		lookup = false;
        		break;
        	}
        }
        if ( ! lookup ) // nao deve procurar, mas sim usar o classloader pai
        {
        	cl = this.getParent().loadClass(name);
        	if ( cl != null )
        	{
        		loaded.put(name, cl);
        	}
        	return cl;
        }
        byte[] classBytes = null;

        if (classes == null || !classes.containsKey(name)) {	// classe nao estah no Map; procura no classpath
     	
           try {
                classBytes = cp.getBytes(name, ".class");
    	        cl = super.defineClass(name, classBytes, 0, classBytes.length);
            	loaded.put(name, cl);
            } catch (IOException e) {
                cl = this.getParent().loadClass(name);
            } 
        } else {     	
            // usa a classe se ela estiver no Map
            //System.out.println("Loading class from MAP: " + name + 
            //   classes.get(name).getClass());
            classBytes = (byte[]) classes.get(name);
	        cl = super.defineClass(name, classBytes, 0, classBytes.length);
        	loaded.put(name, cl);
        }
        // cl = (Class) classes.get(name);
        Debug.D("Loaded: " + name + " by " + cl.getClassLoader());
        if (resolve && cl != null) { 
            resolveClass(cl);
        }

        return cl;
    }
    /*
     public Class findClass(String name) 
     {
     Debug.D("Find: "+ name);
     byte[] b = (byte[]) classes.get(name);
     return super.defineClass(name, b, 0, b.length);
     }
     */

    public void runClass(String name, String[] a) throws Throwable {  
        Class c;

        c = loadClass(name);
        String[] args = a;

        if (args == null) {
            args = new String[] {};
        }
        java.lang.reflect.Method mainMethod = 
                c.getMethod("main", new Class[] { args.getClass() }
                );
        Object[] ob = new Object[] { args };
		
        ILThread ilt = null;

        try {
            ilt = new ILThread(mainMethod, ob);
            ilt.start();
            ilt.join();
        } catch (InterruptedException e) {
            throw e;
        }
        if (ilt.excep != null) {
            throw ilt.excep;
        }
    }

}


class ILThread extends Thread {
    Method main;
    Object[] args;
    Throwable excep;
	
    ILThread(Method m, Object[] o) {
        main = m;
        args = o;
        excep = null;
    }
	
    public void run() {
        try {
            main.invoke(null, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            excep = e.getTargetException();
        } catch (IllegalAccessException e) {
            excep = e;
        }
    }
}
