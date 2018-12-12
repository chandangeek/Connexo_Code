/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    xtype: 'certificates-bulk-step3',
    name: 'selectActionItems',
    ui: 'large',

    requires: [
        'Pkj.store.Timeouts'
    ],
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step3.title', 'PKJ', 'Step 3: Action details'),

    items: [
        {
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'left'
            },
            width: '100%',
            items: [
                {
                    itemId: 'step3-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    text: Uni.I18n.translate('readingtypesmanagment.errors', 'PKJ', 'There are errors on this page that require your attention.')
                }
            ]
        },
        {
            xtype: 'form',
            ui: 'medium',
            itemId: 'new-alias-form',
            margin: '0 0 0 -16',
            title: Uni.I18n.translate('readingtypesmanagment.bulk.revokeCertificates', 'PKJ', 'Revoke certificates'),
            items: [
                {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('readingtypesmanagment.bulk.timeout', 'PKJ', 'Timeout'),
                    margin: '16 0 32 0',
                    required: true,
                    store: 'Pkj.store.Timeouts',
                    allowBlank: false,
                    itemId: 'timeout',
                    name: 'timeout',
                    valueField: 'value',
                    value: 30000,
                    displayField: 'displayValue',
                    queryMode: 'local'
                }
            ]
        }
    ]
});