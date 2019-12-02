package com.energyict.mdc.engine.offline.gui.editors.overrule;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.editors.SpinnerEditor;
import com.jidesoft.swing.TristateCheckBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyDescriptor;

/**
 *  <p>This SpinnerEditor (Numeric Editor using a JSpinner to update the model) is built for situations where the model (can) overrule(s) some default: <code>getOverruledValue</code>
  * To overrule the value, the user needs to check the overrule checkbox provided by this editor. This checkbox is available
  * as a separate component using the method <code>getOverruleCheckBox()</code>. Checking the checkbox will enable the value editor (JBigDecimalField),
  * so the user can enter another value. Unchecking the checkbox will disable the value editor and reset the default value.
  * </p>
 * <p>For multi edit purposes a tristate checkbox can be used to indicate to leave the value unchanged. Use to <code>getOverruleTristate()</code>
 * to get put this component on a JPanel</p>
 * Copyrights EnergyICT
 * Date: 10/04/14
 * Time: 13:19
 */
public abstract class OverruleSpinnerEditor extends SpinnerEditor {
    private boolean doOverrule = false;
    protected JCheckBox overruleCheckBox;
    protected TristateCheckBox overruleTristate;

    public OverruleSpinnerEditor(Object model, PropertyDescriptor propertyDescriptor, SpinnerModel spinnerModel) {
        super(spinnerModel);
        overruleCheckBox = new JCheckBox();
        overruleTristate = new TristateCheckBox();
        init(model, propertyDescriptor);
        overruleCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                overrule(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        overruleTristate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (overruleTristate.isMixed()){
                    getValueComponent().setEnabled(false);
                    overruleTristate.setToolTipText(getMixedTooltipText());
                }else{
                    overrule(overruleTristate.isSelected());
                }
            }
        });
    }

    public JCheckBox getOverruleCheckBox() {
        return overruleCheckBox;
    }
    public TristateCheckBox getOverruleTristate() {
        getValueComponent().setEnabled(!overruleTristate.isMixed());
        return overruleTristate;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (readOnly) {
            super.setReadOnly(true);
        }
        overruleCheckBox.setEnabled(!readOnly);
        overruleTristate.setEnabled(!readOnly);
    }
    @Override
    public void init(Object model, PropertyDescriptor descriptor) {
        super.init(model, descriptor);
        this.doOverrule = isOverruled();
        this.overruleCheckBox.setSelected(doOverrule);
        overruleTristate.setMixed(true);
        overruleTristate.setToolTipText(getMixedTooltipText());
        this.getValueComponent().setEnabled(doOverrule);
        if (!this.doOverrule) {
            overruleCheckBox.setToolTipText(getOverruleCheckBoxUnselectedToolTipText());
            setViewValue(getOverruledValue());
        } else {
            overruleCheckBox.setToolTipText(getOverruleCheckBoxSelectedToolTipText());
        }
    }

    protected void setViewValue(Object value) {
        if (value == null || isValueValid(value)){
            super.setViewValue(value);
        }
    }

    private void overrule(boolean flag) {
        this.doOverrule = flag;
        this.getValueComponent().setEnabled(doOverrule);
        if (!doOverrule) {
            setViewValue(getOverruledValue());
            updateModel();
            getValueComponent().setEnabled(false);
            overruleCheckBox.setToolTipText(getOverruleCheckBoxUnselectedToolTipText());
        } else{
            overruleCheckBox.setToolTipText(getOverruleCheckBoxSelectedToolTipText());
        }

    }

    protected String getMixedTooltipText(){
        return TranslatorProvider.instance.get().getTranslator().getTranslation("unchanged");
    }

    /**
     * @return true is the model's value overrules the default, false if the model does not overrule the default value
     */
    protected abstract boolean isOverruled();

    /**
     * @return the default value.
     */
    protected abstract Integer getOverruledValue();

    /**
     * @return the ToolTipText that appears when hovering the checked check box.
     */
    protected abstract String getOverruleCheckBoxSelectedToolTipText();

    /**
     * @return the ToolTipText that appears when hovering the unchecked check box.
     */
    protected abstract String getOverruleCheckBoxUnselectedToolTipText();

    /**
     * @return the value to set on the model if overrule is set to false.
     */
    protected abstract int modelValueIfNotOverruled();

    @Override
    protected void updateModel() {
        if (doOverrule) {
            super.updateModel();
        } else {
            setModelValue(modelValueIfNotOverruled());
        }
    }

}
