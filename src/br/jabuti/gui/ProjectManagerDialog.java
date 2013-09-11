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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import br.jabuti.graph.CFG;
import br.jabuti.lookup.Program;
import br.jabuti.lookup.RClass;
import br.jabuti.project.JabutiProject;
import br.jabuti.util.ToolConstants;

/**
 * This class is reponsable to construct the dialog box
 * to allow the manipulation of a given project...
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class ProjectManagerDialog extends JDialog {  

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -674324787992712241L;
	// ***************************************************************
    // Variables used to presents the information about each class
    // or project ( to create or change a project )
    // ***************************************************************
    JPanel mainPanelLeft = new JPanel();
    JScrollPane treePanel = new JScrollPane();
    JTree ProgTree = new JTree();

    JPanel mainPanelCenter = new JPanel();
    GridLayout gridLayoutCenter = new GridLayout();	
    JPanel avoidPanel = new JPanel();
    JPanel avoidButtonPanel = new JPanel();
    JButton toAvoidButton = new JButton();			
    JButton noAvoidButton = new JButton();

    JList avoidList = avoidListCreate();
    Border avoidBorderList;
    TitledBorder avoidTitledBorderList;
    JScrollPane avoidScrollPanel = new JScrollPane();
		
    JPanel instrPanel = new JPanel();
    JPanel instrButtonPanel = new JPanel();
    JButton toInstrButton = new JButton();
    JButton noInstrButton = new JButton();

    JList instrList = instrumListCreate();
    Border instrBorderList;
    TitledBorder instrTitledBorderList;
    JScrollPane instrScrollPanel = new JScrollPane();

    JPanel mainPanelRight = new JPanel();
    Border executeBorder;
    TitledBorder executeTitledBorder;
    JPanel executePanel = new JPanel();

    JLabel projectNameLabel = new JLabel();
    JTextField projectNameTextField = new JTextField();
    JScrollPane projectNameScroll = new JScrollPane();
    JButton selectButton = new JButton();

    JLabel classpathLabel = new JLabel();
    JTextArea classpathTextArea = new JTextArea();
    JScrollPane classpathScroll = new JScrollPane();

    JLabel baseClassLabel = new JLabel();			
    JTextField baseClassTextField = new JTextField();

    //JTextField saveTextField = new JTextField();
	JCheckBox cfgOptionCheckBox = new JCheckBox();    
    //ButtonGroup mobilityGroup = new ButtonGroup();
    //JRadioButton nonMobilityButton = new JRadioButton();
    //JRadioButton mobilityButton = new JRadioButton();
  			
    JPanel buttonPanel = new JPanel();
    JButton ok = new JButton();
    JButton cancel = new JButton();

    // ***************************************************************     
    Container contentPane = null;

	// Save the currents set of packages and classes in the
	// current project
	private HashSet origAvoidSet = null;
    private HashSet origInstrSet = null;
    
    private JabutiProject prj = null;
    private int status;
    	
    public ProjectManagerDialog( JFrame fr, JabutiProject p ) { 
        super( fr, "Project Manager", true);
      	
      	origAvoidSet = new HashSet();
      	origInstrSet = new HashSet();
      	
      	prj = p;
      	status = JOptionPane.CANCEL_OPTION;
      	
        initProjectManager();
        loadProject( prj );
        pack();
    }
   
    private void initProjectManager() {
        contentPane = getContentPane();
		
        avoidBorderList = BorderFactory.createEtchedBorder(Color.white, new Color(178, 178, 178));
        avoidTitledBorderList = new TitledBorder(avoidBorderList, "Avoided Packages");
        instrBorderList = BorderFactory.createEtchedBorder(Color.white, new Color(178, 178, 178));
        instrTitledBorderList = new TitledBorder(instrBorderList, "Classes to Instrument");
        executeBorder = BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151));
        executeTitledBorder = new TitledBorder(executeBorder, "Project Configuration");

        contentPane.setLayout(new BorderLayout());

        mainPanelCenter.setLayout(gridLayoutCenter);
        gridLayoutCenter.setColumns(1);
        gridLayoutCenter.setRows(2);

        avoidButtonPanel.setLayout(new BoxLayout(avoidButtonPanel, BoxLayout.Y_AXIS));
        avoidButtonPanel.setBorder(BorderFactory.createEtchedBorder());
	    
        instrButtonPanel.setLayout(new BoxLayout(instrButtonPanel, BoxLayout.Y_AXIS));
        instrButtonPanel.setBorder(BorderFactory.createEtchedBorder());
	    
        toInstrButton.setToolTipText("Include into instrumentation list...");

        Toolkit kit = Toolkit.getDefaultToolkit();	  
        ImageIcon imgRight = new ImageIcon(kit.getImage(
        	ToolConstants.getToolBaseResource("right.png")
                ) 
                );

        toInstrButton.setIcon(imgRight);
        // toInstrButton.setText( "==>" );
	    
        toInstrButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        toInstrButton_actionPerformed(e);
                    }
                }
                );
        noInstrButton.setToolTipText("Remove from intrumentation list...");

        ImageIcon imgLeft = new ImageIcon(kit.getImage(
        	   ToolConstants.getToolBaseResource("left.png")
                ) 
                );

        noInstrButton.setIcon(imgLeft);
        // noInstrButton.setText("<==");
	    
        noInstrButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        noInstrButton_actionPerformed(e);
                    }
                }
                );
        avoidPanel.setLayout(new BorderLayout());
        instrPanel.setLayout(new BorderLayout());
	    
        toAvoidButton.setToolTipText("Avoid this package...");
	    
        toAvoidButton.setIcon(imgRight);
        // toAvoidButton.setText("==>");
	    
        toAvoidButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        toAvoidButton_actionPerformed(e);
                    }
                }
                );

        noAvoidButton.setToolTipText("Include this in the program...");
	    
        noAvoidButton.setIcon(imgLeft);
        // noAvoidButton.setText("<==");
	    
        noAvoidButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        noAvoidButton_actionPerformed(e);
                    }
                }
                );

        instrList.setBorder(instrTitledBorderList);
        avoidList.setBorder(avoidTitledBorderList);
        treePanel.setViewportBorder(BorderFactory.createLineBorder(Color.black));
        treePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        treePanel.setMinimumSize(new Dimension(150, 90));
        ProgTree.setAutoscrolls(true);
        ProgTree.setBorder(BorderFactory.createEtchedBorder());
        ProgTree.setPreferredSize(new Dimension(200, 90));
        ProgTree.setModel(null);
        ProgTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
                    public void treeExpanded(TreeExpansionEvent e) {
                        ProgTree_treeExpanded(e);
                    }

                    public void treeCollapsed(TreeExpansionEvent e) {
                        ProgTree_treeCollapsed(e);
                    }
                }
                );
	    
        mainPanelLeft.setLayout(new BorderLayout());
			    
        mainPanelLeft.add(treePanel, BorderLayout.CENTER);
	    
        mainPanelRight.setLayout(new BorderLayout());
        mainPanelRight.setPreferredSize(new Dimension(400, 200));

        contentPane.add(mainPanelCenter, BorderLayout.CENTER);
	    
        mainPanelCenter.add(avoidPanel, null);
        avoidPanel.add(avoidButtonPanel, BorderLayout.WEST);
        avoidButtonPanel.add(Box.createVerticalGlue());
        avoidButtonPanel.add(toAvoidButton);
        avoidButtonPanel.add(Box.createVerticalGlue());
        avoidButtonPanel.add(noAvoidButton);	    
        avoidButtonPanel.add(Box.createVerticalGlue());
        avoidPanel.add(avoidScrollPanel, BorderLayout.CENTER);
        avoidScrollPanel.getViewport().add(avoidList, null);
	    
        mainPanelCenter.add(instrPanel, null);
        instrPanel.add(instrButtonPanel, BorderLayout.WEST);
        instrButtonPanel.add(Box.createVerticalGlue());
        instrButtonPanel.add(toInstrButton);
        instrButtonPanel.add(Box.createVerticalGlue());
        instrButtonPanel.add(noInstrButton);
        instrButtonPanel.add(Box.createVerticalGlue());
        instrPanel.add(instrScrollPanel, BorderLayout.CENTER);
	    
        contentPane.add(mainPanelLeft, BorderLayout.WEST);

        contentPane.add(mainPanelRight, BorderLayout.EAST);

        mainPanelRight.add(executePanel, BorderLayout.CENTER);
        mainPanelRight.add(buttonPanel, BorderLayout.SOUTH);

        executePanel.setBorder(executeTitledBorder);
	    
        executePanel.setLayout(new GridBagLayout());
	    
        GridBagConstraints gridBagConstraints;

        projectNameLabel.setText("Project Name:");
        projectNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        projectNameLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;        
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;        
        gridBagConstraints.weighty = 2.0;
        executePanel.add(projectNameLabel, gridBagConstraints);

        projectNameTextField.setText("");
/*        projectNameTextField.addCaretListener(new CaretListener() {
                    public void caretUpdate(CaretEvent e) {
                        projectNameTextField_caretUpdate();
                    }
                }
                );*/

        if ( prj.getProjectFile() != null) {
            projectNameTextField.setText( prj.getProjectFile().toString() );
            projectNameTextField.setEnabled( false );
        } else {
        	projectNameTextField.setEnabled( false );
        }
        
        projectNameScroll.setViewportView(projectNameTextField);
		
        projectNameScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // projectNameScroll.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 5);
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 2.0;
        executePanel.add(projectNameScroll, gridBagConstraints);

        selectButton.setText("Select");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 5);
        gridBagConstraints.weighty = 2.0;
        executePanel.add(selectButton, gridBagConstraints);
	
        selectButton.addActionListener(new ActionListener() {  
                    public void actionPerformed(ActionEvent evt) {
                        projectNameTextField.setText("");
                        prj.setProjectFile( null );
                        JabutiGUI.mainWindow().selectPrj_actionPerformed(evt);
                        if ( prj.getProjectFile() != null) {
                            projectNameTextField.setText( prj.getProjectFile().toString());
                        }
                    }
                }
                );

        classpathLabel.setText("Classpath: ");
        classpathLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        classpathLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        gridBagConstraints.weighty = 10.0;
        executePanel.add(classpathLabel, gridBagConstraints);

        classpathTextArea.setText(JabutiGUI.mainWindow().classpathTextArea.getText());
        classpathTextArea.setToolTipText("");
        classpathTextArea.setLineWrap(true);
        classpathTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        classpathTextArea_focusLost(e);
                    }
                }
                );
        
        // classpathScroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // classpathScroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        classpathScroll.setViewportView(classpathTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 10.0;
        
        executePanel.add(classpathScroll, gridBagConstraints);

        baseClassLabel.setText("Base Class:");
        baseClassLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        executePanel.add(baseClassLabel, gridBagConstraints);

        baseClassTextField.setToolTipText("");
        baseClassTextField.setEnabled( false );
        baseClassTextField.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        baseClassTextField_focusLost(e);
                    }
                }
                );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 2.0;
        executePanel.add(baseClassTextField, gridBagConstraints);

		
        cfgOptionCheckBox.setText("Hide CFG Call Nodes");
        cfgOptionCheckBox.setEnabled( false );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        executePanel.add( cfgOptionCheckBox, gridBagConstraints);

        // Ok button update project information
        ok = new JButton("Ok");
        ok.addActionListener(new 
                ActionListener() {  
                    public void actionPerformed(ActionEvent evt) {
                    	status = JOptionPane.OK_OPTION;
                    	
                        if ( prj.getInstr().size() == 0) {
                            JOptionPane.showMessageDialog(null,
                                    "At least one class should be selected to be instrumented!!!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if ( projectNameTextField.getText().length() == 0) {
                            JOptionPane.showMessageDialog(null,
                                    "Invalid project name!!!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            // Update the original project
                            if ( prj != null && prj.changed() ) {
    	
                            	prj.rebuild();
                            	File prjFile = new File( projectNameTextField.getText() );
                            	prj.setProjectFile( prjFile );
                                try {
                                    prj.saveProject();
                                    // Generating the instrumented .jar file
                                    //System.out.println( "SAVING THE JAR FILE: " + saveTextField.getText() );
                                    //saveClasses( saveTextField.getText() );
                                } catch (Exception es) {
                                    String pclass = prj.getProjectFileName();
				
                                    JOptionPane.showMessageDialog(null,
                                            "Error saving file " + pclass + "! ",
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                    ToolConstants.reportException(es, ToolConstants.STDERR);
                                    setVisible(false);
                                    return;
                                }
                            }
                            setVisible(false);
                            return;
                        }
                    } 
                }
                );

        // Cancel button closes the dialog
        cancel = new JButton("Cancel");
        cancel.addActionListener(new 
                ActionListener() {  
                    public void actionPerformed(ActionEvent evt) {
                    	status = JOptionPane.CANCEL_OPTION;
                    	if ( prj != null && prj.changed() ) {
	                    	// Restoring the original set of avoided packages 
	                    	Iterator it = prj.getAvoid().iterator();
	                    	while ( it.hasNext() ) {
	                    		String cName = (String) it.next();
	                    		prj.delAvoid( cName );
	                    	}
	                    	it = origAvoidSet.iterator();
	                    	while (it.hasNext() ) {
	                    		String cName = (String) it.next();
	                    		prj.addAvoid( cName );
	                    	}
	                    	
	                    	// Restoring the original set of instrumented classes
	                    	it = prj.getInstr().iterator();
	                    	while ( it.hasNext() ) {
	                    		String cName = (String) it.next();
	                    		prj.delInstr( cName );
	                    	}
	                    	it = origInstrSet.iterator();
	                    	while (it.hasNext() ) {
	                    		String cName = (String) it.next();
	                    		prj.addInstr( cName );
	                    	}
	                    	prj.rebuild();
	                    	try {
	                    		prj.saveProject();
	                    	} catch (Exception e) {
	                    		ToolConstants.reportException( e, ToolConstants.STDERR);
	                    	}
	                    }
                        setVisible(false); 
                    } 
                }
                );

        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        treePanel.getViewport().add(ProgTree, null);
        instrScrollPanel.getViewport().add(instrList, null);
    }
	
    public JList instrumListCreate() {
        DefaultListModel mf = new DefaultListModel();
        JList jl = new JList(mf);

        jl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jl.setPreferredSize(new Dimension(250, 10));
        return jl;
    }

    public JList avoidListCreate() {
        DefaultListModel mf = new DefaultListModel();
        JList jl = new JList(mf);

        jl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jl.setPreferredSize(new Dimension(250, 10));
        return jl;
    }

    void ProgTree_treeCollapsed(TreeExpansionEvent e) {
        int nRow = ProgTree.getRowCount();

        ProgTree.setPreferredSize(new Dimension(250, 50 + nRow * 20));
    }

    void ProgTree_treeExpanded(TreeExpansionEvent e) {
        int nRow = ProgTree.getRowCount();

        ProgTree.setPreferredSize(new Dimension(250, 50 + nRow * 20));
    }
	
    public void loadProject(JabutiProject prj) {
    	status = JOptionPane.CANCEL_OPTION;
    	
        try {
            loadProjTree( prj );
            loadProjAvoid( prj );
            loadProjInstr( prj );
            loadProjExec( prj );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot clone the current project! ",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public void loadProjTree(JabutiProject prj) {
        if (prj == null) {
            return;
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("User Packages");
        TreeModel tm = new DefaultTreeModel(root);
        Program p = prj.getProgram();
        String[] packs = p.getCodePackages();
        int k = 1;

        for (int i = 0; i < packs.length; i++) {
        	// S� inserir o package se ele n�o for avoided
            DefaultMutableTreeNode nd = new DefaultMutableTreeNode(packs[i]);

            root.add(nd);
            k++;
            String[] cls = p.getCodeClasses(packs[i]);

            for (int j = 0; j < cls.length; j++) {
                String cs = RClass.getClassName(cls[j]);
                // S� inserir a classe se ela n�o pertencer a instrSet
                DefaultMutableTreeNode ndclass = new DefaultMutableTreeNode(cs);

                nd.add(ndclass);
                k++;
            }
        }
        ProgTree.setModel(tm);
        int nRow = ProgTree.getRowCount();

        ProgTree.setPreferredSize(new Dimension(250, 50 + nRow * 18));
    }

    void loadProjAvoid(JabutiProject prj) {
        if (prj == null) {
            return;
        }
        DefaultListModel dlm = (DefaultListModel) avoidList.getModel();

        dlm.removeAllElements();
        Collection c = prj.getAvoid();
        Iterator en = c.iterator();

        while (en.hasNext()) {
            String s = (String) en.next();

            dlm.addElement(s);
        }
        int fsize = dlm.getSize();

        avoidList.setPreferredSize(new Dimension(250, 50 + fsize * 20));
    }

    void loadProjInstr(JabutiProject prj) {
        if (prj == null) {
            return;
        }
        DefaultListModel dlm = (DefaultListModel) instrList.getModel();

        dlm.removeAllElements();
        Collection c = prj.getInstr();
        Iterator en = c.iterator();

        while (en.hasNext()) {
            String s = (String) en.next();

            dlm.addElement(s);
        }
        int fsize = dlm.getSize();

        instrList.setPreferredSize(new Dimension(250, 50 + fsize * 20));
    }

    void loadProjExec(JabutiProject prj) {
        if (prj == null) {
            return;
        }
     
        if ( prj.getProjectFile() != null) {
            projectNameTextField.setText( prj.getProjectFileName() );
        } else {
            projectNameTextField.setText("");
        }		

        String s = prj.getClasspath();

        classpathTextArea.setText(s);
        
        s = prj.getMain();
        baseClassTextField.setText(s);
        
        cfgOptionCheckBox.setSelected( (prj.getCFGOption() == CFG.NONE)? false : true );
//        mobilityButton.setSelected( prj.isMobility() ); 
//        nonMobilityButton.setSelected( !(prj.isMobility()) );         
    }
	
    void toAvoidButton_actionPerformed(ActionEvent e) {
        TreePath[] tp = ProgTree.getSelectionPaths();
        boolean chg = false;

        for (int i = 0; tp != null && i < tp.length; i++) {
            Object[] path = tp[i].getPath();

            if (path.length != 2) {
                continue;
            }
            String pack = "";
            DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) path[1];
            pack = dtn.toString();
            boolean b = prj.addAvoid(pack);

            if (b) {
                chg = true;
            }
        }
        if (chg) {
            loadProjTree( prj );
            loadProjAvoid( prj );
        }
    }
	
    void noAvoidButton_actionPerformed(ActionEvent e) {
        Object[] sel = avoidList.getSelectedValues();
        DefaultListModel dlm = (DefaultListModel) avoidList.getModel();
        boolean chg = false;

        for (int i = 0; sel != null && i < sel.length; i++) {
            String pack = (String) sel[i];

            prj.delAvoid(pack);
            chg = true;
        }
        if (chg) {
            int fsize = dlm.getSize();

            avoidList.setPreferredSize(new Dimension(250, 50 + fsize * 20));
            loadProjTree( prj );
            loadProjAvoid( prj );
        }
    }
	
    void toInstrButton_actionPerformed(ActionEvent e) {
        DefaultListModel dlm = (DefaultListModel) instrList.getModel();
        TreePath[] tp = ProgTree.getSelectionPaths();

        for (int i = 0; tp != null && i < tp.length; i++) {
            Object[] path = tp[i].getPath();

            if (path.length <= 1) {
                continue;
            }
            String pack = "";
            DefaultMutableTreeNode dtn = null;

            for (int j = 1; j < path.length; j++) {
                dtn = (DefaultMutableTreeNode) path[j];
                if (!dtn.toString().equals(RClass.DEFAULT_PACKAGE)) {
                    pack += dtn.toString() + ".";
                }
            }
            int k = dtn.getChildCount();

            if (k == 0) {
                pack = pack.substring(0, pack.length() - 1);
                boolean b = prj.addInstr(pack);

                if (b) {
                    ((DefaultListModel) instrList.getModel()).addElement(pack);
                }
            } else {
                for (int j = 0; j < k; j++) {
                    boolean b = prj.addInstr(pack + dtn.getChildAt(j));

                    if (b) {
                        ((DefaultListModel) instrList.getModel()).addElement(
                                pack + dtn.getChildAt(j));
                    }
                }
            }
        }
        int fsize = dlm.getSize();

        instrList.setPreferredSize(new Dimension(250, 50 + fsize * 20));
    }
	
    void noInstrButton_actionPerformed(ActionEvent e) {
        Object[] sel = instrList.getSelectedValues();
        DefaultListModel dlm = (DefaultListModel) instrList.getModel();

        for (int i = 0; sel != null && i < sel.length; i++) {
            String pack = (String) sel[i];

            dlm.removeElement(pack);
            prj.delInstr(pack);
        }
        int fsize = dlm.getSize();

        instrList.setPreferredSize(new Dimension(250, 50 + fsize * 20));
    }

    void classpathTextArea_focusLost(FocusEvent e) {
        prj.setClasspath(classpathTextArea.getText().trim());
        loadProjExec( prj );
    }

    void baseClassTextField_focusLost(FocusEvent e) {
        prj.setMain(baseClassTextField.getText().trim());
        loadProjExec( prj );
    }

	public int show( JabutiProject p ) {
		prj = p;
		
      	origAvoidSet = new HashSet();
      	origInstrSet = new HashSet();
      	
    	// Saving the original set of packages to be avoided
		Iterator it = prj.getAvoid().iterator();
		while( it.hasNext() ) {
			origAvoidSet.add( new String( (String) it.next() ) );
		}
    	// Saving the original set of classes to be instrumented
		it = prj.getInstr().iterator();
		
		while( it.hasNext() ) {
			origInstrSet.add( new String( (String) it.next() ) );
		}
		
        super.setVisible(true);
        
        return status;
    }
}