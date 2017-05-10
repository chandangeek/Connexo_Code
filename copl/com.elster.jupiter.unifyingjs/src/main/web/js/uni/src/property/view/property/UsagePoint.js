/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.UsagePoint', {
    extend: 'Uni.property.view.property.BaseCombo',
    id: null,

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: Ext.create('Uni.property.store.UsagePoint'),
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            emptyText: Uni.I18n.translate('property.selectUsagePoint', 'UNI', 'Select usage point'),
            valueField: 'id',
            displayField: 'name',
            queryMode: 'remote',
            remoteFilter: true,
            queryParam: 'like',
            queryCaching: false,
            minChars: 1,
            editable: true,
            typeAhead: true,
            listeners: {
                blur: function (combo) {
                    if (!combo.getValue()) {
                        me.restoreDefault();
                    }
                }
            }
        };
    },

    isCombo: function () {
        return this.getComboField();
    },

    getValue: function () {
        var value = {
            id: this.getComboField().getValue(),
            name: this.getComboField().getRawValue()
        };

        if (value.id === value.name) {
            value.id = this.id;
        }
        return value;
    },

    setValue: function (value) {
        if (this.isCombo()) {
            this.id = value.id;
            this.getComboField().setRawValue(value.name);
        } else {
            this.callParent([value.name]);
        }
    }
});