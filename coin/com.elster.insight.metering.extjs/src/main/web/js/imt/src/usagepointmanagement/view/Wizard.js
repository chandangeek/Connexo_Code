Ext.define('Imt.usagepointmanagement.view.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-usage-point-wizard',

    requires: [
        'Imt.usagepointmanagement.view.forms.GeneralInfo'
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
                itemId: 'add-usage-point-step1',
                title: Uni.I18n.translate('usagepoint.wizard.step1title', 'IMT', 'Step 1: General information'),
                isWizardStep: true,
                navigationIndex: 1,
                ui: 'large',
                isPossibleAdd: me.isPossibleAdd,
                defaults: {
                    labelWidth: 260,
                    width: 595
                }
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

        switch (step.navigationIndex) {
            case 1:
                me.callParent(arguments);
                break;
            case 2:
                step.updateRecord();
                me.getRecord().set('techInfo', step.getRecord().getData());
                break;
            default:
                step.updateRecord();
                me.getRecord().customPropertySets().add(step.getRecord());
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
        if (step.xtype === 'cps-info-form') {
            if (!isValid) {
                step.markInvalid(errors);
            } else {
                step.clearInvalid();
            }
        } else {
            if (!isValid) {
                step.getForm().markInvalid(errors);
            } else {
                step.getForm().clearInvalid();
            }
        }
        Ext.resumeLayouts(true);
    }
});