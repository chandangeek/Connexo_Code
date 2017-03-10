/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.DataValidationContent', {
    extend: 'Ext.container.Container',
    defaults: {
        labelAlign: 'left',
        labelStyle: 'font-weight: normal; padding-left: 50px'
    },
    dataValidationLastChecked: null,
    initComponent: function () {
       var me = this;
        me.items = [
            {
                xtype: 'datefield',
                itemId: 'validateLoadProfileFromDate',
                editable: false,
                showToday: false,
                value: me.dataValidationLastChecked,
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validateNow.item1', 'MDC', 'The data of load profile will be validated starting from'),
                labelWidth: 375,
                labelPad: 0.5
            },
            {
                xtype: 'panel',
                itemId: 'validateLoadProfileDateErrors',
                hidden: true,
                bodyStyle: {
                    color: '#eb5642',
                    padding: '0 0 15px 65px'
                },
                html: ''
            },
            {
                xtype: 'displayfield',
                value: '',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.validateNow.item2', 'MDC', 'Note: The date displayed by default is the last checked (the moment when the last interval was checked in the validation process).'),
                labelWidth: 500
            }
        ];
        me.callParent(arguments)
    }
});
