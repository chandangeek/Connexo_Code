/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.transitionexecute.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'usagepointTransitionExecuteBrowse',

    requires: [
        'Imt.usagepointmanagement.view.transitionexecute.WizardNavigation',
        'Imt.usagepointmanagement.view.transitionexecute.Wizard'
    ],
    router: null,
    side: {
        itemId: 'usagepoint-transition-execute-navigation-panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'usagepointTransitionWizardNavigation',
                xtype: 'usagepointTransitionWizardNavigation'
            }
        ]
    },

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'usagepointTransitionExecuteWizard',
                itemId: 'usagepointTransitionExecuteWizard',
                router: me.router
            }
        ];
        me.callParent();
    }
});
