package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.engine.offline.gui.actions.UserAction;

/**
 * Copyrights EnergyICT
 * Date: 21-sep-2010
 * Time: 15:27:02
 */
public class EncryptedStringAdapter extends ValueAdapter {

    private Protectable protectable;

    public EncryptedStringAdapter(DynamicAttributeOwner model, String aspect) {
        super(model, aspect);
        if (model instanceof Protectable) {
            protectable = (Protectable) model;
        }
    }

    public String getValue() {
        return (String) doGetValue();
    }

    public void setValue(String value) {
        doSetValue(value);
    }

    public boolean isAuthorized(UserAction action) {
        return (protectable != null && protectable.isAuthorized(action));
    }
}
