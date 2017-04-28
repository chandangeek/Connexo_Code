/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.UsagePoint', {
    extend: 'Uni.property.view.property.BaseCombo',

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
                blur: {
                    fn: function (combo) {
                        if (!combo.getValue()) {
                            me.restoreDefault();
                        }
                    }
                }
            }
        };
    },

    isCombo: function () {
        return this.getComboField();
    },

    getValue: function () {
        return {
            id: this.getComboField().getValue(),
            name: this.getComboField().getRawValue()
        }
    },

    setValue: function (value) {
        if (this.isCombo()) {
            this.getComboField().setRawValue(value.name);
            this.getComboField().setValue(value.id);
        } else {
            this.callParent([value.name]);
        }
    }
});