/*
 * AspectEditor.java
 *
 * Created on 5 februari 2003, 14:25
 */

package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.Translator;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * Class AspectEditor is the base class for all UI components used to modify an
 * &quot;aspect&quot; (=field) of an object.
 *
 * C is the type of the ValueComponent
 */
abstract public class AspectEditor<C extends JComponent> {

    private PropertyDescriptor descriptor;
    private Object model;
    private boolean forceReadOnly = false;
    private boolean translate = true;
    private boolean customTranslate = false;
    private boolean valueRequired = false;

    /**
     * Creates a new instance of AspectEditor
     */
    protected AspectEditor() {
    }

    public Object getModel() {
        return model;
    }

    /**
     * Initialises the editor with the given model and descriptor
     *
     * @param model      object for which the aspect should be modified
     * @param descriptor to indicate which &quot;aspect&quot; should be modified
     */
    public void init(Object model, PropertyDescriptor descriptor) {
        doInit();
        this.descriptor = descriptor;
        this.model = model;
        updateView();
    }

    /**
     * Hook function for performing additional initializations:
     * adding and initializing components...
     */
    protected void doInit() {
    }

    protected PropertyDescriptor getDescriptor() {
        return descriptor;
    }

    protected void setDescriptor(PropertyDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public void setModel(Object model) {
        this.model = model;
        updateView();
    }

    protected boolean canUpdateModel() {
        return !(forceReadOnly || descriptor.getWriteMethod() == null);
    }

    public void setForceReadOnly(boolean forceReadOnly) {
        this.forceReadOnly = forceReadOnly;
    }

    abstract protected Object getViewValue();

    abstract protected void setViewValue(Object value);

    abstract protected void updateLabel();

    abstract public JLabel getLabelComponent();

    abstract public C getValueComponent();

    public void setReadOnly(boolean readOnly) {
        // for future use. e.g. to disable traversal with TAB/Shift_Tab
    }

    protected Object getModelValue() {
        if (model == null) {
            return null;
        }
        try {
            return descriptor.getReadMethod().invoke(model, (Object[]) null);
        } catch (IllegalAccessException | InvocationTargetException ex ) {
            throw new ApplicationException(ex);
        }
    }

    protected void setModelValue(Object value) {
        Object[] arguments = new Object[1];
        arguments[0] = value;
        try {
            descriptor.getWriteMethod().invoke(model, arguments);
        } catch (IllegalAccessException | InvocationTargetException  ex) {
            throw new ApplicationException(ex);
        }
    }

    public void updateView() {
        updateLabel();
        setViewValue(getModelValue());
        setReadOnly(!canUpdateModel());
    }

    protected void updateModel() {
        if (!canUpdateModel()) {
            return;
        }
        Object viewValue = getViewValue();
        Object modelValue = getModelValue();
        if (modelValue == null) {
            if (viewValue != null) {
                setModelValue(viewValue);
            }
        } else {
            if (!modelValue.equals(viewValue)) {
                setModelValue(viewValue);
            }
        }
    }

    public String getLabelString() {
        return getLabelString(true);
    }

    public void setTranslate(boolean translate) {
        this.translate = translate;
        if (translate) {
            this.customTranslate = false;
        }
    }

    public void setCustomTranslate(boolean customTranslate) {
        this.customTranslate = customTranslate;
        if (customTranslate) {
            this.translate = false;
        }
    }

    public String getLabelString(boolean bAddColon) {
        if (translate) {
            return translator().getTranslation(
                    getDescriptor().getDisplayName()) + (bAddColon ? ":" : "");
        } else if (customTranslate) {
            return translator().getCustomTranslation(
                    getDescriptor().getDisplayName()) + (bAddColon ? ":" : "");
        } else {
            return getDescriptor().getDisplayName() + (bAddColon ? ":" : "");
        }
    }

    private Translator translator() {
        return TranslatorProvider.instance.get().getTranslator();
    }

    final public boolean hasValidValue() {
        return doHasValidValue() && (!isValueRequired() || hasValidModel());
    }

    protected boolean doHasValidValue() {
        return true;
    }

    protected boolean hasValidModel() {
        return getModelValue() != null;
    }

    public void setValueRequired(boolean valueRequired) {
        this.valueRequired = valueRequired;
    }

    public boolean isValueRequired() {
        return valueRequired;
    }
}
