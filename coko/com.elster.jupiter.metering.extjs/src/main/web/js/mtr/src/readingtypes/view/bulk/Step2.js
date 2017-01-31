/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.Step2', {
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
                itemId: 'reading-types-edit-alias',
                boxLabel: '<b>' + Uni.I18n.translate('readingtypesmanagment.bulk.editalias', 'MTR', 'Edit') + '</b>',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('readingtypesmanagment.bulk.editaliasMsg', 'MTR', 'This action allows you to edit reading type\'s alias') + '</span>',
                name: 'operation',
                inputValue: 'edit',
                checked: true
            },
            {
                itemId: 'reading-types-activate',
                boxLabel: '<b>' + Uni.I18n.translate('readingtypesmanagment.bulk.activate', 'MTR', 'Activate') + '</b>',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('readingtypesmanagment.bulk.activateMsg', 'MTR', 'This action makes the selected reading types available') + '</span>',
                name: 'operation',
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