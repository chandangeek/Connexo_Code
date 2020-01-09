package com.energyict.mdc.engine.offline.gui.editors;


import com.energyict.mdc.engine.offline.gui.models.TypedPropertiesOwner;
import com.energyict.mdc.upl.TypedProperties;

import javax.swing.*;
import java.awt.*;

/**
 * User: gde
 * Date: 3/12/12
 */
public class PropertySpecsViewer<T extends TypedPropertiesOwner> extends JPanel {

    private T model;
    private JPanel mainPnl;
    private JPanel valuesPnl;

    public PropertySpecsViewer(T model) {
        this.model = model;
        initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setViewportView(getMainPnl());
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel getMainPnl() {
        if (mainPnl!=null) {
            return mainPnl;
        }
        JPanel innerPnl = new JPanel(new BorderLayout());
        innerPnl.add(getValuesPnl(), BorderLayout.CENTER);

        mainPnl = new JPanel(new FlowLayout(FlowLayout.LEADING));
        mainPnl.add(innerPnl);
        return mainPnl;
    }

    private JPanel getValuesPnl() {
        if (valuesPnl!=null) {
            return valuesPnl;
        }
        valuesPnl = new JPanel(new GridBagLayout());
        valuesPnl.setBorder(BorderFactory.createEmptyBorder(11, 11, 6, 11));

        int row = 0;
        TypedProperties typedProperties = model.getProperties();
        for (String key : typedProperties.localPropertyNames()) {
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridy = row;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.insets = new Insets(2, 2, 2, 2);
            getValuesPnl().add(new JLabel(key + ": "), gridBagConstraints);

            gridBagConstraints.gridx = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            Object object = typedProperties.getProperty(key);
            valuesPnl.add(new JLabel(object.toString()), gridBagConstraints);

            row++;
        }

        return valuesPnl;
    }
}
