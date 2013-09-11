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


import java.util.HashSet;


/** 
 * The Criterion interface defines all methods that a given 
 * criterion have to implement
 *
 * @version: 1.0
 * @author: Marcio Delamaro  
 * @author: Auri Vincenzi  
 *
 **/
public interface Criterion {

    /** Constants to index each testing criterion */
    static public final int PRIMARY_NODES = 0; // AllNodes using only the exception-independent nodes
    static public final int SECONDARY_NODES = 1; // AllNodes using only the exception-dependent nodes
    static public final int PRIMARY_EDGES = 2; // AllEdges using only the exception-independent edges
    static public final int SECONDARY_EDGES = 3; // AllEdges using only the exception-independent edges
    static public final int PRIMARY_USES = 4; // AllUses using only the exception-independent associations
	static public final int SECONDARY_USES = 5; // AllUses using only the exception-dependent associations   
    static public final int PRIMARY_POT_USES = 6; // AllUses using only the exception-independent potential-associations
	static public final int SECONDARY_POT_USES = 7; // AllUses using only the exception-dependent potential-associations    

    static public final int NUM_CRITERIA = 8;

    static public final String[][] names = { { "All-Nodes-ei", "All-Nodes-Exception-Independent" },
    										 { "All-Nodes-ed", "All-Nodes-Exception-Dependent" },
    										 { "All-Edges-ei", "All-Edges-Exception-Independent" },
    										 { "All-Edges-ed", "All-Edges-Exception-Dependent" },
											 { "All-Uses-ei", "All-Uses-Exception-Independent" },
    										 { "All-Uses-ed", "All-Uses-Exception-Dependent" },
											 { "All-Pot-Uses-ei", "All-Pot-Uses-Exception-Independent" },
    										 { "All-Pot-Uses-ed", "All-Pot-Uses-Exception-Dependent" }};
    										
	/*static public final String[] names = {  "All-Pri-Nodes",
											"All-Sec-Nodes",
											"All-Pri-Edges",
											"All-Sec-Edges",
											"All-Pri-Uses",
											"All-Sec-Uses",
											"All-Pri-Pot-Uses",
											"All-Sec-Pot-Uses"};*/
    /** Gets the list of requirements computed for this object.
     *
     * @return An array of {@link Requirements} objects that are the requirements
     */
     public Object[] getRequirements();

    /** 
     * Gets the list of requirements computed for this object, considering 
     * only the ones that is active and feasible.
     *
     * @return An array of {@link Requirements} objects that are the requirements
     */
    public Requirement[] getPossibleRequirements();
	
    /** Get the number of paths that covered each requirement.
     * 
     * @return An array of itegers that tells for each requirement how
     * many paths in the path set have covered the requirement
     */
    public int[] getCoverage();
	
    /** Add a path to the path set. To each path a label is associate so
     * informaton about the path can be obtained and the path can be 
     * removed.
     *
     * @param path The path to be added. The object in the array can be:
     * an string representing the label of a
     * graph node; an {@link Integer} representing the number of a graph 
     * node. They can also be mixed in the array.
     * @param label A label to be assigned to this path. If the label already
     * exists is is replaced by the path used in the call
     */
    public int addPath(Object[] path, String label);

	public void addPathByReq(Object req, String label);
			
    /** Remove a given path.
     * @param label The label of the path to be removed.
     * @return The number of requirements this label covered. If the label
     * does not exist no error is returned, only a negative value.
     */
    public int removePath(String label);
    
	public int disablePath(String label);
	
	public int enablePath(String label);

    /**
     * Remove all path.
     */
    public void removeAllPaths();

    /**
     * Gets the number of requirements for a given criterion
     */	
    public int getNumberOfRequirements();

    /**
     * Gets the number of requirements for a given criterion,
     * descosidering the number of inactive and infeasible requirements
     */	
    public int getNumberOfPossibleRequirements();

    /**
     * Gets the number of covered requirements 
     * for a given criterion, desconsidering the number of 
     * inactive and infeasible requirements
     */	
    public int getNumberOfPossibleCovered();	
	
    /**
     * Gets the list of covered requirements for this object.
     *
     * @return A Set of {@link String} objects that are the
     * covered requirements
     */
    public HashSet getCoveredRequirements();

    /**
     * Gets the list of covered requirements for this object, considering
     * only the ones that are active and feasible.
     *
     * @return A Set of {@link String} objects that are the
     * covered requirements
     */
    public HashSet getPossibleCoveredRequirements();

	
    /**
     * Gets the list of covered requirements for a single path.
     *
     * @return A Set of {@link String} objects that are the
     * covered requirements for the specified path.
     */
    public HashSet getCoveredRequirements(String label);

	/**
	 * Gets the list of covered requirements for a single test case, 
	 * considering only the active and feasible ones.
	 *
	 * @return A Set of {@link String} objects that are the
	 * covered requirements for the specified path.
	 */
	public HashSet getPossibleCoveredRequirements(String label);
	

	/**
	 * Gets the list of effective test cases, i.e., the 
	 * labels of test cases which covered a given requirement.
	 *
	 * @return An {@link String} corresponding to
	 * the label of test case. If no test case covered the
	 * given requirement it returns null.
	 */
	public String getEffectiveTestCases(Requirement req);

	
    /**
     * Converts the trace file information of a single path 
     * to each corresponding version of executed nodes, 
     * considering the labels of the CFG.
     *
     * @return A vector of {@link String} objects that are the
     * covered requirements for the specified path.
     */
    //static public String[] changePath(Graph graph, String[] pat);


	/*
	 * Checks if a given testing requirement is
	 * covered.
	 *
	 * @return true if the requirement is covered, false 
	 * if it is uncovered.
	 */
    public boolean isCovered( Requirement req );

    
	/*
	 * The set of inactive testing requirements
	 **/
	public HashSet getInactiveRequirements();

	/*
	 * Sets a requirement as inactive.
	 * A test requirement can be set as inactive if it is
	 * feasible. Once it is infeasible it is always considered
	 * active.
	 */
    public boolean setInactive( Requirement req );

	/*
	 * Sets a requirement as active.
	 * A test requirement can be set as active if it is
	 * inactive.
	 */
    public boolean setActive( Requirement req );

	/*
	 * Checks if a given testing requirement is
	 * active.
	 *
	 * @return true if the requirement is active, false 
	 * if it is inactive.
	 */
    public boolean isActive( Requirement req );


	/*
	 * The set of infeasible testing requirements
	 **/
	public HashSet getInfeasibleRequirements();
    
	/*
	 * Sets a requirement as infeasible.
	 * A test requirement can be set as infeasible if it is
	 * not yet covered. Once it is set as infeasible it is
	 * not inactive anymore.
	 */
    public boolean setInfeasible( Requirement req );

	/*
	 * Sets a requirement as feasible.
	 * A test requirement can be set as feasible if it is
	 * infeasible.
	 */
    public boolean setFeasible( Requirement req );

	/*
	 * Checks if a given testing requirement is
	 * feasible.
	 *
	 * @return true if the requirement is feasible, false 
	 * if it is infeasible.
	 */
    public boolean isFeasible( Requirement req );
    
    /** 
     * Gets a given requirement from its label.
     *
     * @return An object that is the requirement
     */
    public Requirement getRequirementByLabel( String label );    

	/** 
	 * Checks whether a requirements is valid or not.
	 *
	 * @return true is it is a valid requirement<BR>
	 *         false if not.
	 */
	public boolean isValidRequirement( Requirement req );
}
