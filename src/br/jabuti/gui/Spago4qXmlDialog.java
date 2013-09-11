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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.Criterion;
import br.jabuti.project.Coverage;
import br.jabuti.project.JabutiProject;


/**
 * 
 * @version: 1.0
 * @author: Andre Takeshi Endo
 */
class Spago4qXmlDialog extends JDialog {  
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -2583465933455573860L;
	
	private JTextPane tp = null; 
	private JScrollPane scrollPane = null;

	public Spago4qXmlDialog() 
	{  
        super(JabutiGUI.mainWindow(), "Spago4Q XML file.", true);         
        Container contentPane = getContentPane();

		tp = new JTextPane();
		tp.setEditable(false);
        scrollPane = new JScrollPane(tp);
        scrollPane.setViewportView(tp);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        contentPane.add(scrollPane,BorderLayout.CENTER);
      
        JButton ok = new JButton("Ok");

        ok.addActionListener(new ActionListener() 
        {  
	        public void actionPerformed(ActionEvent evt) { 
	            setVisible(false); 
	        } 
        } );

        JButton saveFile = new JButton("Save file");
        
        saveFile.addActionListener(new ActionListener() 
        {  
            public void actionPerformed(ActionEvent evt) { 
            	JFileChooser fchooser = new JFileChooser();
            	fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);            	
            	
            	int ret = fchooser.showSaveDialog(null);
            	if(ret == JFileChooser.APPROVE_OPTION)
            	{
            		File file = fchooser.getSelectedFile();
            		try 
            		{
						FileWriter filewriter = new FileWriter(file);
						filewriter.write(tp.getText());
						filewriter.close();
					} 
            		catch (IOException e) {
						e.printStackTrace();
					}
            		
            		JOptionPane.showMessageDialog(null, "The XML file was correctly saved!", "Message", JOptionPane.INFORMATION_MESSAGE);
            	}
            } 
        }
        );
      
        JPanel buttonPanel = new JPanel();

        buttonPanel.add(ok);
        buttonPanel.add(saveFile);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setSize(640, 360);
    }
	
	public void generateXML(JabutiProject jbtproject)
	{
        String text = "<GenericItems>\n";
        for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
        	Coverage coverage = jbtproject.getProjectCoverage(i);

        	text += "  <GenericItem>\n";
        	text += "    <resource>" + jbtproject.getProjectFileName() + "</resource>\n";
        	text += "    <metric>" + AbstractCriterion.getName(i) + "</metric>\n";
        	text += "    <value>" + coverage.getPercentage() + "</value>\n";
        	text += "  </GenericItem>\n";
        }
        text += "</GenericItems>";
        tp.setText(text);		
	}
}