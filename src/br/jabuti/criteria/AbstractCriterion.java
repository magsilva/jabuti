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


package br.jabuti.criteria;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import br.jabuti.graph.Graph;
import br.jabuti.graph.GraphNode;

/**
 * This class implements the interface Criterion and includes the common methods
 * for any other testing criteria.
 * 
 * This class has to be extended by a given criterion that have to provide the
 * implementation for the following abstract methods. addPath, removePath and
 * getCoveredRequirements since they are criterion dependent.
 * 
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 * 
 */
public abstract class AbstractCriterion implements Criterion {
	static public final int PRIMARY = 1, SECONDARY = 2, ALL = 3;

	/**
	 * This method is used to padronize the names of the testing criteria over
	 * the tool... It returns the name of the criterion based on the criterion
	 * code
	 * 
	 * @param id -
	 *            a integer value corresponding to a given criterion id.
	 * 
	 * @return a string corresponding to the name of the criterion if the id is
	 *         valid or null otherwise.
	 */
	public static final String getName(int id) {
		if ((id >= 0) && (id < Criterion.NUM_CRITERIA))
			return names[id][0];
		else
			return null;
	}

	/**
	 * This method is used to padronize the names of the testing criteria over
	 * the tool... It returns the name of the criterion based on the criterion
	 * code
	 * 
	 * @param id -
	 *            a integer value corresponding to a given criterion id.
	 * 
	 * @return a string corresponding to the name of the criterion if the id is
	 *         valid or null otherwise.
	 */
	public static final String getDescription(int id) {
		if ((id >= 0) && (id < Criterion.NUM_CRITERIA))
			return names[id][1];
		else
			return null;
	}

	/** The set of required nodes */
	Hashtable required;

	/** The set of inactive testing requirements */
	HashSet inactive;

	/** The set of infeasible testing requirements */
	HashSet infeasible;

	/** The set of inserted paths */
	Hashtable pathSet;

	/** The set of inserted test cases */
	Hashtable testCases;

	/**
	 * This constructor takes the nodes of the graph as the criterion
	 * requirements. It uses the domination relation to discard some nodes. If a
	 * node x dominates a node y, then x can be discarded
	 * 
	 * @param g
	 *            The graph from where the requirements are extracted
	 */
	public AbstractCriterion(Graph g) {
		pathSet = new Hashtable();
		required = new Hashtable();
		inactive = new HashSet();
		infeasible = new HashSet();
		testCases = new Hashtable();
	}

	/**
	 * Gets the list of requirements computed for this object.
	 * 
	 * @return An array of {@link String} objects that are the requirements
	 */
	final public Object[] getRequirements() {
		return required.keySet().toArray();
	}

	/**
	 * Gets the list of requirements computed for this object, considering only
	 * the ones that are active and feasible.
	 * 
	 * @return An array of {@link String} objects that are the requirements
	 */
	final public Requirement[] getPossibleRequirements() {
		Requirement[] posReq = new Requirement[getNumberOfPossibleRequirements()];
		Object[] theReq = getRequirements();
		int k = 0;
		for (int i = 0; i < theReq.length; i++) {
			// Check if the required element is active and feasible
			Requirement req = (Requirement) theReq[i];
			if (isActive(req) && isFeasible(req))
				posReq[k++] = req;
		}
		return posReq;
	}

	/**
	 * Get the number of paths that covered each requirement.
	 * 
	 * @return An array of itegers that tells for each requirement how many
	 *         paths in the path set have covered the requirement
	 */
	final public int[] getCoverage() {
		int[] cv = new int[required.size()];
		Enumeration en = required.elements();
		int i = 0;

		while (en.hasMoreElements()) {
			cv[i++] = ((Integer) en.nextElement()).intValue();
		}
		return cv;
	}

	/**
	 * Add a path to the path set. To each path a label is associate so
	 * informaton about the path can be obtained and the path can be removed.
	 * 
	 * @param path
	 *            The path to be added. The object in the array can be: an
	 *            string representing the label of a graph node; an
	 *            {@link Integer} representing the number of a graph node. They
	 *            can also be mixed in the array.
	 * @param label
	 *            A label to be assigned to this path. If the label already
	 *            exists is is replaced by the path used in the call
	 */
	abstract public int addPath(Object[] path, String label);

	final public void addPathByReq(Object req, String label) {
		HashSet co = (HashSet) pathSet.get(label);
		if (co == null) {
			co = new HashSet();
			pathSet.put(label, co);
		}
		if (!required.containsKey(req))
			return;

		if (!co.contains(req)) {
			co.add(req);
		}
	}

	/**
	 * Remove a given path.
	 * 
	 * @param label
	 *            The label of the path to be removed.
	 * @return The number of requirements this label covered. If the label does
	 *         not exist no error is returned, only a negative value.
	 */
	final public int removePath(String label) {
		if (!pathSet.containsKey(label)) {
			return -1;
		}

		HashSet cov = (HashSet) pathSet.get(label);
		Iterator it = cov.iterator();

			while (it.hasNext()) {
				Requirement req = (Requirement) it.next();
				Integer ki = (Integer) required.get(req);

				required.put(req, new Integer(ki.intValue() - 1));
			}
		pathSet.remove(label);
		return cov.size();
	}

	/**
	 * Disable a given path. It works like the {@link #removePath()} method,
	 * except that it keep the test case in the pathSet.
	 * 
	 * No checking is perforned do verify if the test case is already disabled.
	 * 
	 * @param label
	 *            The label of the path to be disabled.
	 * @return The number of requirements this label covered. If the label does
	 *         not exist no error is returned, only a negative value.
	 */
	final public int disablePath(String label) {
		if (!pathSet.containsKey(label)) {
			return -1;
		}
		HashSet cov = (HashSet) pathSet.get(label);
		Iterator it = cov.iterator();
		while (it.hasNext()) {
			Requirement req = (Requirement) it.next();
			Integer ki = (Integer) required.get(req);

			required.put(req, new Integer(ki.intValue() - 1));
		}
		return cov.size();
	}

	/**
	 * Enable a given path.
	 * 
	 * No checking is perforned do verify if the test case is already enabled.
	 * 
	 * @param label
	 *            The label of the path to be disabled.
	 * @return The number of requirements this label covered. If the label does
	 *         not exist no error is returned, only a negative value.
	 */
	final public int enablePath(String label) {
		if (!pathSet.containsKey(label)) {
			return -1;
		}

		HashSet cov = (HashSet) pathSet.get(label);
		Iterator it = cov.iterator();

		while (it.hasNext()) {
			Requirement req = (Requirement) it.next();
			Integer ki = (Integer) required.get(req);
			required.put(req, new Integer(ki.intValue() + 1));
		}
		return cov.size();
	}

	/**
	 * Remove all existent paths.
	 */
	public void removeAllPaths() {
		Object[] it = pathSet.keySet().toArray();

		for (int i = 0; i < it.length; i++) {
			String label = (String) it[i];

			removePath(label);
		}
	}

	/**
	 * Returns only the number of active and feasible testing requirements
	 * required by this testing criteria
	 */
	public int getNumberOfRequirements() {
		return getRequirements().length;
	}

	/**
	 * Gets the number of requirements for a given criterion, descosidering the
	 * number of inactive and infeasible requirements
	 */
	public int getNumberOfPossibleRequirements() {
		return getRequirements().length - inactive.size() - infeasible.size();
	}

	/**
	 * Gets the number of covered requirements for a given criterion,
	 * desconsidering the number of inactive and infeasible requirements
	 */
	public int getNumberOfPossibleCovered() {
		return getPossibleCoveredRequirements().size();
	}

	/**
	 * Gets the list of covered requirements for this object.
	 * 
	 * @return A Set of {@link String} objects that are the covered requirements
	 */
	final public HashSet getCoveredRequirements() {
		HashSet cov = new HashSet();
		Iterator it = required.keySet().iterator();

		while (it.hasNext()) {
			Requirement req = (Requirement) it.next();
			Integer num = (Integer) required.get(req);

			if (num.intValue() > 0) {
				cov.add(req);
			}
		}
		return cov;
	}

	/**
	 * Gets the list of covered requirements for this object, considering only
	 * the ones that are active and feasible.
	 * 
	 * @return A Set of {@link String} objects that are the covered requirements
	 */
	final public HashSet getPossibleCoveredRequirements() {
		Iterator it = getCoveredRequirements().iterator();
		HashSet posCov = new HashSet();
		while (it.hasNext()) {
			Requirement req = (Requirement) it.next();
			// Check if the covered element is active and feasible
			if (isActive(req) && isFeasible(req))
				posCov.add(req);
		}
		return posCov;
	}

	/**
	 * Gets the list of covered requirements for a single test case.
	 * 
	 * @return A Set of {@link String} objects that are the covered requirements
	 *         for the specified path.
	 */
	final public HashSet getCoveredRequirements(String label) {
		if (pathSet.containsKey(label)) {
			return new HashSet((HashSet) pathSet.get(label));
		}
		return new HashSet();
	}

	/**
	 * Gets the list of covered requirements for a single test case, considering
	 * only the active and feasible ones.
	 * 
	 * @return A Set of {@link String} objects that are the covered requirements
	 *         for the specified path.
	 */
	final public HashSet getPossibleCoveredRequirements(String label) {
		Iterator it = getCoveredRequirements(label).iterator();
		HashSet posCov = new HashSet();
		while (it.hasNext()) {
			Requirement req = (Requirement) it.next();
			// Check if the covered element is active and feasible
			if (isActive(req) && isFeasible(req))
				posCov.add(req);
		}
		return posCov;
	}

	/**
	 * Gets the list of effective test cases, i.e., the labels of test cases
	 * which covered a given requirement.
	 * 
	 * @return A list of test case labels, separeted by a blank character. If no
	 *         test case covered the given requirement it returns an empty
	 *         String.
	 */
	final public String getEffectiveTestCases(Requirement req) {
		Set keys = pathSet.keySet();
		String labels = "";

		if (keys == null)
			return labels;

		Iterator it = keys.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			Set co = (Set) pathSet.get(obj);

			if (co.contains(req)) {
				labels += obj + " ";
			}
		}

		return labels;
	}

	final public static String[] changePath(Graph graph, String[] pat) {
		String[] newPat = new String[pat.length];
		GraphNode[] dft = graph.findDFT(false);

		// Find the entry node for the constructor
		int i = 0;

		while (!("" + dft[i].getNumber()).equals(pat[0])) {
			i++;
		}

		GraphNode gn = dft[i];

		for (; i < pat.length; i++) {
			newPat[i] = gn.getLabel();
			Vector nx = graph.getNext(gn, true);

			for (int k = 0; k < nx.size(); k++) {
				GraphNode gnx = (GraphNode) nx.elementAt(k);

				if (i < pat.length - 1
						&& ("" + gnx.getNumber()).equals(pat[i + 1])) {
					gn = gnx;
					break;
				}
			}
		}
		return newPat;
	}

	/*
	 * The set of inactive testing requirements
	 */
	final public HashSet getInactiveRequirements() {
		return inactive;
	}

	/*
	 * Checks if a given testing requirement is covered.
	 * 
	 * @return true if the requirement is covered, false if it is uncovered.
	 */
	final public boolean isCovered(Requirement req) {
		HashSet hs = getCoveredRequirements();
		if (hs != null)
			return hs.contains(req);
		else
			return false;
	}

	/*
	 * Sets a requirement as inactive. A test requirement can be set as inactive
	 * if it is feasible. Once it is infeasible it is always considered active.
	 */
	final public boolean setInactive(Requirement req) {
		if (isValidRequirement(req) && isFeasible(req)) {
			inactive.add(req);
			return true;
		}
		return false;
	}

	/*
	 * Sets a requirement as active. A test requirement can be set as active if
	 * it is inactive.
	 */
	final public boolean setActive(Requirement req) {
		if (isValidRequirement(req) && !isActive(req)) {
			inactive.remove(req);
			return true;
		}
		return false;
	}

	/*
	 * Checks if a given testing requirement is active.
	 * 
	 * @return true if the requirement is active, false if it is inactive.
	 */
	final public boolean isActive(Requirement req) {
		return !(inactive.contains(req));
	}

	/*
	 * The set of infeasible testing requirements
	 */
	final public HashSet getInfeasibleRequirements() {
		return infeasible;
	}

	/*
	 * Sets a requirement as infeasible. A test requirement can be set as
	 * infeasible if it is not yet covered. Once it is set as infeasible it is
	 * not inactive anymore.
	 */
	final public boolean setInfeasible(Requirement req) {
		if (isValidRequirement(req) && !(isCovered(req))) {
			if (!isActive(req))
				setActive(req);
			infeasible.add(req);
			return true;
		}
		return false;
	}

	/*
	 * Sets a requirement as feasible. A test requirement can be set as feasible
	 * if it is infeasible.
	 */
	final public boolean setFeasible(Requirement req) {
		if (isValidRequirement(req) && !isFeasible(req)) {
			infeasible.remove(req);
			return true;
		}
		return false;
	}

	/*
	 * Checks if a given testing requirement is feasible.
	 * 
	 * @return true if the requirement is feasible, false if it is infeasible.
	 */
	final public boolean isFeasible(Requirement req) {
		return !(infeasible.contains(req));
	}

	/**
	 * Gets a given requirement from its label.
	 * 
	 * @return An object that is the requirement
	 */
	final public Requirement getRequirementByLabel(String label) {
		Object[] reqs = getRequirements();
		Requirement req = null;

		for (int i = 0; (i < reqs.length) && (req == null); i++)
			if (reqs[i].toString().equals(label))
				req = (Requirement) reqs[i];
		return req;
	}

	/**
	 * Checks whether a requirements is valid or not.
	 * 
	 * @return true is it is a valid requirement<BR>
	 *         false if not.
	 */
	final public boolean isValidRequirement(Requirement req) {
		Set reqSet = required.keySet();

		return reqSet.contains(req);
	}
}
