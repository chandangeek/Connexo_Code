/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'certificates-bulk-step1',
    name: 'selectReadingTypes',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Pkj.view.bulk.ReadingTypesSelectionGrid'
    ],

    title: Uni.I18n.translate('certificates.bulk.step1.title', 'PKJ', 'Step 1: Select certificates'),

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
                        text: Uni.I18n.translate('certificates.noCertificatesErrorMsg', 'PKJ', 'It is required to select one or more certificates to go to the next step.')
                    }
                ]
            },
            {
                xtype: 'certificates-selection-grid',
                itemId: 'certificates-selection-grid'
            },
            {
                xtype: 'container',
                itemId: 'step-selection-error',
                margin: '-20 0 0 0',
                hidden: true,
                html: '<span style="color: #eb5642">' + Uni.I18n.translate('searchItems.bulk.selectatleast1device', 'PKJ', 'Select at least 1 certificate') + '</span>'
            }
        ];

        this.callParent(arguments);
    }
});