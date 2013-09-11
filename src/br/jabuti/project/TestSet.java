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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.Criterion;
import br.jabuti.probe.DefaultTraceReader;
import br.jabuti.probe.ProbedNode;
import br.jabuti.probe.TraceReader;
import br.jabuti.util.ToolConstants;

/**
 * This class is responsible do deal with the test set. it reads a trace file
 * and stores all the test cases into test cases objects.
 * 
 * It can also report the coverage of test set, considering only the active test
 * cases.
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 * @author: Tatiana Sugeta
 * 
 */
public class TestSet {

	/**
	 * Coverage of the test set considering only the active test cases
	 */
	private static Coverage[] testSetCoverage;

	/** The trace file being used */
	private static File traceFile;

	/** The test case table */
	private static Hashtable testCaseTable;

	/** The active test cases */
	private static HashSet activeTestCases;

	/** The set of test cases to be removed */
	private static HashSet toDeleteTestCases;

	/** Slice Tool: The failed test cases */
	private static HashSet failSet;

	/** Slice Tool: The successed test cases */
	private static HashSet successSet;

	private static int tcId = 0;

	// Used by toString method to ident the output
	static private String prefix = new String("");

	public static void restart() {
		testSetCoverage = new Coverage[Criterion.NUM_CRITERIA];
		traceFile = null;
		testCaseTable = new Hashtable();
		activeTestCases = new HashSet();
		toDeleteTestCases = new HashSet();

		// Slice Tool
		failSet = new HashSet();
		successSet = new HashSet();

		// Count the number of test cases
		tcId = 0;
	}

	/**
	 * Since all the structures in this class are static, before use it, this
	 * method should be called to initialize all static variables
	 */
	public static void initialize(JabutiProject prj, String traceName) {
		try {
			restart();

			TestSet.setTraceFile(new File(traceName));

			updateTestSetCoverage(prj);
		} catch (NullPointerException npe) {
			testSetCoverage = null;
			traceFile = null;
			testCaseTable = null;
			activeTestCases = null;
		}
	}

	public static boolean loadTraceFile(JabutiProject prj) {
		TraceReader dtr = null;

		if ((traceFile != null) && (traceFile.exists()) && (traceFile.isFile())) {
			try {
				dtr = new DefaultTraceReader(traceFile);
				

				Hashtable<ProbedNode,String[][]> trace = dtr.getPaths();

				int cont = 1;

				Vector newTCs = new Vector();

				while (trace != null) {
					TestCase tc = TestSet.createNewTestCase(prj);
					
					tc.setAlias(dtr.getName()); // recuperando o nome do caso de teste
					
					tc.addTestCaseFromTRC(prj, trace);
					newTCs.add(tc);

					trace = dtr.getPaths();
					cont++;
				}

				if (newTCs.size() > 0) {
					for (int i = 0; i < newTCs.size(); i++) {
						TestCase tc = (TestCase) newTCs.elementAt(i);
						TestSet.activateTestCase(prj, tc.getLabel());
					}
					TestSet.updateOverallCoverage(prj);
					// After read the trace file, delete its content

					if (clearTraceFile(prj.getTraceFileName())) {
						return true;
					} else {
						return false;
					}
				}
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
				return false;
			}
		}
		return false;
	}

	public static boolean loadAndCutTraceFile(JabutiProject prj) {
		TraceReader dtr = null;

		if ((traceFile != null) && (traceFile.exists()) && (traceFile.isFile())) {
			try {
				dtr = new DefaultTraceReader(traceFile);

				Hashtable<ProbedNode,String[][]> trace = dtr.getPaths();

				int cont = 1;

				Vector newTCs = new Vector();
				Vector inutilTCs = new Vector();

				while (trace != null) {
					// pega cobertura anterior
					Coverage[] old = new Coverage[Criterion.NUM_CRITERIA];
					
					for (int i = 0; i < old.length; i++) {
						old[i] = TestSet.getTestSetCoverage(i);
					}
					TestCase tc = TestSet.createNewTestCase(prj);
					
					tc.setAlias(dtr.getName()); // recuperando o nome do caso de teste
					
					tc.addTestCaseFromTRC(prj, trace);
					TestSet.activateTestCase(prj, tc.getLabel());

					TestSet.updateOverallCoverage(prj);

					boolean mudou = false;
					for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
						if (!old[i].equals(TestSet.getTestSetCoverage(i))) {
							mudou = true;
							break;
						}
					}
					if (mudou) {
						newTCs.add(tc);
					} else {
						inutilTCs.add(tc);
					}

					trace = (Hashtable) dtr.getPaths();
					cont++;
				}

				if (inutilTCs.size() > 0) {
					for (int i = 0; i < inutilTCs.size(); i++) {
						TestCase tc = (TestCase) inutilTCs.elementAt(i);
						TestSet.removeTestCase(prj, tc.getLabel());
					}
				}
				TestSet.updateOverallCoverage(prj);

				if (clearTraceFile(prj.getTraceFileName())) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
				return false;
			}
		}
		return false;
	}

	private static boolean clearTraceFile(String name) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(name, false); // append = false, therefore
												// write over
		} catch (IOException e) {
			return false;
		}

		try {
			fw.write("");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/***************************************************************************
	 * Get and Set Methods implementation
	 **************************************************************************/
	public static File getTraceFile() {
		return traceFile;
	}

	private static void setTraceFile(File f) {
		traceFile = f;
	}

	/**
	 * Make an existent test case active
	 */
	public static void activateTestCase(JabutiProject prj, String label) {
		if (!isActive(label)) {
			// System.out.println("Ativando caso de teste: " + label);
			if (isDeleted(label))
				undeleteTestCase(prj, label);
			activeTestCases.add(label);

			/* Enable the test case on the entire project */
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
						criterion.enablePath(label);
					}
				}
			}
		}
	}

	/**
	 * Make an existent test case desactive
	 */
	public static void desactivateTestCase(JabutiProject prj, String label) {
		if (isActive(label)) {
			// System.out.println("Desativando caso de teste: " + label);
			activeTestCases.remove(label);

			/* Disabling the test case on the entire project */
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
						criterion.disablePath(label);
					}
				}
			}
		}
	}

	/**
	 * Set an existent test case to be deleted. The test case will be removed
	 * only when the project is saved. Once a test case is set to be removed it
	 * is also desactivated, such that they are desconsidered when calculating
	 * the coverage.
	 */
	public static void toDeleteTestCase(JabutiProject prj, String label) {
		if (!isDeleted(label)) {
			toDeleteTestCases.add(label);
			TestSet.desactivateTestCase(prj, label);
		}
	}

	/**
	 * This method permanently remove a given test case
	 */
	public static void removeTestCase(JabutiProject prj, String label) {
		if (!TestSet.isActive(label))
			return;

		activeTestCases.remove(label);
		testCaseTable.remove(label);

		/* Permanently remove a given test case on the entire project */
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
					criterion.removePath(label);
				}
			}
		}

	}

	/**
	 * This method permanently remove all test cases
	 */
	public static void removeTestCases(JabutiProject prj) {
		if (TestSet.getNumberOfDeletedTestCases() > 0) {
			Object[] tcLabels = TestSet.getDeletedSet().toArray();
			for (int j = 0; j < tcLabels.length; j++) {
				TestSet.removeTestCase(prj, (String) tcLabels[j]);
			}
		}
	}

	/**
	 * Set an existent test case to be undeleted. Once a test case is set to be
	 * undeleted it is also re-activated, such that they are reconsidered when
	 * calculating the coverage.
	 */
	public static void undeleteTestCase(JabutiProject prj, String label) {
		if (isDeleted(label)) {
			toDeleteTestCases.remove(label);
			TestSet.activateTestCase(prj, label);
		}
	}

	/**
	 * Add a test case in the fail set and remove it of the success set
	 */
	public static void addToFailSet(JabutiProject prj, String label) {
		removeFromSuccessSet(prj, label);
		failSet.add(label);
		activateTestCase(prj, label);
	}

	/**
	 * Remove a test case from the fail set
	 */
	public static void removeFromFailSet(JabutiProject prj, String label) {
		failSet.remove(label);
		TestSet.desactivateTestCase(prj, label);
	}

	/**
	 * Add a test case in the fail set and remove it of the success set
	 */
	public static void addToSuccessSet(JabutiProject prj, String label) {
		TestSet.removeFromFailSet(prj, label);
		successSet.add(label);
		TestSet.activateTestCase(prj, label);
	}

	/**
	 * Remove a test case from the fail set
	 */
	public static void removeFromSuccessSet(JabutiProject prj, String label) {
		successSet.remove(label);
		TestSet.desactivateTestCase(prj, label);
	}

	public static HashSet getFailSet() {
		return failSet;
	}

	public static HashSet getSuccessSet() {
		return successSet;
	}

	public static HashSet getDeletedSet() {
		return toDeleteTestCases;
	}

	/**
	 * This method returns all active test cases w.r.t. a given method method
	 */
	public static HashSet getActiveTestCases() {
		return activeTestCases;
	}

	/**
	 * Get a specific test case object
	 */
	public static TestCase getTestCase(String label) {
		if (testCaseTable.containsKey(label)) {
			return (TestCase) testCaseTable.get(label);
		} else {
			return null;
		}
	}

	/**
	 * Add a single test case
	 */
	private static void addTestCase(JabutiProject prj, TestCase tc,
	// boolean active,
			boolean success, boolean fail) {
		if (tc != null) {
			// Adding the test case into the test set...
			testCaseTable.put(tc.getLabel(), tc);

			// Updating the test case status
			// if ( active )
			// activeTestCases.add( tc.getLabel() );
			// else
			// activeTestCases.remove( tc.getLabel() );

			if (success)
				successSet.add(tc.getLabel());

			if (fail)
				failSet.add(tc.getLabel());
		}
		return;
	}

	/**
	 * Creates a new TestCase instance and adds it to the current test set
	 * @param alias 
	 */
	public static TestCase createEmptyTestCase(JabutiProject prj, String label,
	String alias,
			boolean success, boolean fail) {
		TestCase tc = new TestCase(prj, label, alias);
		TestSet.addTestCase(prj, tc, success, fail);

		// System.out.println( "TEST CASE ADDED: " + tc.getLabel() );

		return tc;
	}

	/**
	 * Creates a new TestCase instance and adds it to the current test set
	 */
	public static TestCase createNewTestCase(JabutiProject prj) {
		TestCase tc = new TestCase(prj, TestSet.newTestCaseLabel(), "");
		TestSet.addTestCase(prj, tc,
		// true,
				false, false);

		return tc;
	}

	/**
	 * Get the labesl of all test cases
	 */
	public static String[] getTestCaseLabels() {
		String[] labels = new String[testCaseTable.size()];
		if (testCaseTable.size() > 0) {
			Iterator it = testCaseTable.keySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				labels[i++] = new String(it.next().toString());
			}
			Arrays.sort(labels);
		}
		return labels;
	}

	/**
	 * Checks if a given test case is active or not
	 */
	public static boolean isActive(String label) {
		return activeTestCases.contains(label);
	}

	/**
	 * Checks if a given test case is success
	 */
	public static boolean isSuccess(String label) {
		return successSet.contains(label);
	}

	/**
	 * Checks if a given test case is fail
	 */
	public static boolean isFail(String label) {
		return failSet.contains(label);
	}

	/**
	 * Checks if a given test case is to be deleted
	 */
	public static boolean isDeleted(String label) {
		return toDeleteTestCases.contains(label);
	}

	/**
	 * Resturns the number of read test cases from the trace file
	 */
	public static int getNumberOfTestCases() {
		if (testCaseTable != null) {
			return testCaseTable.size();
		}
		return 0;
	}

	/**
	 * Resturns the number of read test cases from the trace file
	 */
	public static int getNumberOfDeletedTestCases() {
		if (toDeleteTestCases != null) {
			return toDeleteTestCases.size();
		}
		return 0;
	}

	/**
	 * Resturns the number of active test cases
	 */
	public static int getNumberOfActiveTestCases() {
		if (activeTestCases != null) {
			return activeTestCases.size();
		}
		return 0;
	}

	/**
	 * Returns the coverage w.r.t. all effective test cases in this test set.
	 */
	public static Coverage getTestSetCoverage(int c) {
		if ((c >= 0) && (c < Criterion.NUM_CRITERIA)) {
			return testSetCoverage[c];
		} else {
			return null;
		}
	}

	public static void updateTestSetCoverage(JabutiProject prj) {
		for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
			testSetCoverage[i] = prj.getProjectCoverage(i);
		}

		Iterator it = testCaseTable.values().iterator();

		while (it.hasNext()) {
			TestCase tc = (TestCase) it.next();

			tc.updateTestCaseCoverage(prj);
		}
	}

	public static void updateOverallCoverage(JabutiProject prj) {
		// System.out.println("Atualizando cobertura total!!!");
		// After loaded all test cases:
		// 1) Update the coverage w.r.t the entire
		// project; and
		prj.updateProjectCoverage();
		// 2) Update the coverage w.r.t each test case
		//
		// Should be in this ordem since to update the
		// coverage of each test case, the coverage of
		// a given method should be computed.
		TestSet.updateTestSetCoverage(prj);
	}

	/*
	 * Generates a new Test Case Label based on the current value of test case
	 * identifier. The first valid test case label starts from 1.
	 */
	public static String newTestCaseLabel() {
		return ToolConstants.getFourDigitNumber(++tcId);
	}

	/*
	 * Returns the current test case identifier. The first valid test case
	 * identifier is 1. A value of 0 indicates that there is no test cases in
	 * the current test set.
	 */
	public static int getTestCaseId() {
		return tcId;
	}

	public static void setTestCaseId(int id) {
		tcId = id;
	}

	public static String print(String p) {
		prefix = p;
		return print();
	}

	public static String print() {
		String out = new String(prefix + "TestSet\n");

		out = out + prefix + "\tNumber of test cases: "
				+ TestSet.getNumberOfTestCases() + "\n";
		out = out + prefix + "\tActive test cases: "
				+ TestSet.getNumberOfActiveTestCases() + " of "
				+ TestSet.getNumberOfTestCases() + "\n";
		out = out + coverage2TXT(prefix + "\t");

		return out;
	}

	public static String coverage2TXT(String prefix) {
		String out = prefix
				+ AbstractCriterion.getName(Criterion.PRIMARY_NODES) + ": "
				+ getTestSetCoverage(Criterion.PRIMARY_NODES).toString() + "\n";
		out = out + prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_NODES) + ": "
				+ getTestSetCoverage(Criterion.SECONDARY_NODES).toString()
				+ "\n";
		out = out + prefix + AbstractCriterion.getName(Criterion.PRIMARY_EDGES)
				+ ": " + getTestSetCoverage(Criterion.PRIMARY_EDGES).toString()
				+ "\n";
		out = out + prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_EDGES) + ": "
				+ getTestSetCoverage(Criterion.SECONDARY_EDGES).toString()
				+ "\n";
		out = out + prefix + AbstractCriterion.getName(Criterion.PRIMARY_USES)
				+ ": " + getTestSetCoverage(Criterion.PRIMARY_USES).toString()
				+ "\n";
		out = out + prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_USES) + ": "
				+ getTestSetCoverage(Criterion.SECONDARY_USES).toString()
				+ "\n";
		out = out + prefix
				+ AbstractCriterion.getName(Criterion.PRIMARY_POT_USES) + ": "
				+ getTestSetCoverage(Criterion.PRIMARY_POT_USES).toString()
				+ "\n";
		out = out + prefix
				+ AbstractCriterion.getName(Criterion.SECONDARY_POT_USES)
				+ ": "
				+ getTestSetCoverage(Criterion.SECONDARY_POT_USES).toString()
				+ "\n";
		return out;
	}
}
