Ext.define('Mdc.usagepointmanagement.view.history.MetrologyConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrology-configuration-versions-action-menu',
    plain: true,
    border: false,
    shadow: false,
    router: null,

    initComponent: function() {
        var me = this;

        me.items = [
            {
                itemId: 'action-menu-item-mc-edit',
                privileges: Mdc.privileges.UsagePoint.canAdmin(),
                text: Uni.I18n.translate('usagepoint.actionMenu.edit', 'MDC', 'Edit'),
                tooltip: Uni.I18n.translate('usagepoint.actionMenu.editQtip', 'MDC', 'Last version only can be modified')
            },
            {
                itemId: 'action-menu-item-mc-remove',
                privileges: Mdc.privileges.UsagePoint.canAdmin(),
                text: Uni.I18n.translate('usagepoint.actionMenu.remove', 'MDC', 'Remove'),
                tooltip: Uni.I18n.translate('usagepoint.actionMenu.removeQtip', 'MDC', 'Last version only can be removed')
            }
        ];
        me.callParent(arguments);
    },

    setMenuItems: function(record){
        var me = this;
        if(record){
            me.down('#action-menu-item-mc-edit').setDisabled(!record.get('editable'));
            me.down('#action-menu-item-mc-remove').setDisabled(!record.get('editable') || record.get('current'));
        }
    }
});