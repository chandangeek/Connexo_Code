/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.AvailableTransitions', {
    extend: 'Uni.property.view.property.BaseCombo',

    listeners: {
        afterrender: {
            fn: function () {
                var me = this,
                    store = Ext.getStore('Imt.processes.store.AvailableTransitions');

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
            store: 'Imt.processes.store.AvailableTransitions',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            emptyText: Uni.I18n.translate('usagepoint.setTransitions.selectTransition', 'IMT', 'Select a transition...'),
            displayField: 'name',
            valueField: 'id',
            minChars: 1,
            listeners: {
                change: {
                    fn: function (combo, newValue) {
                        var index = combo.getStore().findExact('id', newValue);
                        combo.mcData = index >= 0 ? combo.getStore().getAt(index).getData() : null;
                    }
                },
                blur: {
                    fn: function (combo) {
                        if (Ext.isEmpty(combo.mcData)) {
                            me.restoreDefault();
                        }
                    }
                }
            }
        };
    },

    getField: function () {
        return this.down('combobox');
    }
});

