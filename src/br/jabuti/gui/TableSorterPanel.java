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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import br.jabuti.criteria.Criterion;
import br.jabuti.criteria.Requirement;
import br.jabuti.metrics.Metrics;
import br.jabuti.project.TestSet;

public class TableSorterPanel extends JScrollPane {
	/**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -6471777981478770441L;

	//public static final int DEFAULT_COL_SIZE = 3;

	// public static final int TEST_CASE_COL_SIZE = 4;
	// public static final int SLICE_COL_SIZE = 5;

	static int MIN_WIDTH = 60;

	static int NUM_COLUMNS = 3;

	static Object[][] rows;

	static String[] columns;

	static JTable table = new JTable();

	public TableSorterPanel(Object[][] __rows, String[] __columns) {
		this(__rows, __columns, null, JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	public TableSorterPanel(Object[][] __rows, String[] __columns, Object o) {
		this(__rows, __columns, o, JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	public TableSorterPanel(Object[][] __rows, String[] __columns, Object o,
			int mode) {
		super();

		rows = __rows;
		columns = __columns;

		NUM_COLUMNS = __columns.length;

		JTableComponentModel myModel = new JTableComponentModel(__rows,
				__columns, o);// new MyTableModel();

		TableSorter sorter = new TableSorter(myModel); // ADDED THIS

		table = new MyTable(sorter);

		for (int i = 0; i < myModel.getColumnCount(); i++) {
			MyHeaderRenderer mhr = new MyHeaderRenderer();

			if (JabutiGUI.isMetricsPanel() && i > 0)
				mhr.setToolTip(Metrics.metrics[i - 1][1]);
			else
				mhr
						.setToolTip("Click (SHIFT+Click) to sort increasing (decreasing) by "
								+ __columns[i]);
			// mhr.setIcon(icon);
			TableColumn col = table.getTableHeader().getColumnModel()
					.getColumn(i);

			col.setHeaderRenderer(mhr);
		}

		TableCellRenderer defaultRenderer = table
				.getDefaultRenderer(JButton.class);

		table.setDefaultRenderer(JButton.class, new JTableButtonRenderer(
				defaultRenderer));

		defaultRenderer = table.getDefaultRenderer(JTextField.class);
		table.setDefaultRenderer(JTextField.class, new JTableButtonRenderer(
				defaultRenderer));

		defaultRenderer = table.getDefaultRenderer(JProgressBar.class);
		table.setDefaultRenderer(JProgressBar.class, new JTableButtonRenderer(
				defaultRenderer));

		defaultRenderer = table.getDefaultRenderer(JPanel.class);
		table.setDefaultRenderer(JPanel.class, new JTableButtonRenderer(
				defaultRenderer));

		table.addMouseListener(new JTableButtonMouseListener(table));

		table.setRowHeight(25);
		table.setAutoResizeMode(mode);

		sorter.addMouseListenerToHeaderInTable(table); // To provide the

		// Setting the default dimension of the table
		// Dimension dim = JabutiGUI.mainWindow().getSize();

		// Dimension dim = new Dimension( 300, 300 );
		// table.setPreferredScrollableViewportSize(new Dimension(dim.width,
		// (int) (dim.height * 0.8)));

		// adding the panel to the component
		setViewportView(table);

		if (JabutiGUI.isMetricsPanel()) {
			TableColumn column = table.getColumnModel().getColumn(0);
			// column.setMaxWidth(10*TableSorterPanel.MIN_WIDTH);
			column.setPreferredWidth(3 * TableSorterPanel.MIN_WIDTH);
			column.setResizable(true);
		} else {
			// Setting the size of the columns that contain only a check box.
			int fixed = 0;

			if (JabutiGUI.isTestCasePanel()) {
				if (JabutiGUI.isCoverage())
					fixed = 2;
				else
					fixed = 2;
			} else if (JabutiGUI.isRequirementsPanel()) {
				fixed = 3;
			}

			for (int i = 0; i < fixed; i++) {
				TableColumn column = table.getColumnModel().getColumn(i);

				column.setMaxWidth(TableSorterPanel.MIN_WIDTH);
				column.setPreferredWidth(TableSorterPanel.MIN_WIDTH);
				column.setResizable(false);
			}
		}

		// Reseting the column sort variable
		TableSorter.setSortedColumn(-1);
		TableSorter.setOrder(true);
	}

	public static boolean isSuccess(int row) {
		boolean isSuccess = ((Boolean) getTable().getValueAt(row, 0))
				.booleanValue();
		return isSuccess;
	}

	public static boolean isFail(int row) {
		boolean isFail = ((Boolean) getTable().getValueAt(row, 1))
				.booleanValue();
		return isFail;
	}

	// Checks if the same test requirement is covered and
	// infeasible at the same time, indicating an error.
	// In this case, the infeasible checkbox became red.
	public static boolean isAnError(int row) {
		boolean active = ((Boolean) getTable().getValueAt(row, 0))
				.booleanValue();
		boolean infeasible = ((Boolean) getTable().getValueAt(row, 2))
				.booleanValue();

		return (active && infeasible);
	}

	public static JTable getTable() {
		return table;
	}

	public static void main(String[] args) {
		Object[][] __rows = {
				{ JTableComponentModel.addButton("Button One"),
						JTableComponentModel.addLabel("One"),
						JTableComponentModel.addProgress(10, 10, 100) },
				{ JTableComponentModel.addButton("Button Two"),
						JTableComponentModel.addLabel("Two"),
						JTableComponentModel.addProgress(20, 20, 100) },
				{ JTableComponentModel.addButton("Button Three"),
						JTableComponentModel.addLabel("Three"),
						JTableComponentModel.addProgress(95, 95, 100) },
				{ JTableComponentModel.addButton("Button Four"),
						JTableComponentModel.addLabel("Four"),
						JTableComponentModel.addProgress(70, 50, 100) } };

		String[] __columns = { "Buttons", "Progress", "Numbers" };

		JFrame frame = new JFrame();
		TableSorterPanel scrollPane = new TableSorterPanel(__rows, __columns);

		// Add the scroll pane to this window.
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.pack();
		frame.setVisible(true);
	}
}

class MyTable extends JTable {
	/**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 6132223386670765513L;

	TableSorter model;

	public MyTable(TableSorter dm) {
		super(dm);
		model = dm;
	}

	/*
	 * The simplest way of shading alternate rows or columns in a JTable
	 * component is to override the prepareRenderer() method. The table calls
	 * this method for every cell, just prior to displaying it. The override
	 * should call the superclass and retrieve the prepared component. It can
	 * then modify the background and foreground colors to achieve any desired
	 * pattern of shaded rows and columns.
	 */
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {

		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);

		c.setBackground(new JButton().getBackground());

		if (JabutiGUI.isSlice() && JabutiGUI.isTestCasePanel()) {
			if (vColIndex == 0) { // sucess column
				if (TableSorterPanel.isSuccess(rowIndex))
					c.setBackground(Color.green);
			} else if (vColIndex == 1) { // fail column
				if (TableSorterPanel.isFail(rowIndex))
					c.setBackground(Color.red);
			}
		} else if (JabutiGUI.isCoverage() && JabutiGUI.isRequirementsPanel()) {
			if (vColIndex == 2) { // infeasible column
				if (TableSorterPanel.isAnError(rowIndex)) {
					c.setBackground(Color.red);
				}
			}
		}
		return c;
	}

	/*
	 * The simplest way of shading alternate rows or columns in a JTable
	 * component is to override the prepareRenderer() method. The table calls
	 * this method for every cell, just prior to displaying it. The override
	 * should call the superclass and retrieve the prepared component. It can
	 * then modify the background and foreground colors to achieve any desired
	 * pattern of shaded rows and columns. // This table shades every other row
	 * yellow JTable table = new JTable() { public Component
	 * prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
	 * Component c = super.prepareRenderer(renderer, rowIndex, vColIndex); if
	 * (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
	 * c.setBackground(Color.yellow); } else { // If not shaded, match the
	 * table's background c.setBackground(getBackground()); } return c; } };
	 *  // This table shades every other column yellow table = new JTable() {
	 * public Component prepareRenderer(TableCellRenderer renderer, int
	 * rowIndex, int vColIndex) { Component c = super.prepareRenderer(renderer,
	 * rowIndex, vColIndex); if (vColIndex % 2 == 0 && !isCellSelected(rowIndex,
	 * vColIndex)) { c.setBackground(Color.yellow); } else { // If not shaded,
	 * match the table's background c.setBackground(getBackground()); } return
	 * c; } };
	 */
}

class JTableButtonRenderer implements TableCellRenderer {
	private TableCellRenderer __defaultRenderer;

	public JTableButtonRenderer(TableCellRenderer renderer) {
		__defaultRenderer = renderer;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Component) {
			return (Component) value;
		}
		return __defaultRenderer.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
	}
}

class JTableComponentModel extends AbstractTableModel {
	/**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 1772207782326100461L;

	private Object[][] __rows;

	private String[] __columns;

	private Object obj;

	public JTableComponentModel(Object[][] rows, String[] columns) {
		this(rows, columns, null);
	}

	public JTableComponentModel(Object[][] rows, String[] columns, Object o) {
		super();
		__rows = rows;
		__columns = columns;
		obj = o;
	}

	public String getColumnName(int column) {
		return __columns[column];
	}

	public int getRowCount() {
		return __rows.length;
	}

	public int getColumnCount() {
		return __columns.length;
	}

	public Object getValueAt(int row, int column) {
		return __rows[row][column];
	}

	/*
	 * Don't need to implement this method unless your table's data can change.
	 */
	public void setValueAt(Object value, int row, int col) {
		if (__rows[row][col] instanceof Boolean) {
			// Saving the original boolean value
			boolean oldV = ((Boolean) __rows[row][col]).booleanValue();

			__rows[row][col] = value;

			if (JabutiGUI.isTestCasePanel()) {
				if (JabutiGUI.isCoverage()) {
					JButton button = (JButton) __rows[row][2];

					String name = button.getText();

					if (col == 0) { // Check if the active is selected
						if (((Boolean) value).booleanValue()) {
							// System.out.println("Test Case: " + name + " is
							// enabled");
							TestSet.activateTestCase(JabutiGUI.getProject(),
									name);
						} else {
							// System.out.println("Test Case: " + name + " is
							// disabled");
							TestSet.desactivateTestCase(JabutiGUI.getProject(),
									name);
						}
					} else if (col == 1) { // Check if the fail is selected
						if (((Boolean) value).booleanValue()) {
							// System.out.println("Test Case: " + name + " is
							// deleted");
							TestSet.toDeleteTestCase(JabutiGUI.getProject(),
									name);
						} else {
							// System.out.println("Test Case: " + name + " is
							// undeleted");
							TestSet.undeleteTestCase(JabutiGUI.getProject(),
									name);
						}
					}
					if (col == 0 || col == 1) {
						JabutiGUI.getProject().coverageChanges();
						JabutiGUI.mainWindow().setUpdateLabelImage(
								JabutiGUI.mainWindow().getSemaforoRedImage());
					}
				} else if (JabutiGUI.isSlice()) {
					JButton button = (JButton) __rows[row][2];

					String name = button.getText();

					if (col == 0) { // Check if the success is selected
						if (!((Boolean) value).booleanValue()) {
							TestSet.removeFromSuccessSet(
									JabutiGUI.getProject(), name);
						} else {
							if (TestSet.getSuccessSet().size() == 2) {
								JOptionPane
										.showMessageDialog(
												null,
												"At most two success test cases can be selected! ",
												"Warning",
												JOptionPane.WARNING_MESSAGE);
								__rows[row][col] = new Boolean(false);
							} else {
								TestSet.addToSuccessSet(JabutiGUI.getProject(),
										name);
								__rows[row][col + 1] = new Boolean(false); // Disable
																			// the
																			// fail
																			// check
																			// box
							}
						}
					} else if (col == 1) { // Check if the fail is selected
						if (!((Boolean) value).booleanValue()) {
							TestSet.removeFromFailSet(JabutiGUI.getProject(),
									name);
						} else {
							if (TestSet.getFailSet().size() == 2) {
								JOptionPane
										.showMessageDialog(
												null,
												"No more than two failed test cases can be selected! ",
												"Warning",
												JOptionPane.WARNING_MESSAGE);
								__rows[row][col] = new Boolean(false);
							} else {
								TestSet.addToFailSet(JabutiGUI.getProject(),
										name);
								__rows[row][col - 1] = new Boolean(false); // Disable
																			// the
																			// fail
																			// check
																			// box
							}
						}
					}
					if (col == 0 || col == 1) {
						JabutiGUI.getProject().coverageChanges();
						JabutiGUI.mainWindow().setUpdateLabelImage(
								JabutiGUI.mainWindow().getSemaforoRedImage());
					}
				}
			} else if (JabutiGUI.isRequirementsPanel()) {
				JButton button = (JButton) __rows[row][3];
				String name = button.getText();

				Criterion criterion = (Criterion) obj;

				// Getting the testing requirement
				Requirement req = criterion.getRequirementByLabel(name);
				boolean newV = oldV;
				if (col == 1) { // Activated
					if (!((Boolean) value).booleanValue()) {
						// Desactivating the testing requirement
						newV = criterion.setInactive(req);

						__rows[row][col] = new Boolean(!newV);

						newV = !newV;
					} else {
						System.out.println(name + "Activated");
						// Activating the testing requirement
						newV = criterion.setActive(req);

						__rows[row][col] = new Boolean(newV);
					}
				} else if (col == 2) { // Feasible
					if (((Boolean) value).booleanValue()) {
						// Setting as infeasible
						newV = criterion.setInfeasible(req);

						__rows[row][col] = new Boolean(newV);
					} else {
						// Setting as feasible
						newV = criterion.setFeasible(req);

						__rows[row][col] = new Boolean(!newV);

						newV = !newV;
					}
				}
				if ((col == 1 || col == 2) && (oldV != newV)) {
					JabutiGUI.getProject().coverageChanges();
					JabutiGUI.mainWindow().setUpdateLabelImage(
							JabutiGUI.mainWindow().getSemaforoRedImage());
				}
			}

			fireTableCellUpdated(row, col);
		}
	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	public boolean isCellEditable(int row, int col) {
		int min = 0;
		int max = 0;

		if (JabutiGUI.isTestCasePanel()) {
			if (JabutiGUI.isCoverage())
				max = 2;
			else if (JabutiGUI.isSlice())
				max = 2;
		} else if (JabutiGUI.isRequirementsPanel()) {
			min = 1;
			max = 3;
		}
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		if (col >= min && col < max) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * public boolean isCellEditable(int row, int column) { return false; }
	 */

	public Class getColumnClass(int column) {
		return getValueAt(0, column).getClass();
	}

	public static JButton addButton(String name) {
		final String bName = name;
		JButton button = new JButton(bName);

		button.setFocusPainted(false);
		button.setHorizontalAlignment(SwingConstants.LEFT);

		/*
		 * button.addActionListener(new java.awt.event.ActionListener() { public
		 * void actionPerformed(ActionEvent e) {
		 * JabutiGUI.mainWindow().tablePanelButtons_actionPerformed(e); } });
		 */

		button.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JabutiGUI.mainWindow().tablePanelButtons_MouseClicked(bName);
			}
		});

		return button;
	}

	public static JTextField addLabel(String text) {
		JTextField label = new JTextField(text);

		label.setHighlighter(null);
		label.setHorizontalAlignment(JTextField.CENTER);
		label.setEditable(false);
		label.setFont((new JButton()).getFont());

		return label;
	}

	public static JProgressBar addProgress(int value) {
		JProgressBar bar = new JProgressBar();

		bar.setValue(value);
		bar.setStringPainted(true);
		bar.setBackground(Color.gray);
		bar.setForeground(Color.cyan.darker());

		return bar;
	}

	public static JPanel addProgress(int value, int width, int maxWidth) {
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constrains = new GridBagConstraints();

		panel.setLayout(layout);

		// panel.setBorder( BorderFactory.createEmptyBorder() );

		constrains.weightx = 2.0;
		constrains.gridwidth = GridBagConstraints.REMAINDER;
		constrains.anchor = GridBagConstraints.WEST;
		constrains.fill = GridBagConstraints.VERTICAL;

		JProgressBar bar = new JProgressBar();

		int windowWidth = (int) (JabutiGUI.mainWindow().getSize().getWidth() - (7 * TableSorterPanel.NUM_COLUMNS))
				/ TableSorterPanel.NUM_COLUMNS;

		// ERROR: division by zero when there is a testing criteria
		// that generates no testing requirements
		// width = (int) (( windowWidth * width ) / maxWidth );

		if (maxWidth != 0)
			width = (int) ((windowWidth * width) / maxWidth);
		else
			width = 0;

		Dimension dim = new Dimension(width, 23);

		bar.setValue(value);
		bar.setStringPainted(true);
		bar.setBackground(Color.gray);
		bar.setForeground(Color.cyan.darker());

		bar.setPreferredSize(dim);

		layout.setConstraints(bar, constrains);

		panel.add(bar);

		return panel;
	}

	public static JCheckBox addCheckBox(String label, boolean selected) {
		JCheckBox check = new JCheckBox(label);

		check.setSelected(selected);

		// return new Boolean( selected );
		return check;
	}

	public static Boolean addCheckBox(boolean selected) {
		return new Boolean(selected);
	}
}

class JTableButtonMouseListener implements MouseListener {
	private JTable __table;

	private void __forwardEventToButton(MouseEvent e) {
		TableColumnModel columnModel = __table.getColumnModel();
		int column = columnModel.getColumnIndexAtX(e.getX());
		int row = e.getY() / __table.getRowHeight();
		Object value;
		JButton button;
		MouseEvent buttonEvent;

		if (row >= __table.getRowCount() || row < 0
				|| column >= __table.getColumnCount() || column < 0) {
			return;
		}

		value = __table.getValueAt(row, column);

		if (!(value instanceof JButton)) {
			return;
		}

		button = (JButton) value;

		buttonEvent = (MouseEvent) SwingUtilities.convertMouseEvent(__table, e,
				button);
		button.dispatchEvent(buttonEvent);
		// This is necessary so that when a button is pressed and released
		// it gets rendered properly. Otherwise, the button may still appear
		// pressed down when it has been released.
		__table.repaint();
	}

	public JTableButtonMouseListener(JTable table) {
		__table = table;
	}

	public void mouseClicked(MouseEvent e) {
		__forwardEventToButton(e);
	}

	public void mouseEntered(MouseEvent e) {
		__forwardEventToButton(e);
	}

	public void mouseExited(MouseEvent e) {
		__forwardEventToButton(e);
	}

	public void mousePressed(MouseEvent e) {
		__forwardEventToButton(e);
	}

	public void mouseReleased(MouseEvent e) {
		__forwardEventToButton(e);
	}
}
