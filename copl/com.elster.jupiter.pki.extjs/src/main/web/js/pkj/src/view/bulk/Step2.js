/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.certificates-bulk-step2',
    border: false,
    name: 'selectOperation',
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step2.title', 'PKJ', 'Step 2: Select action'),
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
                itemId: 'revoke-certificates',
                boxLabel: '<b>' + Uni.I18n.translate('readingtypesmanagment.bulk.revokeCertificates', 'PKJ', 'Revoke certificates') + '</b>',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('readingtypesmanagment.bulk.deactivateMsg', 'PKJ', 'This action makes the selected reading types unavailable') + '</span>',
                name: 'operation',
                checked: true,
                inputValue: 'revoke'
            }
        ]
    }
});