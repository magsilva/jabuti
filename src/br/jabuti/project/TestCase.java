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


package br.jabuti.project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.Criterion;
import br.jabuti.gui.JabutiGUI;
import br.jabuti.probe.ProbedNode;

/**
 *
 * This class is responsible do deal with a single.
 * test case.
 * The first time when a test case is loaded the coverage
 * w.r.t this single test case is computed and stored...
 *
 * Every time when the trace file is read again the
 * coverage is recomputed.
 *
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 * @author: Tatiana Sugeta
 *
 */
public class TestCase {

	/**
	 * Coverage for this particular test case w.r.t. all methods
	 * in all classes.
	 */
	private Coverage[] testCaseCoverage;

	// The label of this test case
	private String label;

	// The alias for this test case (used by JUnit)
	private String alias;
	
	// The test case host name
	private String hostName;

	// Used by toString method to ident the output
	private String prefix = new String("");



	public TestCase(JabutiProject prj, String l, String a) {
		label = l;
		alias = a;
		
		hostName = null;

		testCaseCoverage = new Coverage[Criterion.NUM_CRITERIA];
		for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
			testCaseCoverage[i] =
				new Coverage(
					0,
					prj.getProjectCoverage(i).getNumberOfRequirements());
		}
	}

	/*
	 * This method is responsible to add all paths related with 
	 * a this test case object. The paths are stored in a 
	 * Hashtable that indexes ProbedNode objects in Vector objects.
	 * Each Vector object contains an array os String (String[]),
	 * representing a path.
	 *//*
	public void addTestCasePathsFromXML(String label, Hashtable tab) {

		Iterator it = tab.keySet().iterator();
		while (it.hasNext()) {
			// Recovering the ProbedNodeObject
			HostProbedNode pdn = (HostProbedNode) it.next();
			Vector v = (Vector) tab.get(pdn);

			String[][] pathList = new String[v.size()][];

			for (int i = 0; i < v.size(); i++) {
				pathList[i] = (String[]) v.elementAt(i);
			}
		}
	}*/

	/*
	 * This method is responsible to add all paths related with 
	 * a this test case object. The paths are stored in a 
	 * Hashtable that indexes ProbedNode objects in a matrix of
	 * String objects (String[][]), representing a path the set of
	 * paths of such a test case.
	 */
	public void addTestCaseFromTRC(
		JabutiProject prj,
		Hashtable tab) {

		Hashtable classes = prj.getClassFilesTable();
		Iterator it = classes.values().iterator();

		while (it.hasNext()) {
			ClassFile cf = (ClassFile) it.next();

			HashMap methods = cf.getMethodsTable();
			Iterator mthIt = methods.values().iterator();

			while (mthIt.hasNext()) {
				ClassMethod cm = (ClassMethod) mthIt.next();
				Criterion criterion = null;

				// A test case is composed by several paths,
				// executed in different classes and methods

				if (tab != null) {

					Iterator itP = tab.keySet().iterator();

					// For each one of this path
					while (itP.hasNext()) {
						ProbedNode pdn = (ProbedNode) itP.next();

						// Check if the current path classId is the same as the classId of
						// this method
						if (!pdn.clazz.equals(cf.getClassName())) {
							continue;
						}
						// Check if the path methodId is the same as this methodId
						if (pdn.metodo != cm.getMethodId()) {
							continue;
						}
						// If so, get the sequence of nodes executed by this path
						String pathList[][] = (String[][]) tab.get(pdn);

						// Getting the host name where such a test case
						// was executed
						if (hostName == null) {
							hostName = "localhost";
						}

						// Adding new paths info...
						for (int k = 0; k < pathList.length; k++) {
              
			              /*System.err.println("********************** pathList");
			              for (Object p : pathList[k]) {
			                System.err.println(p);
			              }*/


							Object[] thePath = AbstractCriterion.changePath(
							  cm.getCFG(), pathList[k]);

				              /*System.err.println("********************** thePath");
				              for (Object p : thePath) {
				                System.err.println(p);
				              }*/
														
							for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {								
								criterion = cm.getCriterion(i);
								//System.out.println("Path: " + thePath + " Label: " + getLabel());
								criterion.addPath( thePath,	getLabel() );
							}
						}
					}
				}
			}
		}
	}

	/**********************************************************    
	 * Get and Set Methods implementation                          
	 ***********************************************************/
	/**
	 * Returns the coverage w.r.t. all effective test
	 * cases in this test set.
	 */
	public Coverage getTestCaseCoverage(int c) {
		if ((c >= 0) && (c < Criterion.NUM_CRITERIA)) {
			return testCaseCoverage[c];
		} else {
			return null;
		}
	}

	/**
	 * Returns the coverage of a given path in a given 
	 * HostProbedNode (pdn), considering a given criterion.
	 *
	 * The coverage is the number of required elements covered by
	 * the corresponding path by the total number of covered requirements
	 * by this test case, i.e., how much of the covered requirements 
	 * of a given test case is covered by this particular path.
	 */
	public Coverage getTestCaseCoverageByPath(
		int c,
		ProbedNode pdn,
		String path) {
		String className = pdn.clazz;
		ClassFile cf = JabutiGUI.getProject().getClassFile(className);
		ClassMethod cm = cf.getMethod(pdn.metodo);
		Criterion criterion = cm.getCriterion(c);
		HashSet hs = criterion.getCoveredRequirements(path);

		return new Coverage(
			hs.size(),
			getTestCaseCoverage(c).getNumberOfCovered());
	}

	/**
	 * This method update the coverage w.r.t this this test case.
	 * A test case can be composed by several paths... Each path has a single
	 * name and is stored in a set (pathSet) when this test case is added 
	 * to a given method ({@link ClassMethod#updateClassMethodCoverage}).
	 *
	 */
	public void updateTestCaseCoverage(JabutiProject prj) {
		//if (testCaseCoverage[0].getNumberOfCovered() == 0) {
			//System.out.println("NEED TO UPDATE TEST CASE: " + label);
			int[] totalCovered = new int[Criterion.NUM_CRITERIA];

			for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
				testCaseCoverage[i] =
					new Coverage(
						0,
						prj.getProjectCoverage(i).getNumberOfRequirements());
				totalCovered[i] = 0;
			}

			Hashtable classes = prj.getClassFilesTable();
			Iterator it = classes.values().iterator();

			while (it.hasNext()) {
				ClassFile cf = (ClassFile) it.next();

				HashMap methods = cf.getMethodsTable();
				Iterator mthIt = methods.values().iterator();

				while (mthIt.hasNext()) {
					ClassMethod cm = (ClassMethod) mthIt.next();
					Criterion criterion = null;

					for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
						criterion = cm.getCriterion(i);
						HashSet covered =
							criterion.getPossibleCoveredRequirements(label);

						totalCovered[i] = totalCovered[i] + covered.size();
					}
				}
			}
			for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
				testCaseCoverage[i].setNumberOfCovered(totalCovered[i]);
			}
		//} 
		//else {
		//	System.out.println("IGNORED TEST CASE: " + label);
		//}
	}

	public String getLabel() {
		return label;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String a) {
		alias = a;
	}

	public String toString(String p) {
		prefix = p;
		return toString();
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String n) {
		hostName = n;
	}

	public String toString() {
		String out = new String(prefix + "TestCase: " + label + " - " + alias + "\n");

		out = out + coverage2TXT(prefix + "\t");

		return out;
	}
	public String coverage2TXT(String prefix) {
		String out =
			prefix
				+ AbstractCriterion.getName(Criterion.PRIMARY_NODES)
				+ ": "
				+ getTestCaseCoverage(Criterion.PRIMARY_NODES).toString()
				+ "\n";
		out =
			out
				+ prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_NODES)
				+ ": "
				+ getTestCaseCoverage(Criterion.SECONDARY_NODES).toString()
				+ "\n";
		out =
			out
				+ prefix
				+ AbstractCriterion.getName(Criterion.PRIMARY_EDGES)
				+ ": "
				+ getTestCaseCoverage(Criterion.PRIMARY_EDGES).toString()
				+ "\n";
		out =
			out
				+ prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_EDGES)
				+ ": "
				+ getTestCaseCoverage(Criterion.SECONDARY_EDGES).toString()
				+ "\n";
		out =
			out
				+ prefix
				+ AbstractCriterion.getName(Criterion.PRIMARY_USES)
				+ ": "
				+ getTestCaseCoverage(Criterion.PRIMARY_USES).toString()
				+ "\n";
		out =
			out
				+ prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_USES)
				+ ": "
				+ getTestCaseCoverage(Criterion.SECONDARY_USES).toString()
				+ "\n";
		out =
			out
				+ prefix
				+ AbstractCriterion.getName(Criterion.PRIMARY_POT_USES)
				+ ": "
				+ getTestCaseCoverage(Criterion.PRIMARY_POT_USES).toString()
				+ "\n";
		out =
			out
				+ prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_POT_USES)
				+ ": "
				+ getTestCaseCoverage(Criterion.SECONDARY_POT_USES).toString()
				+ "\n";
		return out;
	}

}
