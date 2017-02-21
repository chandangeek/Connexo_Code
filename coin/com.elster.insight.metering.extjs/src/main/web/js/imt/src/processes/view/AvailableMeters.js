/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.AvailableMeters', {
    extend: 'Uni.property.view.property.BaseCombo',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: 'Imt.usagepointsetup.store.Devices',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            emptyText: Uni.I18n.translate('usagepoint.setMeters.strtTyping', 'IMT', 'Start typing to select a meter'),
            displayField: 'name',
            valueField: 'mRID',
            anyMatch: true,
            queryParam: 'like',
            queryCaching: false,
            minChars: 1,
            listeners: {
                expand: {
                    fn: me.comboLimitNotification
                },
                change: {
                    fn: function (combo, newValue) {
                        var index = combo.getStore().findExact('mRID', newValue);
                        combo.meterData = index >= 0 ? combo.getStore().getAt(index).getData() : null;
                    }
                },
                blur: {
                    fn: function (combo) {
                        if (Ext.isEmpty(combo.meterData)) {
                            me.restoreDefault();
                        }
                    }
                }
            }
        };
    },

    getField: function () {
        return this.down('combobox');
    },

    comboLimitNotification: function (combo) {
        var picker = combo.getPicker(),
            fn = function (view) {
                var store = view.getStore(),
                    el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

                if (store.getTotalCount() > store.getCount()) {
                    el.appendChild({
                        tag: 'li',
                        html: Uni.I18n.translate('usagePoint.setMeters.keepTyping', 'IMT', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    }
});

