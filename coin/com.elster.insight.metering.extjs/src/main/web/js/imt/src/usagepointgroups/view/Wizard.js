/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.addusagepointgroup-wizard',
    xtype: 'addusagepointgroup-wizard',

    requires: [
        'Imt.usagepointgroups.view.Step1',
        'Imt.usagepointgroups.view.Step2',
        'Imt.usagepointgroups.view.Step3',
        'Imt.usagepointgroups.view.Step4'
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
                xtype: 'usagepoint-group-wizard-step1',
                itemId: 'usagepoint-group-wizard-step1',
                title: me.isEdit
                    ? Uni.I18n.translate('usagepointgroup.wizard.step1.title.edit', 'IMT', 'Step 1: Set group name')
                    : Uni.I18n.translate('usagepointgroup.wizard.step1.title.add', 'IMT', 'Step 1: General attributes'),
                navigationIndex: 1,
                isEdit: me.isEdit
            },
            {
                xtype: 'usagepoint-group-wizard-step2',
                itemId: 'usagepoint-group-wizard-step2',
                title: Uni.I18n.translate('usagepointgroup.wizard.step2.title', 'IMT', 'Step 2: Select usage points'),
                navigationIndex: 2,
                service: me.service
            },
            {
                xtype: 'usagepoint-group-wizard-step3',
                itemId: 'usagepoint-group-wizard-step3',
                title: Uni.I18n.translate('usagepointgroup.wizard.step3.title', 'IMT', 'Step 3: Confirmation'),
                navigationIndex: 3
            },
            {
                xtype: 'usagepoint-group-wizard-step4',
                itemId: 'usagepoint-group-wizard-step4',
                title: Uni.I18n.translate('usagepointgroup.wizard.step4.title', 'IMT', 'Step 4: Status'),
                navigationIndex: 4
            }
        ];

        me.bbar = {
            itemId: 'usagepoint-group-wizard-buttons',
            items: [
                {
                    itemId: 'backButton',
                    text: Uni.I18n.translate('general.back', 'IMT', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true
                },
                {
                    itemId: 'nextButton',
                    text: Uni.I18n.translate('general.next', 'IMT', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true
                },
                {

                    itemId: 'confirmButton',
                    text: Uni.I18n.translate('general.confirm', 'IMT', 'Confirm'),
                    ui: 'action',
                    action: 'confirm-action',
                    navigationBtn: true,
                    hidden: true
                },
                {
                    itemId: 'finishButton',
                    text: Uni.I18n.translate('general.finish', 'IMT', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    hidden: true,
                    href: me.returnLink
                },
                {
                    itemId: 'wizardCancelButton',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
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
            usagePoints;

        me.callParent(arguments);
        updatedRecord = record || me.getRecord();
        updatedRecord.beginEdit();
        updatedRecord.set('filter', filter);
        if (!updatedRecord.get('dynamic')) {
            staticGrid = me.down('static-group-usagepoints-grid');
            if (staticGrid.isAllSelected()) {
                updatedRecord.set('usagePoints', !me.service.getFilters().length ? [] : null);
            } else if (staticGrid.usagePoints) {
                updatedRecord.set('usagePoints', staticGrid.usagePoints);
            } else {
                usagePoints = [];
                Ext.Array.each(staticGrid.getSelectionModel().getSelection(), function (up) {
                    usagePoints.push(up.get('id'));
                });
                updatedRecord.set('usagePoints', usagePoints);
            }
        }
        updatedRecord.endEdit();
    },

    parseFilter: function () {
        var me = this,
            store = me.service.getSearchResultsStore();

        return store.getProxy().encodeFilters(me.service.getFilters());
    },

    markInvalid: function (errors) {
        this.toggleValidation(errors);
    },

    clearInvalid: function () {
        this.toggleValidation();
    },

    toggleValidation: function (errors) {
        var me = this,
            isValid = !errors,
            step = me.getLayout().getActiveItem(),
            warning = step.down('uni-form-error-message');

        Ext.suspendLayouts();
        if (warning) {
            warning.setVisible(!isValid);
        }
        if (!isValid) {
            step.getForm().markInvalid(errors);
        } else {
            step.getForm().clearInvalid();
        }
        Ext.resumeLayouts(true);
    }
});
