/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.RegularReadingType', {
    extend: 'Uni.property.view.property.IrregularReadingType',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: 'Uni.property.store.ReadingTypes',
            width: me.width,
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
                                    value: true
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
    }
});