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


package br.jabuti.junitexec;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.runner.JUnitCore;

import br.jabuti.util.ToolConstants;

public class JUnitJabutiCore {

	static public HashMap<String, String> runCollecting(String classpath,
			String ts, PrintStream ps) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			MalformedURLException, IllegalArgumentException,
			InvocationTargetException, SecurityException, NoSuchMethodException {

		Class<?> clazz1 = ToolConstants.getClassFromClasspath("org.junit.runner.JUnitCore", true, classpath);
		JUnitCore juc = (JUnitCore) clazz1.newInstance();

		Class<?> clazz = ToolConstants.getClassFromClasspath("br.jabuti.junitexec.CollectorListener", false, classpath);

		Class[] argsClass = new Class[] { PrintStream.class };
		Constructor<?> cons = clazz.getConstructor(argsClass);
		CollectorListener il = (CollectorListener) cons.newInstance(ps);

		juc.addListener(il);

		clazz = ToolConstants.getClassFromClasspath(ts, false, classpath);

		// Redirecting System.out
		PrintStream current = System.out;
		if (ps != null) {
			System.setOut(ps);
		}
		juc.run(clazz);

		System.setOut(current);
		return il.getTestSet();
	}

	static public void runInstrumenting(String classpath, String ts,
			String trace, Set<String> testSet, PrintStream ps)
			throws ClassNotFoundException, MalformedURLException,
			InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {

		Class<?> clazz1 = ToolConstants.getClassFromClasspath(
				"org.junit.runner.JUnitCore", true, classpath);
		JUnitCore juc = (JUnitCore) clazz1.newInstance();

		Class<?> clazz = ToolConstants.getClassFromClasspath(
				"br.jabuti.junitexec.InstrumenterListener", false, classpath);

		Class[] argsClass = new Class[] { String.class, Set.class,
				PrintStream.class };
		Constructor<?> cons = clazz.getConstructor(argsClass);
		InstrumenterListener il = (InstrumenterListener) cons.newInstance(
				trace, testSet, ps);

		juc.addListener(il);

		clazz = ToolConstants.getClassFromClasspath(ts, false, classpath);

		// Redirecting System.out
		PrintStream current = System.out;
		if (ps != null) {
			System.setOut(ps);
		}
		juc.run(clazz);
		System.setOut(current);
	}

	public static void main(String[] args) throws Exception {
		String tcClass = null, trace = null, classpath = null;
		Set<String> testSet = new HashSet<String>();
		HashMap<String, String> hm;

		if (args.length < 2) {
			JUnitJabutiCore.usage();
		} else {

			for (int i = 0; i < args.length; i++) {
				if ("-trace".equals(args[i]))
					trace = args[++i];
				else if ("-tcClass".equals(args[i]))
					tcClass = args[++i];
				else if ("-cp".equals(args[i]))
					classpath = args[++i];
				else
					testSet.add(args[i]);
			}
			if (trace != null) {
				if (testSet.size() == 0)
					testSet = JUnitJabutiCore.runCollecting(classpath, tcClass, System.out).keySet();
				JUnitJabutiCore.runInstrumenting(classpath, tcClass, trace,	testSet, System.out);
			} else {
				hm = JUnitJabutiCore.runCollecting(classpath, tcClass, System.out);
				Iterator<String> it = hm.keySet().iterator();
				while (it.hasNext()) {
					String n = it.next();
					System.out.println("TC Name: " + n + " STATUS: " + hm.get(n));
				}
			}
		}
	}

	static public void usage() {
		System.out
				.println("JUnitJabutiCore [-trace <file_name>] -cp <test_set_classpath> -tcClass <test_class> [<test_case_name1> <test_case_name2>...]");
	}
}