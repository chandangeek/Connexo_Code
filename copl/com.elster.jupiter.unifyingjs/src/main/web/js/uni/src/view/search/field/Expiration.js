/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.view.search.field.Expiration', {
    extend: 'Uni.view.search.field.internal.Criteria',
    xtype: 'uni-search-criteria-expiration',
    text: Uni.I18n.translate('view.search.field.yesno.label', 'UNI', 'Text'),
    minWidth: 180,
    value: 1,

    defaults: {
        margin: 0,
        padding: 5
    },

    setValue: function(value) {
        var criterium = value ? value[0].get('criteria')[0] : 'expired';
        if (criterium == 'expired') {
            this.down('#uni-expiration-radio-expired').setValue(true);
        } else if (criterium == 'expires_1week') {
            this.down('#uni-expiration-radio-expires-1week').setValue(true);
        } else if (criterium == 'expires_1month') {
            this.down('#uni-expiration-radio-expires-1month').setValue(true);
        } else if (criterium == 'expires_3months') {
            this.down('#uni-expiration-radio-expires-3months').setValue(true);
        } else if (criterium == 'obsolete') {
            this.down('#uni-expiration-radio-obsolete').setValue(true);
        }
    },

    getValue: function () {
        var criterium = 'expired';
        if (this.down('#uni-expiration-radio-expires-1week').getValue()) {
            criterium = 'expires_1week';
        } else if (this.down('#uni-expiration-radio-expires-1month').getValue()) {
            criterium = 'expires_1month';
        } else if (this.down('#uni-expiration-radio-expires-3months').getValue()) {
            criterium = 'expires_3months';
        } else if (this.down('#uni-expiration-radio-obsolete').getValue()) {
            criterium = 'obsolete';
        }
        return [Ext.create('Uni.model.search.Value', {
            operator: '==',
            criteria: criterium
        })];
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.expired', 'UNI', 'Expired'),
                name: me.dataIndex,
                inputValue: "1",
                itemId: 'uni-expiration-radio-expired',
                handler: me.onValueChange,
                scope: me
            },
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.expiresWithinOneWeek', 'UNI', 'Expires within a week'),
                name: me.dataIndex,
                inputValue: "0",
                itemId: 'uni-expiration-radio-expires-1week',
                handler: me.onValueChange,
                scope: me
            },
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.expiresWithinOneMonth', 'UNI', 'Expires within a month'),
                name: me.dataIndex,
                inputValue: "0",
                itemId: 'uni-expiration-radio-expires-1month',
                handler: me.onValueChange,
                scope: me
            },
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.expiresWithinThreeMonths', 'UNI', 'Expires within 3 months'),
                name: me.dataIndex,
                inputValue: "0",
                itemId: 'uni-expiration-radio-expires-3months',
                handler: me.onValueChange,
                scope: me
            },
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('general.expiration.obsolete', 'UNI', 'Obsolete'),
                name: me.dataIndex,
                inputValue: "0",
                itemId: 'uni-expiration-radio-obsolete',
                handler: me.onValueChange,
                scope: me
            }
        ];

        me.callParent(arguments);
    }
});
