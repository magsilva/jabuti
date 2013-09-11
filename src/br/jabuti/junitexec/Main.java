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


package br.jabuti.junitexec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Main extends JDialog implements ActionListener {

	private JPanel statusPanel;

	private JScrollPane statusScrollPanel;

	private JTextField code, junit, jabutiLib, otherLibs, tsuit, jsource;

	private JTextField java, javac;

	private JButton codeBrowse1, codeBrowse2, junitBrowse1, junitBrowse2,
			libBrowse1, jsourceBrowse1;

	private JTextArea textArea;

	private JButton compile;

	private JButton run;

	private JButton importe;

	private JButton clear;

	private JButton selectAll;

	private JButton unselectAll;

	private HashMap<String, JCheckBox> checkBoxes;

	private JFileChooser dirChooser = new JFileChooser(),
			jarChooser = new JFileChooser();

	private JTextField trace;

	static private final String JAVA = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

	static private String JAVAC = "javac";

	// static private final String JUNIT_CORE = "org.junit.runner.JUnitCore";
	static private final String JUNIT_CORE = "br.jabuti.junitexec.JUnitJabutiCore";

	public Main(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		JPanel j1 = new JPanel(new BorderLayout());

		JPanel jleft = new JPanel(new GridLayout(8, 1));
		JPanel j = new JPanel();
		FlowLayout fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);
		j.add(new JLabel("Path to application binary code: "));

		code = new JTextField(30);
		j.add(code);
		codeBrowse1 = new JButton("Directory");
		codeBrowse2 = new JButton("JAR file");
		codeBrowse1.addActionListener(this);
		codeBrowse2.addActionListener(this);
		j.add(codeBrowse1);
		j.add(codeBrowse2);
		jleft.add(j);

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);
		j.add(new JLabel("Path to JUnit test suite source code: "));
		jsource = new JTextField(30);
		j.add(jsource);
		jsourceBrowse1 = new JButton("Browse");
		j.add(jsourceBrowse1);
		jleft.add(j);
		jsourceBrowse1.addActionListener(this);

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);
		j.add(new JLabel("Path to JUnit test suite binary code: "));
		junit = new JTextField(30);
		j.add(junit);
		junitBrowse1 = new JButton("Directory");
		junitBrowse2 = new JButton("JAR file");
		j.add(junitBrowse1);
		j.add(junitBrowse2);
		jleft.add(j);
		junitBrowse1.addActionListener(this);
		junitBrowse2.addActionListener(this);

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);
		j.add(new JLabel("Test suite full qualified name: "));
		tsuit = new JTextField(30);
		j.add(tsuit);
		jleft.add(j);

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);
		j.add(new JLabel("JaBUTi's library: "));
		jabutiLib = new JTextField(30);
		j.add(jabutiLib);

		libBrowse1 = new JButton("Browse");
		libBrowse1.addActionListener(this);
		j.add(libBrowse1);
		jleft.add(j);

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);
		j.add(new JLabel("Others application specific libraries: "));
		otherLibs = new JTextField(30);
		j.add(otherLibs);
		jleft.add(j);

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);

		String s = System.getProperty("java.home");
		if (s.endsWith("jre"))
			s = s.substring(0, s.length() - 3);

		if (!s.endsWith(File.separator))
			s += File.separator;

		JAVAC = "javac";
		JAVAC = s + "bin" + File.separator + JAVAC;

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.LEFT);
		j.add(new JLabel("javac"));
		javac = new JTextField(30);
		javac.setText(JAVAC);
		j.add(javac);

		// j.add(new JLabel("java"));
		java = new JTextField(30);
		java.setText(JAVA);
		// j.add(java);

		j.add(new JLabel("Trace file name"));
		trace = new JTextField(30);
		j.add(trace);

		jleft.add(j);

		j = new JPanel();
		fl = (FlowLayout) j.getLayout();
		fl.setAlignment(FlowLayout.CENTER);
		compile = new JButton("Compile Test Case");
		run = new JButton("Run Normally (No Trace)");
		importe = new JButton("Run Collecting Trace Information");
		importe.setEnabled(false);
		clear = new JButton("Clear text");
		compile.addActionListener(this);
		run.addActionListener(this);
		importe.addActionListener(this);
		clear.addActionListener(this);
		j.add(compile);
		j.add(run);
		j.add(importe);
		j.add(clear);

		jleft.add(j);

		// Test case result panel
		statusPanel = new JPanel();

		JPanel tcPanel = new JPanel();
		JPanel tcButtonPanel = new JPanel();
		statusScrollPanel = new JScrollPane();

		tcPanel.setLayout(new BorderLayout());
		JLabel topLabel = new JLabel("Test Cases Execution Status");
		tcPanel.add(topLabel, BorderLayout.NORTH);

		tcPanel.add(statusScrollPanel, BorderLayout.CENTER);

		tcButtonPanel.setLayout(new java.awt.FlowLayout(FlowLayout.LEFT));

		selectAll = new JButton("Select All");
		selectAll.setToolTipText("Select all successful/fail test cases");
		selectAll.setEnabled(false);
		selectAll.addActionListener(this);

		tcButtonPanel.add(selectAll);

		unselectAll = new JButton("Unselect All");
		unselectAll.setToolTipText("Unselect all successful/fail test cas");
		unselectAll.setEnabled(false);
		unselectAll.addActionListener(this);

		tcButtonPanel.add(unselectAll);
		statusScrollPanel.setViewportView(statusPanel);

		tcPanel.add(tcButtonPanel, BorderLayout.SOUTH);

		// Text output area
		JPanel jright = new JPanel();
		textArea = new JTextArea(30, 80);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		JScrollPane jsp = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setPreferredSize(new Dimension(800, 400));
		jright.add(jsp);

		jarChooser.setFileFilter(new JarFilter());
		jarChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jarChooser.setAcceptAllFileFilterUsed(false);
		// File f = new File(System.getProperty("user.dir"));
		// jarChooser.setCurrentDirectory(f);

		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dirChooser.setAcceptAllFileFilterUsed(false);
		// dirChooser.setCurrentDirectory(f);

		jleft.setBorder(BorderFactory.createEtchedBorder());
		jright.setBorder(BorderFactory.createEtchedBorder());
		j1.add(jleft, BorderLayout.NORTH);
		j1.add(jright, BorderLayout.SOUTH);

		JPanel j3 = new JPanel();
		j3.setLayout(new BorderLayout());
		j3.add(j1, BorderLayout.WEST);
		j3.add(tcPanel, BorderLayout.EAST);

		this.getContentPane().add(j3);
		this.setTitle("JUnit JaBUTi Integrator - V 1.0");
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == codeBrowse1) {
			int returnVal = dirChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					code.setText(dirChooser.getSelectedFile()
							.getCanonicalPath());
				} catch (IOException e1) {
					textArea.append("\n\n" + e1.getClass() + ": "
							+ e1.getMessage() + "\n");
				}
			}
		}
		if (e.getSource() == codeBrowse2) {
			int returnVal = jarChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					code.setText(jarChooser.getSelectedFile()
							.getCanonicalPath());
				} catch (IOException e1) {
					textArea.append("\n\n" + e1.getClass() + ": "
							+ e1.getMessage() + "\n");
				}
			}
		}
		if (e.getSource() == junitBrowse1) {
			int returnVal = dirChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					junit.setText(dirChooser.getSelectedFile().getCanonicalPath());
					compile.setEnabled(true);
				} catch (IOException e1) {
					textArea.append("\n\n" + e1.getClass() + ": " + e1.getMessage() + "\n");
				}
			}
		}
		if (e.getSource() == junitBrowse2) {
			int returnVal = jarChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					junit.setText(jarChooser.getSelectedFile()
							.getCanonicalPath());
					compile.setEnabled(false);
				} catch (IOException e1) {
					textArea.append("\n\n" + e1.getClass() + ": "
							+ e1.getMessage() + "\n");
				}
			}
		}

		if (e.getSource() == libBrowse1) {
			int returnVal = jarChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					jabutiLib.setText(jarChooser.getSelectedFile()
							.getCanonicalPath());
				} catch (IOException e1) {
					textArea.append("\n\n" + e1.getClass() + ": "
							+ e1.getMessage() + "\n");
				}
			}
		}
		if (e.getSource() == jsourceBrowse1) {
			int returnVal = dirChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					jsource.setText(dirChooser.getSelectedFile()
							.getCanonicalPath());
				} catch (IOException e1) {
					textArea.append("\n\n" + e1.getClass() + ": "
							+ e1.getMessage() + "\n");
				}
			}
		}
		
		// Compile
		if (e.getSource() == compile) {
			String outpath = junit.getText().trim();
			String compiler = javac.getText().trim();
			File f = new File(compiler);
			if (! f.exists()) {
				String compiler2 = compiler + ".exe";
				f = new File(compiler2);
				if (! f.exists()) {
					JOptionPane.showMessageDialog(this,
									"Java compiler not found in this location: " + compiler +
									"\nPlease, includes the complete full path to javac 1.5 or above and try again.",
									"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				compiler = compiler2;
				javac.setText(compiler);
			}

			String classpath = code.getText().trim() + File.pathSeparator
					+ jabutiLib.getText().trim() + File.pathSeparator
					+ otherLibs.getText().trim() + File.pathSeparator
					+ jsource.getText().trim() + File.pathSeparator
					+ outpath;

			String sourcepath = jsource.getText().trim() + File.separator;
			sourcepath += tsuit.getText().trim().replace('.', File.separatorChar);

			textArea.append("\nCompiling the test set...\n");
			textArea.append(compiler + " -source 1.5 -target 1.5" + " -cp " + classpath + " -d " + outpath + " " + sourcepath + ".java\n");

			ProcessBuilder pb = new ProcessBuilder(compiler, "-source", "1.5", "-target", "1.5", "-cp", classpath, "-d", outpath, sourcepath + ".java");
			pb.redirectErrorStream(true);
			try {
				Process pr = pb.start();
				InputStream stderr = pr.getInputStream();
				InputStreamReader isr = new InputStreamReader(stderr);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				int exitVal;
				
				textArea.append("\n");
				while ((line = br.readLine()) != null)
					textArea.append(line + "\n");

				exitVal = pr.waitFor();
				textArea.append("Process exitValue: " + exitVal);
				textArea.append("\n...Finished.");
			} catch (Exception ex) {
				textArea.append("\nException:" + ex.getClass());
				return;
			}
			textArea.append("\n");

			selectAll.setEnabled(false);
			unselectAll.setEnabled(false);
			importe.setEnabled(false);
		}
		
		// Run
		if (e.getSource() == run) {
			String classpath = code.getText().trim() + File.pathSeparator
					+ junit.getText().trim() + File.pathSeparator
					+ jabutiLib.getText().trim() + File.pathSeparator
					+ otherLibs.getText().trim();

			textArea.append("\n\nRunning to get tests set information...\n");
			textArea.append(java.getText().trim() +
					" -XX:-UseSplitVerifier" +
					" -cp "	+ jabutiLib.getText().trim() + File.pathSeparator + otherLibs.getText().trim() +
					" " + JUNIT_CORE +
					" -cp "	+ classpath +
					" -tcClass " + tsuit.getText().trim() + "\n");

			File temp = null;
			try {
				temp = File.createTempFile("log-junit-jabuti", ".log");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			HashMap<String, String> hm = new HashMap<String, String>();
			PrintStream ps = System.out;
			try {
				temp.deleteOnExit();
				FileOutputStream fos = new FileOutputStream(temp);
				ps = new PrintStream(fos);

				hm = JUnitJabutiCore.runCollecting(classpath, tsuit.getText().trim(), ps);
				fos.close();
			} catch (Exception e1) {
				e1.printStackTrace(ps);
			} finally {
				ps.close();
			}

			FileInputStream is;
			try {
				is = new FileInputStream(temp);
				while (is.available() > 0) {
					byte[] b = new byte[is.available()];
					is.read(b);
					textArea.append(new String(b));
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			textArea.append("\nFinished.\n");

			// Insert the resultant test cases status info
			checkBoxes = new HashMap<String, JCheckBox>();
			Iterator it = hm.keySet().iterator();
			statusPanel.removeAll();

			// Clearing the current test case panel
			statusPanel = new JPanel();

			// Preparating the test case panel
			statusPanel.setMaximumSize(new java.awt.Dimension(200, selectAll.getHeight() * hm.size()));
			statusPanel.setMinimumSize(new java.awt.Dimension(200, selectAll.getHeight() * hm.size()));
			statusPanel.setPreferredSize(new java.awt.Dimension(200, selectAll.getHeight() * hm.size()));
			statusPanel.setLayout(new javax.swing.BoxLayout(statusPanel, javax.swing.BoxLayout.Y_AXIS));

			while (it.hasNext()) {
				String tc = (String) it.next();
				String st = hm.get(tc);

				JCheckBox check = new JCheckBox(tc.trim());
				check.setSelected(true);

				// Success - painted in gree
				if (JUnitUtil.SUCCESS.equals(st)) {
					check.setBackground(Color.green);
				} else if (JUnitUtil.FAILURE.equals(st)) {
					check.setBackground(Color.red);
				} else {
					check.setBackground(Color.cyan);
					check.setSelected(false);
					check.setEnabled(false);
				}

				statusPanel.add(check);
				checkBoxes.put(tc.trim(), check);
			}
			statusScrollPanel.setViewportView(statusPanel);
			statusPanel.validate();

			if (checkBoxes.size() > 0) {
				selectAll.setEnabled(true);
				unselectAll.setEnabled(true);
				importe.setEnabled(true);
			}
		}
		if (e.getSource() == clear) {
			textArea.setText(null);
		}
		if (e.getSource() == importe) {
			Iterator it = checkBoxes.values().iterator();
			Set<String> ts = new HashSet<String>();
			StringBuffer sb = new StringBuffer();
			while (it.hasNext()) {
				JCheckBox cb = (JCheckBox) it.next();
				if (cb.isSelected()) {
					ts.add(new String(cb.getText()));
					sb.append(new String(cb.getText()) + " ");
				}
			}

			if (ts.size() > 0) {
				String classpath = code.getText().trim() + File.pathSeparator
						+ junit.getText().trim() + File.pathSeparator
						+ jabutiLib.getText().trim() + File.pathSeparator
						+ otherLibs.getText().trim();

				textArea
						.append("\n--------------------------------------------------------\nRunning to get trace information...\n");

				File temp = null;
				try {
					temp = File
							.createTempFile("log-instr-junit-jabuti", ".log");
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				if (trace.getText() == null || trace.getText().length() == 0) {
					JOptionPane
							.showMessageDialog(
									this,
									"Empty trace file name field. A trace file name should be provided to continue.",
									"Error", JOptionPane.ERROR_MESSAGE);
				} else {

					textArea.append(java.getText().trim() + " -cp "
							+ jabutiLib.getText().trim() + File.pathSeparator
							+ otherLibs.getText().trim() + " " + JUNIT_CORE
							+ " -cp " + classpath + " -trace "
							+ trace.getText().trim() + " -tcClass "
							+ tsuit.getText().trim() + " " + sb.toString()
							+ "\n");

					PrintStream ps = System.out;
					try {
						temp.deleteOnExit();
						FileOutputStream fos = new FileOutputStream(temp);
						ps = new PrintStream(fos);

						JUnitJabutiCore.runInstrumenting(classpath, tsuit
								.getText().trim(), trace.getText().trim(), ts,
								ps);
						fos.close();
					} catch (Exception e1) {
						e1.printStackTrace(ps);
					} finally {
						ps.close();
					}

					FileInputStream is = null;
					try {
						is = new FileInputStream(temp);

						while (is.available() > 0) {
							byte[] b = new byte[is.available()];
							is.read(b);
							textArea.append(new String(b));
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				textArea.append("\n...Finished.\n");
			}
		}
		if (e.getSource() == selectAll) {
			Iterator it = checkBoxes.values().iterator();
			while (it.hasNext()) {
				JCheckBox cb = (JCheckBox) it.next();
				if (cb.isEnabled())
					cb.setSelected(true);
			}
		}
		if (e.getSource() == unselectAll) {
			Iterator it = checkBoxes.values().iterator();
			while (it.hasNext()) {
				JCheckBox cb = (JCheckBox) it.next();
				if (cb.isEnabled())
					cb.setSelected(false);
			}
		}

	}

	public void setJabutiLib(String string) {
		jabutiLib.setText(string);
	}

	public String getJabutiLib() {
		return jabutiLib.getText();
	}

	public void setOtherLibs(String string) {
		otherLibs.setText(string);
	}

	public String getOtherLibs() {
		return otherLibs.getText();
	}

	public void setApp(String string) {
		code.setText(string);
	}

	public void setTCClass(String string) {
		tsuit.setText(string);
	}

	public String getTCClass() {
		return tsuit.getText();
	}

	public void setTCBin(String string) {
		junit.setText(string);
	}

	public String getTCBin() {
		return junit.getText();
	}

	public void setTCSource(String string) {
		jsource.setText(string);
	}

	public String getTCSource() {
		return jsource.getText();
	}

	public void setJVM(String string) {
		java.setText(string);
	}

	public void setCompiler(String string) {
		javac.setText(string);
	}

	public void setTrace(String string) {
		trace.setText(string);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * Properties l = System.getProperties(); Enumeration ks = l.keys();
		 * while (ks.hasMoreElements() ) { String s = (String) ks.nextElement();
		 * String v = l.getProperty(s); System.out.println(s + " =\t" + v); }
		 */
		Main m = new Main(null, true);

		for (int i = 0; i < args.length; i++) {
			if ("-usage".equals(args[i]))
				usage();
			else if ("-tcSource".equals(args[i]))
				m.setTCSource(args[++i]);
			else if ("-tc".equals(args[i]))
				m.setTCBin(args[++i]);
			else if ("-tcClass".equals(args[i]))
				m.setTCClass(args[++i]);
			else if ("-app".equals(args[i]))
				m.setApp(args[++i]);
			else if ("-jabutiLib".equals(args[i]))
				m.setJabutiLib(args[++i]);
			else if ("-otherLibs".equals(args[i]))
				m.setOtherLibs(args[++i]);
			else if ("-java".equals(args[i]))
				m.setJVM(args[++i]);
			else if ("-javac".equals(args[i]))
				m.setCompiler(args[++i]);
			if ("-trace".equals(args[i]))
				m.setTrace(args[++i]);
		}

		m.pack();
		m.setVisible(true);
	}

	static public void usage() {
		System.out.println("-tcSource <path>");
		System.out
				.println("\t Gives the path to the JUnit test case source file.");
		System.out.println("-tc <path>");
		System.out
				.println("\t Gives the path to the JUnit test case class file.");
		System.out.println("-tcClass <path>");
		System.out
				.println("\t Gives the full qualified name of the JUnit test case class");
		System.out.println("-app <path>");
		System.out.println("\t Gives the path to the application class files.");
		System.out.println("-jabutiLib <path>");
		System.out
				.println("\t Gives the path to the JaBUTi library (Jabuti-bin.zip file).");
		System.out.println("-otherLibs <path>");
		System.out
				.println("\t Gives the path to the other application specific libraries and directories.");

		System.out.println("-trace <trace file name>");
		System.out
				.println("\t Gives the name of the file which will store the trace information.");
		System.out.println("-java <path>");
		System.out
				.println("\t Gives the path to the JVM to execute the test cases.");
		System.out.println("-javac <path>");
		System.out
				.println("\t Gives the path to the Java compiler to create the test cases.");
	}
}
