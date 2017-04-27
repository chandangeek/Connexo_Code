/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-usage-point-wizard',

    requires: [
        'Imt.usagepointmanagement.view.forms.GeneralInfo',
        'Imt.usagepointmanagement.view.forms.CalendarInfo'
    ],

    layout: {
        type: 'card',
        deferredRender: true
    },

    router: null,
    returnLink: null,
    isPossibleAdd: true,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'general-info-form',
                itemId: 'step-general',
                title: Uni.I18n.translate('usagepoint.wizard.step1title', 'IMT', 'Step 1: General information'),
                isWizardStep: true,
                navigationIndex: 1,
                stepName: 'generalInfo',
                ui: 'large',
                isPossibleAdd: me.isPossibleAdd
            },
            {
                xtype: 'calendar-info-form',
                title: Uni.I18n.translate('usagepoint.wizard.calendarStepTitle', 'IMT', 'Step {0}: {1}', [4, Uni.I18n.translate('general.calendarTransition', 'IMT', 'Calendar transition')]),
                navigationIndex: 3,
                stepName: 'calendarTransitionInfo',
                ui: 'large',
                isWizardStep: true
            },
            {
                xtype: 'life-cycle-transition-info-form',
                itemId: 'step-life-cycle-transition',
                title: Uni.I18n.translate('usagepoint.wizard.cpsStepTitle', 'IMT', 'Step {0}: {1}', [4, Uni.I18n.translate('general.lifeCycleTransition', 'IMT', 'Life cycle transition')]),
                navigationIndex: 4,
                stepName: 'lifeCycleTransitionInfo',
                ui: 'large',
                isWizardStep: true
            }
        ];

        me.bbar = {
            itemId: 'usage-point-wizard-buttons',
            items: [
                {
                    itemId: 'backButton',
                    text: Uni.I18n.translate('general.back', 'IMT', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true,
                    hidden: !me.isPossibleAdd
                },
                {
                    itemId: 'nextButton',
                    text: Uni.I18n.translate('general.next', 'IMT', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true,
                    hidden: !me.isPossibleAdd
                },
                {
                    itemId: 'addButton',
                    text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                    ui: 'action',
                    action: 'add',
                    hidden: true
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

    updateRecord: function () {
        var me = this,
            step = me.getLayout().getActiveItem();
        switch (step.stepName) {
            case 'generalInfo':
                me.callParent(arguments);
                break;
            case 'techInfo':
                step.updateRecord();
                me.getRecord().set('techInfo', step.getRecord().getData());
                break;
            case 'casInfo':
                step.updateRecord();
                me.getRecord().customPropertySets().add(step.getRecord());
                break;
            case 'metrologyConfigurationWithMetersInfo':
                me.getRecord().set(step.getRecord());
                break;
            case 'lifeCycleTransitionInfo':
                me.getRecord().set('transitionToPerform', step.getRecord());
                break;
            case 'calendarTransitionInfo':
                me.getRecord().set('calendars', step.getRecord());
                break;

        }
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
        switch (step.stepName) {
            case 'casInfo':
            case 'metrologyConfigurationWithMetersInfo':
            case 'lifeCycleTransitionInfo':
                if (!isValid) {
                    step.markInvalid(errors);
                } else {
                    step.clearInvalid();
                }
                break;
            case 'generalInfo':
            case 'techInfo':
                if (!isValid) {
                    step.getForm().markInvalid(errors);
                } else {
                    step.getForm().clearInvalid();
                }
                break;
        }
        Ext.resumeLayouts(true);
    }
});