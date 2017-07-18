/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.Boolean', {
    extend: 'Uni.view.search.field.internal.Criteria',
    xtype: 'uni-search-criteria-boolean',
    text: Uni.I18n.translate('view.search.field.yesno.label', 'UNI', 'Text'),
    minWidth: 70,
    value: 1,

    defaults: {
        margin: 0,
        padding: 5
    },

    setValue: function(value) {
        if (value) {
            this.down('radiofield[inputValue="' + value[0].get('criteria')[0] + '"]').setValue(true);
        } else {
            this.down('radiofield[inputValue="' + this.value + '"]').setValue(true);
        }
    },

    getValue: function () {
        return [Ext.create('Uni.model.search.Value', {
            operator: '==',
            criteria: this.down('radiofield[checked]').inputValue
        })];
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.yes', 'UNI', 'Yes'),
                name: me.dataIndex,
                inputValue: "1",
                itemId: 'radio-yes',
                handler: me.onValueChange,
                scope: me
            },
            {
                xtype: 'menuseparator',
                padding: 0
            },
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.no', 'UNI', 'No'),
                name: me.dataIndex,
                inputValue: "0",
                itemId: 'radio-no',
                handler: me.onValueChange,
                scope: me
            }
        ];

        me.callParent(arguments);
    }
});