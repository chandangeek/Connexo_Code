package com.energyict.mdc.engine.offline.gui.models;

import javax.swing.*;

public class SpinnerRolloverModel extends SpinnerNumberModel {

    public SpinnerRolloverModel(int value, int min, int max, int step) {
        super(value, min, max, step);
    }

    public Object getNextValue() {
        int nextValue;
        if ((getValue()).equals(getMaximum())) {
            nextValue = (Integer) getMinimum();
        } else {
            nextValue = (Integer) getValue() + 1;
        }
        return (nextValue);
    }

    public Object getPreviousValue() {
        int prevValue;
        if ((getValue()).equals(getMinimum())) {
            prevValue = (Integer) getMaximum();
        } else {
            prevValue = (Integer) getValue() - 1;
        }
        return (prevValue);
    }

}

