/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.view.AddEditCrlRequest', {
    extend: 'Ext.form.Panel',
    alias: 'widget.crl-request-addedit-tgm',

    itemId: 'crl-request-addedit-tgm',

    require: [
        'Cfg.store.DaysWeeksMonths',
        'Mdc.crlrequest.store.SecurityAccessorsWithPurpose',
        'Mdc.securityaccessors.store.SecurityAccessors'
    ],

    defaults: {
        labelWidth: 250,
        validateOnChange: false,
        validateOnBlur: false
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'combobox',
                itemId: 'crl-security-accessor-purpose',
                name: 'securityAccessorName',
                width: 600,
                fieldLabel: Uni.I18n.translate('general.securityAccessor', 'MDC', 'Security accessor'),
                labelWidth: 250,
                store: 'Mdc.securityaccessors.store.SecurityAccessors',
                required: true,
                emptyText: Uni.I18n.translate('crlrequest.securityAccessorPrompt', 'MDC', 'Select security accessor...'),
                queryDelay: 500,
                queryCaching: false,
                minChars: 0,
                allowBlank: false,
                forceSelection: false,
                displayField: 'name',
                queryMode: 'remote',
                valueField: 'id'
            },
            {
                xtype: 'textfield',
                name: 'caName',
                itemId: 'crl-caName',
                required: true,
                allowBlank: false,
                fieldLabel: Uni.I18n.translate('crlrequest.caName', 'MDC', 'CA name')
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('crlrequest.requestFrequency', 'MDC', 'Request frequency'),
                itemId: 'crl-recurrence-container',
                name: 'timeDurationInfo',
                required: true,
                layout: 'hbox',
                items: [
                    {
                        itemId: 'crl-recurrence-number',
                        xtype: 'numberfield',
                        name: 'recurrence',
                        allowDecimals: false,
                        minValue: 1,
                        value: 1,
                        width: 65,
                        margin: '0 10 0 0'
                    },
                    {
                        itemId: 'crl-recurrence-type',
                        xtype: 'combobox',
                        name: 'recurrenceCode',
                        store: 'Cfg.store.DaysWeeksMonths',
                        queryMode: 'local',
                        displayField: 'displayValue',
                        valueField: 'name',
                        editable: false,
                        width: 125,
                        value: 'hours'
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('crlrequest.startOn', 'MDC', 'Start on'),
                required: true,
                layout: 'hbox',
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'crl-start-on',
                        layout: 'hbox',
                        name: 'nextRun',
                        dateConfig: {
                            allowBlank: true,
                            value: new Date(),
                            editable: false,
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            fieldLabel: Uni.I18n.translate('crlrequest.at', 'MDC', 'at'),
                            labelWidth: 10,
                            margin: '0 0 0 10',
                            value: new Date().getHours()
                        },
                        minutesConfig: {
                            width: 55,
                            value: new Date().getMinutes()
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});
