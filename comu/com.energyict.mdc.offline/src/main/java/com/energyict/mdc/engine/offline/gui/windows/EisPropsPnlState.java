package com.energyict.mdc.engine.offline.gui.windows;

import java.util.ArrayList;
import java.util.List;

/**
 * In this class we can store the state of an EisPropsPnl (currently the current selection [indices or ids])
 * User: gde
 * Date: 1/02/13
 */
public class EisPropsPnlState {
    private List<Integer> selection = new ArrayList<>();

    public EisPropsPnlState(){};
    public EisPropsPnlState(int[] selection) {
        for (int each : selection) {
            this.selection.add(each);
        }
    }

    public List<Integer> getSelection() {
        return selection;
    }

    public void setSelection(List<Integer> selection) {
        this.selection = selection;
    }
}
