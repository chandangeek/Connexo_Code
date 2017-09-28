/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.AddCommandStep3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.add-command-step3',
    ui: 'large',
    items: [
        {
           xtype: 'container',
            itemId: 'msg-container'
        },
        {
            xtype: 'radiogroup',
            name: 'trigger',
            fieldLabel: Uni.I18n.translate('general.trigger', 'MDC', 'Trigger'),
            labelWidth: 100,
            required: true,
            vertical: true,
            columns: 1,
            items: [
                {
                    //itemId: 'dynamicDeviceGroup',
                    boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                    name: 'trigger',
                    inputValue: true
                },
                {
                    //itemId: 'staticDeviceGroup',
                    boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'),
                    name: 'trigger',
                    inputValue: false,
                    checked: true
                }
            ]
        }
    ]
});