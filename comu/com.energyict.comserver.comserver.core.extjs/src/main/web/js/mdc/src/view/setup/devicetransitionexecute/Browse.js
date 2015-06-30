Ext.define('Mdc.view.setup.devicetransitionexecute.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'deviceTransitionExecuteBrowse',
    itemId: 'deviceTransitionExecuteBrowse',

    requires: [
        'Mdc.view.setup.devicetransitionexecute.WizardNavigation',
        'Mdc.view.setup.devicetransitionexecute.Wizard'
    ],

    side: {
        itemId: 'devicetransitionexecutenavigationpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'deviceTransitionWizardNavigation',
                xtype: 'deviceTransitionWizardNavigation'
            }
        ]
    },

    content: [
        {
            xtype: 'deviceTransitionExecuteWizard',
            itemId: 'deviceTransitionExecuteWizard'
        }
    ]
});
