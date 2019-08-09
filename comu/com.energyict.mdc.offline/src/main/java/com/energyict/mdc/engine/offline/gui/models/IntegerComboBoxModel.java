/*
 * IntegerComboBoxModel.java
 *
 * Created on 18 juni 2003, 10:07
 */

package com.energyict.mdc.engine.offline.gui.models;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author GDE
 */
public class IntegerComboBoxModel extends AbstractListModel
        implements ComboBoxModel {

    private List choices;
    private Integer selection;

    public IntegerComboBoxModel(int[] choices) {
        this.choices = new ArrayList(choices.length);

        for (int i = 0; i < choices.length; i++) {
            this.choices.add(new Integer(choices[i]));
        }
    }

    public int getSize() {
        return choices.size();
    }

    public Object getElementAt(int index) {
        return choices.get(index);
    }

    public Object getSelectedItem() {
        return selection;
    }

    public void setSelectedItem(Object newSelection) {
        if (newSelection instanceof Integer) {
            selection = (Integer) newSelection;
            fireContentsChanged(this, -1, -1);
        }
    }
}