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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassPath;

import br.jabuti.util.Debug;

/**
 * This class is used to explore a program structure. Given a class path and a
 * base class X, this class has a few methods to find out which other classes X
 * depends on. The programer can specify whether system classes should be
 * included or excluded in this search and can also determine which packages
 * should be ignored in the search.
 * 
 * @author delamaro
 * 
 * 
 */
public class ClassClosure {
	static final private String[] sysPrefix = new String[] { "java.",
			"javax.lang", "org.omg" };

	ClassPath cp = null;

	public ClassClosure(String classPath) {
		cp = new ClassPath(classPath);
	}

	public ClassClosure() {
		cp = ClassPath.SYSTEM_CLASS_PATH;
	}

	public String[] getClosure(String className) {
		return getClosure(className, true, null);
	}

	/**
	 * Method responsable to parse the code of a given class name and found a
	 * list of all classes related with this one. In this list will be included
	 * neither system class if the variable <code> noSys </code> is true nor the
	 * ones spaecified in <code>toAvoid</code>.
	 * 
	 * @param className
	 *            The name of the starting class. From it all the referenced
	 *            classes are found and included in the structure. The class
	 *            should be found in the classpath
	 * 
	 * @param noSys
	 *            This param tells whether "system" classes should be part of
	 *            the main program structure or just as peripheral classes. If
	 *            <code>true</code>, classes with the following prefix are kept
	 *            out of the main structure:<BR>
	 *            <UL>
	 *            <LI>java.
	 *            <LI>javax.lang
	 *            <LI>org.omg
	 *            </UL>
	 * <BR>
	 *            In addition, any referenced class for wich the code (a .class)
	 *            file can not be found is considered out of the main structure.
	 * 
	 * @param toAvoid
	 *            This is a string that indicates other classes that should be
	 *            avoided in the main structure of the program. For example, if
	 *            the program uses library packages <code>org.dummy</code> and
	 *            <code>br.din.foo</code> the use of "org.dummy br.din.foo" as
	 *            the third argument will keep the classes in these packages out
	 *            of the program structure, even if their class files can be
	 *            found in the classpath
	 */
	public String[] getClosure(String className, boolean noSys, String toAvoid) {
		String s = className;

		JavaClass classFile = null;

		s = findFile(s);
		if (s == null) {
			return new String[0];
		}
		try {
			classFile = new ClassParser(s).parse();
			String pck = RClass.getPackName(className);

			if (!pck.equals(classFile.getPackageName())) {
				Debug.D(pck + " " + classFile.getPackageName() + " "
						+ pck.equals(classFile.getPackageName()));
				return new String[0];
			}
		} catch (IOException e) {
			return new String[0];
		}
		return getJCClosure(classFile, noSys, toAvoid);
	}

	public String[] getJCClosure(JavaClass classFile, boolean noSys,
			String toAvoid) {
		Hashtable interestedClasses = new Hashtable();
		Vector classesToProcess = new Vector();
		Vector classesToAvoid = new Vector();

		if (!doMatch(classFile.getClassName(), noSys, toAvoid)) {
			interestedClasses.put(classFile.getClassName(), classFile);
			classesToProcess.add(classFile);
		} else {
			classesToAvoid.add(classFile.getClassName());
		}
		for (int i = 0; i < classesToProcess.size(); i++) {
			classFile = (JavaClass) classesToProcess.elementAt(i);

			String[] cl = accessedClasses(classFile);

			for (int j = 0; j < cl.length; j++) {
				try {
					if (!doMatch(cl[j], noSys, toAvoid)) { // May throw
															// IOException
						String h = findFile(cl[j]);

						if (h != null) {
							classFile = new ClassParser(h).parse();
							String pck = RClass.getPackName(cl[j]);

							if (pck.equals(classFile.getPackageName())
									&& (!interestedClasses
											.containsKey(classFile
													.getClassName()))) {
								classesToProcess.add(classFile);
								interestedClasses.put(classFile.getClassName(),
										classFile);
							}
						}
					}
				} catch (FileNotFoundException e) {// System.out.println("Skipped "
													// + s1);
				} catch (IOException e) {// System.out.println("Skipped " + s1);
				}
				if (!classesToAvoid.contains(cl[j])) {
					classesToAvoid.add(cl[j]);
				}
			}
		}
		return (String[]) classesToAvoid.toArray(new String[0]);
	}

	public String toPoint(String s) {
		return s.replace('/', '.');
	}

	public String[] accessedClasses(JavaClass javaClass) {
		Vector interestedClasses = new Vector();
		ConstantPool cp = javaClass.getConstantPool();
		Constant[] ct = cp.getConstantPool();

		for (int i = 0; i < ct.length; i++) {
			if (ct[i] instanceof ConstantClass) {
				ConstantClass cc = (ConstantClass) ct[i];
				// System.out.println("accessed: " + cc);
				ConstantUtf8 cutf = (ConstantUtf8) cp.getConstant(cc
						.getNameIndex());
				String t = cutf.getBytes();

				if (t.charAt(0) != '[') {
					interestedClasses.add(toPoint(t));
				}
			}
		}
		return (String[]) interestedClasses.toArray(new String[0]);
	}

	public boolean doMatch(String x, boolean noSys, String toAvoid) {
		if (noSys) {
			for (int i = 0; i < sysPrefix.length; i++) {
				if (x.startsWith(sysPrefix[i])) {
					return true;
				}
			}
		}
		if (toAvoid == null) {
			return false;
		}
		int inde = 0, indd = -1;

		do {
			toAvoid = toAvoid.substring(indd + 1);
			indd = toAvoid.indexOf(' ');

			if (indd < 0) {
				indd = toAvoid.length();
			}
			if (indd <= inde) {
				continue;
			}
			String toAvdStr = toAvoid.substring(inde, indd);
			if (toAvdStr.endsWith("*")) {
				toAvdStr = toAvdStr.substring(0, indd - 1);
				if (x.startsWith(toAvdStr)) {
					return true;
				}
			} else {
				if (x.equals(toAvdStr))
					return true;
			}

		} while (indd < toAvoid.length());
		return false;
	}

	public String findFile(String x) {
		String s = null;

		try {
			s = cp.getPath(x, ".class");
		} catch (IOException e) {
			return null;
		}
		File classFile = new File(s);

		if (!classFile.isFile()) {
			return null;
		}
		return s;
	}

	static public void main(String args[]) throws Exception {
		ClassClosure cc = new ClassClosure();
		String interestedClasses[] = cc.getClosure(args[0]);

		for (int i = 0; i < interestedClasses.length; i++) {
			System.out.println(i + ")" + interestedClasses[i]);
		}
		System.out.println("==================================");
		interestedClasses = cc.getClosure(args[0], false, null);
		for (int i = 0; i < interestedClasses.length; i++) {
			System.out.println(i + ")" + interestedClasses[i]);
		}
		System.out.println("==================================");
		interestedClasses = cc.getClosure(args[0], true, args[1]);
		for (int i = 0; i < interestedClasses.length; i++) {
			System.out.println(i + ")" + interestedClasses[i]);
		}
	}

}
