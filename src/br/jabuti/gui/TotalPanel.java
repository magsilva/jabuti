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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;



/**
 * This class is responsible to create the table panel
 * to show the coverage results 
 *
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class TotalPanel extends JPanel {
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -8874727409224892784L;

	public TotalPanel( String label, String coverage, int percentage, int top, int bottom ) {
        super();
		
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constrains = new GridBagConstraints();
			
        setLayout(layout);

        constrains.weightx = 2.0;
        constrains.gridwidth = 1;
        constrains.fill = GridBagConstraints.BOTH;
        constrains.anchor = GridBagConstraints.NORTHWEST;
        constrains.insets = new Insets(top, 10, bottom, 0);
			
        makebutton(label, layout, constrains );

        constrains.insets = new Insets(top, 0, bottom, 0);
        								
        makelabel(coverage, layout, constrains);
	    		
        constrains.gridwidth = GridBagConstraints.REMAINDER;

	   	constrains.insets = new Insets(top, 0, bottom, 10);
	   	
        makeprogress(percentage, layout, constrains);
    }	

    protected void makebutton(String name,
            GridBagLayout gridbag,
            GridBagConstraints c ) {
        JButton button = new JButton(name);

        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        gridbag.setConstraints(button, c);
        add(button);
    }

    protected void makelabel(String name,
            GridBagLayout gridbag,
            GridBagConstraints c) {
        JTextField label = new JTextField(name);

        label.setHighlighter(null);
        label.setHorizontalAlignment(JTextField.CENTER);
        label.setEditable(false);
        label.setFont((new JButton()).getFont());
        gridbag.setConstraints(label, c);
        add(label);
    }
    
    protected void makeprogress(int grow,
            GridBagLayout gridbag,
            GridBagConstraints c) {
        JProgressBar bar = new JProgressBar();

        bar.setValue(grow);
        bar.setStringPainted(true);
        bar.setBackground(Color.gray);
        bar.setForeground(Color.cyan.darker());
			
        gridbag.setConstraints(bar, c);
       
        add(bar);
    }
}
