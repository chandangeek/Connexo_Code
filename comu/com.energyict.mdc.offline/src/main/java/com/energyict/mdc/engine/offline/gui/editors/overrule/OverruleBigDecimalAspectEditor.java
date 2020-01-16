package com.energyict.mdc.engine.offline.gui.editors.overrule;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.editors.BigDecimalAspectEditor;
import com.jidesoft.swing.TristateCheckBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;

/**
 * <p>This BigDecimalAspectEditor is built for situations where the model (can) overrule(s) some default: <code>getOverruledValue</code>
 * To overrule the value, the user needs to check the overrule checkbox provided by this editor. This checkbox is available
 * as a separate component using the method <code>getOverruleCheckBox()</code>. Checking the checkbox will enable the value editor (JBigDecimalField),
 * so the user can enter another value. Unchecking the checkbox will disable the value editor and reset the default value.
 * </p>
 * <p>For multi edit purposes a tristate checkbox can be used to indicate to leave the value unchanged. Use to <code>getOverruleTristate()</code>
 * to get put this component on a JPanel</p>
 * Copyrights EnergyICT
 * Date: 9/04/14
 * Time: 18:02
 */
public abstract class OverruleBigDecimalAspectEditor extends BigDecimalAspectEditor {

    private boolean doOverrule = false;
    protected JCheckBox overruleCheckBox;
    protected TristateCheckBox overruleTristate;

    public OverruleBigDecimalAspectEditor(Object model, PropertyDescriptor propertyDescriptor) {
        super();
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

    public void init(Object model, PropertyDescriptor descriptor) {
        super.init(model, descriptor);
        doOverrule = isOverruled();
        overruleCheckBox.setSelected(doOverrule);
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

    protected String getMixedTooltipText(){
        return TranslatorProvider.instance.get().getTranslator().getTranslation("unchanged");
    }

    private void overrule(boolean flag) {
        this.doOverrule = flag;
        this.getValueComponent().setEnabled(doOverrule);
        if (!doOverrule) {
            setViewValue(getOverruledValue());
            updateModel();
            getValueComponent().setEnabled(false);
            overruleCheckBox.setToolTipText(getOverruleCheckBoxUnselectedToolTipText());
            overruleTristate.setToolTipText(getOverruleCheckBoxUnselectedToolTipText());
        } else{
            overruleCheckBox.setToolTipText(getOverruleCheckBoxSelectedToolTipText());
            overruleTristate.setToolTipText(getOverruleCheckBoxSelectedToolTipText());
        }
    }

    /**
     * @return true is the model's value overrules the default, false if the model does not overrule the default value
     */
    protected abstract boolean isOverruled();

    /**
     * @return the default value
     */
    protected abstract BigDecimal getOverruledValue();

    /**
     * @return the ToolTipText that appears when hovering the checked check box
     */
    protected abstract String getOverruleCheckBoxSelectedToolTipText();

    /**
     * @return the ToolTipText that appears when hovering the unchecked check box
     */
    protected abstract String getOverruleCheckBoxUnselectedToolTipText();

    @Override
    protected void updateModel() {
        if (doOverrule) {
            super.updateModel();
        } else {
            setModelValue(null);
        }
    }
}

