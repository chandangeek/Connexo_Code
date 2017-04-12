/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.AvailableMetrologyConfigurations', {
    extend: 'Uni.property.view.property.BaseCombo',

    listeners: {
        afterrender: {
            fn: function () {
                var me = this,
                    purposesStore = Ext.getStore('Imt.processes.store.PurposesWithValidationRuleSets'),
                    store = Ext.getStore('Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations');
                purposesStore.events['loadpurposestore'] && purposesStore.events['loadpurposestore'].clearListeners();
                purposesStore.events['clearpurposestore'] && purposesStore.events['clearpurposestore'].clearListeners();

                store.getProxy().setExtraParam('usagePointId', me.up('property-form').context.id);
            }
        },
        reloadPurposeStore: {
            fn: function (metrologyConfigurationId) {
                var me = this,
                    store = Ext.getStore('Imt.processes.store.PurposesWithValidationRuleSets');
                if(metrologyConfigurationId){
                    store.getProxy().setExtraParam('metrologyConfigurationId', metrologyConfigurationId);
                    store.fireEvent('loadpurposestore');
                } else {
                    store.fireEvent('clearpurposestore');
                }
            }
        }
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: 'Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            emptyText: Uni.I18n.translate('usagepoint.setMetrologyConfigurations.selectMC', 'IMT', 'Select a metrology configuration...'),
            displayField: 'name',
            valueField: 'id',
            minChars: 1,
            listeners: {
                change: {
                    fn: function (combo, newValue) {
                        var index = combo.getStore().findExact('id', newValue);
                        combo.mcData = index >= 0 ? combo.getStore().getAt(index).getData() : null;

                        me.fireEvent('reloadPurposeStore', newValue);
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

