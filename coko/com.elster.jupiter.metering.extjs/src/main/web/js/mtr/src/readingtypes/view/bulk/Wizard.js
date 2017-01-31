/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.Wizard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mtr.readingtypes.view.bulk.Step1',
        'Mtr.readingtypes.view.bulk.Step2',
        'Mtr.readingtypes.view.bulk.Step3',
        'Mtr.readingtypes.view.bulk.Step4',
        'Mtr.readingtypes.view.bulk.Step5'
    ],
    alias: 'widget.reading-types-wizard',
    layout: 'card',
    defaults: {
        ui: 'large'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'reading-types-bulk-step1',
                itemId: 'reading-types-bulk-step1'
            },
            {
                xtype: 'reading-types-bulk-step2',
                itemId: 'reading-types-bulk-step2'
            },
            {
                xtype: 'reading-types-bulk-step3',
                itemId: 'reading-types-bulk-step3'
            },
            {
                xtype: 'reading-types-bulk-step4',
                itemId: 'reading-types-bulk-step4'
            },
            {
                xtype: 'reading-types-bulk-step5',
                itemId: 'reading-types-bulk-step5'
            }
        ];

        me.bbar = {
            itemId: 'reading-types-bulk-buttons',
            items: [
                {
                    text: Uni.I18n.translate('general.back', 'MTR', 'Back'),
                    itemId: 'reading-types-bulk-back',
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'MTR', 'Next'),
                    ui: 'action',
                    itemId: 'reading-types-bulk-next'
                },
                {
                    text: Uni.I18n.translate('general.confirm', 'MTR', 'Confirm'),
                    ui: 'action',
                    hidden: true,
                    itemId: 'reading-types-bulk-confirm'
                },
                {
                    text: Uni.I18n.translate('general.finish', 'MTR', 'Finish'),
                    ui: 'action',
                    itemId: 'reading-types-bulk-finish',
                    hidden: true
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'MTR', 'Cancel'),
                    ui: 'link',
                    itemId: 'reading-types-bulk-cancel'
                },
                {
                    text: Uni.I18n.translate('general.finish', 'MTR', 'Finish'),
                    ui: 'remove',
                    action: 'finish',
                    itemId: 'failure-reading-types-bulk-finish',
                    hidden: true
                }
            ]
        };

        me.callParent(arguments);
    }
});