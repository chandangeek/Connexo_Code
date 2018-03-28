
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.changeusagepointlifecycle.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'usagepointChangeLifeCycleExecuteBrowse',
    alias: 'widget.change-usage-point-life-cycle-browse',

    requires: [
        'Imt.usagepointmanagement.view.changeusagepointlifecycle.WizardNavigation',
        'Imt.usagepointmanagement.view.changeusagepointlifecycle.Wizard'
    ],
    router: null,
    side: {
        itemId: 'usagepoint-changelifecycle-execute-navigation-panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'usagepointChangeLifeCycleWizardNavigation',
                xtype: 'usagepointChangeLifeCycleWizardNavigation'
            }
        ]
    },

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'change-usage-point-life-cycle-wizard',
                itemId: 'usagepointChangeLifeCycleExecuteWizard',
                router: me.router
            }
        ];
        me.callParent();
    }
});