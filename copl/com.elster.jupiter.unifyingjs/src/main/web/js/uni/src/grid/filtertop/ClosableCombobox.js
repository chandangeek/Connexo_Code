/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.filtertop.ClosableCombobox', {
//    extend: 'Uni.grid.filtertop.ComboBox',
    extend: 'Ext.container.Container',
    xtype: 'uni-closable-combobox',

    required: [
        'Uni.grid.filtertop.ComboBox'
    ],
    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    layout: {
        type: 'hbox'
    },
    initComponent: function () {
        var me = this;
        me.store = me.store ? Ext.getStore(me.store) || Ext.create(me.store) : Ext.create('Ext.data.Store');
        me.combobox = Ext.create('Uni.grid.filtertop.ComboBox',{
            store: me.store,
            emptyText: me.emptyText,
            queryMode: me.queryMode,
            valueField: me.valueField,
            displayField: me.displayField,
            forceSelection: me.forceSelection,
            multiSelect: me.multiSelect,
            listeners: {
                filterupdate: function() {
                    me.fireFilterUpdateEvent()
                }
            }
        });
        me.items = [
            me.combobox,
            {
                xtype: 'button',
                margin: '0 0 0 2',
                iconCls: 'icon-cancel-circle2',
                action: 'remove',
                listeners: {
                    click: function () {
                        me.removeHandler.apply(me, arguments)
                    }
                }
            }
        ];
        me.callParent(arguments);
    },

    fireFilterUpdateEvent: function () {
        this.fireEvent('filterupdate');
    },

    setFilterValue: function (data) {
        return this.combobox.setFilterValue(data)
    },

    getFilterValue: function () {
        if (this.combobox.getValue) {
            return this.combobox.getValue();
        }
    },

    resetValue: function () {
        var me = this;
        if (me.combobox.reset && !me.deleted) {
            me.combobox.reset();
        }
    },

    isArrayOfNumerics: function (array) {
        return this.combobox.isArrayOfNumerics(array)
    },

    getParamValue: function () {
        return this.combobox.getParamValue()
    },

    createStoreFromOptions: function () {
        return this.combobox.createStoreFromOptions()
    },

    getValueForDisplayValue: function (displayValue) {
        return this.combobox.getValueForDisplayValue(displayValue)
    }
});

