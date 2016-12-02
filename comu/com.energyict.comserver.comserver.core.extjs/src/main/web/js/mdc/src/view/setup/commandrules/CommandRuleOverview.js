Ext.define('Mdc.view.setup.commandrules.CommandRuleOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRuleOverview',
    requires: [
        'Mdc.view.setup.commandrules.CommandRuleSideMenu',
        'Mdc.view.setup.commandrules.CommandRuleActionMenu',
        'Mdc.view.setup.commandrules.CommandRulePreview'
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
                    title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    flex: 1,
                    items: {
                        xtype: 'commandRulePreview',
                        itemId: 'mdc-command-rule-overview-form',
                        margin: '0 0 0 100'
                    }
                },
                {
                    xtype: 'uni-button-action',
                    //privileges: Mdc.privileges.Communication.admin,
                    //margin: '20 0 0 0',
                    menu: {
                        xtype: 'commandRuleActionMenu',
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
            me.down('#mdc-command-rule-overview-form').loadRecord(me.commandRuleRecord);
        }, me, {single:true});
    }
});
