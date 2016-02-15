Ext.define('Imt.usagepointmanagement.view.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-usage-point-wizard',

    requires: [
        'Imt.usagepointmanagement.view.forms.GeneralInfo'
    ],

    layout: 'card',

    router: null,
    returnLink: null,

    defaults: {
        ui: 'large'
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'general-info-form',
                itemId: 'add-usage-point-step1',
                title: Uni.I18n.translate('usagepoint.wizard.step1title', 'IMT', 'Step 1: General information'),
                navigationIndex: 1,
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
                    itemId: 'finishButton',
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
    }/*,

    updateRecord: function () {
        var me = this;
    }*/
});