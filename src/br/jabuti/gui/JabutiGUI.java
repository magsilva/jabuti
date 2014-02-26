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


package br.jabuti.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.filechooser.FileFilter;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.Criterion;
import br.jabuti.criteria.Requirement;
import br.jabuti.graph.CFG;
import br.jabuti.graph.view.gvf.CFGFrame;
import br.jabuti.junitexec.Main;
import br.jabuti.lookup.Program;
import br.jabuti.metrics.Metrics;
import br.jabuti.project.ClassFile;
import br.jabuti.project.ClassMethod;
import br.jabuti.project.ClasspathParser;
import br.jabuti.project.Coverage;
import br.jabuti.project.JabutiProject;
import br.jabuti.project.TestCase;
import br.jabuti.project.TestSet;
import br.jabuti.util.Debug;
import br.jabuti.util.HTMLGen;
import br.jabuti.util.ToolConstants;
import br.jabuti.util.XMLPrettyPrinter;

/**
 * This is the main class responsible to build the JaBUTi GUI.
 * 
 * It uses all other packages to build the functionalities of the testing tool.
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 * 
 */
public class JabutiGUI extends JFrame {

	/**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 1625786250251866044L;

	/***************************************************************************
	 * ****************************************************************
	 * ****************************************************************
	 * ATTRIBUTES DECLARATION
	 * ****************************************************************
	 * ****************************************************************
	 **************************************************************************/

	// This variable represents the current GUI and it
	// is used as a global variable by all other child panel,
	// to share the information common to all of them
	static private JabutiGUI mainWindow = null;

	// Default main window size
	public static final int WIDTH = 860;

	public static final int HEIGHT = 660;

	/***************************************************************************
	 * The variables below are responsible to take care of the JaBUTi menu bar
	 * and its options
	 **************************************************************************/
	// JabutiGUI Menu Bar
	JMenuBar menuBar = new JMenuBar();

	// File Menu
	private JMenu fileMenu = new JMenu();

	// File Submenu Items
	private JMenuItem openClass = new JMenuItem();

	private JMenuItem openJarZip = new JMenuItem();

	private JMenuItem openPrj = new JMenuItem();

	private JMenuItem savePrj = new JMenuItem();

	private JMenuItem saveAsPrj = new JMenuItem();

	private JMenuItem saveInst = new JMenuItem();

	private JMenuItem closePrj = new JMenuItem();

	private JMenuItem exitPrj = new JMenuItem();

	// Tools Menu
	private JMenu toolsMenu = new JMenu();

	// Tools Submenu Items
	private ButtonGroup toolsGroup = new ButtonGroup();

	static private JRadioButtonMenuItem coverageTool = new JRadioButtonMenuItem();

	static private JRadioButtonMenuItem sliceTool = new JRadioButtonMenuItem();

	// Visualization Menu
	private JMenu visualizationMenu = new JMenu();

	// Visualization Submenu Items
	private JMenuItem viewBytecodeFile = new JMenuItem();

	private JMenuItem viewSourceFile = new JMenuItem();

	// private JMenuItem viewControlFlowGraph = new JMenuItem();
	private JMenuItem viewRequiredElements = new JMenuItem();

	private JMenuItem viewStaticMetrics = new JMenuItem();

	private JMenuItem viewDefUseGraph = new JMenuItem();

	private JMenuItem viewCallGraph = new JMenuItem();
	
	
	//Qualipso Menu
	private JMenu qualipsoMenu = new JMenu();
	
	private JMenuItem viewSpago4qFile = new JMenuItem();

	// Nested submenu inside Visualization Menu
	private JMenu viewShow = new JMenu();

	private ButtonGroup viewShowGroup = new ButtonGroup();

	static private JRadioButtonMenuItem allPriorized = new JRadioButtonMenuItem();

	static private JRadioButtonMenuItem highestWeight = new JRadioButtonMenuItem();

	static private JRadioButtonMenuItem nonZeroWeight = new JRadioButtonMenuItem();

	static private JRadioButtonMenuItem zeroWeight = new JRadioButtonMenuItem();

	static private JRadioButtonMenuItem zeroNonZeroWeight = new JRadioButtonMenuItem();

	// Report Menu
	private JMenu coverageMenu = new JMenu();

	// Nested Summary Menu
	private ButtonGroup coverageGroup = new ButtonGroup();

	// Nested Type Menu
	private JCheckBoxMenuItem byType = new JCheckBoxMenuItem();

	private JCheckBoxMenuItem byFile = new JCheckBoxMenuItem();

	private JCheckBoxMenuItem byMethod = new JCheckBoxMenuItem();

	// Test Case Button
	private JMenu testCaseMenu = new JMenu();

	private JMenuItem testCaseView = new JMenuItem();

	// private JMenuItem testCaseByPathView = new JMenuItem();
	private JMenuItem testCaseExecutor = new JMenuItem();

	// Properties Menu
	private JMenu propertiesMenu = new JMenu();

	private JMenu propertiesLookAndFell = new JMenu();

	private ButtonGroup lookAndFellGroup = new ButtonGroup();

	private LookAndFeelRadioButton[] lookButtons;

	private JMenuItem projectManagerProperty = new JMenuItem();

	private JMenuItem testServerProperty = new JMenuItem();

	private JMenuItem memoryMenuItem = new JMenuItem();

	private AvailableMemoryDialog memoryDialog = null;
	private Spago4qXmlDialog spago4qDialog = null;

	// Update Menu Button
	private JMenu updateMenu = new JMenu();

	private JMenuItem updateItem = new JMenuItem();

	private JMenuItem cutItem = new JMenuItem();

	private ImageIcon semaforoRed = new ImageIcon(
			Toolkit
					.getDefaultToolkit()
					.getImage(
							ToolConstants
									.getToolBaseResource("semaforo-red-32x32.png")));

	private JLabel updateLabel = new JLabel(semaforoRed);

	// Rerport Menu
	private JMenu reportMenu = new JMenu();

	// Custom Report Menu Item
	private JMenuItem customReports = new JMenuItem();

	// Summary Report to HTML
	private JMenuItem jtable2HTML = new JMenuItem();

	// Help Menu
	private JMenu helpMenu = new JMenu();

	//Increase Decrease Buttons
	private JButton increaseButton;
	private JButton decreaseButton;
	private JPanel incDecPanel = new JPanel();
		
	// Called in Help Menu
	private AboutDialog dialog = null;

	/***************************************************************************
	 * The variables below are responsible to take care of the JaBUTi tool bar
	 **************************************************************************/
	// Testing Criteria Tool Bar
	private JToolBar toolBar = new JToolBar("Testing Criteria Tool Bar");

	// Currently, this tool bar contains a radion
	// button for each testing criteria
	private ButtonGroup typeGroup = new ButtonGroup();

	static private JRadioButton allPrimaryNodesCriterion = new JRadioButton();

	static private JRadioButton allSecondaryNodesCriterion = new JRadioButton();

	static private JRadioButton allPrimaryEdgesCriterion = new JRadioButton();

	static private JRadioButton allSecondaryEdgesCriterion = new JRadioButton();

	static private JRadioButton allPrimaryUsesCriterion = new JRadioButton();

	static private JRadioButton allSecondaryUsesCriterion = new JRadioButton();

	static private JRadioButton allPrimaryPotUsesCriterion = new JRadioButton();

	static private JRadioButton allSecondaryPotUsesCriterion = new JRadioButton();

	// This is the current active Jabuti Project
	private static JabutiProject jbtProject = null;

	// Fields to open a new class file
	JTextField packageTextField = new JTextField(25);

	JTextArea classpathTextArea = new JTextArea();

	JScrollPane classpathScroll = new JScrollPane();

	JCheckBox cfgOptionCheckBox = new JCheckBox();

	JFileChooser openFileDialog = openFileDialogCreate("Open class file...");

	// JFileChooser openJarZipFileDialog = openJarZipFileDialogCreate("Open
	// jar/zip file...");
	ProjectManagerDialog projectManagerDialog = null;

	JFileChooser projectDialog = projDialogCreate("");

	// ******************************************************
	// THE PANELS RESPONSABLE TO SHOW THE INFORMATION
	// ******************************************************
	static public final int SOURCE_PANEL = 0;

	private SourcePanel sourcePanel = new SourcePanel();

	static public final int BYTECODE_PANEL = 1;

	private BytecodePanel bytecodePanel = new BytecodePanel();

	static public final int TYPE_PANEL = 2;

	private JPanel typePanel = new JPanel();

	static public final int FILE_PANEL = 3;

	private JPanel filePanel = new JPanel();

	static public final int METHOD_PANEL = 4;

	private JPanel methodPanel = new JPanel();

	static public final int TESTCASE_PANEL = 5;

	private JPanel testCasePanel = new JPanel();

	private JPanel middlePanel = new JPanel();

	private JPanel coverageButtonsPanel = new JPanel();

	private JButton tcActivateButton = new JButton();

	private JButton tcDeactivateButton = new JButton();

	private JButton tcDeleteButton = new JButton();

	private JButton tcUndeleteButton = new JButton();

	/*
	 * private JPanel sliceButtonsPanel = new JPanel(); private JButton
	 * tcSuccessButton = new JButton(); private JButton tcNotSuccessButton = new
	 * JButton(); private JButton tcFailButton = new JButton(); private JButton
	 * tcNotFailButton = new JButton();
	 */

	static public final int METRICS_PANEL = 6;

	private JPanel metricsPanel = new JPanel();

	private Metrics mt = null;

	static public final int REQUIREMENTS_PANEL = 7;

	private JPanel requirementsPanel = new JPanel();

	private JPanel buttonsPanel = new JPanel();

	private JButton allActivatedButton = new JButton();

	private JButton allDeactivatedButton = new JButton();

	private JButton allFeasibleButton = new JButton();

	private JButton allInfeasibleButton = new JButton();

	private JPanel comboPanel = new JPanel();

	private JComboBox classCombo = new JComboBox();

	private JComboBox methodCombo = new JComboBox();

	static public final int PATHS_PANEL = 8;

	private JPanel pathsPanel = new JPanel();

	// By default, the Bytecode Panel is shown
	static private int currentCodePanel = JabutiGUI.BYTECODE_PANEL;

	public static final String CLASS_METHOD_SEPARATOR = " ";

	Vector checkButtons = new Vector();

	// Panels to show the coverage results
	private String titlePanelLabel;

	private TitlePanel titlePanel;

	private JPanel centerPanel;

	private JScrollPane tablePanel;

	private JPanel southPanel;

	private SyntesePanel syntesePanel;

	// Dialog panel to visualize the CFG of each method
	static public CFGFrame cfgFrame = null;

	// This variable controls when the trace
	// file has been changed...
	private ProbeCheck probeCheck = null;

	/***************************************************************************
	 * ****************************************************************
	 * **************************************************************** METHODS
	 * DECLARATION
	 * ****************************************************************
	 * ****************************************************************
	 **************************************************************************/

	public JabutiGUI() {
		try {
			JabutiGUIInit();
		} catch (Exception e) {
			ToolConstants.reportException(e, ToolConstants.STDERR);
		}
	}

	public static void main(String[] args) {
		try {
			if (args.length == 1)
				ToolConstants.setSTDERR(args[0]);

			br.jabuti.util.Persistency.init();
			mainWindow = new JabutiGUI();
			mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			mainWindow.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					mainWindow.exitPrj_actionPerformed(new ActionEvent(evt, 0,
							null));
				}
			});

			// Locating the JaBUTi gif logo image

			URL url = JabutiGUI.class.getResource(ToolConstants.mainLogo);

			// String name = ToolConstants.getToolBaseDirectory() +
			// ToolConstants.mainLogo;

			JLabel label = new JLabel(new ImageIcon(url), SwingConstants.CENTER);

			mainWindow.getContentPane().add(label);

			Toolkit kit = Toolkit.getDefaultToolkit();
			Image img = kit.getImage(url);

			mainWindow.setIconImage(img);

			mainWindow.setTitle(ToolConstants.toolName + " v. "
					+ ToolConstants.toolVersion);
			mainWindow.setSize(WIDTH, HEIGHT);

			mainWindow.setVisible(true);

			// Hiding the logo frame after 3 seconds
			Thread.sleep(3000);
			mainWindow.getContentPane().remove(label);
			mainWindow.update(mainWindow.getGraphics());
		} catch (Exception e) {
			ToolConstants.reportException(e, ToolConstants.STDERR);
		}
	}

	/*
	 * This method is responsable to build the complete graphical interface
	 * (with all the menus) and enable only part of the menu options.
	 */
	private void JabutiGUIInit() throws Exception {
		buildFileMenu();
		buildToolsMenu();
		buildVisualizationMenu();
		buildQualipsoMenu();
		menuBar.add(Box.createHorizontalGlue());
		buildSummaryMenu();
		buildTestCaseMenu();
		buildReportMenu();
		buildPropertiesMenu();
		buildUpdateMenu();
		buildHelpMenu();

		buildIncreaseDecreasePanel();
		
		buildToolBar();
		// Adding all menu to the GUI
		setJMenuBar(menuBar);

		toolBar.setFloatable(false);
		toolBar.setBorder(BorderFactory.createEtchedBorder());
		getContentPane().add(toolBar, BorderLayout.NORTH);

		// Althought the first panel to be shown is the bytecode
		// panel... The source code panel is included and
		// set as not-visible to create the scrollBar correctly
		// when it was called...
		sourcePanel.setVisible(false);
		getContentPane().add(sourcePanel, BorderLayout.CENTER);

		bytecodePanel.setVisible(true);
		getContentPane().add(bytecodePanel, BorderLayout.CENTER);

		openFileDialog.setMinimumSize(new Dimension(600, 200));
		openFileDialog.setPreferredSize(new Dimension(800, 400));

		// openJarZipFileDialog.setMinimumSize(new Dimension(600, 200));
		// openJarZipFileDialog.setPreferredSize(new Dimension(800, 400));

		restartToInitialState();
	}

	/*
	 * This method restart the graphica interface to its initial states,
	 * restarting all global variables.
	 */
	private void restartToInitialState() {
		if (probeCheck != null) {
			probeCheck.interrupt();
			probeCheck = null;
		}

		// Setting the current project as null
		if (getProject() != null) {
			getProject().closeProject();
			setProject(null);
		}

		if (mainWindow != null) {
			mainWindow.setTitle(ToolConstants.toolName + " v. "
					+ ToolConstants.toolVersion);
		}

		// restarttin the TestSet variables before create the
		// new ActiveProject
		TestSet.restart();

		// Hidding all panels
		bytecodePanel.setVisible(false);
		sourcePanel.setVisible(false);
		typePanel.setVisible(false);
		filePanel.setVisible(false);
		methodPanel.setVisible(false);
		testCasePanel.setVisible(false);
		/* Resources of test case panel */
		// COVERAGE TEST CASE PANEL
		coverageButtonsPanel = new JPanel();
		coverageButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		coverageButtonsPanel.setBorder(BorderFactory.createEtchedBorder());

		tcActivateButton.setText("Activate All");
		tcActivateButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tcActivateButton_actionPerformed(e);
			}
		});

		tcDeactivateButton.setText("Deactivate All");
		tcDeactivateButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tcDeactivateButton_actionPerformed(e);
					}
				});

		tcDeleteButton.setText("Delete All");
		tcDeleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tcDeleteButton_actionPerformed(e);
			}
		});

		tcUndeleteButton.setText("Undelete All");
		tcUndeleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tcUndeleteButton_actionPerformed(e);
			}
		});

		coverageButtonsPanel.add(tcActivateButton);
		coverageButtonsPanel.add(tcDeactivateButton);
		coverageButtonsPanel.add(tcDeleteButton);
		coverageButtonsPanel.add(tcUndeleteButton);

		// SLICE TEST CASE PANEL
		/*
		 * sliceButtonsPanel = new JPanel(); sliceButtonsPanel.setLayout( new
		 * FlowLayout( FlowLayout.LEFT ) );
		 * sliceButtonsPanel.setBorder(BorderFactory.createEtchedBorder());
		 * 
		 * tcSuccessButton.setText( "Success All" );
		 * tcSuccessButton.addActionListener(new java.awt.event.ActionListener() {
		 * public void actionPerformed(ActionEvent e) {
		 * tcSuccessButton_actionPerformed(e); } } );
		 * 
		 * tcNotSuccessButton.setText( "Not Success All" );
		 * tcNotSuccessButton.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * tcNotSuccessButton_actionPerformed(e); } } );
		 * 
		 * tcFailButton.setText( "Fail All" );
		 * tcFailButton.addActionListener(new java.awt.event.ActionListener() {
		 * public void actionPerformed(ActionEvent e) {
		 * tcFailButton_actionPerformed(e); } } );
		 * 
		 * tcNotFailButton.setText( "Not Fail All" );
		 * tcNotFailButton.addActionListener(new java.awt.event.ActionListener() {
		 * public void actionPerformed(ActionEvent e) {
		 * tcNotFailButton_actionPerformed(e); } } );
		 * 
		 * sliceButtonsPanel.add( tcSuccessButton ); sliceButtonsPanel.add(
		 * tcNotSuccessButton );
		 * 
		 * sliceButtonsPanel.add( tcFailButton ); sliceButtonsPanel.add(
		 * tcNotFailButton );
		 */
		/* End of resources of test case panel */

		metricsPanel.setVisible(false);
		mt = null;

		requirementsPanel.setVisible(false);
		/* Panel used by the requirement panel */
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		allActivatedButton.setText("Activate All");
		allActivatedButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allActivatedButton_actionPerformed(e);
					}
				});

		allDeactivatedButton.setText("Deactivate All");
		allDeactivatedButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allDeactivatedButton_actionPerformed(e);
					}
				});

		allFeasibleButton.setText("Feasible All");
		allFeasibleButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allFeasibleButton_actionPerformed(e);
					}
				});

		allInfeasibleButton.setText("Infeasible All");
		allInfeasibleButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allInfeasibleButton_actionPerformed(e);
					}
				});

		buttonsPanel.add(allActivatedButton);
		buttonsPanel.add(allDeactivatedButton);
		buttonsPanel.add(allFeasibleButton);
		buttonsPanel.add(allInfeasibleButton);
		/* End of the resources used by the requirement Panel */

		pathsPanel.setVisible(false);

		disableDefaultOptions();
	}

	/*
	 * This method specify which are the default options that have to be enabled
	 * when no Project is active.
	 */
	private void disableDefaultOptions() {
		// File Menu
		savePrj.setEnabled(false);
		saveAsPrj.setEnabled(false);
		saveInst.setEnabled(false);
		closePrj.setEnabled(false);

		// Tools Menu
		toolsMenu.setEnabled(false);
		// Visualization Menu
		visualizationMenu.setEnabled(false);
		viewSourceFile.setEnabled(false);
		viewStaticMetrics.setEnabled(false);
		viewDefUseGraph.setEnabled(false);
		// viewControlFlowGraph.setEnabled(false);
		viewRequiredElements.setEnabled(false);
		// viewCallGraph;
		// allPriorized;
		// highestWeight;
		// nonZeroWeight;
		// zeroWeight;
		// zeroNonZeroWeight;
		
		qualipsoMenu.setEnabled(false);
		viewSpago4qFile.setEnabled(false);

		// Summary Menu
		coverageMenu.setEnabled(false);
		// byType;
		// byFile;
		// byProcess;

		// Test Case Button
		testCaseMenu.setEnabled(false);

		// Update Menu
		updateMenu.setEnabled(false);
		setUpdateLabelImage(null);

		// Report Menu
		reportMenu.setEnabled(false);

		// Testing Criteria Tool Bar
		allPrimaryNodesCriterion.setEnabled(false);
		allSecondaryNodesCriterion.setEnabled(false);
		allPrimaryEdgesCriterion.setEnabled(false);
		allSecondaryEdgesCriterion.setEnabled(false);
		allPrimaryUsesCriterion.setEnabled(false);
		allSecondaryUsesCriterion.setEnabled(false);
		allPrimaryPotUsesCriterion.setEnabled(false);
		allSecondaryPotUsesCriterion.setEnabled(false);

		projectManagerProperty.setEnabled(false);
		testServerProperty.setEnabled(false);
	}

	void enableDefaultOptions() {
		// File Menu
		savePrj.setEnabled(true);
		saveAsPrj.setEnabled(true);
		saveInst.setEnabled(true);
		closePrj.setEnabled(true);

		// Tools Menu
		toolsMenu.setEnabled(true);
		coverageTool.setSelected(true);

		// Visualization Menu
		visualizationMenu.setEnabled(true);
		viewSourceFile.setEnabled(true);
		viewStaticMetrics.setEnabled(true);
		viewDefUseGraph.setEnabled(true);
		// viewControlFlowGraph.setEnabled(true);
		viewRequiredElements.setEnabled(true);
		viewCallGraph.setEnabled(false);
		allPriorized.setEnabled(true);
		allPriorized.setSelected(true);

		highestWeight.setEnabled(false);
		nonZeroWeight.setEnabled(false);
		zeroWeight.setEnabled(false);
		zeroNonZeroWeight.setEnabled(false);
		
		qualipsoMenu.setEnabled(true);
		viewSpago4qFile.setEnabled(true);
		
		// Summary Menu
		coverageMenu.setEnabled(true);
		byType.setSelected(false);
		byFile.setSelected(false);
		byMethod.setSelected(false);

		// Test Case Button
		testCaseMenu.setEnabled(true);

		// Update Menu
		updateMenu.setEnabled(true);
		setUpdateLabelImage(null);

		// Report Menu
		reportMenu.setEnabled(true);

		// Testing Criteria Tool Bar
		allPrimaryNodesCriterion.setEnabled(true);
		allPrimaryNodesCriterion.setSelected(true);

		allSecondaryNodesCriterion.setEnabled(true);

		allPrimaryEdgesCriterion.setEnabled(true);
		allSecondaryEdgesCriterion.setEnabled(true);

		allPrimaryUsesCriterion.setEnabled(true);
		allSecondaryUsesCriterion.setEnabled(true);

		allPrimaryPotUsesCriterion.setEnabled(true);
		allSecondaryPotUsesCriterion.setEnabled(true);

		projectManagerProperty.setEnabled(true);
		testServerProperty.setEnabled(true);
	}

	public void updatePane() {
		int n = JabutiGUI.BYTECODE_PANEL;

		if (bytecodePanel.isVisible()) {
			n = JabutiGUI.BYTECODE_PANEL;
		} else if (sourcePanel.isVisible()) {
			n = JabutiGUI.SOURCE_PANEL;
		} else if (typePanel.isVisible()) {
			n = JabutiGUI.TYPE_PANEL;
		} else if (filePanel.isVisible()) {
			n = JabutiGUI.FILE_PANEL;
		} else if (methodPanel.isVisible()) {
			n = JabutiGUI.METHOD_PANEL;
		} else if (testCasePanel.isVisible()) {
			n = JabutiGUI.TESTCASE_PANEL;
		} else if (metricsPanel.isVisible()) {
			n = JabutiGUI.METRICS_PANEL;
		} else if (requirementsPanel.isVisible()) {
			n = JabutiGUI.REQUIREMENTS_PANEL;
		} else if (pathsPanel.isVisible()) {
			n = JabutiGUI.PATHS_PANEL;
		}

		if (isSourcePanel()) {
			updatePane(n, sourcePanel.getCaretLine());
		} else {
			updatePane(n, bytecodePanel.getCaret());
		}

		if (SelectedPoint.isSelected()) {

			ClassFile cf = getProject().getClassFile(
					getProject().getCurClassName());
			ClassMethod cm = cf.getMethod(SelectedPoint.getMethod());
			if (cm != null) {
				getProject().setCurMethodName(cm.getMethodName());
			}
		}

		updateCFGFrame(getProject().getCurClassName(), getProject()
				.getCurMethodName());
	}

	void updateCFGFrame(String className, String methodName) {
		if (cfgIsVisible()) {
			WeightColor.clearClassVariablesTable();
			WeightColor.updateColorAttributes();
			cfgFrame.setSelectedClass(className);
			cfgFrame.setSelectedMethod(methodName);
		}
	}

	public void updatePane(int n, int offset) {
		bytecodePanel.setVisible(false);
		sourcePanel.setVisible(false);
		typePanel.setVisible(false);
		filePanel.setVisible(false);
		methodPanel.setVisible(false);
		testCasePanel.setVisible(false);
		metricsPanel.setVisible(false);
		requirementsPanel.setVisible(false);
		pathsPanel.setVisible(false);

		currentCodePanel = n;

		switch (n) {
		case JabutiGUI.BYTECODE_PANEL:
			updateBytecodePanel(offset);
			bytecodePanel.setVisible(true);
			getContentPane().add(bytecodePanel, BorderLayout.CENTER);
			break;

		case JabutiGUI.SOURCE_PANEL:
			updateSourcePanel(offset);
			sourcePanel.setVisible(true);
			getContentPane().add(sourcePanel, BorderLayout.CENTER);
			break;

		case JabutiGUI.TYPE_PANEL:
			updateByTypePanel();
			typePanel.setVisible(true);
			byType.setSelected(true);
			getContentPane().add(typePanel, BorderLayout.CENTER);
			break;

		case JabutiGUI.FILE_PANEL:
			updateByFilePanel();
			filePanel.setVisible(true);
			byFile.setSelected(true);
			getContentPane().add(filePanel, BorderLayout.CENTER);
			break;

		case JabutiGUI.METHOD_PANEL:
			updateByMethodPanel();
			methodPanel.setVisible(true);
			byMethod.setSelected(true);
			getContentPane().add(methodPanel, BorderLayout.CENTER);
			break;

		case JabutiGUI.TESTCASE_PANEL:
			// Update the coverage since it is based on the project coverage
			TestSet.updateOverallCoverage(getProject());
			if (JabutiGUI.isCoverage())
				updateCoverageTestCasePanel();
			else
				updateSliceTestCasePanel();

			testCasePanel.setVisible(true);
			getContentPane().add(testCasePanel, BorderLayout.CENTER);
			break;

		case JabutiGUI.METRICS_PANEL:
			updateMetricsPanel();
			metricsPanel.setVisible(true);
			getContentPane().add(metricsPanel, BorderLayout.CENTER);
			break;

		case JabutiGUI.REQUIREMENTS_PANEL:
			// Setting the current class to be shown
			updateRequirementsPanel();
			requirementsPanel.setVisible(true);
			getContentPane().add(requirementsPanel, BorderLayout.CENTER);
			break;

		/*
		 * case JabutiGUI.PATHS_PANEL: // Update the coverage since it is based
		 * on the project coverage TestSet.updateTestSetCoverage( getProject() );
		 * updateTestCasePathsPanel(); pathsPanel.setVisible(true);
		 * getContentPane().add( pathsPanel, BorderLayout.CENTER ); break;
		 */
		}
	}

	void updateBytecodePanel(int offset) {
		if (getProject() != null) {
			WeightColor.clearClassVariablesTable();
			WeightColor.updateColorAttributes();
			bytecodePanel = bytecodePanel.showBytecodePanel(offset);
		}
	}

	void updateSourcePanel(int offset) {
		if (getProject() != null) {
			WeightColor.clearClassVariablesTable();
			WeightColor.updateColorAttributes();
			sourcePanel = sourcePanel.showSourcePanel(offset);
		}
	}

	/**
	 * File MENU This method is responsible to buil the File Menu and to add
	 * each submenu option
	 */
	private void buildFileMenu() {
		// Build the entire menu
		// File Menu
		fileMenu.setText("File");
		fileMenu.setMnemonic('F');

		// File submenu
		// Open class Submenu
		openClass.setText("Open Class");
		openClass.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openClass_actionPerformed(e);
			}
		});
		fileMenu.add(openClass);

		// Open Jar/Zip Submenu
		openJarZip.setText("Open Jar/Zip");
		openJarZip.setEnabled(false);
		openJarZip.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openJarZip_actionPerformed(e);
			}
		});
		fileMenu.add(openJarZip);

		// Open Project Submenu
		openPrj.setText("Open Project");
		openPrj.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openPrj_actionPerformed(e);
			}
		});
		fileMenu.add(openPrj);

		// Close Project Submenu
		closePrj.setText("Close Project");
		closePrj.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closePrj_actionPerformed(e);
			}
		});
		fileMenu.add(closePrj);

		fileMenu.addSeparator();

		// Save Project Submenu
		savePrj.setText("Save");
		savePrj.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePrj_actionPerformed(e);
			}
		});
		fileMenu.add(savePrj);

		// Save As Submenu
		saveAsPrj.setText("Save As...");
		saveAsPrj.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsPrj_actionPerformed(e);
			}
		});

		fileMenu.add(saveAsPrj);

		fileMenu.addSeparator();

		// Save Instrumented Classes
		saveInst.setText("Save Instrumented Classes");
		saveInst.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveInst_actionPerformed(e);
			}
		});
		fileMenu.add(saveInst);

		fileMenu.addSeparator();

		exitPrj.setText("Exit");
		exitPrj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.CTRL_MASK));
		exitPrj.setMnemonic('x');
		exitPrj.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitPrj_actionPerformed(e);
			}
		});
		fileMenu.add(exitPrj);

		// Adding Prj Menu to MenuBar
		menuBar.add(fileMenu);
	}

	/**
	 * FILE SUBMENU LISTENERS
	 */
	// openClass action performed
	void openClass_actionPerformed(ActionEvent ex) {
		if (getProject() != null) {
			int opt = JOptionPane.showConfirmDialog(null, "The current project will be closed. Close?", "Confirm", JOptionPane.YES_NO_OPTION);
			if (opt == JOptionPane.NO_OPTION) {
				return;
			} else {
				closePrj_actionPerformed(ex);
			}
		}

		String cpath = "";
		boolean invalid = true;
		String pclass = "";

		do {
			int k = openFileDialog.showOpenDialog(this);
			if (k != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File theFile = openFileDialog.getSelectedFile();
			if (!theFile.isFile()) {
				JOptionPane.showMessageDialog(null, "File " + theFile.getName()	+ " not found", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			pclass = theFile.getName();
			if (! pclass.endsWith(".class")) {
				JOptionPane.showMessageDialog(null, "A .class file is expected.", "Error",	JOptionPane.ERROR_MESSAGE);
				return;
			}
			pclass = pclass.substring(0, pclass.length() - 6);
			String pck = packageTextField.getText().trim();

			if (pck.length() > 0) {
				pclass = pck + "." + pclass;
			}

			cpath = classpathTextArea.getText().trim();
			if (cpath.isEmpty()) {
				JOptionPane.showMessageDialog(
					null,
					"The classpath field cannot be empty. Please, provide the complete path necessary to run the application under testing.",
					"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					ToolConstants.getClassFromClasspath(pclass, false, cpath);
					invalid = false;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(
							null,
							"The provided classpath is not valid. It is not possible to locate " + pclass + " from it.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} while (cpath.length() == 0 || invalid);

		try {
			jbtProject = new JabutiProject(pclass, cpath);
			jbtProject.setCFGOption((cfgOptionCheckBox.isSelected()) ? CFG.NO_CALL_NODE	: CFG.NONE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Cannot parser file " + pclass	+ "! ", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int status = projectManagerProperty_actionPerformed(ex);
		if (status == JOptionPane.CANCEL_OPTION)
			return;

		if (getProject() != null) {
			if (getProject().getProjectFile() != null) {
				// Loading the test set file
				TestSet.initialize(getProject(), getProject().getTraceFileName());
			}
			
			// Reseting the probe check variables...
			if (probeCheck == null) {
				probeCheck = new ProbeCheck(mainWindow);
				probeCheck.start();
			} else {
				probeCheck.setOldSize(0L);
				probeCheck.setNewSize(0L);
			}

			if (cfgFrame != null) {
				cfgFrame.dispose();
			}
			cfgFrame = null;

			enableDefaultOptions();
		}
		
		updatePane();
	}

	// openJarZip action performed
	void openJarZip_actionPerformed(ActionEvent ex) {
		/*
		 * if ( getProject() != null ) { int opt =
		 * JOptionPane.showConfirmDialog(null, "The current project will be
		 * closed. Close?", "Confirm", JOptionPane.YES_NO_OPTION);
		 * 
		 * if (opt == JOptionPane.NO_OPTION) { return; } else {
		 * closePrj_actionPerformed(ex); } }
		 * 
		 * int k = openJarZipFileDialog.showOpenDialog(this);
		 * 
		 * if (k != JFileChooser.APPROVE_OPTION) { return; }
		 * 
		 * File theFile = openJarZipFileDialog.getSelectedFile();
		 * 
		 * if (!theFile.isFile()) // verifica se existe {
		 * JOptionPane.showMessageDialog(null, "File " + theFile.getName() + "
		 * not found", "Error", JOptionPane.ERROR_MESSAGE); return; }
		 * 
		 * String pclass = theFile.getName();
		 * 
		 * if (!pclass.endsWith(".jar") || !pclass.endsWith(".zip")) {
		 * JOptionPane.showMessageDialog(null, "A .jar or .zip file is
		 * expected.", "Error", JOptionPane.ERROR_MESSAGE); return; } pclass =
		 * pclass.substring(0, pclass.length() - 6); String pck =
		 * packageTextField.getText().trim();
		 * 
		 * if (pck.length() > 0) { pclass = pck + "." + pclass; }
		 * 
		 * String cpath = classpathTextArea.getText().trim();
		 * 
		 * if (cpath.length() <= 0) { cpath = null; }
		 * 
		 * Program theProgram = null;
		 * 
		 * try { jbtProject = new JabutiProject(pclass, cpath);
		 * jbtProject.setCFGOption( (cfgOptionCheckBox.isSelected())?
		 * CFG.NO_CALL_NODE : CFG.NONE ); theProgram = jbtProject.getProgram(); }
		 * catch (Exception e) { JOptionPane.showMessageDialog(null, "Cannot
		 * parser file " + pclass + "! ", "Error", JOptionPane.ERROR_MESSAGE);
		 * return; }
		 * 
		 * int status = projectManagerProperty_actionPerformed( ex );
		 * 
		 * if ( status == JOptionPane.CANCEL_OPTION ) return;
		 * 
		 * if ( getProject() != null ) { System.out.println(
		 * getProject().toString() );
		 * 
		 * if ( getProject().getProjectFile() != null) { // Loading the test set
		 * file TestSet.initialize( getProject(),
		 * getProject().getTraceFileName() ); System.out.println(
		 * TestSet.print() ); } // Reseting the probe check variables... if
		 * (probeCheck == null) { probeCheck = new ProbeCheck( mainWindow );
		 * probeCheck.start(); } else { probeCheck.setOldSize( 0L );
		 * probeCheck.setNewSize( 0L ); }
		 * 
		 * if (cfgFrame != null) { cfgFrame.dispose(); } cfgFrame = null;
		 * 
		 * enableDefaultOptions(); } updatePane();
		 */
	}

	class LeftPanel extends JPanel implements java.beans.PropertyChangeListener {
		/**
		 * Added to jdk1.5.0_04 compiler
		 */
		private static final long serialVersionUID = 103506129108142931L;

		GridBagConstraints gridBagConstraints;

		JLabel packageLabel;

		JLabel classpathLabel;

		JLabel blankLabel;

		public LeftPanel(JFileChooser fc) {
			super();

			setLayout(new GridBagLayout());

			packageLabel = new JLabel("Package:   ");

			packageLabel.setText("Package:");
			packageLabel
					.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
			gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
			add(packageLabel, gridBagConstraints);

			packageTextField
					.setToolTipText("The package name for the selected class file...");
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			gridBagConstraints.gridheight = 2;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 5);
			gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
			gridBagConstraints.weightx = 2.0;
			add(packageTextField, gridBagConstraints);

			classpathLabel = new JLabel("Classpath: ");

			classpathLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			classpathLabel
					.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			classpathLabel
					.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
			gridBagConstraints.weighty = 2.0;
			add(classpathLabel, gridBagConstraints);

			classpathTextArea.setText("");
			classpathTextArea.setLineWrap(true);

			classpathScroll.setViewportView(classpathTextArea);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			gridBagConstraints.gridheight = 2;
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
			gridBagConstraints.weightx = 2.0;
			gridBagConstraints.weighty = 2.0;

			add(classpathScroll, gridBagConstraints);

			cfgOptionCheckBox = new JCheckBox("Hide CFG Call Nodes");
			cfgOptionCheckBox.setSelected(true);
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
			gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
			add(cfgOptionCheckBox, gridBagConstraints);

			blankLabel = new JLabel();

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 2.0;
			gridBagConstraints.weighty = 2.0;
			add(blankLabel, gridBagConstraints);

			fc.addPropertyChangeListener(this);
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			String prop = e.getPropertyName();

			if (prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
				File file = (File) e.getNewValue();

				if (file != null && file.toString().endsWith(".class")) {
					getPackageInfo(file);
				}
			}
		}

		public void getPackageInfo(File f) {
			try {
				JavaClass curClass = new ClassParser(f.toString()).parse();

				packageTextField.setText(curClass.getPackageName());
				// System.out.println( curClass.getPackageName() );
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
			}
		}
	}

	public JFileChooser openFileDialogCreate(String name) {
		JFileChooser fc = new JFileChooser(".");
		LeftPanel leftPanel = new LeftPanel(fc);

		fc.setAccessory(leftPanel);
		if (name.length() > 0) {
			fc.setDialogTitle(name);
		}
		javax.swing.filechooser.FileFilter f = new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().endsWith(".class")) {
					return true;
				}
				return false;
			}

			public String getDescription() {
				return "Java bytecode (.class) files";
			}
		};

		fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());
		fc.setFileFilter(f);
		return fc;
	}

	public JFileChooser openJarZipFileDialogCreate(String name) {
		JFileChooser fc = new JFileChooser(".");
		LeftPanel leftPanel = new LeftPanel(fc);

		fc.setAccessory(leftPanel);
		if (name.length() > 0) {
			fc.setDialogTitle(name);
		}
		javax.swing.filechooser.FileFilter f = new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().endsWith(".jar")) {
					return true;
				} else if (f.getName().endsWith(".zip")) {
					return true;
				}
				return false;
			}

			public String getDescription() {
				return "Java jar (.jar) or zip (.zip) files";
			}
		};

		fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());
		fc.setFileFilter(f);
		return fc;
	}

	// openPrj action performed
	void openPrj_actionPerformed(ActionEvent ex) {
		if (getProject() != null) {
			int opt = JOptionPane.showConfirmDialog(null, "he current project will be closed. Close?", "Confirm", JOptionPane.YES_NO_OPTION);
			if (opt == JOptionPane.NO_OPTION) {
				return;
			} else {
				closePrj_actionPerformed(ex);
			}
		}

		projectDialog.setDialogTitle("Open Project...");
		int k = projectDialog.showOpenDialog(this);

		if (k != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File theFile = projectDialog.getSelectedFile();

		if (! theFile.isFile()) {
			JOptionPane.showMessageDialog(null, "File " + theFile.getName() + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// verifica se eh .jbt
		String pclass = theFile.getName();
		if (! pclass.endsWith(ToolConstants.projectExtension)) { 
			JOptionPane.showMessageDialog(null, "A "+ ToolConstants.projectExtension + " file is expected.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			String cp = ClasspathParser.getClassPath(theFile.toString());

			boolean valid, changed = false;
			do {
				valid = true;
				StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
				while (valid && st.hasMoreTokens()) {
					File f = new File(st.nextToken());
					if (!f.exists())
						valid = false;
				}
				if (valid == false) {
					cp = (String) JOptionPane
							.showInputDialog(
									this,
									"The current classpath has one or more invalid paths.\nPlease, provide only valid paths for the project classpath.\n",
									"Invalid project classpath",
									JOptionPane.ERROR_MESSAGE, null, null, cp);
					if (cp == null)
						return;
					changed = true;
				}
			} while (valid == false);

			jbtProject = JabutiProject.reloadProj(theFile.toString(), cp, true);

			int status = projectManagerProperty_actionPerformed(ex);

			if (status == JOptionPane.CANCEL_OPTION)
				return;

			if (getProject() != null) {
				// Reseting the probe check variables
				if (probeCheck == null) {
					probeCheck = new ProbeCheck(mainWindow);
					probeCheck.start();
				} else {
					probeCheck.setOldSize(0L);
					probeCheck.setNewSize(0L);
				}

				if (cfgFrame != null) {
					cfgFrame.dispose();
				}

				cfgFrame = null;

				enableDefaultOptions();
			}
			updatePane();

			if (changed) {
				jbtProject.execChanges();
			}
		} catch (Exception e) {
			ToolConstants.reportException(e, ToolConstants.STDERR);
			JOptionPane.showMessageDialog(null, "Error openning file " + pclass
					+ "! ", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	// selectPrj action performed
	void selectPrj_actionPerformed(ActionEvent ex) {

		projectDialog.setDialogTitle("Select project name...");
		int k = projectDialog.showOpenDialog(this);

		if (k != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File theFile = projectDialog.getSelectedFile();

		String pclass = theFile.getName();

		if (!pclass.endsWith(ToolConstants.projectExtension)) // verifica se
		// eh .jba
		{
			/*
			 * JOptionPane.showMessageDialog(null, "A " +
			 * ToolConstants.projectExtension + " file is expected ", "Error",
			 * JOptionPane.ERROR_MESSAGE); return;
			 */
			// System.out.println("Name: " + pclass);
			pclass = theFile.toString().trim() + ToolConstants.projectExtension;
			// System.out.println("Changed Name: " + pclass);
			theFile = new File(pclass);
		}

		if (theFile.isFile()) // verifica se existe
		{

			int cf = JOptionPane.showConfirmDialog(null, "File "
					+ theFile.getName() + " alread exists. Overwrite?",
					"Confirm", JOptionPane.YES_NO_OPTION);

			if (cf != JOptionPane.YES_OPTION) {
				return;
			}
		}

		getProject().setProjectFile(theFile);

		// Erasing the trace file is exists...
		try {
			File trcFile = new File(getProject().getTraceFileName());

			if (trcFile.exists()) {
				trcFile.delete();
			}
		} catch (Exception e) {
			ToolConstants.reportException(e, ToolConstants.STDERR);
		}
	}

	public JFileChooser projDialogCreate(String name) {
		JFileChooser fc = new JFileChooser();

		if (name != null && ! name.trim().isEmpty()) {
			fc.setDialogTitle(name);
		}
		
		FileFilter f = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().endsWith(ToolConstants.projectExtension)) {
					return true;
				}
				return false;
			}

			public String getDescription() {
				return ToolConstants.toolName + " Project file";
			}
		};

		fc.setFileFilter(f);
		return fc;
	}

	// saveProject action performed
	void savePrj_actionPerformed(ActionEvent ex) {
		if (getProject() == null) {
			return;
		}
		if (getProject().getProjectFile() == null) {
			saveAsPrj_actionPerformed(ex);
			return;
		}
		try {
			if (TestSet.getNumberOfDeletedTestCases() > 0) {
				int opt = JOptionPane
						.showConfirmDialog(
								null,
								"There is at least one test case to be deleted. "
										+ "Saving the project will permanently removed selected test cases. "
										+ "Do you want to continue?",
								"Confirm", JOptionPane.YES_NO_OPTION);

				if (opt == JOptionPane.NO_OPTION) {
					return;
				} else {
					TestSet.removeTestCases(JabutiGUI.getProject());
				}
			}

			getProject().saveProject();

			// Initializing the test set class
			// TestSet.initialize( TheProject.getActiveProject(),
			// TheProject.getTraceFileName() );
			// System.out.println( TestSet.print() );

			// After saved the default options can be enabled...
			enableDefaultOptions();
		} catch (Exception e) {
			String pclass = getProject().getProjectFileName();

			JOptionPane.showMessageDialog(null, "Error saving file " + pclass
					+ "! ", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	// saveAsSession action performed
	void saveAsPrj_actionPerformed(ActionEvent ex) {
		if (getProject() == null) {
			return;
		}
		projectDialog.setDialogTitle("Save As...");
		int k = projectDialog.showSaveDialog(this);

		if (k != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File theFile = projectDialog.getSelectedFile();
		String pclass = theFile.getName();

		if (theFile.isFile()) // verifica se existe
		{
			if (!pclass.endsWith(ToolConstants.projectExtension)) // verifica
			// se eh
			// .jba
			{
				pclass += ToolConstants.projectExtension;
				theFile = new File(theFile.getAbsolutePath()
						+ ToolConstants.projectExtension);
			}

			int cf = JOptionPane.showConfirmDialog(null, "File "
					+ theFile.getName() + " alread exists. Overwrite?",
					"Confirm", JOptionPane.YES_NO_OPTION);

			if (cf != JOptionPane.YES_OPTION) {
				return;
			}
		}

		try {
			if (TestSet.getNumberOfDeletedTestCases() > 0) {
				int opt = JOptionPane
						.showConfirmDialog(
								null,
								"There is at least one test case to be deleted. "
										+ "Saving the project will permanently removed selected test cases. "
										+ "Do you want to continue?",
								"Confirm", JOptionPane.YES_NO_OPTION);

				if (opt == JOptionPane.NO_OPTION) {
					return;
				} else {
					TestSet.removeTestCases(JabutiGUI.getProject());
				}
			}

			getProject().setProjectFile(theFile);
			getProject().saveProject();

			System.out
					.println("Trace File: " + getProject().getTraceFileName());

			// Erasing the trace file is exists...
			try {
				File trcFile = new File(getProject().getTraceFileName());

				if (trcFile.exists()) {
					System.out.println("Erasing: "
							+ getProject().getTraceFileName());
					trcFile.delete();
				}
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
			}

			// Initializing the test set class
			TestSet.initialize(getProject(), getProject().getTraceFileName());
			System.out.println(TestSet.print());

			// After saved the default options can be enabled...
			enableDefaultOptions();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error saving file " + pclass
					+ "!\n " + e.getClass().getName() + ": " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.setTitle(getProject().getProjectFileName() + "");
	}

	void saveInst_actionPerformed(ActionEvent ex) {
		JabutiProject prj = getProject();
		String fileName = prj.getInstrumentedJarFileName();

		boolean noMain = false;
		int op = JOptionPane
		.showConfirmDialog(
				JabutiGUI.mainWindow(),
				"Has the base class \"" + prj.getBaseClass() + "\" a main method?",
				"Warning", JOptionPane.YES_NO_OPTION);
		if (op == JOptionPane.NO_OPTION) {
			noMain = true;
		}
		
		try {
			File jarFile = new File(fileName);
			if (jarFile.exists()) {
				// default icon, custom title
				op = JOptionPane.showConfirmDialog(null,
						"Instrumented JAR File " + fileName
								+ " already exists.\n" + "Overwrite it?",
						"Question", JOptionPane.YES_NO_OPTION);

				if (op == JOptionPane.YES_OPTION) {
					boolean deleted = jarFile.delete();
					if (deleted)
						System.out.println("JAR file " + fileName
								+ " deleted successfuly!!!");
					else
						System.out.println("JAR file " + fileName
								+ " NOT deleted");
				} else
					return;
			}
		} catch (Exception e) {
			ToolConstants.reportException(e, ToolConstants.STDERR);
		}
		
		String[] args = { "-p", prj.getProjectFileName(), "-o", fileName,
				prj.getBaseClass(), (noMain)? "-nomain": "" };

		for (int i = 0; i < args.length; i++) {
			System.out.println("Arg " + i + " " + args[i]);
		}
		
		if (br.jabuti.probe.ProberInstrum.instrumentProject(prj, prj
				.getBaseClass(), fileName, noMain)) {
			JOptionPane.showMessageDialog(null, "File " + fileName
					+ " created successfully.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "File " + fileName
					+ " not created successfully.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// close Project action performed
	void closePrj_actionPerformed(ActionEvent ex) {
		if (getProject() != null) {
			 if (getProject().changed()) {
				 int cf = JOptionPane.showConfirmDialog(null, "Project in use. Save?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
				 if (cf == JOptionPane.CANCEL_OPTION) {
					 return;
				 }
				 if (cf == JOptionPane.YES_OPTION) {
					 try {
						 if (getProject().getProjectFile() != null) {
							 getProject().saveProject();
						 } else {
							 savePrj_actionPerformed(ex);
						 }
					 } catch (Exception es) {
						 String pclass = getProject().getProjectFileName();
						 JOptionPane.showMessageDialog(null, "Error saving file " + pclass + "! ", "Error",	JOptionPane.ERROR_MESSAGE);
						 return;
					 }
				 }
			 }
		}

		if (cfgIsVisible()) {
			cfgFrame.setVisible(false);
		}

		// **********************************************
		// CODE TO COMPLETLY CLEAN ALL GLOBAL VARIABLES
		// **********************************************
		restartToInitialState();
	}

	// exitPrj action performed
	void exitPrj_actionPerformed(ActionEvent ex) {
		int selection = JOptionPane.showConfirmDialog(this,
				"Do you want to exit?", "Exit", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (selection == JOptionPane.YES_OPTION) {
			closePrj_actionPerformed(ex);
			System.exit(0);
		}
	}

	/**
	 * TOOLS MENU This method is responsible to buil the Tools Menu and to add
	 * each submenu option
	 */
	private void buildToolsMenu() {
		// Build the tools menu
		toolsMenu.setText("Tools");
		toolsMenu.setMnemonic('o');

		coverageTool.setText("Coverage Tool");
		coverageTool.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				coverageTool_actionPerformed(e);
			}
		});
		toolsGroup.add(coverageTool);
		toolsMenu.add(coverageTool);

		sliceTool.setText("Slicing Tool");
		sliceTool.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sliceTool_actionPerformed(e);
			}
		});
		toolsGroup.add(sliceTool);
		toolsMenu.add(sliceTool);

		toolsMenu.addSeparator();

		// Adding Tools Menu to MenuBar
		menuBar.add(toolsMenu);
	}

	/**
	 * TOOLS SUBMENU LISTENERS
	 */
	private Object[] activeTestCases;

	// coverageTool Tool listener
	void coverageTool_actionPerformed(ActionEvent ex) {

		// Reactivating test cases
		int i = 0;
		for (; (activeTestCases != null) && (i < activeTestCases.length); i++) {
			TestSet.activateTestCase(JabutiGUI.getProject(),
					(String) activeTestCases[i]);
		}
		if (i > 0)
			getProject().execChanges();

		SelectedPoint.reset();

		allPrimaryEdgesCriterion.setEnabled(true);
		allSecondaryEdgesCriterion.setEnabled(true);
		allPrimaryUsesCriterion.setEnabled(true);
		allSecondaryUsesCriterion.setEnabled(true);
		allPrimaryPotUsesCriterion.setEnabled(true);
		allSecondaryPotUsesCriterion.setEnabled(true);

		updatePane();
	}

	// sliceTool Tool listener
	void sliceTool_actionPerformed(ActionEvent ex) {

		// Saving the list of active test cases...
		activeTestCases = TestSet.getActiveTestCases().toArray();
		int i = 0;
		for (; i < activeTestCases.length; i++) {
			TestSet.desactivateTestCase(JabutiGUI.getProject(),
					(String) activeTestCases[i]);
		}
		if (i > 0)
			getProject().execChanges();

		SelectedPoint.reset();

		allPrimaryNodesCriterion.setSelected(true);

		allSecondaryNodesCriterion.setEnabled(false);
		allPrimaryEdgesCriterion.setEnabled(false);
		allSecondaryEdgesCriterion.setEnabled(false);
		allPrimaryUsesCriterion.setEnabled(false);
		allSecondaryUsesCriterion.setEnabled(false);
		allPrimaryPotUsesCriterion.setEnabled(false);
		allSecondaryPotUsesCriterion.setEnabled(false);

		TestSet.updateOverallCoverage(JabutiGUI.getProject());
		// getProject().updateProjectCoverage();
		updatePane();
	}

	/**
	 * VISUALIZATION MENU This method is responsible to buil the Visualization
	 * Menu and to add each submenu option
	 */
	private void buildVisualizationMenu() {
		// Build the visualization menu
		visualizationMenu.setText("Visualization");
		visualizationMenu.setMnemonic('V');

		// Bytecode
		viewBytecodeFile.setText("Current Bytecode File");
		viewBytecodeFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewBytecodeFile_actionPerformed(e);
			}
		});
		visualizationMenu.add(viewBytecodeFile);

		// Java Source Code
		viewSourceFile.setText("Current Source File");
		viewSourceFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewSourceFile_actionPerformed(e);
			}
		});
		visualizationMenu.add(viewSourceFile);

		// Control-Flow Graph
		/*
		 * viewControlFlowGraph.setText("Control-Flow Graph");
		 * viewControlFlowGraph.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * viewControlFlowGraph_actionPerformed(e); } } );
		 * visualizationMenu.add(viewControlFlowGraph);
		 */

		// Required Elements
		viewRequiredElements.setText("Required Elements");
		viewRequiredElements
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						viewRequiredElements_actionPerformed(e);
					}
				});
		visualizationMenu.add(viewRequiredElements);

		// Def-Use Graph
		viewDefUseGraph.setText("Def-Use Graph");
		viewDefUseGraph.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewDefUseGraph_actionPerformed(e);
			}
		});
		visualizationMenu.add(viewDefUseGraph);

		// Call Graph
		viewCallGraph.setText("Call Graph");
		viewCallGraph.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewCallGraph_actionPerformed(e);
			}
		});
		visualizationMenu.add(viewCallGraph);

		// Complexity Metrics
		viewStaticMetrics.setText("Complexity Metrics");
		viewStaticMetrics
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						viewStaticMetrics_actionPerformed(e);
					}
				});
		visualizationMenu.add(viewStaticMetrics);

		// Nested submenu
		viewShow.setText("Show");

		// The options below enables the way that the
		// information about coverage should be displayed
		allPriorized.setText("All Priorized");
		allPriorized.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				allPriorized_actionPerformed(e);
			}
		});

		viewShowGroup.add(allPriorized);
		viewShow.add(allPriorized);

		highestWeight.setText("Highest Weight");
		highestWeight.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				highestWeight_actionPerformed(e);
			}
		});

		viewShowGroup.add(highestWeight);
		viewShow.add(highestWeight);

		nonZeroWeight.setText("Nonzero Weight");
		nonZeroWeight.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nonZeroWeight_actionPerformed(e);
			}
		});

		viewShowGroup.add(nonZeroWeight);
		viewShow.add(nonZeroWeight);

		zeroWeight.setText("Zero Weight");
		zeroWeight.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zeroWeight_actionPerformed(e);
			}
		});

		viewShowGroup.add(zeroWeight);
		viewShow.add(zeroWeight);

		zeroNonZeroWeight.setText("Zero Nonzero Weight");
		zeroNonZeroWeight
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						zeroNonZeroWeight_actionPerformed(e);
					}
				});

		viewShowGroup.add(zeroNonZeroWeight);
		viewShow.add(zeroNonZeroWeight);

		// Adding the Show Menu to Visualization Menu
		// visualizationMenu.addSeparator();
		// visualizationMenu.add(viewShow);

		// Adding the Visualizatio Menu to MenuBar
		menuBar.add(visualizationMenu);
	}

	/**
	 * VISUALIZATION SUBMENU LISTENERS
	 */

	// Bytecode File listener
	void viewBytecodeFile_actionPerformed(ActionEvent ex) {
		updatePane(JabutiGUI.BYTECODE_PANEL, 0);
	}

	// Source File listener
	void viewSourceFile_actionPerformed(ActionEvent ex) {
		updatePane(JabutiGUI.SOURCE_PANEL, 0);
	}

	// Static Metrics listener
	void viewStaticMetrics_actionPerformed(ActionEvent ex) {
		TableSorter.setSortedColumn(-1); /*
											 * Every time when the table is
											 * called from the menu, uses the
											 * default order
											 */
		updatePane(JabutiGUI.METRICS_PANEL, 0);
	}

	// Control-Flow Graph listener
	/*
	 * void viewControlFlowGraph_actionPerformed(ActionEvent ex) {
	 * 
	 * if (cfgFrame == null) { cfgFrame = new CFGFrame(); } // Updating the
	 * color table WeightColor.clearClassVariablesTable();
	 * WeightColor.updateColorAttributes();
	 * 
	 * cfgFrame.show( TheProject.getCurClassName() );
	 * cfgFrame.setSelectedMethod( TheProject.getCurMethodName() ); }
	 */

	// Def-Use Graph listener
	void viewDefUseGraph_actionPerformed(ActionEvent ex) {
		if (cfgFrame == null) {
			cfgFrame = new CFGFrame();
		}
		// Updating the color table
		WeightColor.clearClassVariablesTable();
		WeightColor.updateColorAttributes();

		cfgFrame.show(getProject().getCurClassName());
		cfgFrame.setSelectedMethod(getProject().getCurMethodName());
	}

	// Required Elements listener
	void viewRequiredElements_actionPerformed(ActionEvent ex) {
		TableSorter.setSortedColumn(-1); /*
											 * Every time when the table is
											 * called from the menu, uses the
											 * default order
											 */
		updatePane(JabutiGUI.REQUIREMENTS_PANEL, 0);
	}

	// Call Graph listener
	void viewCallGraph_actionPerformed(ActionEvent ex) {
	}

	// All Priorized listener
	void allPriorized_actionPerformed(ActionEvent ex) {
	}

	// Highest Weight listener
	void highestWeight_actionPerformed(ActionEvent ex) {
	}

	// Nonzero Weight listener
	void nonZeroWeight_actionPerformed(ActionEvent ex) {
	}

	// Zero Weight listener
	void zeroWeight_actionPerformed(ActionEvent ex) {
	}

	// Zero Nonzero Weight listener
	void zeroNonZeroWeight_actionPerformed(ActionEvent ex) {
	}

	/**
	 * Qualipso menu
	 */
	private void buildQualipsoMenu() {
//		qualipsoMenu.setText("QualiPSo");
//		qualipsoMenu.setMnemonic('Q');
//		
//		viewSpago4qFile.setText("Current Spago4Q file");
//		viewSpago4qFile.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				viewSpago4qFile_actionPerformed(e);
//			}
//		});
//		qualipsoMenu.add(viewSpago4qFile);	
//		
//		menuBar.add(qualipsoMenu);
	}

	void viewSpago4qFile_actionPerformed(ActionEvent ex) {
		spago4qDialog = new Spago4qXmlDialog();
		spago4qDialog.generateXML(jbtProject);
		spago4qDialog.setVisible(true); // pop up dialog
	}	
	
	/**
	 * Summary MENU This method is responsible to buil the Report Menu and to
	 * add each submenu option
	 */	
	private void buildSummaryMenu() {
		// Build the Summary menu
		// Summary Menu
		coverageMenu.setText("Summary");
		coverageMenu.setMnemonic('C');

		byType.setText("By Criterion");
		byType.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byType_actionPerformed(e);
			}
		});

		coverageGroup.add(byType);

		byFile.setText("By Class");
		byFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byFile_actionPerformed(e);
			}
		});
		coverageGroup.add(byFile);

		byMethod.setText("By Method");
		byMethod.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byMethod_actionPerformed(e);
			}
		});
		coverageGroup.add(byMethod);

		coverageMenu.add(byType);
		coverageMenu.add(byFile);
		coverageMenu.add(byMethod);

		// Adding Summary Menu to MenuBar
		menuBar.add(coverageMenu);

	}

	/**
	 * REPORT SUBMENU LISTENERS
	 */

	// By Type Listener
	void byType_actionPerformed(ActionEvent ex) {
		TableSorter.setSortedColumn(-1); /*
											 * Every time when the table is
											 * called from the menu, uses the
											 * default order
											 */

		byType.setSelected(true);
		byFile.setSelected(false);
		byMethod.setSelected(false);
		updatePane(JabutiGUI.TYPE_PANEL, 0);
	}

	private void updateByTypePanel() {
		String[] columns = { "Testing Criterion", "Coverage", "Percentage" };

		Object[][] rows = new Object[Criterion.NUM_CRITERIA][columns.length];

		typePanel = new JPanel();
		titlePanelLabel = "Overall Coverage Summary by Criterion";
		titlePanel = new TitlePanel(titlePanelLabel);

		for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
			Coverage cov = getProject().getProjectCoverage(i);

			rows[i][0] = JTableComponentModel.addButton(AbstractCriterion
					.getName(i));

			rows[i][1] = JTableComponentModel.addLabel(cov.getNumberOfCovered()
					+ " of " + cov.getNumberOfRequirements());
			rows[i][2] = JTableComponentModel.addProgress((int) cov
					.getPercentage());
		}

		tablePanel = new TableSorterPanel(rows, columns);

		String specificTool = new String("");

		if (coverageTool.isSelected()) {
			specificTool = new String("Coverage");
		} else {
			specificTool = new String("Slice");
		}

		syntesePanel = new SyntesePanel(ToolConstants.toolName + ": "
				+ specificTool, "Bytecode Files: "
				+ getProject().getClassFilesTable().size() + " of "
				+ getProject().getClassFilesTable().size(),
				"Active Test Cases: "
						+ TestSet.getNumberOfActiveTestCases()
						+ " of "
						+ (TestSet.getNumberOfTestCases() - TestSet
								.getNumberOfDeletedTestCases()));

		typePanel.setLayout(new BorderLayout());

		typePanel.add(titlePanel, BorderLayout.NORTH);

		// JScrollPane scrollTable = new JScrollPane(tablePanel);

		// scrollTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// typePanel.add(scrollTable, BorderLayout.CENTER);
		typePanel.add(tablePanel, BorderLayout.CENTER);
		typePanel.add(syntesePanel, BorderLayout.SOUTH);
	}

	// By File Listener
	void byFile_actionPerformed(ActionEvent ex) {

		TableSorter.setSortedColumn(-1); /*
											 * Every time when the table is
											 * called from the menu, uses the
											 * default order
											 */

		byType.setSelected(false);
		byFile.setSelected(true);
		byMethod.setSelected(false);

		updatePane(JabutiGUI.FILE_PANEL, 0);
	}

	/*
	 * This method is responsible to update the information of the by class
	 * file. It is called every time when such panel is shown in the GUI or when
	 * it is currently open and the Update button is clicked.
	 */
	private void updateByFilePanel() {
		Hashtable allClasses = getProject().getClassFilesTable();
		int numberOfClasses = getProject().getNumberOfClasses();

		filePanel = new JPanel();

		// Rows and Columns of the Table...
		String[] columns = { "Class File Names", "Coverage", "Percentage" };

		Object[][] rows = new Object[numberOfClasses][columns.length];

		String criterion = null;
		int criterionNumber = -1;

		int i = 0;
		int sunCov = 0;
		int sunTotal = 0;
		int mostCov = 0;

		if (JabutiGUI.isAllPrimaryNodes()) {
			criterionNumber = Criterion.PRIMARY_NODES;
		} else if (JabutiGUI.isAllSecondaryNodes()) {
			criterionNumber = Criterion.SECONDARY_NODES;
		} else if (JabutiGUI.isAllPrimaryEdges()) {
			criterionNumber = Criterion.PRIMARY_EDGES;
		} else if (JabutiGUI.isAllSecondaryEdges()) {
			criterionNumber = Criterion.SECONDARY_EDGES;
		} else if (JabutiGUI.isAllPrimaryUses()) {
			criterionNumber = Criterion.PRIMARY_USES;
		} else if (JabutiGUI.isAllSecondaryUses()) {
			criterionNumber = Criterion.SECONDARY_USES;
		} else if (JabutiGUI.isAllPrimaryPotUses()) {
			criterionNumber = Criterion.PRIMARY_POT_USES;
		} else if (JabutiGUI.isAllSecondaryPotUses()) {
			criterionNumber = Criterion.SECONDARY_POT_USES;
		}

		criterion = AbstractCriterion.getName(criterionNumber);
		titlePanelLabel = criterion + " Coverage per Class File";
		titlePanel = new TitlePanel(titlePanelLabel);

		Object[] classNames = allClasses.keySet().toArray();

		Arrays.sort(classNames);

		for (int k = 0; k < classNames.length; k++) {
			String cName = (String) classNames[k];
			ClassFile cf = (ClassFile) allClasses.get(cName);

			rows[i][0] = JTableComponentModel.addButton(cName);

			Coverage mCov = cf.getClassFileCoverage(criterionNumber);

			int cov = mCov.getNumberOfCovered();

			sunCov += cov;

			int total = mCov.getNumberOfRequirements();

			sunTotal += total;

			if (total > mostCov) {
				mostCov = total;
			}

			rows[i][1] = mCov;
			i++;
		}

		for (int k = 0; k < i; k++) {
			Coverage mCov = (Coverage) rows[k][1];

			rows[k][1] = JTableComponentModel.addLabel(mCov.toString());

			int width = mCov.getNumberOfRequirements();
			int maxWidth = mostCov;
			rows[k][2] = JTableComponentModel.addProgress((int) mCov
					.getPercentage(), width, maxWidth);
		}

		tablePanel = new TableSorterPanel(rows, columns);

		String specificTool = new String("");

		if (coverageTool.isSelected()) {
			specificTool = new String("Coverage");
		} else {
			specificTool = new String("Slice");
		}

		syntesePanel = new SyntesePanel(ToolConstants.toolName + ": "
				+ specificTool, criterion + " Covered: " + sunCov + " of "
				+ sunTotal, "Active Test Cases: "
				+ TestSet.getNumberOfActiveTestCases()
				+ " of "
				+ (TestSet.getNumberOfTestCases() - TestSet
						.getNumberOfDeletedTestCases()));

		filePanel.setLayout(new BorderLayout());

		filePanel.add(titlePanel, BorderLayout.NORTH);

		// JScrollPane scrollTable = new JScrollPane(tablePanel);

		// scrollTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// filePanel.add(scrollTable, BorderLayout.CENTER);
		filePanel.add(tablePanel, BorderLayout.CENTER);
		filePanel.add(syntesePanel, BorderLayout.SOUTH);
	}

	// By Process Listener
	void byMethod_actionPerformed(ActionEvent ex) {

		TableSorter.setSortedColumn(-1); /*
											 * Every time when the table is
											 * called from the menu, uses the
											 * default order
											 */

		byType.setSelected(false);
		byFile.setSelected(false);
		byMethod.setSelected(true);

		updatePane(JabutiGUI.METHOD_PANEL, 0);
	}

	/*
	 * This method is responsible to update the information of the by method
	 * panel. It is called every time when such panel is shown in the GUI or
	 * when it is currently open and the Update button is clicked.
	 */
	private void updateByMethodPanel() {
		Hashtable allClasses = getProject().getClassFilesTable();
		Iterator it = allClasses.values().iterator();
		int numberOfMethods = 0;

		while (it.hasNext()) {
			ClassFile cl = (ClassFile) it.next();

			numberOfMethods = numberOfMethods + cl.getNumberOfMethods();
		}

		// Rows and Columns of the Table...
		String[] columns = { "Method Names", "Coverage", "Percentage" };

		Object[][] rows = new Object[numberOfMethods][columns.length];

		// The panel being updated
		methodPanel = new JPanel();

		String criterion = null;
		int criterionNumber = -1;

		int i = 0;
		int sunCov = 0;
		int sunTotal = 0;
		int mostCov = 0;

		if (JabutiGUI.isAllPrimaryNodes()) {
			criterionNumber = Criterion.PRIMARY_NODES;
		} else if (JabutiGUI.isAllSecondaryNodes()) {
			criterionNumber = Criterion.SECONDARY_NODES;
		} else if (JabutiGUI.isAllPrimaryEdges()) {
			criterionNumber = Criterion.PRIMARY_EDGES;
		} else if (JabutiGUI.isAllSecondaryEdges()) {
			criterionNumber = Criterion.SECONDARY_EDGES;
		} else if (JabutiGUI.isAllPrimaryUses()) {
			criterionNumber = Criterion.PRIMARY_USES;
		} else if (JabutiGUI.isAllSecondaryUses()) {
			criterionNumber = Criterion.SECONDARY_USES;
		} else if (JabutiGUI.isAllPrimaryPotUses()) {
			criterionNumber = Criterion.PRIMARY_POT_USES;
		} else if (JabutiGUI.isAllSecondaryPotUses()) {
			criterionNumber = Criterion.SECONDARY_POT_USES;
		}

		criterion = AbstractCriterion.getName(criterionNumber);
		titlePanelLabel = criterion + " Coverage per Method";
		titlePanel = new TitlePanel(titlePanelLabel);

		Object[] classNames = allClasses.keySet().toArray();

		Arrays.sort(classNames);

		for (int k = 0; k < classNames.length; k++) {
			String cName = (String) classNames[k];
			ClassFile cl = (ClassFile) allClasses.get(cName);

			HashMap methods = cl.getMethodsTable();

			Object[] names = methods.keySet().toArray(new String[0]);

			Arrays.sort(names);

			for (int x = 0; x < names.length; x++) {
				String mName = (String) names[x];
				ClassMethod cm = (ClassMethod) methods.get(mName);

				rows[i][0] = JTableComponentModel.addButton(cName
						+ JabutiGUI.CLASS_METHOD_SEPARATOR + mName);

				Coverage mCov = cm.getClassMethodCoverage(criterionNumber);

				int cov = mCov.getNumberOfCovered();

				sunCov += cov;

				int total = mCov.getNumberOfRequirements();

				sunTotal += total;

				if (total > mostCov) {
					mostCov = total;
				}

				rows[i][1] = mCov;
				i++;
			}
		}

		for (int k = 0; k < i; k++) {
			Coverage mCov = (Coverage) rows[k][1];
			rows[k][1] = JTableComponentModel.addLabel(mCov.toString());

			int width = mCov.getNumberOfRequirements();
			int maxWidth = mostCov;
			rows[k][2] = JTableComponentModel.addProgress((int) mCov
					.getPercentage(), width, maxWidth);
		}

		tablePanel = new TableSorterPanel(rows, columns);

		String specificTool = new String("");

		if (coverageTool.isSelected()) {
			specificTool = new String("Coverage");
		} else {
			specificTool = new String("Slice");
		}

		syntesePanel = new SyntesePanel(ToolConstants.toolName + ": "
				+ specificTool, criterion + " Covered: " + sunCov + " of "
				+ sunTotal, "Active Test Cases: "
				+ TestSet.getNumberOfActiveTestCases()
				+ " of "
				+ (TestSet.getNumberOfTestCases() - TestSet
						.getNumberOfDeletedTestCases()));

		methodPanel.setLayout(new BorderLayout());

		methodPanel.add(titlePanel, BorderLayout.NORTH);

		// JScrollPane scrollTable = new JScrollPane(tablePanel);

		// scrollTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// methodPanel.add(scrollTable, BorderLayout.CENTER);
		methodPanel.add(tablePanel, BorderLayout.CENTER);
		methodPanel.add(syntesePanel, BorderLayout.SOUTH);
	}

	/**
	 * Criteria Tool Bar This method is responsible to buil the Criteria Tool
	 * Bar and to add each submenu option
	 */

	private void buildToolBar() {
		// All-Primary-Nodes Criterion
		allPrimaryNodesCriterion.setText(AbstractCriterion
				.getName(Criterion.PRIMARY_NODES));
		allPrimaryNodesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.PRIMARY_NODES));
		allPrimaryNodesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allNodesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allPrimaryNodesCriterion);

		// All-Secondary-Nodes Criterion
		allSecondaryNodesCriterion.setText(AbstractCriterion
				.getName(Criterion.SECONDARY_NODES));
		allSecondaryNodesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.SECONDARY_NODES));
		allSecondaryNodesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allNodesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allSecondaryNodesCriterion);

		// Decision Criterion ( considering only the primary edges )
		allPrimaryEdgesCriterion.setText(AbstractCriterion
				.getName(Criterion.PRIMARY_EDGES));
		allPrimaryEdgesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.PRIMARY_EDGES));
		allPrimaryEdgesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allPrimaryEdgesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allPrimaryEdgesCriterion);

		// Exception Edges Criterion ( considering only the secondary edges )
		allSecondaryEdgesCriterion.setText(AbstractCriterion
				.getName(Criterion.SECONDARY_EDGES));
		allSecondaryEdgesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.SECONDARY_EDGES));
		allSecondaryEdgesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allSecondaryEdgesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allSecondaryEdgesCriterion);

		// Primary All-Uses Criterion ( considering only the uses associated
		// with
		// the primary edges )
		allPrimaryUsesCriterion.setText(AbstractCriterion
				.getName(Criterion.PRIMARY_USES));
		allPrimaryUsesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.PRIMARY_USES));
		allPrimaryUsesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allPrimaryUsesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allPrimaryUsesCriterion);

		// Exception All-Uses Criterion ( considering only the uses associated
		// with
		// the secondary edges )
		allSecondaryUsesCriterion.setText(AbstractCriterion
				.getName(Criterion.SECONDARY_USES));
		allSecondaryUsesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.SECONDARY_USES));
		allSecondaryUsesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allSecondaryUsesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allSecondaryUsesCriterion);

		// Primary All-Potential-Uses Criterion ( considering only the uses
		// associated with the primary edges )
		allPrimaryPotUsesCriterion.setText(AbstractCriterion
				.getName(Criterion.PRIMARY_POT_USES));
		allPrimaryPotUsesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.PRIMARY_POT_USES));
		allPrimaryPotUsesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allPrimaryPotUsesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allPrimaryPotUsesCriterion);

		// Exception All-Potential-Uses Criterion ( considering only the uses
		// associated with the secondary edges )
		allSecondaryPotUsesCriterion.setText(AbstractCriterion
				.getName(Criterion.SECONDARY_POT_USES));
		allSecondaryPotUsesCriterion.setToolTipText(AbstractCriterion
				.getDescription(Criterion.SECONDARY_POT_USES));
		allSecondaryPotUsesCriterion
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						allSecondaryPotUsesCriterion_actionPerformed(e);
					}
				});

		typeGroup.add(allSecondaryPotUsesCriterion);

		// Testing criteria radio buttons order
		toolBar.addSeparator();

		toolBar.add(allPrimaryNodesCriterion);
		toolBar.add(allPrimaryEdgesCriterion);
		toolBar.add(allPrimaryUsesCriterion);
		toolBar.add(allPrimaryPotUsesCriterion);

		toolBar.add(allSecondaryNodesCriterion);
		toolBar.add(allSecondaryEdgesCriterion);
		toolBar.add(allSecondaryUsesCriterion);
		toolBar.add(allSecondaryPotUsesCriterion);
	}

	/**
	 * CRITERIA TOOL BAR LISTENERS
	 */

	// Block Criterion Listener
	void allNodesCriterion_actionPerformed(ActionEvent ex) {
		SelectedPoint.reset();

		if (cfgIsVisible()) {
			cfgFrame.repaint();
		}

		updatePane();
	}

	// Decision Criterion Listener
	public void allPrimaryEdgesCriterion_actionPerformed(ActionEvent ex) {
		SelectedPoint.reset();
		if (cfgIsVisible()) {
			cfgFrame.repaint();
		}

		updatePane();
	}

	// Exception Edges Criterion Listener
	public void allSecondaryEdgesCriterion_actionPerformed(ActionEvent ex) {
		SelectedPoint.reset();
		if (cfgIsVisible()) {
			cfgFrame.repaint();
		}

		updatePane();
	}

	// Primary All-Uses Criterion Listener
	public void allPrimaryUsesCriterion_actionPerformed(ActionEvent ex) {
		SelectedPoint.reset();

		if (cfgIsVisible()) {
			cfgFrame.repaint();
		}

		updatePane();
	}

	// Exception All-Uses Criterion Listener
	public void allSecondaryUsesCriterion_actionPerformed(ActionEvent ex) {
		SelectedPoint.reset();

		if (cfgIsVisible()) {
			cfgFrame.repaint();
		}

		updatePane();
	}

	// Primary All-Potential-Uses Criterion Listener
	public void allPrimaryPotUsesCriterion_actionPerformed(ActionEvent ex) {
		SelectedPoint.reset();

		if (cfgIsVisible()) {
			cfgFrame.repaint();
		}

		updatePane();
	}

	// Exception All-Potential-Uses Criterion Listener
	public void allSecondaryPotUsesCriterion_actionPerformed(ActionEvent ex) {
		SelectedPoint.reset();

		if (cfgIsVisible()) {
			cfgFrame.repaint();
		}

		updatePane();
	}

	/**
	 * TEST CASE BUTTON This method is responsible to build the Test Case Button
	 * and to add it to the Menu Bar
	 */

	private void buildTestCaseMenu() {
		// Build the Test Case Menu

		testCaseMenu.setText("Test Case");

		testCaseView.setText("Report By Test Case");

		testCaseView.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testCaseView_actionPerformed(e);
			}
		});

		testCaseMenu.add(testCaseView);

		/*
		 * testCaseByPathView.setText("Report By Test Case Path");
		 * 
		 * testCaseByPathView.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * testCaseByPathView_actionPerformed(e); } } );
		 * 
		 * testCaseMenu.add( testCaseByPathView );
		 */

		testCaseMenu.addSeparator();

		testCaseExecutor.setText("Executing JUnit Test Set...");

		testCaseExecutor.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testCaseExecutor_actionPerformed(e);
			}
		});

		testCaseMenu.add(testCaseExecutor);

		// Adding the Test Case Button to the Menu Bar
		menuBar.add(testCaseMenu);

	}

	// reportTestCase action performed
	void testCaseView_actionPerformed(ActionEvent ex) {
		TableSorter.setSortedColumn(-1); /*
											 * Every time when the table is
											 * called from the menu, uses the
											 * default order
											 */

		updatePane(JabutiGUI.TESTCASE_PANEL, 0);
	}

	// reportTestCaseByPath action performed
	void testCaseByPathView_actionPerformed(ActionEvent ex) {
		TableSorter.setSortedColumn(-1); /*
											 * Every time when the table is
											 * called from the menu, uses the
											 * default order
											 */

		updatePane(JabutiGUI.PATHS_PANEL, 0);
	}

	// reportTestCase action performed
	void testCaseExecutor_actionPerformed(ActionEvent ex) {
		String app = JabutiGUI.getProject().getInstrumentedJarFileName();
		File appF = new File(app);
		if (!appF.exists()) {
			int op = JOptionPane
					.showConfirmDialog(
							JabutiGUI.mainWindow(),
							"There is no instrumented project jar file. Do you want to generate it?",
							"Warning", JOptionPane.YES_NO_OPTION);
			if (op == JOptionPane.YES_OPTION) {
				this.saveInst_actionPerformed(ex);
			} else {
				JOptionPane
						.showMessageDialog(
								JabutiGUI.mainWindow(),
								"Execution trace information can only be generated when test cases are executed against instrumented classes.",
								"Information", JOptionPane.INFORMATION_MESSAGE);
				app = JabutiGUI.getProject().getBaseClass();
				try {
					app = JabutiGUI.getProject().getProjectResource(app);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		final Main executor = new br.jabuti.junitexec.Main(JabutiGUI
				.mainWindow(), true);
		executor.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				JabutiGUI.getProject().setJunitSrcDir(executor.getTCSource());
				JabutiGUI.getProject().setJunitBinDir(executor.getTCBin());
				JabutiGUI.getProject().setJunitTestSet(executor.getTCClass());
				JabutiGUI.getProject().setJUnitJar(executor.getJabutiLib());
				JabutiGUI.getProject().execChanges();
			}
		});

		executor.setApp(app);
		executor.setTCSource(JabutiGUI.getProject().getJunitSrcDir());
		executor.setTCBin(JabutiGUI.getProject().getJunitBinDir());
		executor.setTCClass(JabutiGUI.getProject().getJunitTestSet());
		executor.setJabutiLib(JabutiGUI.getProject().getJUnitJar());
		executor.setOtherLibs("");
		executor.setTrace(JabutiGUI.getProject().getTraceFileName());

		executor.pack();
		executor.setVisible(true);
	}

	/*
	 * This method is responsible to update the information of the test case
	 * panel when the coverage tool is activated. It is called every time when
	 * such panel is shown in the GUI or when it is currently open and the
	 * Update button is clicked.
	 */
	private void updateCoverageTestCasePanel() {
		String criterion = null;

		int numTC = TestSet.getNumberOfTestCases();

		testCasePanel = new JPanel();

		if (numTC > 0) {
			String[] columns = { "Active", "Delete", "Test Case", "JUnit Name",
					"Host", "Total Coverage", "Percentage" };

			Object[][] rows = new Object[numTC][columns.length];

			int criterionNumber = -1;

			if (JabutiGUI.isAllPrimaryNodes()) {
				criterionNumber = Criterion.PRIMARY_NODES;
			} else if (JabutiGUI.isAllSecondaryNodes()) {
				criterionNumber = Criterion.SECONDARY_NODES;
			} else if (JabutiGUI.isAllPrimaryEdges()) {
				criterionNumber = Criterion.PRIMARY_EDGES;
			} else if (JabutiGUI.isAllSecondaryEdges()) {
				criterionNumber = Criterion.SECONDARY_EDGES;
			} else if (JabutiGUI.isAllPrimaryUses()) {
				criterionNumber = Criterion.PRIMARY_USES;
			} else if (JabutiGUI.isAllSecondaryUses()) {
				criterionNumber = Criterion.SECONDARY_USES;
			} else if (JabutiGUI.isAllPrimaryPotUses()) {
				criterionNumber = Criterion.PRIMARY_POT_USES;
			} else if (JabutiGUI.isAllSecondaryPotUses()) {
				criterionNumber = Criterion.SECONDARY_POT_USES;
			}

			criterion = AbstractCriterion.getName(criterionNumber);
			titlePanelLabel = criterion + " Coverage per Test Case";
			titlePanel = new TitlePanel(titlePanelLabel);

			Object[] names = TestSet.getTestCaseLabels();

			Arrays.sort(names);

			for (int i = 0; i < names.length; i++) {

				boolean active = false;
				// build the check box table
				if (TestSet.isActive((String) names[i])) {
					active = true;
				}
				rows[i][0] = JTableComponentModel.addCheckBox(active);

				boolean delete = false;
				// build the check box table
				if (TestSet.isDeleted((String) names[i])) {
					delete = true;
				}
				rows[i][1] = JTableComponentModel.addCheckBox(delete);

				rows[i][2] = JTableComponentModel.addButton((String) names[i]);

				TestCase tc = TestSet.getTestCase((String) names[i]);

				rows[i][3] = JTableComponentModel.addLabel(tc.getAlias()
						.length() == 0 ? "ProberLoader " + names[i] : tc
						.getAlias());

				// Getting the host name
				String hostName = (tc.getHostName() != null) ? tc.getHostName()
						: "localhost";

				rows[i][4] = JTableComponentModel.addLabel(hostName);

				// Calculating the coverage
				Coverage cov = tc.getTestCaseCoverage(criterionNumber);

				rows[i][5] = JTableComponentModel.addLabel(cov
						.getNumberOfCovered()
						+ " of " + cov.getNumberOfRequirements());

				rows[i][6] = JTableComponentModel.addProgress((int) cov
						.getPercentage());
			}

			Coverage cov = getProject().getProjectCoverage(criterionNumber);

			tablePanel = new TableSorterPanel(rows, columns);

			TotalPanel totalPanel = new TotalPanel(" TOTAL COVERAGE", cov
					.getNumberOfCovered()
					+ " of " + cov.getNumberOfRequirements(), (int) cov
					.getPercentage(), 10, 10);

			String specificTool = new String("");

			if (coverageTool.isSelected()) {
				specificTool = new String("Coverage");
			} else {
				specificTool = new String("Slice");
			}

			syntesePanel = new SyntesePanel(ToolConstants.toolName + ": "
					+ specificTool, " Coverage: " + criterion,
					"Active Test Cases: "
							+ TestSet.getNumberOfActiveTestCases()
							+ " of "
							+ (TestSet.getNumberOfTestCases() - TestSet
									.getNumberOfDeletedTestCases()));

			southPanel = new JPanel();

			southPanel.setLayout(new BorderLayout());
			southPanel.add(totalPanel, BorderLayout.NORTH);
			southPanel.add(syntesePanel, BorderLayout.SOUTH);

			testCasePanel.setLayout(new BorderLayout());

			testCasePanel.add(titlePanel, BorderLayout.NORTH);

			// JScrollPane scrollTable = new JScrollPane(tablePanel);

			// scrollTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			// testCasePanel.add(scrollTable, BorderLayout.CENTER);
			middlePanel = new JPanel();
			middlePanel.setLayout(new BorderLayout());
			middlePanel.add(coverageButtonsPanel, BorderLayout.NORTH);
			middlePanel.add(tablePanel, BorderLayout.CENTER);

			testCasePanel.add(middlePanel, BorderLayout.CENTER);
			testCasePanel.add(southPanel, BorderLayout.SOUTH);
		} else {
			JOptionPane.showMessageDialog(null, "No Test Case to be shown!",
					"Message", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// Activate All testing cases of
	void tcActivateButton_actionPerformed(ActionEvent ex) {
		String[] tcNames = TestSet.getTestCaseLabels();

		for (int i = 0; i < tcNames.length; i++) {
			TestSet.activateTestCase(JabutiGUI.getProject(), tcNames[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	// Deactivate All testing cases of
	void tcDeactivateButton_actionPerformed(ActionEvent ex) {
		String[] tcNames = TestSet.getTestCaseLabels();

		for (int i = 0; i < tcNames.length; i++) {
			TestSet.desactivateTestCase(JabutiGUI.getProject(), tcNames[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	// Delete all testing cases of
	void tcDeleteButton_actionPerformed(ActionEvent ex) {
		String[] tcNames = TestSet.getTestCaseLabels();

		for (int i = 0; i < tcNames.length; i++) {
			TestSet.toDeleteTestCase(JabutiGUI.getProject(), tcNames[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	// Undelete all testing cases of
	void tcUndeleteButton_actionPerformed(ActionEvent ex) {
		String[] tcNames = TestSet.getTestCaseLabels();

		for (int i = 0; i < tcNames.length; i++) {
			TestSet.undeleteTestCase(JabutiGUI.getProject(), tcNames[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	/*
	 * This method is responsible to update the information of the test case
	 * panel when the slice tool is activated. It is called every time when such
	 * panel is shown in the GUI or when it is currently open and the Update
	 * button is clicked.
	 */
	private void updateSliceTestCasePanel() {
		String criterion = null;

		int numTC = TestSet.getNumberOfTestCases();

		testCasePanel = new JPanel();

		if (numTC > 0) {
			String[] columns = { "Success", "Fail", "Test Case", "JUnit Name",
					"Host", "Total Coverage", "Percentage" };

			Object[][] rows = new Object[numTC][columns.length];

			// Slice tool uses the block criterion always...
			int criterionNumber = -1;

			if (JabutiGUI.isAllPrimaryNodes()) {
				criterionNumber = Criterion.PRIMARY_NODES;
				criterion = AbstractCriterion.getName(criterionNumber);
			}
			titlePanelLabel = criterion + " Coverage per Test Case";
			titlePanel = new TitlePanel(titlePanelLabel);

			Object[] names = TestSet.getTestCaseLabels();

			Arrays.sort(names);

			for (int i = 0; i < names.length; i++) {
				boolean isSuccess = false, isFail = false;

				// Sucess test case selected
				if (TestSet.getSuccessSet().contains((String) names[i]))
					isSuccess = true;
				else
					isSuccess = false;
				rows[i][0] = JTableComponentModel.addCheckBox(isSuccess);

				// Fail test set selected
				if (TestSet.getFailSet().contains((String) names[i]))
					isFail = true;
				else
					isFail = false;
				rows[i][1] = JTableComponentModel.addCheckBox(isFail);

				// Test case name
				rows[i][2] = JTableComponentModel.addButton((String) names[i]);

				TestCase tc = TestSet.getTestCase((String) names[i]);

				rows[i][3] = JTableComponentModel.addLabel(tc.getAlias()
						.length() == 0 ? "ProberLoader " + names[i] : tc
						.getAlias());

				// Getting the host name
				String hostName = (tc.getHostName() != null) ? tc.getHostName()
						: "localhost";

				Coverage cov = tc.getTestCaseCoverage(criterionNumber);

				rows[i][4] = JTableComponentModel.addLabel(hostName);

				// Test case coverage
				rows[i][5] = JTableComponentModel.addLabel(cov
						.getNumberOfCovered()
						+ " of " + cov.getNumberOfRequirements());

				// Test case percentage
				rows[i][6] = JTableComponentModel.addProgress((int) cov
						.getPercentage());
			}

			Coverage cov = getProject().getProjectCoverage(criterionNumber);

			tablePanel = new TableSorterPanel(rows, columns);

			TotalPanel totalPanel = new TotalPanel(" TOTAL COVERAGE", cov
					.getNumberOfCovered()
					+ " of " + cov.getNumberOfRequirements(), (int) cov
					.getPercentage(), 10, 10);

			String specificTool = new String("");

			if (coverageTool.isSelected()) {
				specificTool = new String("Coverage");
			} else {
				specificTool = new String("Slice");
			}

			syntesePanel = new SyntesePanel(ToolConstants.toolName + ": "
					+ specificTool, " Coverage: " + criterion,
					"Active Test Cases: "
							+ TestSet.getNumberOfActiveTestCases()
							+ " of "
							+ (TestSet.getNumberOfTestCases() - TestSet
									.getNumberOfDeletedTestCases()));

			southPanel = new JPanel();

			southPanel.setLayout(new BorderLayout());
			southPanel.add(totalPanel, BorderLayout.NORTH);
			southPanel.add(syntesePanel, BorderLayout.SOUTH);

			testCasePanel.setLayout(new BorderLayout());

			testCasePanel.add(titlePanel, BorderLayout.NORTH);

			/*
			 * middlePanel = new JPanel(); middlePanel.setLayout( new
			 * BorderLayout() ); middlePanel.add( sliceButtonsPanel,
			 * BorderLayout.NORTH ); middlePanel.add( tablePanel,
			 * BorderLayout.CENTER );
			 */

			testCasePanel.add(tablePanel, BorderLayout.CENTER);
			testCasePanel.add(southPanel, BorderLayout.SOUTH);
		} else {
			JOptionPane.showMessageDialog(null, "No Test Case to be shown!",
					"Message", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/*
	 * This method is responsible to colect the data used to generate the
	 * complexity metric panel.
	 */
	private void updateMetricsPanel() {

		metricsPanel = new JPanel();

		if (getProject() != null) {
			titlePanelLabel = "Static Metrics per Class";
			titlePanel = new TitlePanel(titlePanelLabel);

			Program prog = getProject().getProgram();
			String[] classes = getProject().getAllClassFileNames();

			Object[][] rows;
			String[] columns;

			// Calculating the metrics
			// Metrics mt = new Metrics( prog, classes );
			if (mt == null)
				mt = new Metrics(prog);

			columns = new String[Metrics.metrics.length + 1];
			columns[0] = new String("Class File Name");
			for (int i = 0; i < Metrics.metrics.length; i++) {
				columns[i + 1] = Metrics.metrics[i][0].toUpperCase();
			}

			// String[] classNames = classes;
			String[] classNames = prog.getCodeClasses();

			rows = new Object[classNames.length][Metrics.metrics.length + 1];
			for (int i = 0; i < classNames.length; i++) {
				// Checks if is an instrumented class
				if (getProject().getClassFile(classNames[i]) != null) {
					rows[i][0] = JTableComponentModel.addButton(classNames[i]);
				} else {
					rows[i][0] = JTableComponentModel.addButton("{"
							+ classNames[i] + "}");
				}

				Object[] metrics = mt.getClassMetrics(classNames[i]);

				for (int j = 0; j < metrics.length; j++) {
					rows[i][j + 1] = metrics[j];
				}
			}

			// tablePanel = new MetricsPanel( prog, classes );
			tablePanel = new TableSorterPanel(rows, columns, mt,
					JTable.AUTO_RESIZE_OFF);

			String specificTool = new String("");

			if (coverageTool.isSelected()) {
				specificTool = new String("Coverage");
			} else {
				specificTool = new String("Slice");
			}

			syntesePanel = new SyntesePanel(ToolConstants.toolName + ": "
					+ specificTool, "Number of Metrics: "
					+ Metrics.metrics.length, "Number of Classes: "
					+ classes.length);

			metricsPanel.setLayout(new BorderLayout());

			metricsPanel.add(titlePanel, BorderLayout.NORTH);

			metricsPanel.add(tablePanel, BorderLayout.CENTER);
			metricsPanel.add(syntesePanel, BorderLayout.SOUTH);
		} else {
			JOptionPane.showMessageDialog(null,
					"No Project open to calculate the static metrics!!!",
					"Message", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/*
	 * This method is responsible to update the information of the requirements
	 * panel. It shows the testing requirements of the current class name and
	 * method name, obtained from the static class {@link TheProject}.
	 */
	private void updateRequirementsPanel() {
		comboPanel = new JPanel();

		JabutiProject prj = getProject();
		String[] classes = prj.getAllClassFileNames();

		requirementsPanel = new JPanel();

		// Monta um choice com o nome de todas as classes
		if (classes != null) {
			comboPanel.setLayout(new BorderLayout());

			// Choice com o nome de todas as classes
			// de todos os m�todos
			classCombo = new JComboBox(classes);
			classCombo.setSelectedItem(getProject().getCurClassName());

			classCombo.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					classCombo_itemSelected(e);
				}
			});

			ClassFile cf = prj.getClassFile(getProject().getCurClassName());
			String[] methods = cf.getAllMethodsNames();

			// Choice com o nome de todos os m�todos da classe selecionada
			if (methods != null) {
				methodCombo = new JComboBox(methods);
				methodCombo.setSelectedItem(getProject().getCurMethodName());
			} else
				methodCombo = new JComboBox();

			methodCombo.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					methodCombo_itemSelected(e);
				}
			});

			comboPanel.add(classCombo, BorderLayout.NORTH);
			comboPanel.add(methodCombo, BorderLayout.CENTER);
			comboPanel.add(buttonsPanel, BorderLayout.SOUTH);

			// Getting the information about which testing criterion
			// is selected
			String criterionName = new String();
			int criterionNumber = -1;

			// Identifying the active testing criterion
			if (JabutiGUI.isAllPrimaryNodes()) {
				criterionNumber = Criterion.PRIMARY_NODES;
			} else if (JabutiGUI.isAllSecondaryNodes()) {
				criterionNumber = Criterion.SECONDARY_NODES;
			} else if (JabutiGUI.isAllPrimaryEdges()) {
				criterionNumber = Criterion.PRIMARY_EDGES;
			} else if (JabutiGUI.isAllSecondaryEdges()) {
				criterionNumber = Criterion.SECONDARY_EDGES;
			} else if (JabutiGUI.isAllPrimaryUses()) {
				criterionNumber = Criterion.PRIMARY_USES;
			} else if (JabutiGUI.isAllSecondaryUses()) {
				criterionNumber = Criterion.SECONDARY_USES;
			} else if (JabutiGUI.isAllPrimaryPotUses()) {
				criterionNumber = Criterion.PRIMARY_POT_USES;
			} else if (JabutiGUI.isAllSecondaryPotUses()) {
				criterionNumber = Criterion.SECONDARY_POT_USES;
			}

			criterionName = AbstractCriterion.getName(criterionNumber);
			titlePanelLabel = criterionName + " Testing Requirements";
			titlePanel = new TitlePanel(titlePanelLabel);

			ClassMethod cm = cf.getMethod(getProject().getCurMethodName());
			Criterion criterion = cm.getCriterion(criterionNumber);

			Object[] requirements = criterion.getRequirements();
			HashSet covered = criterion.getCoveredRequirements();
			HashSet inactive = criterion.getInactiveRequirements();
			HashSet infeasible = criterion.getInfeasibleRequirements();

			Object[][] rows;
			String[] columns = { "Covered", "Active", "Infeasible",
					"Testing Requirement" };
			;

			// Showing the testing requirements in a JTable
			if (requirements != null) {
				rows = new Object[requirements.length][columns.length];

				for (int i = 0; i < requirements.length; i++) {
					Object req = requirements[i];

					// Evaluating if the requirement is covered
					boolean value = false;
					if (covered.contains(req))
						value = true;

					rows[i][0] = JTableComponentModel.addCheckBox(value);

					// Evaluating if the requirement is active
					value = true;
					if (inactive.contains(req))
						value = false;

					rows[i][1] = JTableComponentModel.addCheckBox(value);

					// Evaluating if the requirement is infeasible
					value = false;
					if (infeasible.contains(req))
						value = true;

					rows[i][2] = JTableComponentModel.addCheckBox(value);

					// Testing requirement
					rows[i][3] = JTableComponentModel.addButton(req.toString());
				}

				tablePanel = new TableSorterPanel(rows, columns, criterion);
			}

			Coverage cov = cm.getClassMethodCoverage(criterionNumber);

			TotalPanel methodPanel = new TotalPanel(" METHOD COVERAGE", cov
					.getNumberOfCovered()
					+ " of " + cov.getNumberOfRequirements(), (int) cov
					.getPercentage(), 10, 0);

			cov = prj.getProjectCoverage(criterionNumber);

			TotalPanel totalPanel = new TotalPanel(" TOTAL  COVERAGE", cov
					.getNumberOfCovered()
					+ " of " + cov.getNumberOfRequirements(), (int) cov
					.getPercentage(), 0, 10);

			String specificTool = new String("");

			if (coverageTool.isSelected()) {
				specificTool = new String("Coverage");
			} else {
				specificTool = new String("Slice");
			}

			syntesePanel = new SyntesePanel(ToolConstants.toolName + ": "
					+ specificTool, " Coverage: " + criterionName,
					"Active Test Cases: "
							+ TestSet.getNumberOfActiveTestCases()
							+ " of "
							+ (TestSet.getNumberOfTestCases() - TestSet
									.getNumberOfDeletedTestCases()));

			southPanel = new JPanel();
			southPanel.setLayout(new BorderLayout());
			southPanel.add(methodPanel, BorderLayout.NORTH);
			southPanel.add(totalPanel, BorderLayout.CENTER);
			southPanel.add(syntesePanel, BorderLayout.SOUTH);

			requirementsPanel.setLayout(new BorderLayout());

			requirementsPanel.add(titlePanel, BorderLayout.NORTH);

			centerPanel = new JPanel();
			centerPanel.setBorder(BorderFactory.createEtchedBorder());
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(comboPanel, BorderLayout.NORTH);
			if (requirements.length == 0) {
				JLabel label = new JLabel(criterionName
						+ " generates no requirements for method "
						+ getProject().getCurMethodName() + ".");
				label.setHorizontalAlignment(SwingConstants.CENTER);
				centerPanel.add(label, BorderLayout.CENTER);
			} else {
				centerPanel.add(tablePanel, BorderLayout.CENTER);
			}

			requirementsPanel.add(centerPanel, BorderLayout.CENTER);
			requirementsPanel.add(southPanel, BorderLayout.SOUTH);
		}
	}

	void classCombo_itemSelected(ItemEvent ex) {
		String className = classCombo.getSelectedItem().toString();
		getProject().setCurClassName(className);
		updatePane();
	}

	void methodCombo_itemSelected(ItemEvent ex) {
		String className = classCombo.getSelectedItem().toString();
		String methodName = methodCombo.getSelectedItem().toString();
		getProject().setCurClassName(className);
		getProject().setCurMethodName(methodName);

		updatePane();
	}

	// Activate All testing requirements of the current
	// method
	void allActivatedButton_actionPerformed(ActionEvent ex) {
		ClassFile cf = getProject()
				.getClassFile(getProject().getCurClassName());
		ClassMethod cm = cf.getMethod(getProject().getCurMethodName());
		Criterion c = cm.getCriterion(getActiveCriterionId());

		Object[] reqs = c.getRequirements();
		for (int i = 0; i < reqs.length; i++) {
			c.setActive((Requirement) reqs[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	// Deactivate all testing requirements of the current
	// method
	void allDeactivatedButton_actionPerformed(ActionEvent ex) {
		ClassFile cf = getProject()
				.getClassFile(getProject().getCurClassName());
		ClassMethod cm = cf.getMethod(getProject().getCurMethodName());
		Criterion c = cm.getCriterion(getActiveCriterionId());

		Object[] reqs = c.getRequirements();
		for (int i = 0; i < reqs.length; i++) {
			c.setInactive((Requirement) reqs[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	// Set all testing requirements of the current
	// method as feasible
	void allFeasibleButton_actionPerformed(ActionEvent ex) {
		ClassFile cf = getProject()
				.getClassFile(getProject().getCurClassName());
		ClassMethod cm = cf.getMethod(getProject().getCurMethodName());
		Criterion c = cm.getCriterion(getActiveCriterionId());

		Object[] reqs = c.getRequirements();
		for (int i = 0; i < reqs.length; i++) {
			c.setFeasible((Requirement) reqs[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	// Set all testing requirements of the current
	// method as infeasible
	void allInfeasibleButton_actionPerformed(ActionEvent ex) {
		ClassFile cf = getProject()
				.getClassFile(getProject().getCurClassName());
		ClassMethod cm = cf.getMethod(getProject().getCurMethodName());
		Criterion c = cm.getCriterion(getActiveCriterionId());

		Object[] reqs = c.getRequirements();
		for (int i = 0; i < reqs.length; i++) {
			c.setInfeasible((Requirement) reqs[i]);
		}

		getProject().coverageChanges();
		setUpdateLabelImage(semaforoRed);

		// Updating the graphical interface
		updatePane();
	}

	/*
	 * This method is responsible to update the information of the test case
	 * paths panel when the coverage tool is activated. It is called every time
	 * when such panel is shown in the GUI or when it is currently open and the
	 * Update button is clicked.
	 */
	/*
	 * private void updateTestCasePathsPanel() { String criterion = null;
	 * 
	 * int numTC = TestSet.getNumberOfTestCases();
	 * 
	 * pathsPanel = new JPanel();
	 * 
	 * if (numTC > 0) {
	 * 
	 * Object[] names = TestSet.getTestCaseLabels(); Arrays.sort(names); //
	 * Calculating the total number of rows int numRows = 0; for (int i = 0; i <
	 * names.length; i++) { TestCase tc = TestSet.getTestCase( (String) names[i] );
	 * 
	 * numRows += tc.getPathSetLabels().size(); }
	 * 
	 * 
	 * Object[][] rows = new Object[numRows][TableSorterPanel.DEFAULT_COL_SIZE +
	 * 4]; String[] columns = { "Test Case", "Path", "Host", "Thread Code",
	 * "Object Code", "Coverage", "Percentage" };
	 * 
	 * int criterionNumber = -1;
	 * 
	 * if (JabutiGUI.isAllPrimaryNodes()) { criterionNumber =
	 * Criterion.PRIMARY_NODES; } else if (JabutiGUI.isAllSecondaryNodes()) {
	 * criterionNumber = Criterion.SECONDARY_NODES; } else if
	 * (JabutiGUI.isAllPrimaryEdges()) { criterionNumber =
	 * Criterion.PRIMARY_EDGES; } else if (JabutiGUI.isAllSecondaryEdges()) {
	 * criterionNumber = Criterion.SECONDARY_EDGES; } else if
	 * (JabutiGUI.isAllPrimaryUses()) { criterionNumber =
	 * Criterion.PRIMARY_USES; } else if (JabutiGUI.isAllSecondaryUses()) {
	 * criterionNumber = Criterion.SECONDARY_USES; } else if
	 * (JabutiGUI.isAllPrimaryPotUses()) { criterionNumber =
	 * Criterion.PRIMARY_POT_USES; } else if (JabutiGUI.isAllSecondaryPotUses()) {
	 * criterionNumber = Criterion.SECONDARY_POT_USES; }
	 * 
	 * criterion = AbstractCriterion.getName( criterionNumber ); titlePanelLabel =
	 * criterion + " Coverage per Test Case Path"; titlePanel = new TitlePanel(
	 * titlePanelLabel ); int count = 0; for (int i = 0; i < names.length; i++) {
	 * TestCase tc = TestSet.getTestCase( (String) names[i] ); //
	 * ************************************************************** // Get one
	 * test case Hashtable paths = tc.getPathsTable(); // A test case is
	 * composed by several paths, // executed in different classes and methods
	 * Iterator itP = paths.keySet().iterator(); // For each one of this path
	 * while (itP.hasNext()) { HostProbedNode pdn = (HostProbedNode) itP.next(); //
	 * Recovering the list of paths of a given HostProbedNode Iterator it =
	 * tc.getPathSetLabelsByHost( pdn ).iterator(); // Collecting the
	 * information for each path. while( it.hasNext() ) { String thePath =
	 * (String) it.next(); rows[count][0] = JTableComponentModel.addButton(
	 * (String) names[i] );
	 * 
	 * rows[count][1] = JTableComponentModel.addButton( thePath );
	 * 
	 * rows[count][2] = JTableComponentModel.addLabel( pdn.getHost() );
	 * 
	 * rows[count][3] = JTableComponentModel.addLabel( pdn.threadCode );
	 * 
	 * rows[count][4] = JTableComponentModel.addLabel( pdn.objectCode );
	 * 
	 * Coverage cov = tc.getTestCaseCoverageByPath(criterionNumber, pdn,
	 * thePath);
	 * 
	 * rows[count][5] = JTableComponentModel.addLabel( cov.getNumberOfCovered() + "
	 * of " + cov.getNumberOfRequirements() );
	 * 
	 * rows[count++][6] = JTableComponentModel.addProgress( (int)
	 * cov.getPercentage() ); } } }
	 * 
	 * Coverage cov = getProject().getProjectCoverage(criterionNumber);
	 * 
	 * tablePanel = new TableSorterPanel( rows, columns );
	 * 
	 * TotalPanel totalPanel = new TotalPanel( " TOTAL COVERAGE",
	 * cov.getNumberOfCovered() + " of " + cov.getNumberOfRequirements(), (int)
	 * cov.getPercentage(), 10, 10 );
	 * 
	 * String specificTool = new String("");
	 * 
	 * if (coverageTool.isSelected()) { specificTool = new String("Coverage"); }
	 * else { specificTool = new String("Slice"); }
	 * 
	 * syntesePanel = new SyntesePanel(ToolConstants.toolName + ": " +
	 * specificTool, " Coverage: " + criterion, "Active Test Cases: " +
	 * TestSet.getNumberOfActiveTestCases() + " of " +
	 * (TestSet.getNumberOfTestCases() - TestSet.getNumberOfDeletedTestCases()) );
	 * 
	 * southPanel = new JPanel();
	 * 
	 * southPanel.setLayout(new BorderLayout()); southPanel.add(totalPanel,
	 * BorderLayout.NORTH); southPanel.add(syntesePanel, BorderLayout.SOUTH);
	 * 
	 * pathsPanel.setLayout(new BorderLayout());
	 * 
	 * pathsPanel.add(titlePanel, BorderLayout.NORTH);
	 * 
	 * pathsPanel.add(tablePanel, BorderLayout.CENTER);
	 * pathsPanel.add(southPanel, BorderLayout.SOUTH); } else {
	 * JOptionPane.showMessageDialog(null, "No Test Case to be shown!",
	 * "Message", JOptionPane.INFORMATION_MESSAGE); } }
	 */

	/**
	 * PROPERTIES BUTTON This method is responsible to build the Properties
	 * Button and to add it to the Menu Bar
	 */
	private void buildPropertiesMenu() {
		// Build the Properties Menu
		propertiesMenu.setText("Properties");
		propertiesMenu.setMnemonic('P');

		// Look and Fell Option
		propertiesLookAndFell.setText("Look and Feel");
		propertiesLookAndFell.setMnemonic('L');
		propertiesMenu.add(propertiesLookAndFell);

		UIManager.LookAndFeelInfo[] looks = UIManager
				.getInstalledLookAndFeels();

		lookButtons = new LookAndFeelRadioButton[looks.length];

		for (int i = 0; i < lookButtons.length; i++) {
			lookButtons[i] = new LookAndFeelRadioButton();
			lookButtons[i].setLookInfo(looks[i].getClassName());
			lookButtons[i].setText(lookButtons[i].getName());
			lookButtons[i]
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							propertiesLookAndFell_actionPerformed(e);
						}
					});

			if (lookButtons[i].getName().equals("MetalLookAndFeel"))
				lookButtons[i].setSelected(true);
			lookAndFellGroup.add(lookButtons[i]);
			propertiesLookAndFell.add(lookButtons[i]);
		}

		// Project Property
		projectManagerProperty.setText("Project Manager...");
		projectManagerProperty.setMnemonic('M');
		projectManagerProperty
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						projectManagerProperty_actionPerformed(e);
					}
				});

		propertiesMenu.add(projectManagerProperty);

		// Test Server
		testServerProperty.setText("Test Server...");
		// testServerProperty.setMnemonic('M');
		testServerProperty
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						testServerProperty_actionPerformed(e);
					}
				});

		propertiesMenu.add(testServerProperty);

		memoryMenuItem.setText("Available Memory Dialog...");

		memoryMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				memoryDialog = new AvailableMemoryDialog();
				memoryDialog.setVisible(true); // pop up dialog
			}
		});

		propertiesMenu.add(memoryMenuItem);

		// Adding the Properties Menu to the Menu Bar
		menuBar.add(propertiesMenu);
	}

	// propertiesButton action performed
	void propertiesLookAndFell_actionPerformed(ActionEvent ex) {
		try {
			int i = 0;
			for (; i < lookButtons.length; i++)
				if (lookButtons[i].isSelected())
					break;
			System.out.println(lookButtons[i].getName());
			UIManager.setLookAndFeel(lookButtons[i].getLookInfo());
			SwingUtilities.updateComponentTreeUI(mainWindow);
		} catch (Exception e) {
			ToolConstants.reportException(e, ToolConstants.STDERR);
		}
	}

	// propertiesButton action performed
	void testServerProperty_actionPerformed(ActionEvent ex) {
		new ServerProperties(JabutiGUI.mainWindow(), true).setVisible(true);
	}

	// propertiesButton action performed
	int projectManagerProperty_actionPerformed(ActionEvent ex) {
		int status;

		if (jbtProject == null) {
			JOptionPane.showMessageDialog(null,
					"No Project currently loaded! ", "Error",
					JOptionPane.ERROR_MESSAGE);
			status = JOptionPane.CANCEL_OPTION;

		} else {
			if (projectManagerDialog == null) { // first time

				projectManagerDialog = new ProjectManagerDialog(JabutiGUI
						.mainWindow(), jbtProject);

			} else {

				projectManagerDialog.loadProject(jbtProject);

			}

			status = projectManagerDialog.show(jbtProject); // pop up dialog

			if (getProject() != null && status == JOptionPane.OK_OPTION) {
				this.setTitle(ToolConstants.toolName + " v. "
						+ ToolConstants.toolVersion + " -- "
						+ getProject().getProjectFileName()
						+ (getProject().changed() ? "*" : ""));
			} else {
				this.setTitle(ToolConstants.toolName + " v. "
						+ ToolConstants.toolVersion);
			}
		}
		return status;
	}

	/**
	 * UPDATE BUTTON This method is responsible to build the Update Button and
	 * to add it to the Menu Bar
	 */

	private void buildUpdateMenu() {
		// Build the Update Button
		updateMenu.setText("Update");
		updateMenu.setToolTipText("Update coverage information...");
		updateItem.setText("Update");
		updateItem
				.setToolTipText("Cumulative update. Keeps the test case even without coverage improvement.");
		cutItem.setText("Update and cut");
		cutItem
				.setToolTipText("Non-cumulative update. Drops the test case when no coverage improvement is obtained.");

		updateMenu.add(updateItem);
		updateMenu.add(cutItem);

		updateItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateItem_actionPerformed(e);
			}
		});

		cutItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cutItem_actionPerformed(e);
			}
		});

		// Adding the Update Button to the Menu Bar
		menuBar.add(updateMenu);
		menuBar.add(updateLabel);
	}

	// updateMenu action performed
	void updateItem_actionPerformed(ActionEvent ex) {
		if (probeCheck.getNewSize() != probeCheck.getOldSize()) {
			System.out.println("Update by new test cases...");
			probeCheck.setOldSize(probeCheck.getNewSize());

			// Currently, all test case is loaded each time...
			if (TestSet.loadTraceFile(getProject())) {
				probeCheck.setOldSize(0L);
				probeCheck.setNewSize(0L);
			}

			// Setting the project as changed
			getProject().execChanges();

			setUpdateLabelImage(null);

			// Update the color attributes
			WeightColor.clearClassVariablesTable();
			WeightColor.updateColorAttributes();

			// Updating the graphical interface
			updatePane();
		} else if (getProject().coverageChanged()) {
			System.out.println("Update by project changes...");

			setUpdateLabelImage(null);

			// Updating the project coverage
			TestSet.updateOverallCoverage(JabutiGUI.getProject());
			// getProject().updateProjectCoverage();
			// and the CFG color
			if (JabutiGUI.cfgIsVisible())
				WeightColor.updateColorAttributes();

			// Setting the project coverage as updated
			getProject().coverageUpdated();

			// Setting the project as changed
			getProject().execChanges();

			// Updating the graphical interface
			updatePane();
		}
	}

	void cutItem_actionPerformed(ActionEvent ex) {
		if (probeCheck.getNewSize() != probeCheck.getOldSize()) {
			System.out.println("Update by new test cases...");
			probeCheck.setOldSize(probeCheck.getNewSize());

			// Currently, all test case is loaded each time...
			if (TestSet.loadAndCutTraceFile(getProject())) {
				probeCheck.setOldSize(0L);
				probeCheck.setNewSize(0L);
			}

			// Setting the project as changed
			getProject().execChanges();

			setUpdateLabelImage(null);

			// Update the color attributes
			WeightColor.clearClassVariablesTable();
			WeightColor.updateColorAttributes();

			// Updating the graphical interface
			updatePane();
		} else if (getProject().coverageChanged()) {
			System.out.println("Update by project changes...");

			setUpdateLabelImage(null);

			// Updating the project coverage
			TestSet.updateOverallCoverage(JabutiGUI.getProject());
			// getProject().updateProjectCoverage();
			// and the CFG color
			if (JabutiGUI.cfgIsVisible())
				WeightColor.updateColorAttributes();

			// Setting the project coverage as updated
			getProject().coverageUpdated();

			// Setting the project as changed
			getProject().execChanges();

			// Updating the graphical interface
			updatePane();
		}
	}

	/*
	 * This method is responsible to construct the Report menu
	 */
	private void buildReportMenu() {
		// Build the Properties Menu
		reportMenu.setText("Reports");
		// reportMenu.setMnemonic('R');

		// customReports Option
		customReports.setText("Custom Reports");
		customReports.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				customReports_actionPerformed(e);
			}
		});
		reportMenu.add(customReports);

		// jtable2HTML Option
		jtable2HTML.setText("Summary to HTML");
		jtable2HTML.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jtable2HTML_actionPerformed(e);
			}
		});
		reportMenu.add(jtable2HTML);

		// Adding the Properties Menu to the Menu Bar
		menuBar.add(reportMenu);
	}

	// Custom Reports Listener
	void customReports_actionPerformed(ActionEvent ex) {
		new CustomReportsDialog(JabutiGUI.mainWindow(), true).setVisible(true);
	}

	/*
	 * JTable2HTML Listener This method is responsible to convert any JTable
	 * object used by JaBUTi in an HTML file
	 */
	void jtable2HTML_actionPerformed(ActionEvent ex) {
		if (JabutiGUI.isBytecodePanel() || JabutiGUI.isSourcePanel()) {
			JOptionPane.showMessageDialog(null,
					"No Summary report is currently active.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String inputValue = JOptionPane
				.showInputDialog("Please input the HTML report file name");

		if (inputValue == null)
			return;

		if (inputValue.equals("")) {
			JOptionPane.showMessageDialog(null, "Invalid file name!!!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Appending the .html extension
		if (!(inputValue.endsWith(".html") || inputValue.endsWith(".htm"))) {
			inputValue += ".html";
		}

		File repFile = new File(inputValue);
		if (repFile.exists()) {
			int option = JOptionPane.showConfirmDialog(null, "File \""
					+ inputValue + "\" already exists. Overwrite it?",
					"Information", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (option == JOptionPane.NO_OPTION)
				return;
		}

		org.w3c.dom.Document doc = null;

		if (JabutiGUI.isRequirementsPanel()) {
			doc = HTMLGen.requirements2HTML(titlePanelLabel);
		} else if (JabutiGUI.isMetricsPanel()) {
			doc = HTMLGen.metrics2HTML(titlePanelLabel);
		} else if (JabutiGUI.isByTypePanel() || JabutiGUI.isByFilePanel()
				|| JabutiGUI.isByMethodPanel() || JabutiGUI.isTestCasePanel()
				|| JabutiGUI.isPathsPanel()) {
			doc = HTMLGen.overallCoverage2HTML(titlePanelLabel);
		}

		try {
			XMLPrettyPrinter.writeDocument(doc, inputValue);
		} catch (Exception pce) {
			ToolConstants.reportException(pce, ToolConstants.STDERR);
		} finally {
			doc = null;
			HTMLGen.restart();
		}
	}

	/*
	 * This method is responsible to construct the Help menu
	 */
	private void buildHelpMenu() {
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');

		JMenuItem indexItem = new JMenuItem("Index...");

		indexItem.setMnemonic('I');
		// helpMenu.add(indexItem);

		helpMenu.addSeparator();
		// you can also add the mnemonic key to an action
		JMenuItem aboutAction = new JMenuItem("About...", 'A');

		aboutAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (dialog == null) { // first time
					dialog = new AboutDialog();
				}
				dialog.setVisible(true); // pop up dialog
			}
		});

		helpMenu.add(aboutAction);

		// Adding Help Menu to Menu Bar
		menuBar.add(helpMenu);
	}
	
	/*
	 * This method is responsible to construct the Increase/Decrease Panel
	 */
	private void buildIncreaseDecreasePanel() {
		incDecPanel.setLayout(new GridLayout(2,1));
		incDecPanel.setPreferredSize(new Dimension(16,32));
		incDecPanel.setMinimumSize(new Dimension(16,32));
		incDecPanel.setMaximumSize(new Dimension(16,32));
		
		URL url = JabutiGUI.class.getResource("plus.png");
		increaseButton = new JButton(new ImageIcon(url));
		increaseButton.setToolTipText("Increase GUI components font size.");
		increaseButton.setVerticalAlignment(AbstractButton.CENTER);
		increaseButton.setHorizontalAlignment(AbstractButton.CENTER);
		
		url = JabutiGUI.class.getResource("minus.png");
		decreaseButton = new JButton(new ImageIcon(url));
		decreaseButton.setToolTipText("Decrease GUI components font size.");
		decreaseButton.setVerticalAlignment(AbstractButton.CENTER);
		decreaseButton.setHorizontalAlignment(AbstractButton.CENTER);
		
		incDecPanel.add(increaseButton);
		incDecPanel.add(decreaseButton);
		
		increaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ToolConstants.sourceFontSize++;
				adjustFont( 1 );
			}
		});

		decreaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ToolConstants.sourceFontSize--;
				adjustFont( -1 );
			}
		});

		// Adding Help Menu to Menu Bar
		menuBar.add(incDecPanel);
	}

	// Returns true if the CFG is currently
	// visible
	static public boolean cfgIsVisible() {
		return (cfgFrame != null && cfgFrame.isShowing());
	}

	// This method enables the communication between the report table and the
	// JabutiGUI
	/*
	 * public void tablePanelButtons_actionPerformed(ActionEvent e) {
	 * System.out.println(e.getActionCommand()); String buttonText =
	 * e.getActionCommand(); int offset = -1;
	 * 
	 * if (byMethod.isSelected()) { int index =
	 * buttonText.indexOf(JabutiGUI.CLASS_METHOD_SEPARATOR); String className =
	 * buttonText.substring(0, index); String methodName =
	 * buttonText.substring(index + 1, buttonText.length());
	 * 
	 * System.out.println("Class Name: " + className);
	 * System.out.println("Method Name: " + methodName);
	 * 
	 * ClassFile cf = getProject().getClassFile(className); // If the selected
	 * class is different from the // current class the current labelNodeTable
	 * of // each method can be cleaned... if (!className.equals(
	 * TheProject.getCurClassName() ) ) { ClassFile oldCF =
	 * getProject().getClassFile( TheProject.getCurClassName() );
	 * 
	 * oldCF.releaseLabelNodeTable();
	 * 
	 * if (cfgIsVisible()) { // ERROR: Change className by currentClassName //
	 * Consequence: it always show the last class // Test Case: create a Project
	 * with mode that one class // go to // Visualization->CFG // Summary -> By
	 * Method // Choose a method of a different class // (not the current) //
	 * The CFG was't updated correctly. // cfgFrame.setSelectedClass(
	 * currentClassFile ); cfgFrame.setSelectedClass(
	 * TheProject.getCurClassName() ); } } TheProject.setCurClassName( className );
	 * 
	 * ClassMethod mth = cf.getMethod(methodName);
	 * 
	 * if (cfgIsVisible()) { cfgFrame.setSelectedMethod(methodName); }
	 * 
	 * offset = mth.getBeginBytecodeOffset();
	 * 
	 * Debug.D("POSICAO DO TOKEN: " + offset); System.out.println("POSICAO DO
	 * TOKEN: " + offset);
	 * 
	 * updatePane(JabutiGUI.BYTECODE_PANEL, offset); } else if
	 * (byFile.isSelected()) { String className = buttonText;
	 * 
	 * offset = 0;
	 * 
	 * System.out.println("Class Name: " + className); // If the selected class
	 * is different from the // current class the current labelNodeTable of //
	 * each method can be cleaned... if (!className.equals(
	 * TheProject.getCurClassName() ) ) { ClassFile oldCF =
	 * getProject().getClassFile( TheProject.getCurClassName() );
	 * 
	 * oldCF.releaseLabelNodeTable();
	 * 
	 * if (cfgIsVisible()) { // ERROR: Change className by currentClassName //
	 * Consequence: it always show the last class // Test Case: create a Project
	 * with mode that one class // go to // Visualization->CFG // Summary -> By
	 * File // Choose a different class (not the current) // The CFG was't
	 * updated correctly. // cfgFrame.setSelectedClass( currentClassFile );
	 * 
	 * cfgFrame.setSelectedClass( TheProject.getCurClassName() ); } }
	 * TheProject.setCurClassName( className );
	 * 
	 * Debug.D("POSICAO DO TOKEN: " + offset); System.out.println("POSICAO DO
	 * TOKEN: " + offset);
	 * 
	 * updatePane(JabutiGUI.BYTECODE_PANEL, offset); } }
	 */

	// This method enables the communication between the report table and the
	// JabutiGUI
	public void tablePanelButtons_MouseClicked(String buttonText) {
		int offset = -1;
		if (JabutiGUI.isByMethodPanel()) {
			int index = buttonText.indexOf(JabutiGUI.CLASS_METHOD_SEPARATOR);
			String className = buttonText.substring(0, index);
			String methodName = buttonText.substring(index + 1, buttonText
					.length());

			ClassFile cf = getProject().getClassFile(className);

			// If the selected class is different from the
			// current class the current labelNodeTable of
			// each method can be cleaned...
			if (!className.equals(getProject().getCurClassName())) {
				ClassFile oldCF = getProject().getClassFile(
						getProject().getCurClassName());

				oldCF.releaseLabelNodeTable();

				if (cfgIsVisible()) {
					// ERROR: Change className by currentClassName
					// Consequence: it always show the last class
					// Test Case: create a Project with mode that one class
					// go to
					// Visualization->CFG
					// Summary -> By Method
					// Choose a method of a different class
					// (not the current)
					// The CFG was't updated correctly.
					// cfgFrame.setSelectedClass( currentClassFile );
					cfgFrame.setSelectedClass(getProject().getCurClassName());
				}
			}
			getProject().setCurClassName(className);
			getProject().setCurMethodName(methodName);

			ClassMethod mth = cf.getMethod(methodName);

			if (cfgIsVisible()) {
				cfgFrame.setSelectedClass(className);
				cfgFrame.setSelectedMethod(methodName);
			}

			offset = mth.getBeginBytecodeOffset();

			Debug.D("POSICAO DO TOKEN: " + offset);
			updatePane(JabutiGUI.BYTECODE_PANEL, offset);

		} else if (JabutiGUI.isByFilePanel() || JabutiGUI.isMetricsPanel()) {
			String className = buttonText;

			offset = 0;

			ClassFile cf = getProject().getClassFile(className);

			if (cf != null) {
				// If the selected class is different from the
				// current class the current labelNodeTable of
				// each method can be cleaned...
				if (!className.equals(getProject().getCurClassName())) {
					ClassFile oldCF = getProject().getClassFile(
							getProject().getCurClassName());

					oldCF.releaseLabelNodeTable();

					if (cfgIsVisible()) {
						// ERROR: Change className by currentClassName
						// Consequence: it always show the last class
						// Test Case: create a Project with mode that one class
						// go to
						// Visualization->CFG
						// Summary -> By File
						// Choose a different class (not the current)
						// The CFG was't updated correctly.
						// cfgFrame.setSelectedClass( currentClassFile );

						cfgFrame.setSelectedClass(getProject()
								.getCurClassName());
					}
				}
				getProject().setCurClassName(className);
				if (cfgIsVisible()) {
					cfgFrame.setSelectedClass(className);
				}

				Debug.D("POSICAO DO TOKEN: " + offset);

				updatePane(JabutiGUI.BYTECODE_PANEL, offset);
			}
		}
	}

	/***************************************************************************
	 * ****************************************************************
	 * **************************************************************** GET AND
	 * SET METHODS
	 * ****************************************************************
	 * ****************************************************************
	 **************************************************************************/

	static public JabutiGUI mainWindow() {
		return mainWindow;
	}

	static public JabutiProject getProject() {
		return jbtProject;
	}

	static public void setProject(JabutiProject prj) {
		jbtProject = prj;
	}

	/**
	 * This method changes the icon image of the JLabel on the right side of the
	 * updateMenu. A red circle appears when there is new trace information to
	 * update the project coverage.
	 */
	void setUpdateLabelImage(ImageIcon c) {
		if (updateLabel != null)
			if (c != null) {
				updateLabel.setIcon(c);
				updateLabel.setEnabled(true);
				updateLabel
						.setToolTipText("Possible coverage changes. Go to Update menu...");
			} else {
				updateLabel.setEnabled(false);
				updateLabel
						.setToolTipText("A red button indicates possible coverage changes.");
			}
	}

	/**
	 * Checks if the bytecode panel is active
	 */
	static public boolean isBytecodePanel() {
		return (currentCodePanel == JabutiGUI.BYTECODE_PANEL);
	}

	/**
	 * Checks if the source code panel is active
	 */
	static public boolean isSourcePanel() {
		return (currentCodePanel == JabutiGUI.SOURCE_PANEL);
	}

	/**
	 * Checks if the by type panel is active
	 */
	static public boolean isByTypePanel() {
		return (currentCodePanel == JabutiGUI.TYPE_PANEL);
	}

	/**
	 * Checks if the by file panel is active
	 */
	static public boolean isByFilePanel() {
		return (currentCodePanel == JabutiGUI.FILE_PANEL);
	}

	/**
	 * Checks if the by method panel is active
	 */
	static public boolean isByMethodPanel() {
		return (currentCodePanel == JabutiGUI.METHOD_PANEL);
	}

	/**
	 * Checks if the test case panel is active
	 */
	static public boolean isTestCasePanel() {
		return (currentCodePanel == JabutiGUI.TESTCASE_PANEL);
	}

	/**
	 * Checks if the metrics panel is active
	 */
	static public boolean isMetricsPanel() {
		return (currentCodePanel == JabutiGUI.METRICS_PANEL);
	}

	/**
	 * Checks if the requirements panel is active
	 */
	static public boolean isRequirementsPanel() {
		return (currentCodePanel == JabutiGUI.REQUIREMENTS_PANEL);
	}

	/**
	 * Checks if the paths panel is active
	 */
	static public boolean isPathsPanel() {
		return (currentCodePanel == JabutiGUI.PATHS_PANEL);
	}

	static public int getCurrentPanel() {
		return currentCodePanel;
	}

	/**
	 * The methods below identify which is the current active testing criterion.
	 */
	static public boolean isAllPrimaryNodes() {
		return allPrimaryNodesCriterion.isSelected();
	}

	static public boolean isAllSecondaryNodes() {
		return allSecondaryNodesCriterion.isSelected();
	}

	static public boolean isAllPrimaryEdges() {
		return allPrimaryEdgesCriterion.isSelected();
	}

	static public boolean isAllSecondaryEdges() {
		return allSecondaryEdgesCriterion.isSelected();
	}

	static public boolean isAllPrimaryUses() {
		return allPrimaryUsesCriterion.isSelected();
	}

	static public boolean isAllSecondaryUses() {
		return allSecondaryUsesCriterion.isSelected();
	}

	static public boolean isAllPrimaryPotUses() {
		return allPrimaryPotUsesCriterion.isSelected();
	}

	static public boolean isAllSecondaryPotUses() {
		return allSecondaryPotUsesCriterion.isSelected();
	}

	/**
	 * The methods below return the name of criterion currently selected
	 */
	public String getActiveCriterionName() {
		if (JabutiGUI.isAllPrimaryNodes())
			return AbstractCriterion.getName(Criterion.PRIMARY_NODES);
		else if (JabutiGUI.isAllSecondaryNodes())
			return AbstractCriterion.getName(Criterion.SECONDARY_NODES);
		else if (JabutiGUI.isAllPrimaryEdges())
			return AbstractCriterion.getName(Criterion.PRIMARY_EDGES);
		else if (JabutiGUI.isAllSecondaryEdges())
			return AbstractCriterion.getName(Criterion.SECONDARY_EDGES);
		else if (JabutiGUI.isAllPrimaryUses())
			return AbstractCriterion.getName(Criterion.PRIMARY_USES);
		else if (JabutiGUI.isAllSecondaryUses())
			return AbstractCriterion.getName(Criterion.SECONDARY_USES);
		else if (JabutiGUI.isAllPrimaryPotUses())
			return AbstractCriterion.getName(Criterion.PRIMARY_POT_USES);
		else if (JabutiGUI.isAllSecondaryPotUses())
			return AbstractCriterion.getName(Criterion.SECONDARY_POT_USES);
		return AbstractCriterion.getName(Criterion.PRIMARY_NODES);
	}

	/**
	 * The methods below return the name of criterion currently selected
	 */
	public int getActiveCriterionId() {
		if (JabutiGUI.isAllPrimaryNodes())
			return Criterion.PRIMARY_NODES;
		else if (JabutiGUI.isAllSecondaryNodes())
			return Criterion.SECONDARY_NODES;
		else if (JabutiGUI.isAllPrimaryEdges())
			return Criterion.PRIMARY_EDGES;
		else if (JabutiGUI.isAllSecondaryEdges())
			return Criterion.SECONDARY_EDGES;
		else if (JabutiGUI.isAllPrimaryUses())
			return Criterion.PRIMARY_USES;
		else if (JabutiGUI.isAllSecondaryUses())
			return Criterion.SECONDARY_USES;
		else if (JabutiGUI.isAllPrimaryPotUses())
			return Criterion.PRIMARY_POT_USES;
		else if (JabutiGUI.isAllSecondaryPotUses())
			return Criterion.SECONDARY_POT_USES;

		return Criterion.PRIMARY_NODES;
	}

	/**
	 * These methods identify the kind of priority currently in use. For now,
	 * only the allPriorized is implemented.
	 */

	static public boolean isAllPriorized() {
		return allPriorized.isSelected();
	}

	/*
	 * static public boolean isHighestWeight() { return
	 * highestWeight.isSelected(); }
	 * 
	 * static public boolean isNonZeroWeight() { return
	 * nonZeroWeight.isSelected(); }
	 * 
	 * static public boolean isZeroWeight() { return zeroWeight.isSelected(); }
	 * 
	 * static public boolean isZeroNonZeroWeight() { return
	 * zeroNonZeroWeight.isSelected(); }
	 */

	/*
	 * The methods below identify which are the active tool
	 */
	static public boolean isCoverage() {
		return coverageTool.isSelected();
	}

	static public boolean isSlice() {
		return sliceTool.isSelected();
	}

	/**
	 * The methods below return the name of tool currently selected
	 */
	public String getActiveToolName() {
		if (isCoverage())
			return coverageTool.getText();
		else if (isSlice())
			return sliceTool.getText();
		return coverageTool.getText();
	}

	/**
	 * @return a reference to image representing the update button image.
	 */
	public ImageIcon getSemaforoRedImage() {
		return semaforoRed;
	}
	
	/**
	 * Adjunst the font size of all Swing UI components
	 * @param adjust
	 */
    public void adjustFont( int adjust )
    {
        Object[] objs = UIManager.getLookAndFeel().getDefaults().keySet().toArray();
        for( int i = 0; i < objs.length; i++ )
        {
            if( objs[i].toString().toUpperCase().indexOf( ".FONT" ) != -1 )
            {
                Font font = UIManager.getFont( objs[i] );
                font = font.deriveFont( (float)(font.getSize() + adjust ));
                UIManager.put( objs[i], new FontUIResource(font) );
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }
}