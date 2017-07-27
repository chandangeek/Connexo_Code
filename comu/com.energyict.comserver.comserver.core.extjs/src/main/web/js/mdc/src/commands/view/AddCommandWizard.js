/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.AddCommandWizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-command-wizard',

    requires: [
        'Mdc.commands.view.AddCommandStep1',
        'Mdc.commands.view.AddCommandStep2',
        'Mdc.commands.view.AddCommandStep3',
        'Mdc.commands.view.AddCommandStep4'
    ],

    layout: 'card',

    router: null,
    returnLink: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'add-command-step1',
                itemId: 'mdc-add-command-step1',
                title:Uni.I18n.translate('add.command.step1.title', 'MDC', 'Step 1: Select device group'),
                navigationIndex: 1
            },
            {
                xtype: 'add-command-step2',
                itemId: 'mdc-add-command-step2',
                title:Uni.I18n.translate('add.command.step2.title', 'MDC', 'Step 2: Select command'),
                navigationIndex: 2
            },
            {
                xtype: 'add-command-step3',
                itemId: 'mdc-add-command-step3',
                title:Uni.I18n.translate('add.command.step3.title', 'MDC', 'Step 3: Confirmation'),
                navigationIndex: 3
            },
            {
                xtype: 'add-command-step4',
                itemId: 'mdc-add-command-step4',
                title:Uni.I18n.translate('add.command.step4.title', 'MDC', 'Step 4: Status'),
                navigationIndex: 4
            }
        ];

        me.bbar = {
            itemId: 'mdc-add-command-wizard-buttons',
            items: [
                {
                    itemId: 'mdc-add-command-wizard-backButton',
                    text: Uni.I18n.translate('general.back', 'MDC', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true
                },
                {
                    itemId: 'mdc-add-command-wizard-nextButton',
                    text: Uni.I18n.translate('general.next', 'MDC', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true
                },
                {

                    itemId: 'mdc-add-command-wizard-confirmButton',
                    text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                    ui: 'action',
                    action: 'confirm-action',
                    navigationBtn: true,
                    hidden: true
                },
                {
                    itemId: 'mdc-add-command-wizard-finishButton',
                    text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    hidden: true,
                    href: me.returnLink
                },
                {
                    itemId: 'mdc-add-command-wizard-cancelButton',
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    href: me.returnLink
                }
            ]
        };

        me.callParent(arguments);
    }

    //,
    //
    //updateRecord: function (record) {
    //    var me = this,
    //        filter = me.parseFilter(),
    //        staticGrid,
    //        updatedRecord,
    //        devices;
    //
    //    me.callParent(arguments);
    //    updatedRecord = record || me.getRecord();
    //    updatedRecord.beginEdit();
    //    updatedRecord.set('filter', filter);
    //    if (!updatedRecord.get('dynamic')) {
    //        staticGrid = me.down('static-group-devices-grid');
    //        if (staticGrid.isAllSelected()) {
    //            updatedRecord.set('devices', !me.service.getFilters().length ? [] : null);
    //        } else if (staticGrid.devices) {
    //            updatedRecord.set('devices', staticGrid.devices);
    //        } else {
    //            devices = [];
    //            Ext.Array.each(staticGrid.getSelectionModel().getSelection(), function (device) {
    //                devices.push(device.get('id'));
    //            });
    //            updatedRecord.set('devices', devices);
    //        }
    //    }
    //    updatedRecord.endEdit();
    //},
    //
    //parseFilter: function () {
    //    var me = this,
    //        store = me.service.getSearchResultsStore();
    //
    //    return store.getProxy().encodeFilters(me.service.getFilters());
    //}
});
