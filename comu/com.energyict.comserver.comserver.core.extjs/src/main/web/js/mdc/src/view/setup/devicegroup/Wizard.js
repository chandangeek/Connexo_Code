/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.adddevicegroup-wizard',

    requires: [
        'Mdc.view.setup.devicegroup.Step1',
        'Mdc.view.setup.devicegroup.Step2',
        'Mdc.view.setup.devicegroup.Step3',
        'Mdc.view.setup.devicegroup.Step4'
    ],

    layout: 'card',

    router: null,
    returnLink: null,
    isEdit: false,
    service: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'device-group-wizard-step1',
                itemId: 'devicegroup-wizard-step1',
                title: me.isEdit
                    ? Uni.I18n.translate('devicegroup.wizard.step1.title.edit', 'MDC', 'Step 1: Set group name')
                    : Uni.I18n.translate('devicegroup.wizard.step1.title.add', 'MDC', 'Step 1: General attributes'),
                navigationIndex: 1,
                isEdit: me.isEdit
            },
            {
                xtype: 'device-group-wizard-step2',
                itemId: 'devicegroup-wizard-step2',
                title: Uni.I18n.translate('devicegroup.wizard.step2.title', 'MDC', 'Step 2: Select devices'),
                navigationIndex: 2,
                service: me.service
            },
            {
                xtype: 'device-group-wizard-step3',
                itemId: 'devicegroup-wizard-step3',
                title: Uni.I18n.translate('devicegroup.wizard.step3.title', 'MDC', 'Step 3: Confirmation'),
                navigationIndex: 3
            },
            {
                xtype: 'device-group-wizard-step4',
                itemId: 'devicegroup-wizard-step4',
                title: Uni.I18n.translate('devicegroup.wizard.step4.title', 'MDC', 'Step 4: Status'),
                navigationIndex: 4
            }
        ];

        me.bbar = {
            itemId: 'device-group-wizard-buttons',
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
