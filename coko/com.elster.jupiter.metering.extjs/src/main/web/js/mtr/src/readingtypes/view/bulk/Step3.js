/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    xtype: 'reading-types-bulk-step3',
    name: 'selectActionItems',
    ui: 'large',

    requires: [
    ],
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step3.title', 'MTR', 'Step 3: Action details'),


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
                    text: Uni.I18n.translate('readingtypesmanagment.errors', 'MTR', 'There are errors on this page that require your attention.')
                }
            ]
        },
        {
            xtype: 'form',
            ui: 'medium',
            itemId: 'new-alias-form',
            margin: '0 0 0 -16',
            title: Uni.I18n.translate('readingtypesmanagment.bulk.editreadingtypes', 'MTR', 'Edit reading types'),
            items: [
                {
                    xtype: 'textfield',
                    fieldLabel: Uni.I18n.translate('readingtypesmanagment.bulk.alias', 'MTR', 'Alias'),
                    margin: '16 0 32 0',
                    required: true,
                    allowBlank: false,
                    itemId: 'new-alias',
                    name: 'fullAlias'
                }
            ]
        }
    ]
});