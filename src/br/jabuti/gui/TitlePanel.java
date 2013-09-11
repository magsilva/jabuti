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

import javax.swing.JPanel;
import javax.swing.JTextField;

import br.jabuti.util.ToolConstants;

/**
 * This class is responsable to create a JPanel
 * containig the text that is placed in the top
 * of each report window.
 *
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class TitlePanel extends JPanel {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -3322186015055610846L;
	public TitlePanel(String label) {
        super();
		
        setContent(label);
    }
	
    public void setContent(String label) {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constrains = new GridBagConstraints();
		
        constrains.fill = GridBagConstraints.BOTH;			
	
        setLayout(layout);

        constrains.weightx = 1.0;
        constrains.gridwidth = 1;
        constrains.insets = new Insets(20, 10, 20, 10);
			
        makelabel(label, layout, constrains);
    }		
	
    protected void makelabel(String name,
            GridBagLayout gridbag,
            GridBagConstraints c) {
        JTextField label = new JTextField(name);

        label.setHighlighter(null);
        label.setHorizontalAlignment(JTextField.CENTER);
        label.setEditable(false);
        label.setFont(ToolConstants.titleFont);
        gridbag.setConstraints(label, c);
        add(label);
    }
}
