/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.communicationsbulk.Wizard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Dsh.view.communicationsbulk.Step1',
        'Dsh.view.communicationsbulk.Step2',
        'Dsh.view.communicationsbulk.Step3',
        'Dsh.view.communicationsbulk.Step4'
    ],
    alias: 'widget.communications-bulk-wizard',
    layout: 'card',
    defaults: {
        ui: 'large'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'communications-bulk-step1',
                itemId: 'cmbw-step1',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStep.title', 'DSH', 'Step {0}:'), 1)
                + ' ' + Uni.I18n.translate('communication.bulk.selectCommunications', 'DSH', 'Select communications'),
                router: me.router
            },
            {
                xtype: 'communications-bulk-step2',
                itemId: 'cmbw-step2',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStep.title', 'DSH', 'Step {0}:'), 2)
                + ' ' + Uni.I18n.translate('general.selectAction', 'DSH', 'Select action')
            },
            {
                xtype: 'communications-bulk-step3',
                itemId: 'cmbw-step3',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStep.title', 'DSH', 'Step {0}:'), 3)
                + ' ' + Uni.I18n.translate('general.confirmation', 'DSH', 'Confirmation')
            },
            {
                xtype: 'communications-bulk-step4',
                itemId: 'cmbw-step4',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStep.title', 'DSH', 'Step {0}:'), 4)
                + ' ' + Uni.I18n.translate('general.status', 'DSH', 'Status'),
                router: me.router
            }
        ];

        me.bbar = {
            itemId: 'cmbw-buttons',
            items: [
                {
                    text: Uni.I18n.translate('general.back', 'DSH', 'Back'),
                    action: 'step-back',
                    itemId: 'cmbw-step-back',
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'DSH', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    itemId: 'cmbw-step-next'
                },
                {
                    text: Uni.I18n.translate('general.confirm', 'DSH', 'Confirm'),
                    ui: 'action',
                    action: 'confirm-action',
                    itemId: 'cmbw-confirm-action',
                    hidden: true
                },
                {
                    text: Uni.I18n.translate('general.finish', 'DSH', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    itemId: 'cmbw-finish',
                    hidden: true,
                    href: me.router.getRoute('workspace/communications/details').buildUrl()
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'DSH', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    itemId: 'cmbw-finish-cancel',
                    href: me.router.getRoute('workspace/communications/details').buildUrl()
                }
            ]
        };

        me.callParent(arguments);
    }
});