package com.icapps.niddler.ui.form;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
public class MainWindow {
	private JPanel rootPanel;
	private JComboBox adbTargetSelection;
	private JScrollPane contentScroller;
	private JTextArea dummyContentPanel;
	private JToolBar toolbar;
	private JToggleButton buttonTimeline;
	private JToggleButton buttonLinkedMode;
	private JButton buttonClear;
	private JPanel detailPanel;

	public JPanel getRootPanel() {
		return rootPanel;
	}

	public JComboBox getAdbTargetSelection() {
		return adbTargetSelection;
	}

	public JScrollPane getContentScroller() {
		return contentScroller;
	}

	public JTextArea getDummyContentPanel() {
		return dummyContentPanel;
	}

	public JButton getButtonClear() {
		return buttonClear;
	}

	public JPanel getDetailPanel() {
		return detailPanel;
	}

	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout(0, 0));
		rootPanel.setPreferredSize(new Dimension(200, 200));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		rootPanel.add(panel1, BorderLayout.NORTH);
		adbTargetSelection = new JComboBox();
		panel1.add(adbTargetSelection);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(0, 0));
		rootPanel.add(panel2, BorderLayout.WEST);
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setMargin(new Insets(0, 4, 0, 4));
		toolbar.setOrientation(1);
		panel2.add(toolbar, BorderLayout.NORTH);
		buttonTimeline = new JToggleButton();
		buttonTimeline.setFocusPainted(true);
		buttonTimeline.setHideActionText(false);
		buttonTimeline.setIcon(new ImageIcon(getClass().getResource("/ic_chronological.png")));
		buttonTimeline.setInheritsPopupMenu(false);
		buttonTimeline.setMargin(new Insets(0, 2, 0, 2));
		buttonTimeline.setMaximumSize(new Dimension(32, 32));
		buttonTimeline.setMinimumSize(new Dimension(32, 32));
		buttonTimeline.setPreferredSize(new Dimension(32, 32));
		buttonTimeline.setSelected(true);
		buttonTimeline.setText("");
		toolbar.add(buttonTimeline);
		buttonLinkedMode = new JToggleButton();
		buttonLinkedMode.setFocusPainted(true);
		buttonLinkedMode.setIcon(new ImageIcon(getClass().getResource("/ic_link.png")));
		buttonLinkedMode.setMargin(new Insets(0, 2, 0, 2));
		buttonLinkedMode.setMaximumSize(new Dimension(32, 32));
		buttonLinkedMode.setMinimumSize(new Dimension(32, 32));
		buttonLinkedMode.setPreferredSize(new Dimension(32, 32));
		buttonLinkedMode.setText("");
		toolbar.add(buttonLinkedMode);
		buttonClear = new JButton();
		buttonClear.setIcon(new ImageIcon(getClass().getResource("/ic_delete.png")));
		buttonClear.setLabel("");
		buttonClear.setMaximumSize(new Dimension(32, 32));
		buttonClear.setMinimumSize(new Dimension(32, 32));
		buttonClear.setPreferredSize(new Dimension(32, 32));
		buttonClear.setText("");
		toolbar.add(buttonClear);
		final JSplitPane splitPane1 = new JSplitPane();
		splitPane1.setResizeWeight(1.0);
		rootPanel.add(splitPane1, BorderLayout.CENTER);
		final JScrollPane scrollPane1 = new JScrollPane();
		splitPane1.setLeftComponent(scrollPane1);
		dummyContentPanel = new JTextArea();
		scrollPane1.setViewportView(dummyContentPanel);
		detailPanel = new JPanel();
		detailPanel.setLayout(new BorderLayout(0, 0));
		splitPane1.setRightComponent(detailPanel);
		ButtonGroup buttonGroup;
		buttonGroup = new ButtonGroup();
		buttonGroup.add(buttonTimeline);
		buttonGroup.add(buttonLinkedMode);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}
}
