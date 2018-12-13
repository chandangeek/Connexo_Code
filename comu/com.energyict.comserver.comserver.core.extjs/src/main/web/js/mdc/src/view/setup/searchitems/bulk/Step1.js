/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'searchitems-bulk-step1',
    name: 'selectDevices',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.searchitems.bulk.DevicesSelectionGrid'
    ],

    title: Uni.I18n.translate('searchItems.bulk.step1.title', 'MDC', 'Step 1: Select devices'),

    initComponent: function () {
        this.items = [
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'left'
                },
                width: '100%',
                items: [
                    {
                        itemId: 'step1-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true,
                        text: Uni.I18n.translate('searchItems.bulk.devicesError', 'MDC', 'It is required to select one or more devices to go to the next step.')
                    }
                ]
            },
            {
                xtype: 'devices-selection-grid',
                store: this.deviceStore,
                itemId: 'devicesgrid'
            },
            {
                xtype: 'container',
                itemId: 'stepSelectionError',
                margin: '-20 0 0 0',
                hidden: true,
                html: '<span style="color: #eb5642">' + Uni.I18n.translate('searchItems.bulk.selectatleast1device', 'MDC', 'Select at least 1 device') + '</span>'
            }
        ];

        this.callParent(arguments);
    }

});