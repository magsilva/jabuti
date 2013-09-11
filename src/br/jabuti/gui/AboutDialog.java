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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import br.jabuti.util.ToolConstants;

/**
 * This class is responsible to show information about
 * the autors of the tool.
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class AboutDialog extends JDialog {  
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 1415420631163977766L;

	public AboutDialog() {  
        super(JabutiGUI.mainWindow(), "About...", true);         
        Container contentPane = getContentPane();

		// add Jabuti About figure
		// Locating the JaBUTi gif logo image     
		
			
        JLabel logo = new JLabel(new ImageIcon(
        	                 ToolConstants.getToolBaseResource(
       	                 	     ToolConstants.aboutLogo)), SwingConstants.CENTER);
        contentPane.add(logo,BorderLayout.WEST);

        // add HTML label to center
        contentPane.add(new JLabel(
                "<HTML><CENTER><P><H2><I>" 
                + ToolConstants.toolName + " " 
                + ToolConstants.toolVersion
                + "</I></H2><HR ALIGN=center WIDTH=80%>" 
                + "<B>Auri Marcelo Rizzo Vincenzi </B><BR>(<TT>auri@fundanet.br</TT>)<P>" 
                + "<B>M&aacute;rcio Eduardo Delamaro<BR>Eric Wong<BR>Jos&eacute; Carlos Maldonado</B>" 
                + "<BR><I>Copyright &copy; 2002</I>.</CENTER><P></HTML>"),
                BorderLayout.EAST);

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
      
        JPanel panel = new JPanel();

        panel.add(ok);
        contentPane.add(panel, BorderLayout.SOUTH);

        setSize(340, 250);
    }
}