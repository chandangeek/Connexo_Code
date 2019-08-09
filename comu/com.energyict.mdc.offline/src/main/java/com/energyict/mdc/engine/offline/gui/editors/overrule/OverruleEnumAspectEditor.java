package com.energyict.mdc.engine.offline.gui.editors.overrule;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.editors.EnumAspectEditor;
import com.jidesoft.swing.TristateCheckBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyDescriptor;

/**
 * <p>This EnumAspectEditor is built for situations where the model (can) overrule(s) some default: <code>getOverruledValue</code>
 * To overrule the value, the user needs to check the overrule checkbox provided by this editor. This checkbox is available
 * as a separate component using the method <code>getOverruleCheckBox()</code>. Checking the checkbox will enable the value editor (combobox),
 * so the user can select another value. Unchecking will disable the value editor and reset the default value.
 * </p>
 * <p>For multi edit purposes a tristate checkbox can be used to indicate to leave the value unchanged. Use to <code>getOverruleTristate()</code>
 * to get put this component on a JPanel</p>*
 * <p>Here an example implementation</p>
 * <Code>
 *       private class OverruleReadingMethodAspectEditor extends OverruleEnumAspectEditor<ReadingMethod> {
 *
 *           OverruleReadingMethodAspectEditor() {
 *                super(ReadingMethod.class, ASPECT_READING_METHOD);
 *                try {
 *                    init(ChannelPropsPnl.this.getBuilder().getModel(), new PropertyDescriptor(ASPECT_READING_METHOD, ChannelPropsPnl.this.getBuilder().getModel().getClass()));
 *                } catch (IntrospectionException e) {
 *                    throw new ApplicationException(e);
 *                }
 *                getOverruleCheckBox().setText(TranslatorProvider.instance.get().getTranslator().getTranslation("readingMethodOverrulesSpec"));
 *            }
 *
 *            protected boolean isOverruled() {
 *                return ((ChannelShadow) getModel()).getReadingMethod() != null;
 *            }
 *
 *            protected ReadingMethod getOverruledValue() {
 *                ChannelSpec spec = getChannelSpec();
 *                return (spec != null ? spec.getReadingMethod() : null);
 *            }
 *
 *            protected String getOverruleCheckBoxSelectedToolTipText() {
 *                return getCheckBoxToolTipText(true);
 *            }
 *
 *            protected String getOverruleCheckBoxUnselectedToolTipText() {
 *                return getCheckBoxToolTipText(false);
 *            }
 *       }
 * </Code>
 * <p>Usage within a JPanel</p>
 * <Code>
 *        final OverruleReadingMethodAspectEditor readingMethodEditor = new OverruleReadingMethodAspectEditor();
 *        readingMethodEditor.setReadOnly(auditView);
 *        final JComboBox<ReadingMethod> readingMethodCombo = (JComboBox<ReadingMethod>) readingMethodEditor.getValueComponent();
 *        final JCheckBox readingMethodOverruleCheckBox = readingMethodEditor.getOverruleCheckBox();
 *        getBuilder().setEditor(ASPECT_READING_METHOD, readingMethodEditor);
 *        readingMethodOverruleCheckBox.setEnabled(!auditView);
 *
 *         // adding the label, valuecomponent and checkbox to the JPanel
 *         addToPanelWithDefaultGridBagConstraint(rmrContentPanel, readingMethodEditor.getLabelComponent(), 0, y);
 *         addToPanelWithDefaultGridBagConstraint(rmrContentPanel, readingMethodCombo, 1, y);
 *         addToPanelWithDefaultGridBagConstraint(rmrContentPanel, readingMethodOverruleCheckBox, 2, y)*
 *
 * </Code>
 * Date: 9/04/14
 * Time: 17:16
 */
public abstract class OverruleEnumAspectEditor<Enum> extends EnumAspectEditor<Enum> {

    private boolean doOverrule = false;
    protected JCheckBox overruleCheckBox;
    protected TristateCheckBox overruleTristate;

    public OverruleEnumAspectEditor(Class<Enum> enumClass) {
        super(enumClass);
        overruleCheckBox = initOverruleCheckBox();
        overruleTristate = new TristateCheckBox();
        overruleCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!settingModel) {
                    overrule(e.getStateChange() == ItemEvent.SELECTED);
                }
            }
        });
        overruleTristate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!settingModel){
                    if (overruleTristate.isMixed()){
                        getValueComponent().setEnabled(false);
                        overruleTristate.setToolTipText(getMixedTooltipText());
                    }else{
                        overrule(overruleTristate.isSelected());
                    }
                }
            }
        });
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (readOnly) {
            super.setReadOnly(true);
        }
        overruleCheckBox.setEnabled(!readOnly);
        overruleTristate.setEnabled(!readOnly);
    }

    /**
     * @return the &quot;Overrule&quot; CheckBox by which the user indicates
     * if the model should overrule the value or use a default value
     */
    public JCheckBox getOverruleCheckBox() {
        return overruleCheckBox;
    }
    /**
     * @return the &quot;Overrule&quot; TristateCheckbox by which the user indicates
     * if the model should overrule the value or use a default value. The Mixed state can
     * be used for multi edit sessions to indicate the value as unchanged
     */
    public TristateCheckBox getOverruleTristate() {
        getValueComponent().setEnabled(!overruleTristate.isMixed());
        return overruleTristate;
    }


    public void init(Object model, PropertyDescriptor descriptor) {
        super.init(model, descriptor);
        settingModel = true;
        doOverrule = isOverruled();
        overruleTristate.setMixed(true);
        overruleTristate.setToolTipText(getMixedTooltipText());
        overruleCheckBox.setSelected(doOverrule);
        getValueComponent().setEnabled(doOverrule);
        if (!this.doOverrule) {
            overruleCheckBox.setToolTipText(getOverruleCheckBoxUnselectedToolTipText());
            ((JComboBox) this.getValueComponent()).setSelectedItem(getOverruledValue());
        } else {
            overruleCheckBox.setToolTipText(getOverruleCheckBoxSelectedToolTipText());
        }
        this.settingModel = false;
    }

    protected String getMixedTooltipText(){
        return TranslatorProvider.instance.get().getTranslator().getTranslation("unchanged");
    }

    protected JCheckBox initOverruleCheckBox() {
        return new JCheckBox();
    }

    private void overrule(boolean flag) {
        this.doOverrule = flag;
        this.getValueComponent().setEnabled(doOverrule);
        if (!doOverrule) {
            updateModel();
            overruleCheckBox.setToolTipText(getOverruleCheckBoxUnselectedToolTipText());
        } else {
            overruleCheckBox.setToolTipText(getOverruleCheckBoxSelectedToolTipText());
        }
    }

    /**
     * @return true is the model's value overrules the default, false if the model does not overrule the default value
     */
    protected abstract boolean isOverruled();

    /**
     * @return the default value
     */
    protected abstract Enum getOverruledValue();

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
            ((JComboBox) this.getValueComponent()).setSelectedItem(getOverruledValue());
            setModelValue(null);
        }
    }
}
