package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.Utils;

/**
 * Extends the int aspect editor to provide support for "nullable" integer fields. When the value of an Integer is null,
 * and an {@link IntAspectEditor} is used, the value is automagically set to 0, where we don't want that to happen. In this case
 * the value should just be empty.
 *
 * @author Alexander Bollaert
 * @since Aug 25, 2008
 */
public final class IntegerAspectEditor extends IntAspectEditor {

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void setViewValue(final Object value) {
        if (value != null && !(value instanceof Integer)) {
            throw new IllegalStateException("Integer aspect editor can only work with Integer class instances, received [" + value.getClass() + "] instead here !");
        }

        final Integer integerValue = (Integer) value;

        super.ignoreDocumentChanges(true);

        if (value != null) {
            this.getJIntegerField().setValue(integerValue.intValue());
        } else {
            this.getJIntegerField().setText("");
        }

        super.ignoreDocumentChanges(false);
    }

    protected Object getViewValue() {
        if (Utils.isNull(jValue.getText())) {
            return null;
        }
        return super.getViewValue();
    }


}
