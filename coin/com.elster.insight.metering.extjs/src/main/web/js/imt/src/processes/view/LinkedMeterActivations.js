/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.LinkedMeterActivations', {
    extend: 'Uni.property.view.property.BaseCombo',

    listeners: {
        afterrender: {
            fn: function () {
                var me = this,
                    store = Ext.getStore('Imt.usagepointmanagement.store.MeterRoles');

                store.getProxy().setExtraParam('usagePointId', me.up('property-form').context.id);
            }
        }
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: 'Imt.usagepointmanagement.store.MeterRoles',
            queryMode: 'remote',
            displayField: 'meter',
            valueField: 'id',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            editable: false,
            listeners: {
                change: {
                    fn: Ext.emptyFn //disable validation
                }
            }
        }
    },

    getField: function () {
        return this.down('combobox');
    }
});
