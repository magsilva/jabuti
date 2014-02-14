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

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.AllEdges;
import br.jabuti.criteria.AllNodes;
import br.jabuti.criteria.AllPotUses;
import br.jabuti.criteria.AllUses;
import br.jabuti.criteria.Criterion;
import br.jabuti.criteria.DefUse;
import br.jabuti.criteria.Edge;
import br.jabuti.criteria.Node;
import br.jabuti.graph.CFG;
import br.jabuti.graph.CFGNode;
import br.jabuti.graph.DominatorTree;
import br.jabuti.graph.DominatorTreeNode;
import br.jabuti.graph.GraphNode;
import br.jabuti.graph.RRDominator;
import br.jabuti.graph.ReduceNode;
import br.jabuti.util.ToolConstants;

public class ClassMethod {

	/** The current project */
	private JabutiProject proj;

	/** The method object information */
	private MethodGen mg;
	private ConstantPoolGen cp;
	private ClassGen cg;

	/** The method name (name + signature)*/
	private String methodName = null;

	/** The method id inside this class */
	private int methodId = -1;

	/** The begin and end offset of this method in the 
	 * textual representation of the bytecode
	 */
	private int beginByteOffset = -1;
	private int endByteOffset = -1;

	/** The begin and end source code offset of this method */
	private int beginSrcLine = -1;
	private int endSrcLine = -1;

	/** The CFG of a given method */
	private CFG cfg = null;
	//private String cfgPersistent = null;

	/** The testing criteria for this method */
	private Criterion[] criteria;

	private Coverage[] methodCoverage;

	// Used by toString method to ident the output
	private String prefix = new String("");

	// Correspondency between CFG node labesl and the CFGNode
	private HashMap labelNodeTable = null;

	public ClassMethod(
		JabutiProject p,
		ClassFile cf,
		MethodGen m,
		ConstantPoolGen c,
		ClassGen cl,
		String n,
		int id) {
		proj = p;
		mg = m;
		cg = cl;
		cp = c;
		methodName = n;
		methodId = id;
		beginByteOffset = -1;
		endByteOffset = -1;

		beginSrcLine = -1;
		endSrcLine = -1;

		labelNodeTable = null;

		try {
			
			cfg = new CFG(mg, cg, proj.getCFGOption());
			//cfgPersistent = util.Persistency.add(cfg);
			
			criteria = new Criterion[Criterion.NUM_CRITERIA];
			methodCoverage = new Coverage[Criterion.NUM_CRITERIA];

			criteria[Criterion.PRIMARY_NODES] =
				new AllNodes(cfg, AbstractCriterion.PRIMARY);
			methodCoverage[Criterion.PRIMARY_NODES] =
				new Coverage(
					0,
					criteria[Criterion
						.PRIMARY_NODES]
						.getNumberOfPossibleRequirements());

			criteria[Criterion.SECONDARY_NODES] =
				new AllNodes(cfg, AbstractCriterion.SECONDARY);
			
			methodCoverage[Criterion.SECONDARY_NODES] =
				new Coverage(
					0,
					criteria[Criterion
						.SECONDARY_NODES]
						.getNumberOfPossibleRequirements());

			criteria[Criterion.PRIMARY_EDGES] =
				new AllEdges(cfg, AllEdges.PRIMARY);
			
			methodCoverage[Criterion.PRIMARY_EDGES] =
				new Coverage(
					0,
					criteria[Criterion
						.PRIMARY_EDGES]
						.getNumberOfPossibleRequirements());

			criteria[Criterion.SECONDARY_EDGES] =
				new AllEdges(cfg, AllEdges.SECONDARY);
			
			methodCoverage[Criterion.SECONDARY_EDGES] =
				new Coverage(
					0,
					criteria[Criterion
						.SECONDARY_EDGES]
						.getNumberOfPossibleRequirements());

			criteria[Criterion.PRIMARY_USES] = new AllUses(cfg, false);
			
			methodCoverage[Criterion.PRIMARY_USES] =
				new Coverage(
					0,
					criteria[Criterion
						.PRIMARY_USES]
						.getNumberOfPossibleRequirements());

			criteria[Criterion.SECONDARY_USES] = new AllUses(cfg, true);
			
			methodCoverage[Criterion.SECONDARY_USES] =
				new Coverage(
					0,
					criteria[Criterion
						.SECONDARY_USES]
						.getNumberOfPossibleRequirements());

			criteria[Criterion.PRIMARY_POT_USES] = new AllPotUses(cfg, false);
			
			methodCoverage[Criterion.PRIMARY_POT_USES] =
				new Coverage(
					0,
					criteria[Criterion
						.PRIMARY_POT_USES]
						.getNumberOfPossibleRequirements());

			criteria[Criterion.SECONDARY_POT_USES] = new AllPotUses(cfg, true);
			
			methodCoverage[Criterion.SECONDARY_POT_USES] =
				new Coverage(
					0,
					criteria[Criterion
						.SECONDARY_POT_USES]
						.getNumberOfPossibleRequirements());

			//            cfg.releaseInstructionGraph();
			System.gc();
		} catch (Exception e) {
			ToolConstants.reportException(e, ToolConstants.STDERR);
		}
	}

	/***********************************************************
	* Get and Set Methods implementation                     
	***********************************************************/
	public String getMethodName() {
		return methodName;
	}

	/*private void setMethodName(String n) {
		methodName = n;
	}*/

	public int getMethodId() {
		return methodId;
	}

	public MethodGen getMethodGen() {
		return mg;
	}

	public ConstantPoolGen getConstantPoolGen() {
		return cp;
	}

	public Method getMethod() {
		return mg.getMethod();
	}

	public Criterion getCriterion(int c) {
		if ((c >= 0) && (c < Criterion.NUM_CRITERIA)) {
			return criteria[c];
		} else {
			return null;
		}
	}

	public Coverage getClassMethodCoverage(int c) {
		if ((c >= 0) && (c < Criterion.NUM_CRITERIA)) {
			return methodCoverage[c];
		} else {
			return null;
		}
	}

	public HashSet getCoveredRequirementsByTestCase(
		int c,
		String label) {
		HashSet cov = new HashSet();

		cov.addAll(criteria[c].getCoveredRequirements(label));
		return cov;
	}

	/**
	 *
	 * This method update the coverage of this method 
	 * considering only the active test cases. First, all test cases (test paths)
	 * are removed and the coverage is set to 0. So, for each active test case,
	 * the new coverage is recalculated.
	 *
	 */
	public void updateClassMethodCoverage() {

		/** Restarting current coverage */
		for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
			methodCoverage[i] = new Coverage();
		}
		/*
		/* Disabling disabled test cases  
		String[] testSetLabels = TestSet.getTestCaseLabels();
		for (int j = 0; j < testSetLabels.length; j++) {
			if (!TestSet.isActive(testSetLabels[j]))
				continue;
			for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
				criteria[i].disablePath(testSetLabels[j]);
			}
		}

		HashSet testSet = TestSet.getActiveTestCases();

		if (testSet != null) {
			// Adding only the active test cases and 
			// getting the new coverage
			Iterator it = testSet.iterator();

			while (it.hasNext()) {
				String label = (String) it.next();

				System.out.println("\t\t\tACTIVATING TEST CASE: "+ label);

				for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
					criteria[i].enablePath(label);
				}
			}
		}*/

		for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
			methodCoverage[i] =
				new Coverage(
					criteria[i].getNumberOfPossibleCovered(),
					criteria[i].getNumberOfPossibleRequirements());
			//System.out.println(getMethodName() + " Cobertura " + AbstractCriterion.getName(i) + " = " + methodCoverage[i]);
		}		
	}

		public int getBeginSourceLine() {
			if (beginSrcLine == -1) {
				beginSrcLine = 0;

				try {
					CFG cfg = getCFG();
					GraphNode[] fdt = cfg.findDFT(true);

					if (fdt.length > 0) {
						GraphNode gn = fdt[0];

						int begin = ((CFGNode) gn).getStart();
						LineNumberTable lnTable = mg.getLineNumberTable(cp);

						beginSrcLine = lnTable.getSourceLine(begin);
						// Decrementing two lines trying to
						// get the method header
						if (beginSrcLine > 1) {
							beginSrcLine -= 2;
						}
					}
				} catch (Exception e) {
					beginSrcLine = 0;
					ToolConstants.reportException(e, ToolConstants.STDERR);
				}
			}
			return beginSrcLine;
		}

		public int getEndSourceLine() {
			if (endSrcLine == -1) {
				endSrcLine = 0;

				try {
					CFG cfg = getCFG();
					GraphNode[] fdt = cfg.findDFT(true);

					LineNumberTable lnTable = mg.getLineNumberTable(cp);

					for (int i = 0; i < fdt.length; i++) {
						GraphNode gn = fdt[i];

						int end = ((CFGNode) gn).getEnd();
						int line = lnTable.getSourceLine(end);

						if (endSrcLine < line) {
							endSrcLine = line;
						}
					}
				} catch (Exception e) {
					endSrcLine = 0;
					ToolConstants.reportException(e, ToolConstants.STDERR);
				}
			}
			return endSrcLine;
		}

		public int bytecodeOffset2SourceLine(int byteOffset) {
			int srcLine = -1;

			/*
			 InstructionList instList = mg.getInstructionList();
			 InstructionHandle handle = instList.findHandle( byteOffset );
			 */
			LineNumberTable lnTable = mg.getLineNumberTable(cp);

			srcLine = lnTable.getSourceLine(byteOffset);

			return srcLine;
		}

		public void setBeginBytecodeOffset(int o) {
			beginByteOffset = o;
		}

		public int getBeginBytecodeOffset() {
			if (beginByteOffset == -1) {
				return 0;
			}
			return beginByteOffset;
		}

		public void setEndBytecodeOffset(int o) {
			endByteOffset = o;
		}

		public int getEndBytecodeOffset() {
			if (endByteOffset == -1) {
				return 0;
			}
			return endByteOffset;
		}

		public HashSet getWeightByNode(int cId) {
			HashSet weightSet = new HashSet();
			Criterion criterion = null;

			if (cId == Criterion.PRIMARY_NODES)
				criterion = criteria[Criterion.PRIMARY_NODES];
			else
				criterion = criteria[Criterion.SECONDARY_NODES];

			/*
			 * ALL-NODES has a special case, treated in
			 * the method WeightColor.updateColorAttributes.
			 *
			 * Looks for ALL-NODES SPECIAL CASE
			 */

			//        HashSet covered = criterion.getCoveredRequirements();

			Object[] required = criterion.getPossibleRequirements();
			HashSet covered = criterion.getPossibleCoveredRequirements();
	
			if (required.length == 0) {
				removeCFGWeightData();
				return weightSet;
			}

			DominatorTree bbDom = getDominatorTree();

			if (bbDom != null) {
				for (int z1 = 0; z1 < bbDom.size(); z1++) {
					DominatorTreeNode dtn =
						(DominatorTreeNode) bbDom.elementAt(z1);

					// Updating the weight w.r.t. the covered nodes...
					GraphNode[] nodes = dtn.getOriginalNodes();

					for (int z = 0; z < nodes.length; z++) {
						GraphNode curNode =
							((ReduceNode) nodes[z]).getOriginalNode();

						// AURI:
						// Checar se deve usr curNode.getLabel() ou 
						// curNode.getNumber()
						Node aux = new Node(curNode.getLabel());
						if (covered.contains(aux)) {
							// The reduceNode should be used to update the coverage
							bbDom.markCovered(nodes[z]);
						}
					}

					// Storing the weigth information into the node...
					int weight = bbDom.getWeigth(dtn);

					// Decrementing the weight considering the
					// only the possible covered nodes
					for (int z = 0; z < nodes.length; z++) {
						boolean isRequired = false;
						GraphNode curNode =
							((ReduceNode) nodes[z]).getOriginalNode();
						// Verifica se o n� que pertence ao super-bloco 
						// faz parte do conjunto de requisitos do crit�rio
						if (criterion.getRequirementByLabel(curNode.getLabel())
							!= null) {
							for (int i = 0; i < required.length; i++) {
								if (curNode
									.getLabel()
									.equals(required[i].toString()))
									isRequired = true;
							}
							if (!isRequired) {
								System.out.println(
									"Decrementing weight: NODE: "
										+ curNode.getLabel());
								weight--;
								if (weight < 0){
									weight = 0;
									System.out.println("\tERROR: Requirement with negative weight!!!");
									System.out.println("\t       This is caused by incorrect infeasible requirements.");
									System.out.println("\t       Please, go to Visualization->Required Elements menu and look for red requirements.");
									
									//ToolConstants.reportException(new Exception("Negative weight exception"), ToolConstants.STDERR);
								}
							}
						} else {
							System.out.println(
								"Requisito naoo pertence ao criterio atual: "
									+ curNode.getLabel());
						}
					}

					for (int z = 0; z < nodes.length; z++) {
						boolean isRequired = false;

						GraphNode curNode =
							((ReduceNode) nodes[z]).getOriginalNode();

						for (int i = 0; i < required.length; i++) {
							if (curNode
								.getLabel()
								.equals(required[i].toString()))
								isRequired = true;
						}
						if (isRequired) {
							/*System.out.println(
								"Painting Node: "
									+ curNode.getLabel()
									+ " weight "
									+ weight);*/
							curNode.setUserData(
								ToolConstants.LABEL_WEIGHT,
								new Integer(weight));
						} else {
							/*System.out.println(
								"UNPAINTING Node: " + curNode.getLabel());*/
							curNode.removeUserData(ToolConstants.LABEL_WEIGHT);
						}
					}
					weightSet.add(new Integer(weight));
				}
			}
			return weightSet;
		}

		public Hashtable getWeightByEdge(int cId) {

			Hashtable edgesWeight = new Hashtable();

			Criterion criterion = null;

			if (cId == Criterion.PRIMARY_EDGES)
				criterion = criteria[Criterion.PRIMARY_EDGES];
			else
				criterion = criteria[Criterion.SECONDARY_EDGES];

			//        Object[] required = criterion.getRequirements();
			//        HashSet covered = criterion.getCoveredRequirements();

			Object[] required = criterion.getPossibleRequirements();
			HashSet covered = criterion.getPossibleCoveredRequirements();

			DominatorTree bbDom = getDominatorTree();

			if (bbDom != null) {
				boolean hasDecision = false;
				for (int i = 0; i < required.length; i++) {
					int weight = 0;
					boolean looping = false;

					Edge mainEdg = (Edge) required[i];

					/* If all-primary edge is selected, it is only necessary 
					   to cover the decisions. Such edges are identified checking 
					   whether there are more than one testing requirement with
					   the same source node (From)
					 */
					if (cId == Criterion.PRIMARY_EDGES) {

						int count = 0;

						for (int k = 0;
							k < required.length && count < 2;
							k++) {
							Edge other = (Edge) required[k];

							if (mainEdg.getFrom().equals(other.getFrom())) {
								count++;
							}
						}
						if (count < 2) {
							// This if is a special case when the method
							// has no decision. In this case, the first
							// node and its child is painted (if it has a child)
							if ((i == (required.length - 1)) && !hasDecision) {
								// Finding the edge related with the entry node.
								String theNode = getCFG().getEntry().getLabel();
								mainEdg = null;
								for (int k = 0;
									k < required.length && mainEdg == null;
									k++) {
									Edge other = (Edge) required[k];

									if (theNode.equals(other.getFrom())) {
										mainEdg = other;
									}
								}
								System.out.println(
									"NO DECISION, PAINTING: "
										+ mainEdg.toString());
							} else
								continue;
						} else {
							hasDecision = true;
						}
					}

					// Checking if is a LOOPING Statement
					// We are considering a node as a looping node
					// if its set of incomming edges (arriving) is greater than 1.
					GraphNode gn = getGraphNodeByLabel(mainEdg.getFrom());

					if (gn.getPrimArriving().size() > 1) {
						looping = true;
					}

					// If requirement not covered, calculates its weight
					// otherwise, weight is kept equals 0.
					if (!(covered.contains(mainEdg))) {

						HashSet sourceSet =
							getClousureSet(mainEdg.getFrom(), bbDom, true);

						/*
						 Iterator it = sourceSet.iterator();
						 System.out.println("Evaluating the clousure set for: " + mainEdg);
						 while (it.hasNext()) {
						 System.out.println(it.next());
						 }
						 */

						sourceSet.addAll(
							getClousureSet(mainEdg.getTo(), bbDom, true));

						/*
						 it = sourceSet.iterator();
						 System.out.println("UNION: " + mainEdg);
						 while (it.hasNext()) {
						 System.out.println(it.next());
						 }
						 */

						// Check for all other requirements
						// if the nodes are contained in the sourceSet
						// if so, increment the weight of this specific
						// requirement.
						for (int j = 0; j < required.length; j++) {
							Edge edg = (Edge) required[j];

							// System.out.println("Required element: " + edg);
							if (!(covered.contains(edg))) {
								if (i == j) { // mainEdg is always covered
									weight++;
								} else { // When is a DECICION (if and switch) 
									// statment only one of
									// this branchs will be covered
									if ((looping)
										|| (!mainEdg
											.getFrom()
											.equals(edg.getFrom()))) {
										if ((sourceSet.contains(edg.getFrom()))
											&& (sourceSet
												.contains(edg.getTo()))) {
											weight++;
										}
									}
								}
							}
						}
					}

					Hashtable to;
					GraphNode gnFrom = getGraphNodeByLabel(mainEdg.getFrom());
					GraphNode gnTo = getGraphNodeByLabel(mainEdg.getTo());

					if (edgesWeight.containsKey(gnFrom)) {
						to = (Hashtable) edgesWeight.get(gnFrom);
					} else {
						to = new Hashtable(2);
					}

					to.put(gnTo, new Integer(weight));
					edgesWeight.put(gnFrom, to);
				}
			}
			return edgesWeight;
		}

		public Hashtable getWeightByUse(int cId) {

			Hashtable usesWeight = new Hashtable();

			Criterion criterion = null;

			if (cId == Criterion.PRIMARY_USES)
				criterion = criteria[Criterion.PRIMARY_USES];
			else if (cId == Criterion.SECONDARY_USES)
				criterion = criteria[Criterion.SECONDARY_USES];
			else if (cId == Criterion.PRIMARY_POT_USES)
				criterion = criteria[Criterion.PRIMARY_POT_USES];
			else
				criterion = criteria[Criterion.SECONDARY_POT_USES];

			//        Object[] required = criterion.getRequirements();
			//        HashSet covered = criterion.getCoveredRequirements();

			Object[] required = criterion.getPossibleRequirements();
			HashSet covered = criterion.getPossibleCoveredRequirements();

			DominatorTree bbDom = getDominatorTree();

			if (bbDom != null) {
				for (int i = 0; i < required.length; i++) {
					int weight = 0;
					boolean looping = false;
					boolean puse = false;

					DefUse mainDu = (DefUse) required[i];

					// If requirement not covered, calculates its weight
					// otherwise, weight is kept equals 0.
					if (!(covered.contains(mainDu))) {

						// Clousure Set for the definition node
						HashSet sourceSet =
							getClousureSet(mainDu.getDef(), bbDom, true);

						/*
						 Iterator it = sourceSet.iterator();
						 System.out.println("Evaluating the clousure set for: " + mainDu );
						 while (it.hasNext()) {
						 System.out.println(it.next());
						 }
						 */

						// Clousure Set for the use node (c-use)
						sourceSet.addAll(
							getClousureSet(mainDu.getUseFrom(), bbDom, true));

						// Checks if is a p-use
						if (mainDu.getUseTo() != null) {
							puse = true;

							// Checking if is a LOOPING Statement
							// We are considering a node as a looping node
							// if its set of incomming edges (arriving) is greater than 1.
							GraphNode gn =
								getGraphNodeByLabel(mainDu.getUseFrom());

							if (gn.getPrimArriving().size() > 1) {
								looping = true;
							}

							sourceSet.addAll(
								getClousureSet(mainDu.getUseTo(), bbDom, true));
						}

						/*
						 it = sourceSet.iterator();
						 System.out.println("UNION: " + mainDu );
						 while (it.hasNext()) {
						 System.out.println(it.next());
						 }
						 */
						// Check for all other requirements
						// if the nodes are contained in the sourceSet
						// if so, increment the weight of this specific
						// requirement.
						for (int j = 0; j < required.length; j++) {
							DefUse du = (DefUse) required[j];

							// System.out.println("Required element: " + du);
							if (!(covered.contains(du))) {
								if (i == j) { // mainDu is always covered
									weight++;
								} else {
									if (sourceSet.contains(du.getDef())) {
										// When is a DECICION (if and switch) 
										// statment only one of
										// this branchs will be covered
										if ((looping)
											|| (!(mainDu
												.getUseFrom()
												.equals(du.getUseFrom())))
											|| (!looping && !puse)) {
											if (du.getUseTo() != null) {
												if ((sourceSet
													.contains(du.getUseFrom()))
													&& (sourceSet
														.contains(
															du.getUseTo()))) {
													weight++;
												}
											} else {
												if (sourceSet
													.contains(
														du.getUseFrom())) {
													weight++;
												}
											}
										} else if (puse) {
											if (du.getUseTo() != null) {
												if ((mainDu
													.getUseFrom()
													.equals(du.getUseFrom()))) {
													if ((mainDu
														.getUseTo()
														.equals(du.getUseTo()))
														&& (sourceSet
															.contains(
																du
																	.getUseFrom()))
														&& (sourceSet
															.contains(
																du
																	.getUseTo()))) {
														weight++;
													}
												} else {
													if ((sourceSet
														.contains(
															du.getUseFrom()))
														&& (sourceSet
															.contains(
																du
																	.getUseTo()))) {
														weight++;
													}
												}
											} else {
												if (sourceSet
													.contains(
														du.getUseFrom())) {
													weight++;
												}
											}
										}
									}
								}
							}
						}
					}
					// Building the hashtable...
					Hashtable def;

					GraphNode gnDef = getGraphNodeByLabel(mainDu.getDef());

					String varDefName = mainDu.getVar();

					Hashtable use;
					if (usesWeight.containsKey(gnDef)) {
						def = (Hashtable) usesWeight.get(gnDef);
					} else {
						def = new Hashtable(2);
					}

					if (def.containsKey(varDefName)) {
						use = (Hashtable) def.get(varDefName);
					} else {
						use = new Hashtable(5);
					}

					use.put(mainDu, new Integer(weight));
					def.put(varDefName, use);

					usesWeight.put(gnDef, def);
				}
			}
			return usesWeight;
		}

		public int getDecisionNodeSourceLine(GraphNode gn) {
			InstructionHandle ih =
				br.jabuti.util.InstructCtrl.findInstruction(
					getMethodGen(),
					((CFGNode) gn).getEnd());
			String inst = ih.toString();
			int index = inst.indexOf(":");
			Integer off = new Integer(inst.substring(0, index).trim());

			return (bytecodeOffset2SourceLine(off.intValue()));
		}

		public GraphNode getDecisionNodeFromOffset(int offset) {
			GraphNode theNode = null;

			try {
				CFG cfg = getCFG();

				GraphNode[] fdt = cfg.findDFT(true);

				for (int i = 0; i < fdt.length && theNode == null; i++) {
					GraphNode gn = fdt[i];

					InstructionHandle ih =
						br.jabuti.util.InstructCtrl.findInstruction(
							getMethodGen(),
							((CFGNode) gn).getEnd());
					String inst = ih.toString();
					int index = inst.indexOf(":");
					Integer off = new Integer(inst.substring(0, index).trim());

					if (off.intValue() == offset) {
						theNode = gn;
					}
				}
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
				return null;
			}
			return theNode;
		}

		public GraphNode getGraphNodeFromOffset(int offset) {
			GraphNode theNode = null;

			try {
				CFG cfg = getCFG();

				GraphNode[] fdt = cfg.findDFT(true);

				for (int i = 0; i < fdt.length && theNode == null; i++) {
					GraphNode gn = fdt[i];

					InstructionHandle ih =
						br.jabuti.util.InstructCtrl.findInstruction(
							getMethodGen(),
							((CFGNode) gn).getStart());
					String inst = ih.toString();
					int index = inst.indexOf(":");
					Integer off = new Integer(inst.substring(0, index).trim());

					if (off.intValue() == offset) {
						theNode = gn;
					}

					while ((theNode == null)
						&& (ih.getPosition() != ((CFGNode) gn).getEnd())) {
						ih = ih.getNext();
						inst = ih.toString();
						index = inst.indexOf(":");
						off = new Integer(inst.substring(0, index).trim());

						if (off.intValue() == offset) {
							theNode = gn;
						}
					}
				}
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
				return null;
			}
			return theNode;
		}

		public GraphNode getGraphNodeByLabel(String label) {
			if (labelNodeTable == null) {
				labelNodeTable = new HashMap();

				try {
					CFG cfg = getCFG();

					GraphNode[] fdt = cfg.findDFT(true);

					for (int i = 0; i < fdt.length; i++) {
						GraphNode gn = fdt[i];

						labelNodeTable.put(gn.getLabel(), gn);
					}
				} catch (Exception e) {
					ToolConstants.reportException(e, ToolConstants.STDERR);
					return null;
				}
			}

			if (labelNodeTable.containsKey(label)) {
				return (GraphNode) labelNodeTable.get(label);
			} else {
				return null;
			}
		}

		public void releaseLabelNodeTable() {
			if (labelNodeTable != null) {
				labelNodeTable.clear();
				labelNodeTable = null;
			}
		}

		private DominatorTree getDominatorTree() {
			DominatorTree bbDom = null;

			try {
				CFG cfg = getCFG();

				RRDominator rd = new RRDominator(ToolConstants.LABEL_DOMINATOR);

				cfg.roundRobinAlgorithm(rd, true);

				rd = new RRDominator(ToolConstants.LABEL_IDOMINATOR);
				cfg.roundIRobinAlgorithm(rd, true);

				// Calculating the pre and post dominators tree...
				DominatorTree dtDom =
					new DominatorTree(cfg, ToolConstants.LABEL_DOMINATOR);

				dtDom.setDefaultNumbering();
				DominatorTree dtIDom =
					new DominatorTree(cfg, ToolConstants.LABEL_IDOMINATOR);

				dtIDom.setDefaultNumbering();

				// Merging both trees
				dtDom.merge(dtIDom);

				// Calculating the Basic Block Dominator TREE
				bbDom = (DominatorTree) DominatorTree.reduceSCC(dtDom, false);
				if (dtDom.getEntry() != null) {
					bbDom.setEntry(bbDom.getReduceNodeOf(dtDom.getEntry()));
					bbDom.setDefaultNumbering();

					// Calculating the Final Basic Block Dominator TREE
					bbDom.removeComposite(false);
				}
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
				return null;
			}
			return bbDom;
		}

		HashSet clousureSet = new HashSet();
		private HashSet getClousureSet(
			String n,
			DominatorTree bbDom,
			boolean newSet) {
			if (newSet) {
				clousureSet = new HashSet();
			}

			HashSet nodesSet = new HashSet();
			DominatorTreeNode theNode = null;

			for (int z1 = 0; z1 < bbDom.size() && theNode == null; z1++) {
				DominatorTreeNode dtn = (DominatorTreeNode) bbDom.elementAt(z1);

				// Updating the weight w.r.t. the covered nodes...
				GraphNode[] nodes = dtn.getOriginalNodes();

				for (int z = 0; z < nodes.length && theNode == null; z++) {
					GraphNode gn = ((ReduceNode) nodes[z]).getOriginalNode();

					if (gn.getLabel().equals(n)) {
						theNode = dtn;
						// System.out.println( "NODE " + n + " FOUNDED!!!" );
					}
				}
			}
			if (theNode != null) {
				// Updating the weight w.r.t. the covered nodes...
				GraphNode[] nodes = theNode.getOriginalNodes();

				for (int z = 0; z < nodes.length; z++) {
					GraphNode gn = ((ReduceNode) nodes[z]).getOriginalNode();

					// System.out.println( "OTHER NODES: " + gn.getLabel() );
					nodesSet.add(gn.getLabel());
				}

				nodesSet.removeAll(clousureSet);
				Iterator it = nodesSet.iterator();

				while (it.hasNext()) {
					String node = (String) it.next();

					clousureSet.add(node);
					getClousureSet(node, bbDom, false);
				}
			}
			return clousureSet;
		}

		public CFG getCFG() {
			/*CFG cfg = null;
			try
			{
				cfg = (CFG) util.Persistency.get(cfgPersistent);
				return cfg;
			}
			catch (Exception e)
			{
				return null;
			}*/
			return cfg;
		}

		public LocalVariableGen[] getLocalVariables() {
			return mg.getLocalVariables();
		}

		public LocalVariableTable getLocalVariableTable() {
			return mg.getLocalVariableTable(cp);
		}

		/**
		 * This method is responsible to try to identify the real
		 * source conde name of a given bytecode name variable in a
		 * given offset. Observe that the same bytecode name can be
		 * used to identify different source code variables. The 
		 * distintion is made considering the valid offset (scope)
		 * fo a given local variable.
		 *
		 * It is also important to notice that the precision of this
		 * method depends on that the current class has been compiled
		 * using the depuration option (-g).
		 */
		public String getVariableSourceName(String name, int offset) {

			// Getting the local variable table of a given method...
			LocalVariableGen[] localVar = getLocalVariables();

			int localVarIndex = -1;
			boolean instance = false;
			String firstName = name;
			String secondName = null;

			String sourceName = new String(" - ");

			if (name.startsWith("L@") && (name.indexOf(".") > 0)) {
				int index = name.indexOf(".");

				firstName = name.substring(0, index);
				secondName = name.substring(index + 1, name.length());
				instance = true;
			}

			if (firstName.startsWith("L@") && (firstName.indexOf(".") < 0)) {
				String numVar = firstName.substring(2, firstName.length());

				//Check if it is a vector
				int v = numVar.indexOf("[");
				if (v > 0) {
					numVar = numVar.substring(0, v);
				}

				localVarIndex = Integer.parseInt(numVar);

				int currentDiff = -1;

				for (int i = 0; i < localVar.length; i++) {
					if (localVar[i].getIndex() == localVarIndex) {
						if ((offset <= localVar[i].getEnd().getPosition())) {
							int diff =
								Math.abs(
									localVar[i].getStart().getPosition()
										- offset);

							if ((currentDiff == -1) || (diff <= currentDiff)) {
								sourceName = localVar[i].getName();
								currentDiff = diff;
							}
						}
					}
				}
			}

			if (instance) {
				sourceName = sourceName + "." + secondName;
			}

			return sourceName;
		}

		public void removeCFGWeightData() {
			CFG cfg = getCFG();
			GraphNode[] fdt = cfg.findDFT(true);

			for (int i = 0; i < fdt.length; i++) {
				GraphNode gn = fdt[i];

				gn.removeUserData(ToolConstants.LABEL_WEIGHT);
			}
		}

		public String toString(String p) {
			prefix = p;
			return toString();
		}

		public String toString() {
			String out =
				new String(prefix + "Method Name: " + getMethodName() + "\n");

			out = out + coverage2TXT(prefix + "\t");
			return out;
		}

		public String coverage2TXT(String prefix) {
			String out =
				prefix
					+ AbstractCriterion.getName(Criterion.PRIMARY_NODES)
					+ ": "
					+ getClassMethodCoverage(Criterion.PRIMARY_NODES).toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.PRIMARY_NODES).getPercentage()
					+ "\n";
			out =
				out
					+ prefix
					+ AbstractCriterion.getName(Criterion.SECONDARY_NODES)
					+ ": "
					+ getClassMethodCoverage(Criterion.SECONDARY_NODES)
						.toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.SECONDARY_NODES).getPercentage()
					+ "\n";
			out =
				out
					+ prefix
					+ AbstractCriterion.getName(Criterion.PRIMARY_EDGES)
					+ ": "
					+ getClassMethodCoverage(Criterion.PRIMARY_EDGES).toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.PRIMARY_EDGES).getPercentage()
					+ "\n";
			out =
				out
					+ prefix
					+ AbstractCriterion.getName(Criterion.SECONDARY_EDGES)
					+ ": "
					+ getClassMethodCoverage(Criterion.SECONDARY_EDGES)
						.toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.SECONDARY_EDGES).getPercentage()
					+ "\n";
			out =
				out
					+ prefix
					+ AbstractCriterion.getName(Criterion.PRIMARY_USES)
					+ ": "
					+ getClassMethodCoverage(Criterion.PRIMARY_USES).toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.PRIMARY_USES).getPercentage()
					+ "\n";
			out =
				out
					+ prefix
					+ AbstractCriterion.getName(Criterion.SECONDARY_USES)
					+ ": "
					+ getClassMethodCoverage(Criterion.SECONDARY_USES).toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.SECONDARY_USES).getPercentage()
					+ "\n";
			out =
				out
					+ prefix
					+ AbstractCriterion.getName(Criterion.PRIMARY_POT_USES)
					+ ": "
					+ getClassMethodCoverage(Criterion.PRIMARY_POT_USES)
						.toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.PRIMARY_POT_USES).getPercentage()
					+ "\n";
			out =
				out
					+ prefix
					+ AbstractCriterion.getName(Criterion.SECONDARY_POT_USES)
					+ ": "
					+ getClassMethodCoverage(Criterion.SECONDARY_POT_USES)
						.toString()
					+ " - "
					+ getClassMethodCoverage(Criterion.SECONDARY_POT_USES).getPercentage()
					+ "\n";
			return out;
		}
	}
