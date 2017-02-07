/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.Wizard', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.layout.container.Card',
        'Mdc.view.setup.searchitems.bulk.Step1',
        'Mdc.view.setup.searchitems.bulk.Step2',
        'Mdc.view.setup.searchitems.bulk.Step3',
        'Mdc.view.setup.searchitems.bulk.Step4',
        'Mdc.view.setup.searchitems.bulk.Step5',
        'Mdc.view.setup.searchitems.bulk.Step5ViewDevices',
        'Uni.view.notifications.NotificationPanel'
    ],
    alias: 'widget.searchitems-wizard',
    autoHeight: true,
    border: false,
    layout: 'card',
    activeItemId: 0,
    buttonAlign: 'left',

    initComponent: function () {
        this.items = [
            {
                xtype: 'searchitems-bulk-step1',
                itemId: 'searchitems-bulk-step1',
                deviceStore: this.deviceStore,
                navigationIndex: 0
            },
            {
                xtype: 'searchitems-bulk-step2',
                itemId: 'searchitems-bulk-step2',
                navigationIndex: 1
            },
            {
                xtype: 'searchitems-bulk-step3',
                itemId: 'searchitems-bulk-step3',
                navigationIndex: 2
            },
            {
                xtype: 'searchitems-bulk-step4',
                itemId: 'searchitems-bulk-step4',
                navigationIndex: 3
            },
            {
                xtype: 'searchitems-bulk-step5',
                itemId: 'searchitems-bulk-step5',
                navigationIndex: 4
            },
            {
                xtype: 'searchitems-bulk-step5-viewdevices',
                itemId: 'searchitems-bulk-step5-viewdevices',
                navigationIndex: 5
            }
        ];

        this.callParent(arguments);
    },

    bbar: {
        defaults: {
            xtype: 'button'
        },
        items: [
            {
                text: Uni.I18n.translate('general.back', 'MDC', 'Back'),
                action: 'back',
                itemId: 'backButton',
                disabled: true
            },
            {
                text: Uni.I18n.translate('general.next', 'MDC', 'Next'),
                ui: 'action',
                action: 'next',
                itemId: 'nextButton'
            },
            {
                text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                ui: 'action',
                action: 'confirm',
                itemId: 'confirmButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                ui: 'action',
                action: 'finish',
                itemId: 'finishButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                ui: 'remove',
                action: 'finish',
                itemId: 'failureFinishButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                ui: 'link',
                action: 'cancel',
                itemId: 'wizardCancelButton',
                href: ''
            }
        ]
    }
});