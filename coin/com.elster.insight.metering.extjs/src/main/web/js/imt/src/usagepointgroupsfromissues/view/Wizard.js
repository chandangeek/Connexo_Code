/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.view.Wizard', {

    extend: 'Ext.form.Panel',

    alias: 'widget.add-group-wizard',

    requires: [
        'Imt.usagepointgroupsfromissues.view.step.GeneralAttributes',
        'Imt.usagepointgroupsfromissues.view.step.SelectIssues',
        'Imt.usagepointgroupsfromissues.view.step.SelectUsagePoints',
        'Imt.usagepointgroupsfromissues.view.step.Confirmation',
        'Imt.usagepointgroupsfromissues.view.step.Status'
    ],

    layout: 'card',

    router: null,

    returnLink: null,

    usagePointDomainSearchService: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'general-attributes-step',
                itemId: 'general-attributes-step',
                title: Uni.I18n.translate('usagepointgroupfromissues.wizard.step.generalAttributes.title', 'IMT', 'Step 1: General attributes'),
                navigationIndex: 1
            },
            {
                xtype: 'select-issues-step',
                itemId: 'select-issues-step',
                title: Uni.I18n.translate('usagepointgroupfromissues.wizard.step.selectIssues.title', 'IMT', 'Step 2: Select issues'),
                navigationIndex: 2,
            },
            {
                xtype: 'select-usage-points-step',
                itemId: 'select-usage-points-step',
                title: Uni.I18n.translate('usagepointgroupfromissues.wizard.step.selectUsagePoints.title', 'IMT', 'Step 3: Select usage points'),
                navigationIndex: 3,
                service: me.usagePointDomainSearchService
            },
            {
                xtype: 'confirmation-step',
                itemId: 'confirmation-step',
                title: Uni.I18n.translate('usagepointgroupfromissues.wizard.step.confirmation.title', 'IMT', 'Step 4: Confirmation'),
                navigationIndex: 4
            },
            {
                xtype: 'status-step',
                itemId: 'status-step',
                title: Uni.I18n.translate('usagepointgroupfromissues.wizard.step.status.title', 'IMT', 'Step 5: Status'),
                navigationIndex: 5
            }
        ];

        me.bbar = {
            itemId: 'add-group-wizard-buttons',
            items: [
                {
                    itemId: 'step-back-button',
                    text: Uni.I18n.translate('general.back', 'IMT', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true
                },
                {
                    itemId: 'step-next-button',
                    text: Uni.I18n.translate('general.next', 'IMT', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true
                },
                {
                    itemId: 'confirm-button',
                    text: Uni.I18n.translate('general.confirm', 'IMT', 'Confirm'),
                    ui: 'action',
                    action: 'confirm',
                    navigationBtn: true,
                    hidden: true
                },
                {
                    itemId: 'finish-button',
                    text: Uni.I18n.translate('general.finish', 'IMT', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    href: me.returnLink,
                    hidden: true
                },
                {
                    itemId: 'cancel-button',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
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
            selectedUsagePoints = grid.getSelectedItems(),
            selectedUsagePointsIds = [],
            updatedRecord = me.getRecord();

        Ext.each(selectedUsagePoints, function (usagePoint) {
            selectedUsagePointsIds.push(usagePoint.get('id'));
        });

        updatedRecord.beginEdit();
        updatedRecord.set('usagePoints', selectedUsagePointsIds);
        updatedRecord.endEdit();
    },

    parseFilter: function (grid) {
        var store = grid.getService().getSearchResultsStore();
        return store.getProxy().encodeFilters(grid.getService().getFilters());
    }

});
