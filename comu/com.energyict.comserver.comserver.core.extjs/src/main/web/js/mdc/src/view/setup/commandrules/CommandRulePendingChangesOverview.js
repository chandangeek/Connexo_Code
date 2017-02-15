/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.commandrules.CommandRulePendingChangesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRulesPendingChangesOverview',
    requires: [
        'Mdc.view.setup.commandrules.CommandRuleSideMenu',
        'Uni.store.PendingChanges',
        'Uni.view.widget.PendingChanges',
        'Mdc.privileges.CommandLimitationRules'
    ],

    router: undefined,
    commandRuleRecord: null,

    initComponent: function () {
        var me = this;
        me.content = {
            xtype: 'container',
            items: [
                {
                    xtype: 'pendingChangesPanel',
                    itemId: 'mdc-command-rule-pending-changes-pnl',
                    store: Ext.isEmpty(me.commandRuleRecord.getDualControl())
                        ? Ext.create('Uni.store.PendingChanges')
                        : me.commandRuleRecord.getDualControl().changes(),
                    approveRejectButtonsVisible: Mdc.privileges.CommandLimitationRules.canApproveReject(),
                    approveButtonDisabled: Ext.isEmpty(me.commandRuleRecord.getDualControl())
                        ? false
                        : me.commandRuleRecord.getDualControl().get('hasCurrentUserAccepted')
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'commandRuleSideMenu',
                        commandRuleName: me.commandRuleRecord.get('name'),
                        router: me.router,
                        itemId: 'mdc-command-rule-pending-changes-sidemenu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }

});
