Ext.define('Imt.usagepointmanagement.view.transitionexecute.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagepointTransitionExecuteWizard',

    requires: [
        'Ext.layout.container.Card',
        'Imt.usagepointmanagement.view.transitionexecute.Step1',
        'Imt.usagepointmanagement.view.transitionexecute.Step2'
    ],

    layout: 'card',
    router: null,
    items: [
        {
            xtype: 'usagepointtransitionexecute-wizard-step1',
            itemId: 'usagepointtransitionexecute-wizard-step1',
            navigationIndex: 0
        },
        {
            xtype: 'usagepointtransitionexecute-wizard-step2',
            itemId: 'usagepointtransitionexecute-wizard-step2',
            navigationIndex: 1
        }
    ],

    initComponent: function () {
        var me = this;
        me.bbar = {
            itemId: 'wizard-buttons',
            items: [
                {
                    itemId: 'backButton',
                    text: Uni.I18n.translate('general.back', 'IMT', 'Back'),
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'IMT', 'Next'),
                    ui: 'action',
                    action: 'next',
                    itemId: 'nextButton'
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    itemId: 'wizardCancelButton',
                    href: me.router.getRoute('usagepoints/view').buildUrl()
                }
            ]
        };
        me.callParent();
    }
});
