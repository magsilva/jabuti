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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * This class is responsible to show information about
 * the autors of the tool.
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class AvailableMemoryDialog extends JDialog {  
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -2583465933455573860L;

	public AvailableMemoryDialog() {  
        super(JabutiGUI.mainWindow(), "Free Memory Available...", true);         
        Container contentPane = getContentPane();

		JPanel panel = new JPanel();
		
		panel.setLayout( new GridLayout(4,1) );
		
		System.gc();
		
		//Getting the total amount of memory
		Runtime runtime = Runtime.getRuntime();
		
		long factor = 1048576;
		String unit = "Mb";
		
		long totalMemory = runtime.totalMemory() / factor;
		long freeMemory = runtime.freeMemory() / factor;
		long usedMemory = totalMemory - freeMemory;

//		long maxMemory = runtime.maxMemory() / factor;
				
//		JLabel maxLabel = new JLabel( "Maximum Memory: " + maxMemory + unit );
		JLabel totalLabel = new JLabel( "Total Memory: " + totalMemory + unit );
		JLabel freeLabel = new JLabel( "Free Memory: " + freeMemory + unit );		
		JLabel usedLabel = new JLabel( "Used Memory: " + usedMemory + unit );
				
//		panel.add( maxLabel );
		panel.add( totalLabel );
		panel.add( freeLabel );
		panel.add( usedLabel );

        contentPane.add(panel,BorderLayout.CENTER);

        // Ok button closes the dialog
      
        JButton ok = new JButton("Ok");

        ok.addActionListener(new 
                ActionListener() {  
                    public void actionPerformed(ActionEvent evt) { 
                        setVisible(false); 
                    } 
                }
                );

        // add Ok button to southern border
      
        JPanel buttonPanel = new JPanel();

        buttonPanel.add(ok);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setSize(340, 250);
    }
}