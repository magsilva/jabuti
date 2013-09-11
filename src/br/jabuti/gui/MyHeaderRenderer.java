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

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * To support column header icons and tool tips. :-)
 * Usage:
 * >P<
 * >PRE<
 *  MyHeaderRenderer mhr = new MyHeaderRenderer();
 *  mhr.setToolTip(toolTipText);
 *  mhr.setIcon(icon);
 *  TableColumn col = getColumnModel().getColumn(columnIndex);
 *  col.setHeaderRenderer(mhr);
 * >/PRE<
 * This means that you need a new MyHeaderRenderer for each column. The alternative
 * would have been to use only one global renderer and do all the column (index)
 * mappings within this class which would save memory but would be slower. ;-)
 */
class MyHeaderRenderer extends DefaultTableCellRenderer {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -8906289390379315038L;
	/** label to display also an icon */
    JLabel label = null;

    /**
     */
    public MyHeaderRenderer() {
        super();
        label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createRaisedBevelBorder());
    }// constructor

    /**
     * Overwrites DefaultTableCellRenderer.
     */
    public Component getTableCellRendererComponent(JTable table, Object
            value, boolean isSelected,
            boolean hasFocus, int row,
            int column) {

        if (value != null) {
            label.setText("" + value);
        } else {
            label.setText("");
        }

        return label;
    }// getTableCellRendererComponent()

    /**
     * Overwrires DefaultTableCellRenderer.
     */
    protected void setValue(Object value) {
        if (value != null) {
            label.setText("" + value);
        } else {
            label.setText("");
        }
    }// setValue()

    /**
     * @param toolTip text to use for label's tool tip
     */
    public void setToolTip(String toolTip) {
        if (toolTip != null) {
            label.setToolTipText(toolTip);
        }
    }// setToolTip()

    /**
     * @param icon icon to set for the label
     */
    public void setIcon(Icon icon) {
        if (label != null) {
            label.setIcon(icon);
        }
    }// setIcon()

}// MyHeaderRenderer
