/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.bulk.Wizard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Bpm.view.task.bulk.Step1',
        'Bpm.view.task.bulk.Step2',
        'Bpm.view.task.bulk.Step3',
        'Bpm.view.task.bulk.Step4',
        'Bpm.view.task.bulk.Step5',
        'Uni.view.container.ContentContainer'
    ],
    alias: 'widget.tasks-bulk-wizard',
    layout: 'card',
    defaults: {
        ui: 'large'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'tasks-bulk-step1',
                itemId: 'tskbw-step1',
                title: Ext.String.format(Uni.I18n.translate('general.bulkTaskTitle', 'BPM', 'Step {0}:'), 1)
                + ' ' + Uni.I18n.translate('bpm.task.bulk.selectTasks', 'BPM', 'Select tasks'),
                router: me.router
            },
            {
                xtype: 'tasks-bulk-step2',
                itemId: 'tskbw-step2',
                title: Ext.String.format(Uni.I18n.translate('general.bulkTaskTitle', 'BPM', 'Step {0}:'), 2)
                + ' ' + Uni.I18n.translate('general.selectAction', 'BPM', 'Select action')
            },
            {
                xtype: 'tasks-bulk-step3',
                itemId: 'tskbw-step3',
                title: Ext.String.format(Uni.I18n.translate('general.bulkTaskTitle', 'BPM', 'Step {0}:'), 3)
                + ' ' + Uni.I18n.translate('general.actionDetails', 'BPM', 'Action details')
            },
            {
                xtype: 'tasks-bulk-step4',
                itemId: 'tskbw-step4',
                title: Ext.String.format(Uni.I18n.translate('general.bulkTaskTitle', 'BPM', 'Step {0}:'), 4)
                + ' ' + Uni.I18n.translate('general.confirmation', 'BPM', 'Confirmation')
            },
            {
                xtype: 'tasks-bulk-step5',
                itemId: 'tskbw-step5',
                title: Ext.String.format(Uni.I18n.translate('general.bulkTaskTitle', 'BPM', 'Step {0}:'), 5)
                + ' ' + Uni.I18n.translate('general.status', 'BPM', 'Status'),
                router: me.router
            }
        ];

        me.bbar = {
            itemId: 'tskbw-buttons',
            items: [
                {
                    text: Uni.I18n.translate('general.back', 'BPM', 'Back'),
                    action: 'step-back',
                    itemId: 'tskbw-step-back',
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'BPM', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    itemId: 'tskbw-step-next'
                },
                {
                    text: Uni.I18n.translate('general.confirm', 'BPM', 'Confirm'),
                    ui: 'action',
                    action: 'confirm-action',
                    itemId: 'tskbw-confirm-action',
                    hidden: true
                },
                {
                    text: Uni.I18n.translate('general.finish', 'BPM', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    itemId: 'tskbw-finish',
                    hidden: true
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'BPM', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    itemId: 'tskbw-finish-cancel'
                }
            ]
        };

        me.callParent(arguments);
    }
});