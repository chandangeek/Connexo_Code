package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.cbo.Unit;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

/*
 * Created by JFormDesigner on Thu May 14 10:26:20 CEST 2009
 */

/**
 * @author jme
 */
public class DeviceRegistersInfoPnl extends JPanel {

    private List<OfflineRegister> offlineRegisters;

    private final String NLS_REGISTERS = UiHelper.translate("devices.registers.title");
    private final String NLS_NAME = UiHelper.translate("name");
    private final String NLS_OBISCODE = UiHelper.translate("obisCode");
    private final String NLS_UNIT = UiHelper.translate("unit");

    public DeviceRegistersInfoPnl(List<OfflineRegister> offlineRegisters) {
        this.offlineRegisters = offlineRegisters;
        initComponents();
        fillTable();
    }

    private void fillTable() {
        if (offlineRegisters == null) return;
        for (OfflineRegister register : offlineRegisters) {
            addToTable(register);
        }
    }

    private void initComponents() {
        jScrollPanePropertiesTable = new JScrollPane();
        jTableProperties = new JTable();

        //======== this ========
        setBorder(new TitledBorder(null, NLS_REGISTERS, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 16)));
        setLayout(new GridBagLayout());
        ((GridBagLayout) getLayout()).columnWidths = new int[]{0, 0};
        ((GridBagLayout) getLayout()).rowHeights = new int[]{0, 0};
        ((GridBagLayout) getLayout()).columnWeights = new double[]{1.0, 1.0E-4};
        ((GridBagLayout) getLayout()).rowWeights = new double[]{1.0, 1.0E-4};

        //======== jScrollPanePropertiesTable ========
        {

            //---- jTableProperties ----
            jTableProperties.setModel(new DefaultTableModel(
                    new Object[][]{},
                    new String[]{ NLS_NAME, NLS_OBISCODE, NLS_UNIT }
            ) {
                Class[] columnTypes = new Class[]{ String.class, ObisCode.class, Unit.class };

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnTypes[columnIndex];
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            });
            {
                TableColumnModel cm = jTableProperties.getColumnModel();
                cm.getColumn(0).setPreferredWidth(150);
                cm.getColumn(1).setPreferredWidth(50);
                cm.getColumn(2).setPreferredWidth(30);
            }
            jScrollPanePropertiesTable.setViewportView(jTableProperties);
        }
        add(jScrollPanePropertiesTable, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
    }

    private JScrollPane jScrollPanePropertiesTable;
    private JTable jTableProperties;

    private void addToTable(OfflineRegister offlineRegister) {
        Object[] obj = new Object[]{
                offlineRegister.getName(),
                offlineRegister.getObisCode(),
                offlineRegister.getUnit(),
        };
        getTableModel().addRow(obj);
    }

    private DefaultTableModel getTableModel() {
        return (DefaultTableModel) jTableProperties.getModel();
    }
}