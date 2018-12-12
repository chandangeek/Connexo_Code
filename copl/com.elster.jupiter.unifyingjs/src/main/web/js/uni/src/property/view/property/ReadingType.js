/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.ReadingType', {
    extend: 'Uni.property.view.property.BaseCombo',

    getEditCmp: function () {
        var me = this,
            filter = me.getFilter('', me.property.getPropertyType().get('simplePropertyType'));

        Ext.getStore('Uni.property.store.ReadingTypes').getProxy().setExtraParam('filter', Ext.encode(filter));

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
                            filter = me.getFilter(combo.getRawValue(), me.property.getPropertyType().get('simplePropertyType'));

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

    getDisplayCmp: function () {
        return {
            xtype: 'displayfield',
            flex: 1,
            fieldLabel: this.fieldLabel,
            minWidth: 130,
            maxWidth: 200,
            name: this.getName(),
            itemId: this.key + 'displayfield',
            renderer: function (value) {
                if (value === '-') {
                    this.flex = undefined;
                    this.minWidth = undefined;
                }
                return value;
            }
        }
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
            filter = me.getFilter(value.fullAliasName, me.property.getPropertyType().get('simplePropertyType'));

        combo.getStore().getProxy().setExtraParam('filter', Ext.encode(filter));
        combo.readingTypeData = value;
        combo.getStore().load();
    },

    getFilter: function (value, type) {
        var defaultFilter = {
            property: 'fullAliasName',
            value: '*' + value + '*'
        };

        switch (type) {
            case 'ANY_READINGTYPE':
                return [defaultFilter];
            case 'IRREGULAR_READINGTYPE':
                return [
                    defaultFilter,
                    {
                        property: 'equidistant',
                        value: false
                    }
                ];
            case 'REGULAR_READINGTYPE':
                return [
                    defaultFilter,
                    {
                        property: 'equidistant',
                        value: true
                    }
                ];
            default:
                return [defaultFilter];
        }
    }
});