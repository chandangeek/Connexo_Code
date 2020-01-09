/*
 * TableBuilder.java
 *
 * Created on 10 februari 2003, 10:39
 */

package com.energyict.mdc.engine.offline.gui.beans;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.ObjectShadow;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.models.AspectTableModel;
import com.energyict.mdc.engine.offline.gui.models.EditableAspectTableModel;
import com.energyict.mdc.engine.offline.gui.table.TableUtils;
import com.energyict.mdc.engine.offline.gui.table.renderer.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.table.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;


/**
 * @author Karel
 */
public class TableBuilder<T> {

    private static final Log logger = LogFactory.getLog(TableBuilder.class);

    private List<T> models;
    private Class<T> beanType;
    private List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
    private String[] aspects;
    private boolean readOnly = true;
    private AspectTableModel<T> tableModel = null;
    private JTable table = null;
    private JDialog dialog;
    private TableBuilder<ObjectShadow> subBuilder;
    // so to make them accessible via getAddButton and getRemoveButton
    // this permits to implement an own actionlistener
    private JButton jAdd = null, jRemove = null;
    private boolean extendable = false;

    /**
     * Creates a new instance of TableBuilder
     */
    public TableBuilder(List<T> models) {
        this.models = models;
    }

    public TableBuilder(List<T> models, String[] aspects) {
        this(models);
        this.aspects = aspects;
    }

    public TableBuilder(List<T> models, List<String> lstAspects) {
        this.models = models;
        aspects = lstAspects.toArray(new String[lstAspects.size()]);
    }

    public TableBuilder(List<T> models, Class<T> beanType) {
        this(models);
        this.beanType = beanType;
    }

    public TableBuilder(List<T> models, String[] aspects, Class<T> beanType) {
        this(models, aspects);
        this.beanType = beanType;
    }

    public TableBuilder(List<T> models, List<String> lstAspects, Class<T> beanType) {
        this(models, lstAspects);
        this.beanType = beanType;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

        public void setExtendable(boolean extendable) {
        this.extendable = extendable;
    }

    // By default the columnname is set to the displayname of the descriptor
    // Sometimes it seems necessary to be able to set another translationkey

    public void setDisplayName(String aspect, String translationKey) {
        if (aspect == null) {
            throw new IllegalArgumentException("Parameter 'aspect' cannot be null");
        }
        for (PropertyDescriptor each : descriptors) {
            if (aspect.equals(each.getName())) {
                each.setDisplayName(translationKey);
                break;
            }
        }
    }

    protected  List<T> getModels() {
        return models;
    }

    protected boolean isReadOnly(){
        return readOnly;
    }

    protected List<PropertyDescriptor> getDescriptors() {
        return descriptors;
    }

    protected boolean isExtendable() {
        return this.extendable;
    }

    protected void buildDescriptors() {
        if (beanType == null) {
            if (models.isEmpty()) {
                return;
            } else {
                beanType = (Class<T>) models.get(0).getClass();
            }
        }
        descriptors = new ArrayList<PropertyDescriptor>();
        if (aspects == null) {
            addAllDescriptors();
        } else {
            addSelectedDescriptors();
        }
    }

    protected void addAllDescriptors() {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
            PropertyDescriptor[] allDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < allDescriptors.length; i++) {
                if (allDescriptors[i].getReadMethod() != null) {
                    descriptors.add(allDescriptors[i]);
                }
            }
        } catch (IntrospectionException ex) {
            throw new ApplicationException(ex);
        }
    }

    protected void addSelectedDescriptors() {
        for (int i = 0; i < aspects.length; i++) {
            PropertyDescriptor descriptor;
            try {
                descriptor = new PropertyDescriptor(aspects[i], beanType);
            } catch (IntrospectionException ex) {
                String getter = "get" + capitalize(aspects[i]);
                try {
                    descriptor = new PropertyDescriptor(aspects[i], beanType, getter, null);
                } catch (IntrospectionException ex2) {
                    getter = "is" + capitalize(aspects[i]);
                    try {
                        descriptor = new PropertyDescriptor(aspects[i], beanType, getter, null);
                    } catch (IntrospectionException ex3) {
                        descriptor = null;
                    }
                }
            }
            if (descriptor != null || extendable) {
                descriptors.add(descriptor);
            }
        }
    }

    public AspectTableModel<T> getTableModel() {
        if (tableModel == null) {
            tableModel = constructTableModel();
        }
        return tableModel;
    }

    protected  AspectTableModel<T> constructTableModel(){
        buildDescriptors();
        if (readOnly) {
                return new AspectTableModel<T>(models, descriptors);
        } else {
            return new EditableAspectTableModel<T>(models, descriptors);
        }
    }

    public JTable getTable() {
        return getTable(true);
    }

    // new Flag: setMissingResourcesFlag -
    // should the 'MR' (missing resource) indicator being used in the table header Yes/No    

    public JTable getTable(boolean missingResourcesFlag) {
        if (table == null) {
            AspectTableModel tableModel = getTableModel();
            tableModel.setMissingResourcesFlag(missingResourcesFlag);

            table = new JTable(tableModel);
            customize(table);
        }
        return table;
    }

    public void customize(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableUtils.customize(table);
    }
    // Set the columns widths and set the columns identifier */
    // this can only be done with table created with a tablebuilder !!
    // use this method where you want to use the new TableColumnChooseModel

    public void setColumnIdentifiers(JTable table) {
        identifyColumns(table);
        table.setAutoCreateColumnsFromModel(false);
    }

    public void setHeaderRenderer(JTable table) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setDefaultRenderer(new HeaderRenderer(table));
        }
    }

    public void setRenderers(JTable table) {
        table.setDefaultRenderer(BigDecimal.class, new BigDecimalRenderer());
        table.setDefaultRenderer(Date.class, new DateRenderer());
        table.setDefaultRenderer(TimeZone.class, new TimeZoneRenderer());
        table.setDefaultRenderer(Level.class, new LevelRenderer());
    }

    public void setEditors(JTable table) {
        //    System.out.println("combo height = " + new JComboBox().getPreferredSize().height);
        //table.setRowHeight(new JComboBox().getPreferredSize().height);
        table.setRowHeight(20);
        table.setDefaultEditor(BigDecimal.class, new BigDecimalCellEditor(table));
        table.setDefaultEditor(Integer.class, new IntegerCellEditor(table));
        table.setDefaultEditor(Date.class, new DateCellEditor());
    }

    public void initColumnSizes(JTable table) {
        TableModel model = table.getModel();
        TableColumnModel cModel = table.getColumnModel();
        int iRowCnt = table.getModel().getRowCount();
        //limit to the first ten rows
        if (iRowCnt > 20) {
            iRowCnt = 20;
        }
        for (int i = 0; i < cModel.getColumnCount(); i++) {
            TableColumn column = cModel.getColumn(i);
            TableCellRenderer renderer = column.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            java.awt.Component comp = renderer.getTableCellRendererComponent(
                    table,
                    column.getHeaderValue(),
                    false,
                    false,
                    0,
                    0);
            int iMaxWidth = comp.getPreferredSize().width + 1;
            for (int iRow = 0; iRow < iRowCnt; iRow++) {
                renderer = column.getCellRenderer();
                if (renderer == null) {
                    renderer = table.getDefaultRenderer(model.getColumnClass(i));
                }
                comp = renderer.getTableCellRendererComponent(
                        table,
                        model.getValueAt(iRow, i),
                        false,
                        false,
                        0,
                        i);
                int iCellWidth = comp.getPreferredSize().width + 1;
                iMaxWidth = Math.max(iMaxWidth, iCellWidth);
            }
            column.setPreferredWidth(iMaxWidth);
        }
    }


    public JScrollPane getScrollPane() {
        return new JScrollPane(getTable());
    }

    public JPanel getEditPanel() {
        JPanel result = new JPanel(new java.awt.BorderLayout());
        Box east = Box.createVerticalBox();
        jAdd = new JButton();
        jRemove = new JButton();
        east.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        jAdd.setText("Add");
        jAdd.setMaximumSize(new java.awt.Dimension(100, 26));
        jAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAddActionPerformed(evt);
            }
        });
        east.add(jAdd);
        east.add(Box.createVerticalStrut(10));
        jRemove.setText("Remove");
        jRemove.setMaximumSize(new java.awt.Dimension(100, 26));
        jRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRemoveActionPerformed(evt);
            }
        });
        east.add(jRemove);
        result.add(east, java.awt.BorderLayout.EAST);
        result.add(getScrollPane(), java.awt.BorderLayout.CENTER);
        return result;
    }

    public JButton getAddButton() {
        return jAdd;
    }

    public JButton getRemoveButton() {
        return jRemove;
    }

    private void identifyColumns(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        List<TableColumn> columns = Collections.list(columnModel.getColumns());
        for (int i = 0; i < aspects.length; i++) {
            if (i == columns.size()) {
                break;
            }
            TableColumn each = columns.get(i);
            each.setIdentifier(aspects[i]);
        }
    }

        private void jAddActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            getTableModel().add(beanType.newInstance());
        } catch (InstantiationException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

//    public JPanel getAddRemovePanel(final BusinessObjectFactory factory) {
//        JPanel result = new JPanel(new java.awt.BorderLayout());
//        Box east = Box.createVerticalBox();
//        jAdd = new JButton();
//        jRemove = new JButton();
//        east.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
//        jAdd.setText("Add");
//        jAdd.setMaximumSize(new java.awt.Dimension(100, 26));
//        jAdd.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                jAddActionPerformed(evt, factory);
//            }
//        });
//        east.add(jAdd);
//        east.add(Box.createVerticalStrut(10));
//        jRemove.setText("Remove");
//        jRemove.setMaximumSize(new java.awt.Dimension(100, 26));
//        jRemove.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                jRemoveActionPerformed(evt);
//            }
//        });
//        east.add(jRemove);
//        result.add(east, java.awt.BorderLayout.EAST);
//        getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//        result.add(getScrollPane(), java.awt.BorderLayout.CENTER);
//        return result;
//    }
//
//
//    private void jAddActionPerformed(java.awt.event.ActionEvent evt, final BusinessObjectFactory factory) {
//        try {
//            Iterator it = factory.findAll().iterator();
//            List<ObjectShadow> shadows = new ArrayList<ObjectShadow>();
//            while (it.hasNext()) {
//                Object businessObject = it.next();
//                Method method = businessObject.getClass().getMethod("getShadow", null);
//                shadows.add((ObjectShadow) method.invoke(businessObject, null));
//            }
//            subBuilder = new TableBuilder<ObjectShadow>(shadows, factory.getAspects());
//            dialog =
//                    subBuilder.getSelectionDialog(
//                            new java.awt.event.ActionListener() {
//                                public void actionPerformed(java.awt.event.ActionEvent evt) {
//                                    jSelectActionPerformed(evt, factory);
//                                }
//                            }
//                    );
//            dialog.pack();
//            dialog.show();
//        } catch (NoSuchMethodException ex) {
//            logger.error(ex.getMessage(), ex);
//        } catch (IllegalAccessException ex) {
//            logger.error(ex.getMessage(), ex);
//        } catch (InvocationTargetException ex) {
//            logger.error(ex.getMessage(), ex);
//        }
//    }
//
//    private void jSelectActionPerformed(java.awt.event.ActionEvent evt, BusinessObjectFactory factory) {
//        ObjectShadow selection = subBuilder.getSelection();
//        if (selection != null) {
//            try {
//                Method method = factory.getClass().getMethod("create", selection.getClass());
//                T bo = (T) method.invoke(factory, selection);
//
//                getTableModel().add(bo);
//                dialog.dispose();
//
//            } catch (NoSuchMethodException e) {
//                logger.error(e.getMessage(), e);
//            } catch (InvocationTargetException e) {
//                logger.error(e.getMessage(), e);
//            } catch (IllegalAccessException e) {
//                logger.error(e.getMessage(), e);
//            }
//        }
//    }

    private void jRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        int selIndex = getTable().getSelectionModel().getMinSelectionIndex();
        if (selIndex == -1) {
            return;
        }
        getTableModel().remove(selIndex);
    }

    public JDialog getSelectionDialog(java.awt.event.ActionListener listener) {
        JDialog result = new JDialog();
        java.awt.Container container = result.getContentPane();
        container.setLayout(new java.awt.BorderLayout());
        JButton jSelect = new JButton("Select");
        jSelect.addActionListener(listener);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(jSelect);
        container.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        container.add(getScrollPane(), java.awt.BorderLayout.CENTER);
        return result;
    }

    public T getSelection() {
        int index = getTable().getSelectionModel().getMinSelectionIndex();
        if (index == -1) {
            return null;
        } else {
            return models.get(index);
        }
    }

    static public String capitalize(String in) {
        return Utils.capitalize(in);
    }

    public void setColumnSortable(int colIndex, boolean sortable) {
        getTableModel().setColumnSortable(colIndex, sortable);
    }
}
    