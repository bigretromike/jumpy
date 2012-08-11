package net.pms.external.infidel.jumpy;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalIconFactory;

import net.pms.PMS;
import net.pms.io.SystemUtils;
import net.pms.io.BasicSystemUtils;
import net.pms.io.MacSystemUtils;

// convenience class to combine interfaces for inlining
abstract class multiListener implements ItemListener, ChangeListener, ActionListener {}


public class config {

	static final String websiteurl = "https://github.com/skeptical/jumpy";
	static final String forumurl = 	"http://www.ps3mediaserver.org/forum/viewtopic.php?f=12&t=12518";
	static final String xbmcurl = "http://xbmc.org";
	static final String pythonuri = "http://www.python.org/download/releases/2.7.3";
	static final String py4juri = "http://py4j.sourceforge.net";

	static final Insets border = new Insets(10,10,10,10);
	static final Insets items = new Insets(5,5,5,5);
	static final Insets none = new Insets(0,0,0,0);

	public static jumpy jumpy;

	public static multiListener listener = new multiListener() {
		public void stateChanged(ChangeEvent e) {
			setopt(((JComponent)e.getSource()), 0);
		}
		public void itemStateChanged(ItemEvent e) {
			setopt(((JComponent)e.getItem()), e.getStateChange());
		}
		public void setopt(JComponent c, int state) {
			String opt = c.getToolTipText();
			PMS.debug("setopt " + opt);
			if (opt.equals("bookmarks")) {
				jumpy.showBookmarks = (state == ItemEvent.DESELECTED ? false : true);
			} else if (opt.equals("verbose_bookmarks")) {
				jumpy.verboseBookmarks = (state == ItemEvent.DESELECTED ? false : true);
			} else if (opt.equals("refresh")) {
				jumpy.refresh = (Integer)(((JSpinner)c).getValue());
			} else if (opt.equals("debug")) {
				jumpy.debug = (state == ItemEvent.DESELECTED ? false : true);
			}
		}
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Save")) {
				PMS.debug("Save");
				jumpy.writeconf();
			} else if (cmd.equals("Revert")) {
				PMS.debug("Revert");
				jumpy.readconf();
				rebuild((JComponent)e.getSource());
			}
		}
	};

	public static void init(final jumpy obj) {
		jumpy = obj;
	}

	public static JComponent mainPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel top = new JPanel();
		top.add(new JLabel("<html><center>"
			+ "<font size=+1 color=#0000a0><font color=#ff6600>Jumpy</font> - " + jumpy.version + "</font>"
			+ "<center></html>"));
		c.insets = none;
		panel.add(top, c);

		JPanel files = new JPanel(new GridBagLayout());
		c.insets = items;
		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 0;
		files.add(editButton(jumpy.scriptsini), c);
		c.gridx = 1;
		files.add(editButton(jumpy.bookmarksini), c);
		c.gridx = 2;
		files.add(editButton(jumpy.jumpyconf), c);
		c.gridx = 3;
		files.add(editButton(jumpy.jumpylog), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0; c.gridy = 2;
		c.insets = none;
		panel.add(files, c);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Options", options());
		for (player p : jumpy.players.subList(1, jumpy.players.size())) {
			tabs.addTab(p.name() + " player", playerPanel(p, false));
		}
		tabs.addTab("About", about());
		c.gridx = 0; c.gridy = 1;
		c.insets = none;
		panel.add(tabs, c);

		return panel;
	}

	public static JPanel options() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;

		c.insets = items;
		c.gridx = 0; c.gridy = 0;
		panel.add(checkbox("Enable bookmarks", jumpy.showBookmarks, "bookmarks"), c);
		c.gridx = 0; c.gridy = 1;
		panel.add(checkbox("Qualify bookmark names (e.g. 'Live :ESPN' instead of just 'Live')", jumpy.verboseBookmarks, "verbose_bookmarks"), c);
		c.gridx = 0; c.gridy = 2;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		panel.add(new JLabel("Refresh folder content every (minutes) :", SwingConstants.LEFT), c);
		c.gridx = 1; c.gridy = 2;
		c.weightx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(numberBox(Integer.valueOf(jumpy.refresh), 0, 1000, 1, "refresh"), c);
		c.gridx = 0; c.gridy = 3;
		panel.add(checkbox("Print log messages to console", jumpy.debug, "debug"), c);

		JPanel p = new JPanel();
		p.add(actionButton("Revert", "Reload settings from disk."));
		p.add(actionButton("Save", jumpy.jumpyconf));
		c.insets = none;
		c.gridx = 1; c.gridy = 4;
		panel.add(p, c);

		JPanel frame = new JPanel(new GridBagLayout());
		c.insets = border;
		frame.add(panel, c);
		return frame;
	}

	public static JPanel about() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.CENTER;
		c.insets = border;

		panel.add(new JLabel("<html><center>"
			+ "Jumpy is a general purpose framework for<br>"
			+ "scripts in any language and other external processes<br>"
			+ "to plug into PMS.<br><br>"
			+ "Included is a set of python scripts to run xbmc addons.<br>"
			+ "</center></html>"), c);
		JPanel links = new JPanel();
		links.add(linkButton("Readme", "file://" + jumpy.home + "readme.html"));
		links.add(linkButton("Code", websiteurl));
		links.add(linkButton("Forum", forumurl));
		c.insets = none;
		c.gridy++;
		panel.add(links, c);

		return panel;
	}

	public static JComponent playerPanel(player p, boolean inibutton) {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = border;
		c.gridx = 0; c.gridy = 0;
		String about = "<html>"
			+ (p.desc == null ? "Jumpy User-Defined Player" : p.desc)
			+ "<br><br>Supported : <font color=blue>" + p.supportStr + "</font>"
			+ (p.cmdStr == null || "".equals(p.cmdStr) ? "" : ("<br>Command : <font color=blue>" + p.cmdStr + "</font>"))
			+ "</html>";
		JLabel label = new JLabel(about, SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.TOP);
		panel.add(label, c);
		if (p.cmdline != null) {
			c.gridx = 0; c.gridy = 1;
			panel.add(editButton(p.cmdline.argv.get(p.cmdline.scriptarg)), c);
			if (inibutton) {
				c.gridx = 0; c.gridy = 2;
				panel.add(editButton(p.jumpy.scriptsini), c);
			}
		}
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(panel);
		return top;
	}

	public static JButton actionButton(String label, String help) {
		JButton button = new JButton(label);
		button.setActionCommand(label);
		button.setToolTipText(help);
		button.addActionListener(listener);
		return button;
	}

	public static JSpinner numberBox(int value, int min, int max, int step, String help) {
		SpinnerModel model = new SpinnerNumberModel(new Integer(value), new Integer(min), new Integer(max), new Integer(step));
		JSpinner spinner = new JSpinner(model);
		spinner.addChangeListener(listener);
		spinner.setToolTipText(help);
		return spinner;
	}

	public static JCheckBox checkbox(String label, boolean val, String help) {
		JCheckBox checkbox = new JCheckBox(label, val);
		checkbox.setToolTipText(help);
		checkbox.addItemListener(listener);
		return checkbox;
	}

	public static JButton linkButton(String label, final String uri) {
		JButton link = new JButton("<html><a href=''>" + label + "</a></html>");
		link.setBorderPainted(false);
		link.setToolTipText(uri);
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				utils.browse(uri);
			}
		});
		return link;
	}

	public static JButton editButton(final String uri) {
		final File file = new File(uri);
		final boolean exists = file.exists();
		JButton edit =  new JButton((exists ? "" : "+ ") + file.getName(),
				exists ? MetalIconFactory.getTreeLeafIcon() : null);
		edit.setToolTipText(uri);
		edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean isnew = !exists && create(file);
				if (! (exists || isnew)) {
					return;
				}
				utils.textedit(uri);
				if (isnew) {
					rebuild((JComponent)e.getSource());
				}
			}
		});
		return edit;
	}

	private static boolean create(File file) {
		if (file.exists()) {
			return true;
		}
		if(JOptionPane.showConfirmDialog(null, "Create " + file.getName() + "?", "Create File", JOptionPane.YES_NO_OPTION) == 1) {
			return false;
		}
		if (file.getAbsolutePath().equals(jumpy.jumpyconf)) {
			jumpy.writeconf();
		} else {
			try {
				file.createNewFile();
			}
			catch (Exception e) { return false; }
		}
		return true;
	}

	private static void rebuild(JComponent c) {
		// self-destruct and rebuild
		((Window)c.getTopLevelAncestor()).dispose();
		JOptionPane.showOptionDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
			mainPanel(), "Options", JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
	}
}
