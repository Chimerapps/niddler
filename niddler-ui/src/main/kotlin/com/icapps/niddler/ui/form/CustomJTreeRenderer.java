package com.icapps.niddler.ui.form;

import com.icapps.niddler.ui.model.ParsedNiddlerMessage;
import com.icapps.niddler.ui.model.ParsedNiddlerMessageSubItem;
import com.icapps.niddler.ui.model.ui.NiddlerMessageTreeNode;
import com.icapps.niddler.ui.model.ui.NiddlerMessageSubItemTreeNode;
import com.icapps.niddler.ui.util.StatusCodeMappingsKt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Created by maartenvangiel on 15/11/2016.
 */
public class CustomJTreeRenderer extends DefaultTreeCellRenderer {

    private final DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private final JLabel timestampLabel = new JLabel();
    private final JLabel requestIdLabel = new JLabel();
    private final JLabel urlLabel = new JLabel();
    private final JLabel methodLabel = new JLabel();
    private final JLabel statusCodeLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel valueLabel = new JLabel();

    private final JPanel rootPanel = new JPanel();
    private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    private int height;
    private int width;

    public CustomJTreeRenderer() {
        timestampLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
        timestampLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rootPanel.add(timestampLabel);

        requestIdLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
        requestIdLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rootPanel.add(requestIdLabel);

        methodLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        methodLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        rootPanel.add(methodLabel);

        urlLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        urlLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        rootPanel.add(urlLabel);

        statusCodeLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        statusCodeLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        rootPanel.add(statusCodeLabel);

        nameLabel.setPreferredSize(new Dimension(63, 15));
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        rootPanel.add(nameLabel);

        valueLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        valueLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        rootPanel.add(valueLabel);

        rootPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        width = tree.getWidth();
        height = leaf ? 20 : 60;

        if (value == null || !(value instanceof DefaultMutableTreeNode)) {
            return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }

        if(value instanceof NiddlerMessageTreeNode){
            final ParsedNiddlerMessage message = ((NiddlerMessageTreeNode) value).getItem();
            final boolean isRequest = message.isRequest();

            nameLabel.setVisible(false);
            valueLabel.setVisible(false);
            timestampLabel.setVisible(true);
            requestIdLabel.setVisible(true);
            urlLabel.setVisible(isRequest);
            methodLabel.setVisible(isRequest);
            statusCodeLabel.setVisible(!isRequest);

            timestampLabel.setText(formatter.format(new Date(message.getTimestamp())));
            urlLabel.setText(message.getUrl());
            methodLabel.setText(message.getMethod());
            requestIdLabel.setText(message.getRequestId());
            statusCodeLabel.setText(message.getStatusCode() + " " + StatusCodeMappingsKt.getStatusCodeString(message.getStatusCode()));

            return rootPanel;
        } else if (value instanceof NiddlerMessageSubItemTreeNode){
            final ParsedNiddlerMessageSubItem item = ((NiddlerMessageSubItemTreeNode) value).getItem();

            nameLabel.setVisible(true);
            valueLabel.setVisible(true);
            urlLabel.setVisible(false);
            statusCodeLabel.setVisible(false);
            timestampLabel.setVisible(false);
            requestIdLabel.setVisible(false);

            nameLabel.setText(item.getName() + ":");
            valueLabel.setText(item.getValue());

            return rootPanel;
        }

        return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

}
