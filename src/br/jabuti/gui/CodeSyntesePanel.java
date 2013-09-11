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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import br.jabuti.util.ToolConstants;

/**
 * This class is reponsable to shows the panel 
 * in the end of the source code and bytecode panel.
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class CodeSyntesePanel extends JPanel {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 5401513057226944371L;

	JTextField field1, field2, field3, field4, field5;
			
    public CodeSyntesePanel(String l1, String l2, String l3, String l4, String l5) {
        super();
        
        field1 = new JTextField();
        field2 = new JTextField();
        field3 = new JTextField();
        field4 = new JTextField();
        field5 = new JTextField();				
        setContent(l1, l2, l3, l4, l5);
    }
	
    public void setContent(String l1, String l2, String l3, String l4, String l5) {
        this.removeAll();
		
        setBorder(BorderFactory.createEtchedBorder());
		
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constrains = new GridBagConstraints();
			
        constrains.fill = GridBagConstraints.BOTH;			
	
        setLayout(layout);
	
        constrains.weightx = 2.0;
        constrains.gridwidth = 1;
        constrains.insets = new Insets(20, 10, 10, 10);
        makelabel(field1, l1, layout, constrains);
        constrains.insets = new Insets(20, 10, 10, 10);			
        makelabel(field2, l2, layout, constrains);
        makelabel(field3, l3, layout, constrains);
        makelabel(field4, l4, layout, constrains);
        constrains.insets = new Insets(20, 10, 10, 10);				    	
        constrains.gridwidth = GridBagConstraints.REMAINDER;
        makelabel(field5, l5, layout, constrains);	
    }
	
    protected void makelabel(JTextField label, String name,
            GridBagLayout gridbag,
            GridBagConstraints c) {
        label.setText(name);
        label.setToolTipText(name);
        label.setHighlighter(null);
        label.setHorizontalAlignment(JTextField.CENTER);
        label.setEditable(false);
        label.setFont(ToolConstants.titleFont);
        gridbag.setConstraints(label, c);
        add(label);
    }
    
    public void setLineLabel(String line) {
        field3.setText(line);
        field3.setToolTipText(line);
    }
}