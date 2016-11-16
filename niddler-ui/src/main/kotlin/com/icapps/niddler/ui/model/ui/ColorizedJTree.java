package com.icapps.niddler.ui.model.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTree;

/**
 * Created by maartenvangiel on 16/11/2016.
 */
public class ColorizedJTree extends JTree {

    private final Color selectionColor = new Color(240, 240, 240);
    private final Color requestColor = new Color(180, 180, 240);
    private final Color responseColor = new Color(180, 240, 180);

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        final int[] selectionRows = getSelectionRows();
        if (getSelectionCount() > 0 && selectionRows != null) {
            g.setColor(selectionColor);
            for (final int i : selectionRows) {
                final Rectangle r = getRowBounds(i);
                g.fillRect(0, r.y, getWidth(), r.height);
            }
        }

        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }

    @Override
    public int getRowHeight() {
        return 25;
    }
}
