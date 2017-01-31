/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by Lucian on 12/12/2014.
 */
Ext.define('Yfn.view.controls.MultiSelectCombo', {
    extend: 'Uni.form.filter.FilterCombobox',
    alias: 'widget.multiselect-combo',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',

    requires:['Yfn.view.controls.MultiSelectBoundList'],

    loadStore: true,

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },
    createPicker: function() {

        var me = this,
            picker,
            pickerCfg = Ext.apply({
                xtype: 'multi-select-boundlist',
                pickerField: me,
                selModel: {
                    mode: me.multiSelect ? 'SIMPLE' : 'SINGLE'
                },
                floating: true,
                hidden: true,
                store: me.store,
                displayField: me.displayField,
                focusOnToFront: false,
                pageSize: me.pageSize,
                tpl: me.tpl,
                emptyText: ' '
            }, me.listConfig, me.defaultListConfig);

        picker = me.picker = Ext.widget(pickerCfg);
        if (me.pageSize) {
            picker.pagingToolbar.on('beforechange', me.onPageChange, me);
        }

        me.mon(picker, {
            itemclick: me.onItemClick,
            refresh: me.onListRefresh,
            scope: me
        });

        me.mon(picker.getSelectionModel(), {
            beforeselect: me.onBeforeSelect,
            beforedeselect: me.onBeforeDeselect,
            selectionchange: me.onListSelectionChange,
            scope: me
        });

        return picker;
    }
});
