package com.energyict.mdc.engine.offline.gui;

import javax.swing.*;
import java.awt.*;

public class LineComponent extends JPanel {

    private Color lineColor = Color.BLACK;

    public LineComponent() {
    }

    public LineComponent(Color color2Use) {
        this.lineColor = color2Use;
    }

    @Override
    public void paint(Graphics g) {
        Dimension d = this.getSize(); //Get the current size of this component
        g.setColor(lineColor); //draw in black
        g.drawLine(0,d.height/2,d.width,d.height/2); //draw a centered horizontal line
    }
}
