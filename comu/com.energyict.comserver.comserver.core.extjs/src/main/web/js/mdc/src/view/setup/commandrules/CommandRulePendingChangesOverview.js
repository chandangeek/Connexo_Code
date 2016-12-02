Ext.define('Mdc.view.setup.commandrules.CommandRulePendingChangesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRulesPendingChangesOverview',

    router: undefined,

    initComponent: function () {
        var me = this;
        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.pendingChanges', 'MDC', 'Pending changes'),
            items: []
        };
        this.callParent(arguments);
    }

});
