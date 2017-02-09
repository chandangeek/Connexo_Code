/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.commandrules.CommandRuleOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRuleOverview',
    requires: [
        'Mdc.view.setup.commandrules.CommandRuleSideMenu',
        'Mdc.view.setup.commandrules.CommandRuleActionMenu',
        'Mdc.view.setup.commandrules.CommandRulePreviewForm'
    ],
    router: null,
    commandRuleRecord: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'mdc-commandRule-overview-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'commandRulePreviewForm',
                        itemId: 'mdc-commandRule-overview-preview'
                    }
                },
                {
                    xtype: 'uni-button-action',
                    //privileges: Mdc.privileges.Communication.admin,
                    menu: {
                        xtype: 'commandRuleActionMenu',
                        itemId: 'mdc-commandRule-overview-menu',
                        record: me.commandRuleRecord
                    }
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
                        itemId: 'mdc-command-rule-overview-sidemenu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);

        me.on('afterrender', function() {
            me.down('commandRulePreviewForm').loadRecord(me.commandRuleRecord);
        }, me, {single:true});
    }
});
