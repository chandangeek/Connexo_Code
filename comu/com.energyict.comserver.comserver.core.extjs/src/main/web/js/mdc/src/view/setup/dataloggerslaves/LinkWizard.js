/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.dataloggerslave-link-wizard',

    requires: [
        'Mdc.view.setup.dataloggerslaves.LinkWizardStep1',
        'Mdc.view.setup.dataloggerslaves.LinkWizardStep2',
        'Mdc.view.setup.dataloggerslaves.LinkWizardStep3',
        'Mdc.view.setup.dataloggerslaves.LinkWizardStep4',
        'Mdc.view.setup.dataloggerslaves.LinkWizardStep5',
        'Mdc.view.setup.dataloggerslaves.LinkWizardStep6'
    ],

    layout: 'card',

    router: null,
    returnLink: null,
    service: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'dataloggerslave-link-wizard-step1',
                itemId: 'mdc-dataloggerslave-link-wizard-step1',
                title: Uni.I18n.translate('linkwizard.step1.title', 'MDC', 'Step 1: Select data logger slave'),
                navigationIndex: 1
            },
            {
                xtype: 'dataloggerslave-link-wizard-step2',
                itemId: 'mdc-dataloggerslave-link-wizard-step2',
                title: Uni.I18n.translate('linkwizard.step2.title', 'MDC', 'Step 2: Map channels'),
                navigationIndex: 2
            },
            {
                xtype: 'dataloggerslave-link-wizard-step3',
                itemId: 'mdc-dataloggerslave-link-wizard-step3',
                title: Uni.I18n.translate('linkwizard.step3.title', 'MDC', 'Step 3: Map registers'),
                navigationIndex: 3
            },
            {
                xtype: 'dataloggerslave-link-wizard-step4',
                itemId: 'mdc-dataloggerslave-link-wizard-step4',
                title: Uni.I18n.translate('linkwizard.step4.title', 'MDC', 'Step 4: Select linking date'),
                navigationIndex: 4
            },
            {
                xtype: 'dataloggerslave-link-wizard-step5',
                itemId: 'mdc-dataloggerslave-link-wizard-step5',
                title: Uni.I18n.translate('linkwizard.step5.title', 'MDC', 'Step 5: Confirmation'),
                navigationIndex: 5
            },
            {
                xtype: 'dataloggerslave-link-wizard-step6',
                itemId: 'mdc-dataloggerslave-link-wizard-step6',
                title: Uni.I18n.translate('linkwizard.step6.title', 'MDC', 'Step 6: Status'),
                navigationIndex: 6
            }
        ];

        me.bbar = {
            itemId: 'mdc-dataloggerslave-link-wizard-buttons',
            items: [
                {
                    itemId: 'backButton',
                    text: Uni.I18n.translate('general.back', 'MDC', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true
                },
                {
                    itemId: 'nextButton',
                    text: Uni.I18n.translate('general.next', 'MDC', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true
                },
                {
                    itemId: 'confirmButton',
                    text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                    ui: 'action',
                    action: 'confirm-action',
                    navigationBtn: true,
                    hidden: true
                },
                {
                    itemId: 'finishButton',
                    text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    hidden: true,
                    href: me.returnLink
                },
                {
                    itemId: 'wizardCancelButton',
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    href: me.returnLink
                }
            ]
        };

        me.callParent(arguments);
    },

    updateRecord: function (record) {
        var me = this,
            filter = me.parseFilter(),
            staticGrid,
            updatedRecord,
            devices;

        me.callParent(arguments);
        updatedRecord = record || me.getRecord();
        updatedRecord.beginEdit();
        updatedRecord.set('filter', filter);
        if (!updatedRecord.get('dynamic')) {
            staticGrid = me.down('static-group-devices-grid');
            if (staticGrid.isAllSelected()) {
                updatedRecord.set('devices', !me.service.getFilters().length ? [] : null);
            } else if (staticGrid.devices) {
                updatedRecord.set('devices', staticGrid.devices);
            } else {
                devices = [];
                Ext.Array.each(staticGrid.getSelectionModel().getSelection(), function (device) {
                    devices.push(device.get('id'));
                });
                updatedRecord.set('devices', devices);
            }
        }
        updatedRecord.endEdit();
    },

    parseFilter: function () {
        var me = this,
            store = me.service.getSearchResultsStore();

        return store.getProxy().encodeFilters(me.service.getFilters());
    }
});
