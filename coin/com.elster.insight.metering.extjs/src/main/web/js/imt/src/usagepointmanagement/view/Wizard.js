Ext.define('Imt.usagepointmanagement.view.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-usage-point-wizard',

    requires: [
        'Imt.usagepointmanagement.view.forms.GeneralInfo'
    ],

    layout: 'card',

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
            step = me.getLayout().getActiveItem(),
            customProperties,
            cps;

        switch (step.navigationIndex) {
            case 1:
                me.callParent(arguments);
                break;
            case 2:
                step.updateRecord();
                me.getRecord().set('techInfo', step.getRecord().getData());
                break;
            default:
                customProperties = me.getRecord().customProperties();
                step.updateRecord();
                cps = step.getRecord();
                if (customProperties.getById() === step.getRecord().getId()) {
                    customProperties.remove(cps)
                }
                me.getRecord().customProperties().add(cps);
        }
    },

    markInvalid: function (errors) {
        var me = this,
            step = me.getLayout().getActiveItem(),
            warning = step.down('uni-form-error-message');

        Ext.suspendLayouts();
        if (warning) {
            warning.show()
        }
        step.getForm().markInvalid(errors);
        Ext.resumeLayouts(true);
    },

    clearInvalid: function () {
        var me = this,
            step = me.getLayout().getActiveItem(),
            warning = step.down('uni-form-error-message');

        Ext.suspendLayouts();
        if (warning) {
            warning.hide()
        }
        step.getForm().clearInvalid();
        Ext.resumeLayouts(true);
    }
});