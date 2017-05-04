/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.IrregularReadingType', {
    extend: 'Uni.property.view.property.BaseCombo',
    layout: 'fit',
    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: 'Uni.property.store.ReadingTypes',
            width: me.width,
            maxWidth: 283,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            emptyText: Uni.I18n.translate('property.readingType.emptyText', 'UNI', 'Start typing to select a reading type'),
            displayField: 'fullAliasName',
            valueField: 'mRID',
            anyMatch: true,
            queryCaching: false,
            minChars: 1,
            listeners: {
                expand: {
                    fn: me.comboLimitNotification
                },
                change: {
                    fn: function (combo, newValue) {
                        var index = combo.getStore().findExact('mRID', newValue),
                            filter = [
                                {
                                    property: 'fullAliasName',
                                    value: '*' + combo.getRawValue() + '*'
                                },
                                {
                                    property: 'equidistant',
                                    value: false
                                }
                            ];

                        combo.readingTypeData = index >= 0 ? combo.getStore().getAt(index).getData() : null;
                        combo.getStore().getProxy().setExtraParam('filter', Ext.encode(filter));
                    }
                },
                blur: {
                    fn: function (combo) {
                        if (Ext.isEmpty(combo.readingTypeData)) {
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
                        html: Uni.I18n.translate('property.readingType.keepTyping', 'UNI', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    },

    getValue: function () {
        var me = this;
        return {
            mRID: me.getField().getValue(),
            fullAliasName: me.getField().getRawValue()
        };
    },

    setValue: function (value) {
        var me = this,
            combo = me.getField();

        if (me.isEdit && value) {
            combo.suspendEvent('change');
            combo.setValue(value.mRID);
            combo.setRawValue(value.fullAliasName);
            combo.resumeEvent('change');
            me.setDefaultFilter(value);
        } else {
            me.callParent([value.fullAliasName]);
        }
    },

    setDefaultFilter: function (value) {
        var me = this,
            combo = me.getField(),
            filter = [
                {
                    property: 'fullAliasName',
                    value: '*' + value.fullAliasName + '*'
                },
                {
                    property: 'equidistant',
                    value: false
                }
            ];

        combo.getStore().getProxy().setExtraParam('filter', Ext.encode(filter));
        combo.readingTypeData = value;
        combo.getStore().load();
    }
});