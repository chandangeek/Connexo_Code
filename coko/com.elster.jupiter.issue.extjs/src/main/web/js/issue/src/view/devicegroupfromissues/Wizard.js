/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.devicegroupfromissues.Wizard', {

    extend: 'Ext.form.Panel',

    alias: 'widget.add-group-wizard',

    requires: [
        'Isu.view.devicegroupfromissues.step.GeneralAttributes',
        'Isu.view.devicegroupfromissues.step.SelectIssues',
        'Isu.view.devicegroupfromissues.step.SelectDevices',
        'Isu.view.devicegroupfromissues.step.Confirmation',
        'Isu.view.devicegroupfromissues.step.Status'
    ],

    layout: 'card',

    router: null,

    returnLink: null,

    deviceDomainSearchService: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'general-attributes-step',
                itemId: 'general-attributes-step',
                title: Uni.I18n.translate('devicegroupfromissues.wizard.step.generalAttributes.title', 'ISU', 'Step 1: General attributes'),
                navigationIndex: 1
            },
            {
                xtype: 'select-issues-step',
                itemId: 'select-issues-step',
                title: Uni.I18n.translate('devicegroupfromissues.wizard.step.selectIssues.title', 'ISU', 'Step 2: Select issues'),
                navigationIndex: 2,
            },
            {
                xtype: 'select-devices-step',
                itemId: 'select-devices-step',
                title: Uni.I18n.translate('devicegroupfromissues.wizard.step.selectDevices.title', 'ISU', 'Step 3: Select devices'),
                navigationIndex: 3,
                service: me.deviceDomainSearchService
            },
            {
                xtype: 'confirmation-step',
                itemId: 'confirmation-step',
                title: Uni.I18n.translate('devicegroupfromissues.wizard.step.confirmation.title', 'ISU', 'Step 4: Confirmation'),
                navigationIndex: 4
            },
            {
                xtype: 'status-step',
                itemId: 'status-step',
                title: Uni.I18n.translate('devicegroupfromissues.wizard.step.status.title', 'ISU', 'Step 5: Status'),
                navigationIndex: 5
            }
        ];

        me.bbar = {
            itemId: 'add-group-wizard-buttons',
            items: [
                {
                    itemId: 'step-back-button',
                    text: Uni.I18n.translate('general.back', 'ISU', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true
                },
                {
                    itemId: 'step-next-button',
                    text: Uni.I18n.translate('general.next', 'ISU', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true
                },
                {
                    itemId: 'confirm-button',
                    text: Uni.I18n.translate('general.confirm', 'ISU', 'Confirm'),
                    ui: 'action',
                    action: 'confirm',
                    navigationBtn: true,
                    hidden: true
                },
                {
                    itemId: 'finish-button',
                    text: Uni.I18n.translate('general.finish', 'ISU', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    href: me.returnLink,
                    hidden: true
                },
                {
                    itemId: 'cancel-button',
                    text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    href: me.returnLink
                }
            ]
        };

        me.callParent(arguments);
    },

    updateRecordWithGroupName: function () {
        var me = this,
            groupName = me.down('#group-name').getValue(),
            updatedRecord = me.getRecord();

        updatedRecord.beginEdit();
        updatedRecord.set('name', groupName);
        updatedRecord.endEdit();
    },

    updateRecordWithItems: function (grid) {
        var me = this,
            selectedDevices = grid.getSelectedItems(),
            selectedDevicesIds = [],
            updatedRecord = me.getRecord();

        Ext.each(selectedDevices, function (device) {
            selectedDevicesIds.push(device.get('id'));
        });

        updatedRecord.beginEdit();
        updatedRecord.set('devices', selectedDevicesIds);
        updatedRecord.endEdit();
    },

    parseFilter: function (grid) {
        var store = grid.getService().getSearchResultsStore();
        return store.getProxy().encodeFilters(grid.getService().getFilters());
    }

});
