/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.reading-types-bulk-step2',
    border: false,
    name: 'selectOperation',
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step2.title', 'MTR', 'Step 2: Select action'),
    ui: 'large',
    items: {
        itemId: 'reading-types-bulk-step2-radio-group',
        xtype: 'radiogroup',
        columns: 1,
        vertical: true,
        defaults: {
            name: 'operation',
            submitValue: false,
            padding: '0 0 30 0'
        },
        items: [
            {
                itemId: 'reading-types-activate',
                boxLabel: '<b>' + Uni.I18n.translate('readingtypesmanagment.bulk.activate', 'MTR', 'Activate') + '</b>',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('readingtypesmanagment.bulk.activateMsg', 'MTR', 'This action makes the selected reading types available') + '</span>',
                name: 'operation',
                checked: true,
                inputValue: 'activate'
            },
            {
                itemId: 'reading-types-deactivate',
                boxLabel: '<b>' + Uni.I18n.translate('readingtypesmanagment.bulk.deactivate', 'MTR', 'Deactivate') + '</b>',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('readingtypesmanagment.bulk.deactivateMsg', 'MTR', 'This action makes the selected reading types unavailable') + '</span>',
                name: 'operation',
                inputValue: 'deactivate'
            }
        ]
    }
});