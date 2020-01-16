package com.energyict.mdc.engine.offline.gui.editors;

import javax.swing.*;

/**
 * AspectEditor that can be used when options are limited by an <Code>Enum</Code>
 * Copyrights EnergyICT
 * Date: 19-mei-2011
 * Time: 16:41:11
 */
public class EnumAspectEditor<Enum> extends JComboBoxAspectEditor<Enum> {

    public EnumAspectEditor(Class<Enum> enumClass) {
        super(new DefaultComboBoxModel<>(enumClass.getEnumConstants()));
    }

    public EnumAspectEditor(Class<Enum> enumClass, ListCellRenderer renderer) {
        super(new DefaultComboBoxModel<>(enumClass.getEnumConstants()), renderer);
    }

}
