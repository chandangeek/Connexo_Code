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
            emptyText: Uni.I18n.translate('property.regularReadingType.emptyText', 'UNI', 'Select a reading type'),
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