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


package br.jabuti.metrics;


/**
 * @version 1.00 1999-07-17
 * @author Cay Horstmann
 */

import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.w3c.dom.Document;

import br.jabuti.lookup.Program;
import br.jabuti.util.HTMLGen;
import br.jabuti.util.ToolConstants;
import br.jabuti.util.XMLPrettyPrinter;


class SortFilterModel extends AbstractTableModel {
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 3038790791099591096L;

	public SortFilterModel(TableModel m) {
        model = m;
        rows = new Row[model.getRowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new Row();
            rows[i].index = i;
        }
        
        System.out.println("Number of Metrics: " + Metrics.metrics.length );
        columnTip = new String[Metrics.metrics.length + 1];
        columnTip[0] = new String( "Instrumented Classes" );
        for( int i = 0; i < Metrics.metrics.length; i++ ) {
        	columnTip[i+1] = Metrics.metrics[i][1];
        }
    }

    public void sort(int c) {
        sortColumn = c;
        Arrays.sort(rows);
        fireTableDataChanged();
    }

    public void addMouseListener(final JTable table) {
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {  // check for double click
                if (event.getClickCount() < 2) {
                    return;
                }

                // find column of click and
                int tableColumn
                        = table.columnAtPoint(event.getPoint());

                // translate to table model index and sort
                int modelColumn
                        = table.convertColumnIndexToModel(tableColumn);

                sort(modelColumn);

				Document doc = HTMLGen.jtable2HTML( HTMLGen.getBody( "Static Metrics" ), 
					"Static Metrics", 
					table );
					
				XMLPrettyPrinter.writeDocument( doc, "metrics.html" );
                
            }
        }
        );
    }

    public void addMouseMotionListener(final JTable table) {
        table.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent event) {  
                // find column of click and
                int tableColumn
                        = table.columnAtPoint(event.getPoint());
                  
                table.getTableHeader().setToolTipText(columnTip[ tableColumn ]);
            }
        }
        );
    }

    /* compute the moved row for the three methods that access
     model elements
     */

    public Object getValueAt(int r, int c) {
        return model.getValueAt(rows[r].index, c);
    }

    public boolean isCellEditable(int r, int c) {  // return model.isCellEditable(rows[r].index, c);
        return false;
    }

    public void setValueAt(Object aValue, int r, int c) {
        model.setValueAt(aValue, rows[r].index, c);
    }

    /* delegate all remaining methods to the model
     */

    public int getRowCount() {
        return model.getRowCount();
    }

    public int getColumnCount() {
        return model.getColumnCount();
    }

    public String getColumnName(int c) {
        return model.getColumnName(c);
    }

    public Class getColumnClass(int c) {
        return model.getColumnClass(c);
    }

    /* this inner class holds the index of the model row
     Rows are compared by looking at the model row entries
     in the sort column
     */

    private class Row implements Comparable {
        public int index;
        public int compareTo(Object other) {
            Row otherRow = (Row) other;
            Object a = model.getValueAt(index, sortColumn);
            Object b = model.getValueAt(otherRow.index, sortColumn);

            if (a instanceof Comparable) {
                return ((Comparable) a).compareTo(b);
            } else {
                return index - otherRow.index;
            }
        }
    }

    private TableModel model;
    private int sortColumn;
    private Row[] rows;
   
    String[] columnTip;
}


public class TableSortTest extends JFrame {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 1004145705236081303L;
	private Object[][] cells;
    private String[] columnNames;

    public TableSortTest(String[] args) {
        setTitle("TableSortTest");
        setSize(300, 200);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        }
        );

        try {
            Program p = null;

            p = new Program(args[0], true, null, args[1]);
            //String[] classes = {"br.jabuti.metrics.Metrics"};
            String[] classes = p.getCodeClasses();
            
            Metrics mt = new Metrics(p, classes);

            columnNames = new String[Metrics.metrics.length + 1];
            columnNames[0] = new String("Class Name");
            for (int i = 0; i < Metrics.metrics.length; i++) {
                columnNames[i + 1] = Metrics.metrics[i][0];
            }
	  
            //String[] classNames = p.getCodeClasses();
            String[] classNames = classes;
	  
            cells = new Object[classNames.length][Metrics.metrics.length + 1];
            for (int i = 0; i < classNames.length; i++) {
                cells[i][0] = classNames[i];
                Object[] metrics = mt.getClassMetrics(classNames[i]);

                for (int j = 0; j < metrics.length; j++) {
                    cells[i][j + 1] = metrics[j];
                }
            }
        } catch (Exception e) {
            ToolConstants.reportException(e, ToolConstants.STDERR);
            System.exit(0);
        }
        // set up table model and interpose sorter
        DefaultTableModel model
                = new DefaultTableModel(cells, columnNames);
        SortFilterModel sorter = new SortFilterModel(model);

        // show table
        JTable table = new JTable(sorter);

        table.getTableHeader().setToolTipText("My Header");
        // table.setToolTipText( "My Table" );
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Container contentPane = getContentPane();

        contentPane.add(new JScrollPane(table), "Center");
        // set up double click handler for column headers

        sorter.addMouseListener(table);
        sorter.addMouseMotionListener(table);

		Document doc = HTMLGen.jtable2HTML( HTMLGen.getBody( "Static Metrics" ), 
			"Static Metrics", 
			table );
			
		XMLPrettyPrinter.writeDocument( doc, "metrics.html" );
    }
    public static void main(String[] args) {
        JFrame frame = new TableSortTest(args);

        frame.setVisible(true);
    }
}
