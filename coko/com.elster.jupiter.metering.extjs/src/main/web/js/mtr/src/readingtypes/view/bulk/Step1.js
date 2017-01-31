/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'reading-types-bulk-step1',
    name: 'selectReadingTypes',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mtr.readingtypes.view.bulk.ReadingTypesSelectionGrid'
    ],

    title: Uni.I18n.translate('readingtypesmanagment.bulk.step1.title', 'MTR', 'Step 1: Select reading types'),

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
                        text: Uni.I18n.translate('readingtypesmanagment.noReadingTypesSelectedError', 'MTR', 'It is required to select one or more reading types to go to the next step.')
                    }
                ]
            },
            {
                xtype: 'reading-types-selection-grid',
                itemId: 'reading-types-selection-grid'
            },
            {
                xtype: 'container',
                itemId: 'step-selection-error',
                margin: '-20 0 0 0',
                hidden: true,
                html: '<span style="color: #eb5642">' + Uni.I18n.translate('searchItems.bulk.selectatleast1device', 'MTR', 'Select at least 1 reading type') + '</span>'
            }
        ];

        this.callParent(arguments);
    }

});