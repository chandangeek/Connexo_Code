/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Wizard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Pkj.view.bulk.Step1',
        'Pkj.view.bulk.Step2',
        'Pkj.view.bulk.Step3',
        'Pkj.view.bulk.Step4',
        'Pkj.view.bulk.Step5'
    ],
    alias: 'widget.certificates-wizard',
    layout: 'card',
    defaults: {
        ui: 'large'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'certificates-bulk-step1',
                itemId: 'reading-types-bulk-step1'
            },
            {
                xtype: 'certificates-bulk-step2',
                itemId: 'reading-types-bulk-step2'
            },
            {
                xtype: 'certificates-bulk-step3',
                itemId: 'reading-types-bulk-step3'
            },
            {
                xtype: 'certificates-bulk-step4',
                itemId: 'reading-types-bulk-step4'
            },
            {
                xtype: 'certificates-bulk-step5',
                itemId: 'reading-types-bulk-step5'
            }
        ];

        me.bbar = {
            itemId: 'reading-types-bulk-buttons',
            items: [
                {
                    text: Uni.I18n.translate('general.back', 'PKJ', 'Back'),
                    itemId: 'reading-types-bulk-back',
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'PKJ', 'Next'),
                    ui: 'action',
                    itemId: 'reading-types-bulk-next'
                },
                {
                    text: Uni.I18n.translate('general.confirm', 'PKJ', 'Confirm'),
                    ui: 'action',
                    hidden: true,
                    itemId: 'reading-types-bulk-confirm'
                },
                {
                    text: Uni.I18n.translate('general.finish', 'PKJ', 'Finish'),
                    ui: 'action',
                    itemId: 'reading-types-bulk-finish',
                    hidden: true
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'PKJ', 'Cancel'),
                    ui: 'link',
                    itemId: 'reading-types-bulk-cancel'
                },
                {
                    text: Uni.I18n.translate('general.finish', 'PKJ', 'Finish'),
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