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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.Criterion;
import br.jabuti.criteria.DefUse;
import br.jabuti.criteria.Edge;
import br.jabuti.criteria.Node;
import br.jabuti.graph.CFG;
import br.jabuti.util.ToolConstants;

/**
 * SAXHandler extends DefaultHandler to extract the necessary information from
 * the project file.
 */
public class XML2Project extends DefaultHandler {
	/*
	 * This constants represent the complete set of tags which can be found in a
	 * JaBUTi project file. Please see the project/jabutiprj.dtd file to know
	 * the set of attributes and their types.
	 */
	public static final String JABUTI = "JABUTI";

	public static final String PROJECT = "PROJECT";

	public static final String BASE_CLASS = "BASE_CLASS";

	public static final String JUNIT_SRC_DIR = "JUNIT_SRC_DIR";

	public static final String JUNIT_BIN_DIR = "JUNIT_BIN_DIR";

	public static final String JUNIT_TEST_SET = "JUNIT_TEST_SET";

	public static final String JUNIT_JAR = "JUNIT_JAR";

	public static final String CLASSPATH = "CLASSPATH";

	public static final String AVOIDED_PACKAGES = "AVOIDED_PACKAGES";

	public static final String PACKAGE = "PACKAGE";

	public static final String CLASS = "CLASS";

	public static final String INST_CLASS = "INST_CLASS";

	public static final String EXTEND = "EXTEND";

	public static final String IMPLEMENT = "IMPLEMENT";

	public static final String SOURCE = "SOURCE";

	public static final String METHOD = "METHOD";

	public static final String ALL_PRI_NODES = AbstractCriterion
			.getName(Criterion.PRIMARY_NODES);

	public static final String ALL_SEC_NODES = AbstractCriterion
			.getName(Criterion.SECONDARY_NODES);

	public static final String NODE = "NODE";

	public static final String ALL_PRI_EDGES = AbstractCriterion
			.getName(Criterion.PRIMARY_EDGES);

	public static final String ALL_SEC_EDGES = AbstractCriterion
			.getName(Criterion.SECONDARY_EDGES);

	public static final String EDGE = "EDGE";

	public static final String ALL_PRI_USES = AbstractCriterion
			.getName(Criterion.PRIMARY_USES);

	public static final String ALL_SEC_USES = AbstractCriterion
			.getName(Criterion.SECONDARY_USES);

	public static final String P_USE = "P_USE";

	public static final String C_USE = "C_USE";

	public static final String ALL_PRI_POT_USES = AbstractCriterion
			.getName(Criterion.PRIMARY_POT_USES);

	public static final String ALL_SEC_POT_USES = AbstractCriterion
			.getName(Criterion.SECONDARY_POT_USES);

	public static final String TEST_SET = "TEST_SET";

	public static final String TEST_CASE = "TEST_CASE";

	// public static final String PATH_SET = "PATH_SET";
	// public static final String PATH = "PATH";

	private boolean isJabuti = false;

	private boolean isProject = false;

	// private boolean isBaseClass = false;
	// private boolean isClasspath = false;
	private boolean isAvoidedPackages = false;

	// private boolean isPackage = false;
	// private boolean isClass = false;
	private boolean isInstClass = false;

	// private boolean isExtend = false;
	// private boolean isSource = false;
	private boolean isMethod = false;

	private boolean isAllPriNodes = false;

	private boolean isAllSecNodes = false;

	// private boolean isNode = false;
	private boolean isAllPriEdges = false;

	private boolean isAllSecEdges = false;

	// private boolean isEdge = false;
	private boolean isAllPriUses = false;

	private boolean isAllSecUses = false;

	// private boolean isPUse = false;
	// private boolean isCUse = false;
	private boolean isAllPriPotUses = false;

	private boolean isAllSecPotUses = false;

	private boolean isTestSet = false;

	// private boolean isTestCase = false;
	// private boolean isPathSet = false;
	// private boolean isPath = false;

	private SAXParser saxParser;

	private String urlString, classPath, baseClass;

	private boolean completely;

	private JabutiProject jbtProject;

	private boolean isMobility;

	private int cfgOption;

	// Variables to manipulate the set of classes
	private ClassFile cf;

	private String className;

	private String methodName;

	private ClassMethod cm;

	private Criterion criterion;

	// Variables used to compute the test set
	private int countTC;

	private TestCase tc;

	private String label;

	private String alias;

	private boolean active;

	private boolean success;

	private boolean fail;

	// private Hashtable pathsTable; // the complete set of paths of one test
	// case
	// private Vector paths; // the set of paths of one ProbedNode
	private Vector<TestCase> newTCs = null;

	public XML2Project(String cp) {
		saxParser = null;
		urlString = null;
		classPath = cp;
		baseClass = null;
		cf = null;
		className = null;
		cm = null;
		methodName = null;
		criterion = null;
		tc = null;

		label = null;
		// pathsTable = null;
		// paths = null;
		completely = true;
		jbtProject = null;
		create();
	}

	/**
	 * Create the SAX parser
	 */
	private void create() {
		try {
			// Obtain a new instance of a SAXParserFactory.
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Specifies that the parser produced by this code will provide
			// support for XML namespaces.
			factory.setNamespaceAware(true);
			// Specifies that the parser produced by this code will validate
			// documents as they are parsed.
			factory.setValidating(true);
			// Creates a new instance of a SAXParser using the currently
			// configured factory parameters.
			saxParser = factory.newSAXParser();
		} catch (Throwable t) {
			ToolConstants.reportException(t, ToolConstants.STDERR);
		}
	}

	/**
	 * Parse a URI
	 * 
	 * @param uri -
	 *            String
	 */
	public void parse(String uri, boolean c) {
		try {
			completely = c;
			urlString = uri;
			saxParser.parse(urlString, this);
		} catch (Throwable t) {
			ToolConstants.reportException(t, ToolConstants.STDERR);
		}
	}

	/**
	 * This method returns a reference to a JabutiProject object. It can only be
	 * called after the parse() method had been called.
	 */
	public JabutiProject getParsedJabutiProject() {
		return jbtProject;
	}

	/**
	 * Receive notification of the start of an element.
	 * 
	 * @param namespaceURI -
	 *            The Namespace URI, or the empty string if the element has no
	 *            Namespace URI or if Namespace processing is not being
	 *            performed.
	 * @param localName -
	 *            The local name (without prefix), or the empty string if
	 *            Namespace processing is not being performed.
	 * @param qName -
	 *            The qualified name (with prefix), or the empty string if
	 *            qualified names are not available.
	 * @param atts -
	 *            The attributes attached to the element. If there are no
	 *            attributes, it shall be an empty Attributes object.
	 * @throws SAXException -
	 *             Any SAX exception, possibly wrapping another exception.
	 */
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (JABUTI.equals(localName)) {
			jabutiFound();
		} else if (PROJECT.equals(localName)) {
			projectFound(atts);
		} else if (BASE_CLASS.equals(localName)) {
			baseClassFound(atts);
		} else if (CLASSPATH.equals(localName)) {
			classPathFound(atts);
		} else if (JUNIT_SRC_DIR.equals(localName)) {
			junitSrcDirFound(atts);
		} else if (JUNIT_BIN_DIR.equals(localName)) {
			junitBinDirFound(atts);
		} else if (JUNIT_TEST_SET.equals(localName)) {
			junitTestSetFound(atts);
		} else if (JUNIT_JAR.equals(localName)) {
			junitJarFound(atts);
		} else if (AVOIDED_PACKAGES.equals(localName)) {
			avoidedPackagesFound();
		} else if (PACKAGE.equals(localName)) {
			packageFound(atts);
		} else if (INST_CLASS.equals(localName)) {
			instrumentedClassFound(atts);
		} else if (EXTEND.equals(localName)) {
			extendsFound(atts);
		} else if (SOURCE.equals(localName)) {
			sourceFound(atts);
			// It is used to recover the project completely (by JabutiGUI) or
			// partialy (by ProberLoader)
		} else if (completely) {
			if (METHOD.equals(localName)) {
				methodFound(atts);
			} else if (ALL_PRI_NODES.equals(localName)) {
				allPriNodesFound(atts);
			} else if (ALL_SEC_NODES.equals(localName)) {
				allSecNodesFound(atts);
			} else if (NODE.equals(localName)) {
				nodeFound(atts);
			} else if (ALL_PRI_EDGES.equals(localName)) {
				allPriEdgesFound(atts);
			} else if (ALL_SEC_EDGES.equals(localName)) {
				allSecEdgesFound(atts);
			} else if (EDGE.equals(localName)) {
				edgeFound(atts);
			} else if (ALL_PRI_USES.equals(localName)) {
				allPriUsesFound(atts);
			} else if (ALL_SEC_USES.equals(localName)) {
				allSecUsesFound(atts);
			} else if (ALL_PRI_POT_USES.equals(localName)) {
				allPriPotUsesFound(atts);
			} else if (ALL_SEC_POT_USES.equals(localName)) {
				allSecPotUsesFound(atts);
			} else if (P_USE.equals(localName)) {
				puseFound(atts);
			} else if (C_USE.equals(localName)) {
				cuseFound(atts);
			} else if (TEST_SET.equals(localName)) {
				testSetFound(atts);
			} else if (TEST_CASE.equals(localName)) {
				testCaseFound(atts);
			} /*
				 * else if (PATH_SET.equals(localName) ) { pathSetFound(); }
				 * else if (PATH.equals(localName) ) { pathFound(atts); } else { ; }
				 */
		}
	}

	/**
	 * Receive notification of the end of an element.
	 * 
	 * @param namespaceURI -
	 *            The Namespace URI, or the empty string if the element has no
	 *            Namespace URI or if Namespace processing is not being
	 *            performed.
	 * @param localName -
	 *            The local name (without prefix), or the empty string if
	 *            Namespace processing is not being performed.
	 * @param qName -
	 *            The qualified name (with prefix), or the empty string if
	 *            qualified names are not available.
	 * @throws SAXException -
	 *             Any SAX exception, possibly wrapping another exception.
	 */
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (JABUTI.equals(localName)) {
			isJabuti = false;
		} else if (PROJECT.equals(localName)) {
			isProject = false;
		} else if (BASE_CLASS.equals(localName)) {
			// isBaseClass = false;
		} else if (CLASSPATH.equals(localName)) {
			// isClasspath = false;
		} else if (AVOIDED_PACKAGES.equals(localName)) {
			isAvoidedPackages = false;
		} else if (PACKAGE.equals(localName)) {
			// isPackage = false;
		} else if (CLASS.equals(localName)) {
			// isClass = false;
		} else if (INST_CLASS.equals(localName)) {
			isInstClass = false;
			resetClass();
		} else if (EXTEND.equals(localName)) {
			// isExtend = false;
		} else if (SOURCE.equals(localName)) {
			// isSource = false;
		} else if (METHOD.equals(localName)) {
			isMethod = false;
			resetMethod();
		} else if (ALL_PRI_NODES.equals(localName)) {
			isAllPriNodes = false;
			resetCriterion();
		} else if (ALL_SEC_NODES.equals(localName)) {
			isAllSecNodes = false;
			resetCriterion();
		} else if (NODE.equals(localName)) {
			// isNode = false;
		} else if (ALL_PRI_EDGES.equals(localName)) {
			isAllPriEdges = false;
			resetCriterion();
		} else if (ALL_SEC_EDGES.equals(localName)) {
			isAllSecEdges = false;
			resetCriterion();
		} else if (EDGE.equals(localName)) {
			// isEdge = false;
		} else if (ALL_PRI_USES.equals(localName)) {
			isAllPriUses = false;
			resetCriterion();
		} else if (ALL_SEC_USES.equals(localName)) {
			isAllSecUses = false;
			resetCriterion();
		} else if (ALL_PRI_POT_USES.equals(localName)) {
			isAllPriPotUses = false;
			resetCriterion();
		} else if (ALL_SEC_POT_USES.equals(localName)) {
			isAllSecPotUses = false;
			resetCriterion();
		} else if (P_USE.equals(localName)) {
			// isPUse = false;
		} else if (C_USE.equals(localName)) {
			// isCUse = false;
		} else if (TEST_SET.equals(localName)) {
			isTestSet = false;
			updateTestSetCoverage();
		} else if (TEST_CASE.equals(localName)) {
			// isTestCase = false;
			// includeNewTestCase();
		} /*
			 * else if (PATH_SET.equals(localName) ) { isPathSet = false; } else
			 * if (PATH.equals(localName) ) { isPath = false; }
			 */
	}

	public boolean isValid(String localName) throws SAXException {
		if (PROJECT.equals(localName)) {
			return isJabuti;
		} else if (BASE_CLASS.equals(localName)) {
			return isJabuti && isProject;
		} else if (CLASSPATH.equals(localName)) {
			return isJabuti && isProject;

		} else if (JUNIT_SRC_DIR.equals(localName)) {
			return isJabuti && isProject;
		} else if (JUNIT_BIN_DIR.equals(localName)) {
			return isJabuti && isProject;
		} else if (JUNIT_TEST_SET.equals(localName)) {
			return isJabuti && isProject;
		} else if (JUNIT_JAR.equals(localName)) {
			return isJabuti && isProject;

		} else if (AVOIDED_PACKAGES.equals(localName)) {
			return isJabuti && isProject;
		} else if (PACKAGE.equals(localName)) {
			return isJabuti && isProject && isAvoidedPackages;
		} else if (CLASS.equals(localName)) {
			return isJabuti && isProject;
		} else if (INST_CLASS.equals(localName)) {
			return isJabuti && isProject;
		} else if (EXTEND.equals(localName)) {
			return isJabuti && isProject && isInstClass;
		} else if (SOURCE.equals(localName)) {
			return isJabuti && isProject && isInstClass;
		} else if (METHOD.equals(localName)) {
			return isJabuti && isProject && isInstClass;
		} else if (ALL_PRI_NODES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (ALL_SEC_NODES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (NODE.equals(localName)) {
			return isJabuti
					&& isProject
					&& isInstClass
					&& isMethod
					&& ((isAllPriNodes && !isAllSecNodes) || (!isAllPriNodes && isAllSecNodes));
		} else if (ALL_PRI_EDGES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (ALL_SEC_EDGES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (EDGE.equals(localName)) {
			return isJabuti
					&& isProject
					&& isInstClass
					&& isMethod
					&& ((isAllPriEdges && !isAllSecEdges) || (!isAllPriEdges && isAllSecEdges));
		} else if (ALL_PRI_USES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (ALL_SEC_USES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (P_USE.equals(localName)) {
			return isJabuti
					&& isProject
					&& isInstClass
					&& isMethod
					&& ((isAllPriUses && !isAllSecUses)
							|| (!isAllPriUses && isAllSecUses)
							|| (isAllPriPotUses && !isAllSecPotUses) || (!isAllPriPotUses && isAllSecPotUses));
		} else if (C_USE.equals(localName)) {
			return isJabuti
					&& isProject
					&& isInstClass
					&& isMethod
					&& ((isAllPriUses && !isAllSecUses)
							|| (!isAllPriUses && isAllSecUses)
							|| (isAllPriPotUses && !isAllSecPotUses) || (!isAllPriPotUses && isAllSecPotUses));
		} else if (ALL_PRI_POT_USES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (ALL_SEC_POT_USES.equals(localName)) {
			return isJabuti && isProject && isInstClass && isMethod;
		} else if (TEST_SET.equals(localName)) {
			return isJabuti && isProject;
		} else if (TEST_CASE.equals(localName)) {
			return isJabuti && isProject && isTestSet;
		} /*
			 * else if (PATH_SET.equals(localName) ) { return isJabuti &&
			 * isProject && isTestSet && isTestCase; } else if
			 * (PATH.equals(localName) ) { return isJabuti && isProject &&
			 * isTestSet && isTestCase && isPathSet; }
			 */
		return false;
	}

	private void jabutiFound() {
		isJabuti = true;
	}

	private void projectFound(Attributes atts) throws SAXException {
		if (isValid(PROJECT)) {
			isProject = true;

			// Is a project of mobile code??
			String ans = atts.getValue("mobility");
			isMobility = false;
			if (ans.equals("Y"))
				isMobility = true;

			// CFG Option - using call node or not
			ans = atts.getValue("CFGOption");
			try {
				cfgOption = Integer.parseInt(ans);
			} catch (Exception e) {
				cfgOption = CFG.NO_CALL_NODE;
			}
		} else {
			throw new SAXException(PROJECT + " found before a " + JABUTI);
		}
	}

	private void baseClassFound(Attributes atts) throws SAXException {
		if (isValid(BASE_CLASS)) {
			// isBaseClass = true;
			baseClass = atts.getValue("name");
		} else {
			throw new SAXException(BASE_CLASS + " found outside a " + PROJECT);
		}
	}

	private void classPathFound(Attributes atts) throws SAXException {
		if (isValid(CLASSPATH)) {
			if (classPath == null) {
				// isClasspath = true;
				classPath = atts.getValue("path");

				// Converting the classpath in a system independent way (more
				// less)
				StringTokenizer st = new StringTokenizer(classPath);
				StringBuffer sb = new StringBuffer();
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					if (File.pathSeparatorChar == ';') { // If windows
						s = s.replace(':', File.pathSeparatorChar); // replacing
																	// driver
																	// letter d:
																	// by d? for
																	// instance.
						s = s.replace('/', File.separatorChar);
					} else {
						s = s.replace(';', File.pathSeparatorChar); // replacing
																	// driver
																	// letter d:
																	// by d? for
																	// instance.
						s = s.replace('\\', File.separatorChar);
					}
					s = s.replace('?', ':');
					sb.append(s + File.pathSeparatorChar);
				}
				classPath = sb.toString();
			}

			// Creating the JaBUTi's project object
			try {
				jbtProject = new JabutiProject(baseClass, classPath);
			} catch (Exception e) {
				throw new SAXException("Error creating a project from "
						+ urlString);
			}

			if (jbtProject == null) {
				throw new SAXException("Error creating a project from "
						+ urlString);
			}
			// Setting the project file name.
			jbtProject.setProjectFile(new File(urlString));

			jbtProject.setMobility(isMobility);
			jbtProject.setCFGOption(cfgOption);

		} else {
			throw new SAXException(CLASSPATH + " found outside a " + PROJECT);
		}
	}

	private void junitSrcDirFound(Attributes atts) throws SAXException {
		if (isValid(JUNIT_SRC_DIR)) {
			String junitSrcDir = atts.getValue("dir");
			jbtProject.setJunitSrcDir(junitSrcDir);
		} else {
			throw new SAXException(JUNIT_SRC_DIR + " found outside a "
					+ PROJECT);
		}
	}

	private void junitBinDirFound(Attributes atts) throws SAXException {
		if (isValid(JUNIT_BIN_DIR)) {
			String junitBinDir = atts.getValue("dir");
			jbtProject.setJunitBinDir(junitBinDir);
		} else {
			throw new SAXException(JUNIT_BIN_DIR + " found outside a "
					+ PROJECT);
		}
	}

	private void junitTestSetFound(Attributes atts) throws SAXException {
		if (isValid(JUNIT_TEST_SET)) {
			String junitTestSet = atts.getValue("name");
			jbtProject.setJunitTestSet(junitTestSet);
		} else {
			throw new SAXException(JUNIT_TEST_SET + " found outside a "
					+ PROJECT);
		}
	}

	private void junitJarFound(Attributes atts) throws SAXException {
		if (isValid(JUNIT_JAR)) {
			String junitJar = atts.getValue("name");
			jbtProject.setJUnitJar(junitJar);
		} else {
			throw new SAXException(JUNIT_JAR + " found outside a " + PROJECT);
		}
	}

	private void avoidedPackagesFound() throws SAXException {
		if (isValid(AVOIDED_PACKAGES)) {
			isAvoidedPackages = true;
		} else {
			throw new SAXException(AVOIDED_PACKAGES + " found outside a "
					+ PROJECT);
		}
	}

	private void packageFound(Attributes atts) throws SAXException {
		if (isValid(PACKAGE)) {
			// isPackage = true;
			String ans = atts.getValue("name");

			jbtProject.addAvoid(ans);
		} else {
			throw new SAXException(AVOIDED_PACKAGES + " found outside a "
					+ PROJECT);
		}
	}

	private void instrumentedClassFound(Attributes atts) throws SAXException {
		if (isValid(INST_CLASS)) {
			isInstClass = true;

			className = atts.getValue("name");

			int id = jbtProject.getClassId(className);
			if (id == -1) {
				System.out.println("INVALID CLASS: " + className
						+ ": this class does not belongs to the set "
						+ "of classes extracted from " + baseClass);
			}
			jbtProject.addInstr(className);

			// Only recover the complete class info in case
			// it is necessary
			if (completely) {
				cf = jbtProject.createNewClassFileEntry(className);
				if (cf == null)
					throw new SAXException("Class " + className + " not found.");
			}
		} else {
			throw new SAXException(INST_CLASS + " found outside a " + PROJECT);
		}
	}

	private void extendsFound(Attributes atts) {
	}

	private void sourceFound(Attributes atts) {
	}

	private void methodFound(Attributes atts) throws SAXException {
		if (isValid(METHOD)) {
			isMethod = true;

			methodName = atts.getValue("name");
			cm = cf.getMethod(methodName);
			if (cm == null)
				throw new SAXException("Method " + methodName
						+ " not found in class " + className);

		} else {
			throw new SAXException(METHOD + " found outside a " + INST_CLASS);
		}
	}

	private void allPriNodesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_PRI_NODES)) {
			isAllPriNodes = true;

			criterion = cm.getCriterion(Criterion.PRIMARY_NODES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_PRI_NODES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_PRI_NODES + " found outside a " + METHOD);
		}
	}

	private void allSecNodesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_SEC_NODES)) {
			isAllSecNodes = true;

			criterion = cm.getCriterion(Criterion.SECONDARY_NODES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_SEC_NODES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_SEC_NODES + " found outside a " + METHOD);
		}
	}

	private void nodeFound(Attributes atts) throws SAXException {
		if (isValid(NODE)) {
			// isNode = true;
			setNodeAtts(atts);
		} else {
			throw new SAXException(NODE + " found outside a " + ALL_PRI_NODES
					+ " or " + ALL_SEC_NODES + " criterion.");
		}
	}

	private void allPriEdgesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_PRI_EDGES)) {
			isAllPriEdges = true;

			criterion = cm.getCriterion(Criterion.PRIMARY_EDGES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_PRI_EDGES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_PRI_EDGES + " found outside a " + METHOD);
		}
	}

	private void allSecEdgesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_SEC_EDGES)) {
			isAllSecEdges = true;

			criterion = cm.getCriterion(Criterion.SECONDARY_EDGES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_SEC_EDGES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_SEC_EDGES + " found outside a " + METHOD);
		}
	}

	private void edgeFound(Attributes atts) throws SAXException {
		if (isValid(EDGE)) {
			// isEdge = true;
			setEdgesAtts(atts);
		} else {
			throw new SAXException(EDGE + " found outside a " + ALL_PRI_EDGES
					+ " or " + ALL_SEC_EDGES + " criterion.");
		}
	}

	private void allPriUsesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_PRI_USES)) {
			isAllPriUses = true;

			criterion = cm.getCriterion(Criterion.PRIMARY_USES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_PRI_USES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_PRI_USES + " found outside a " + METHOD);
		}
	}

	private void allSecUsesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_SEC_USES)) {
			isAllSecUses = true;

			criterion = cm.getCriterion(Criterion.SECONDARY_USES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_SEC_USES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_SEC_USES + " found outside a " + METHOD);
		}
	}

	private void allPriPotUsesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_PRI_POT_USES)) {
			isAllPriPotUses = true;

			criterion = cm.getCriterion(Criterion.PRIMARY_POT_USES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_PRI_POT_USES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_PRI_POT_USES + " found outside a "
					+ METHOD);
		}
	}

	private void allSecPotUsesFound(Attributes atts) throws SAXException {
		if (isValid(ALL_SEC_POT_USES)) {
			isAllSecPotUses = true;

			criterion = cm.getCriterion(Criterion.SECONDARY_POT_USES);

			if (criterion == null)
				throw new SAXException("Invalid " + ALL_SEC_POT_USES
						+ " criterion for method " + methodName);
		} else {
			throw new SAXException(ALL_SEC_POT_USES + " found outside a "
					+ METHOD);
		}
	}

	private void puseFound(Attributes atts) throws SAXException {
		if (isValid(P_USE)) {
			// isPUse = true;
			setPUseAtts(atts);
		} else {
			throw new SAXException(P_USE + " found outside a " + ALL_PRI_USES
					+ " or " + ALL_SEC_USES + " criterion.");
		}
	}

	private void cuseFound(Attributes atts) throws SAXException {
		if (isValid(C_USE)) {
			// isCUse = true;
			setCUseAtts(atts);
		} else {
			throw new SAXException(C_USE + " found outside a " + ALL_PRI_USES
					+ " or " + ALL_SEC_USES + " criterion.");
		}
	}

	private void testSetFound(Attributes atts) throws SAXException {
		if (isValid(TEST_SET)) {
			isTestSet = true;

			// At this point, all classes to be instrumented have already been
			// processed and it is required to create the others objects for
			// each one of them
			jbtProject.rebuild();

			String ans = atts.getValue("last_id");

			int id = 0;
			try {
				id = Integer.parseInt(ans);
			} catch (Exception e) {
				id = 0;
			}
			TestSet.initialize(jbtProject, jbtProject.getTraceFileName());
			TestSet.setTestCaseId(id);
			countTC = 0;
			newTCs = new Vector<TestCase>();
		} else {
			throw new SAXException(TEST_SET + " found outside a " + PROJECT);
		}
	}

	private void testCaseFound(Attributes atts) throws SAXException {
		if (isValid(TEST_CASE)) {
			// isTestCase = true;

			// Getting the label and the status of existent test cases
			label = atts.getValue("label");
			alias = atts.getValue("alias");
			active = atts.getValue("active").equals("Y");
			success = atts.getValue("success").equals("Y");
			fail = atts.getValue("fail").equals("Y");

			tc = TestSet.createEmptyTestCase(jbtProject, label, alias, success,
					fail);

			tc.setHostName(atts.getValue("host"));

			if (active) {
				newTCs.add(tc);
			}

			// restarting the pathsTable for the new test case;
			// pathsTable = new Hashtable();
			countTC++;
		} else {
			throw new SAXException(TEST_CASE + " found outside a " + TEST_SET);
		}
	}

	/*
	 * private void pathSetFound() throws SAXException { if ( isValid( PATH_SET ) ) {
	 * isPathSet = true; } else { throw new SAXException( PATH_SET + " found
	 * outside a " + TEST_CASE ); } }
	 * 
	 * private void pathFound(Attributes atts) throws SAXException { if (
	 * isValid( PATH ) ) { isPath = true;
	 * 
	 * String pathAtt = atts.getValue("path");
	 * 
	 * Vector v = HostTraceReader.loadFromString( pathAtt );
	 * 
	 * if ( v.size() == 2 ) { // Recovering the ProbedNodeObject HostProbedNode
	 * pdn = (HostProbedNode) v.elementAt(0);
	 *  // and the executed path String[] path = (String[]) v.elementAt(1);
	 *  // Checking if the HostProbedNode object already exists if (
	 * pathsTable.containsKey( pdn ) ) { paths = (Vector) pathsTable.get( pdn ); }
	 * else { paths = new Vector(); } paths.add( path );
	 * 
	 * pathsTable.put( pdn, paths ); } } else { throw new SAXException( PATH + "
	 * found outside a " + PATH_SET ); } }
	 * 
	 * private void includeNewTestCase() { // Updating the test case paths if (
	 * tc != null && label != null && pathsTable != null )
	 * tc.addTestCasePathsFromXML( label, pathsTable ); }
	 */

	private void updateTestSetCoverage() {
		// Recovering test set information and test case status
		if (countTC > 0) {
			for (int i = 0; i < newTCs.size(); i++) {
				TestCase tc = (TestCase) newTCs.elementAt(i);
				TestSet.activateTestCase(jbtProject, tc.getLabel());
			}

			TestSet.updateOverallCoverage(jbtProject);
		}
	}

	private void resetClass() {
		cf = null;
		className = null;
	}

	private void resetMethod() {
		cm = null;
		methodName = null;
	}

	private void resetCriterion() {
		criterion = null;
	}

	private void setNodeAtts(Attributes atts) {
		String from = atts.getValue("label");
		boolean active = atts.getValue("active").equals("Y");
		boolean infeasible = atts.getValue("infeasible").equals("Y");

		// Creating the Node object
		Node req = new Node(from);

		// Setting the testing requirement as active/inactive
		if (active)
			criterion.setActive(req);
		else
			criterion.setInactive(req);

		// Setting the testing requirement as infeasible/feasible
		if (infeasible)
			criterion.setInfeasible(req);
		else
			criterion.setFeasible(req);

		StringTokenizer st = new StringTokenizer(atts.getValue("effectivetcs"));
		while (st.hasMoreTokens()) {
			criterion.addPathByReq(req, st.nextToken());
		}
	}

	private void setEdgesAtts(Attributes atts) {
		// Getting the attributes of an Edge
		String from = atts.getValue("from");
		String to = atts.getValue("to");
		boolean active = atts.getValue("active").equals("Y");
		boolean infeasible = atts.getValue("infeasible").equals("Y");

		// Creating the Edge object
		Edge req = new Edge(from, to);

		// Setting the testing requirement as active/inactive
		if (active)
			criterion.setActive(req);
		else
			criterion.setInactive(req);

		// Setting the testing requirement as infeasible/feasible
		if (infeasible) {
			criterion.setInfeasible(req);
		} else {
			criterion.setFeasible(req);
		}

		StringTokenizer st = new StringTokenizer(atts.getValue("effectivetcs"));
		while (st.hasMoreTokens()) {
			criterion.addPathByReq(req, st.nextToken());
		}
	}

	private void setCUseAtts(Attributes atts) {
		String var = atts.getValue("var");
		String def = atts.getValue("def");
		String from = atts.getValue("use");
		String to = null;

		boolean active = atts.getValue("active").equals("Y");
		boolean infeasible = atts.getValue("infeasible").equals("Y");

		// Creating the Edge object
		DefUse req = new DefUse(var, def, from, to);

		// Setting the testing requirement as active/inactive
		if (active)
			criterion.setActive(req);
		else
			criterion.setInactive(req);

		// Setting the testing requirement as infeasible/feasible
		if (infeasible)
			criterion.setInfeasible(req);
		else
			criterion.setFeasible(req);

		StringTokenizer st = new StringTokenizer(atts.getValue("effectivetcs"));
		while (st.hasMoreTokens()) {
			criterion.addPathByReq(req, st.nextToken());
		}
	}

	private void setPUseAtts(Attributes atts) {
		String var = atts.getValue("var");
		String def = atts.getValue("def");
		String from = atts.getValue("from");
		String to = atts.getValue("to");

		boolean active = atts.getValue("active").equals("Y");
		boolean infeasible = atts.getValue("infeasible").equals("Y");

		// Creating the Edge object
		DefUse req = new DefUse(var, def, from, to);

		// Setting the testing requirement as active/inactive
		if (active)
			criterion.setActive(req);
		else
			criterion.setInactive(req);

		// Setting the testing requirement as infeasible/feasible
		if (infeasible)
			criterion.setInfeasible(req);
		else
			criterion.setFeasible(req);

		StringTokenizer st = new StringTokenizer(atts.getValue("effectivetcs"));
		while (st.hasMoreTokens()) {
			criterion.addPathByReq(req, st.nextToken());
		}
	}

	public static void main(String[] args) {

		XML2Project jfs = new XML2Project(null);

		// Parse the XML file, handler generates the output
		jfs.parse(args[0], true);

		System.out.println("\n\n The parsed xml.");
	}
}
