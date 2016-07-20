Ext.define('Mdc.usagepointmanagement.view.history.MetrologyConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrology-configuration-versions-action-menu',
    plain: true,
    border: false,
    shadow: false,
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'action-menu-item-mc-edit',
                privileges: Mdc.privileges.UsagePoint.canAdmin(),
                text: Uni.I18n.translate('usagepoint.actionMenu.edit', 'MDC', 'Edit'),
                action: 'edit'
            },
            {
                itemId: 'action-menu-item-mc-remove',
                privileges: Mdc.privileges.UsagePoint.canAdmin(),
                text: Uni.I18n.translate('usagepoint.actionMenu.remove', 'MDC', 'Remove'),
                action: 'remove'
            }
        ];
        me.callParent(arguments);
    },

    setMenuItems: function (record) {
        var me = this;
        if (record) {
            var edititem = me.down('#action-menu-item-mc-edit'),
                removeItem = me.down('#action-menu-item-mc-remove');
            if (!record.get('editable')) {
                edititem.disable();
                edititem.setTooltip(Uni.I18n.translate('usagepoint.actionMenu.editQtip', 'MDC', 'Future version only can be modified'));
                if (record.get('current')) {
                    removeItem.enable();
                    removeItem.setTooltip(false);
                } else {
                    removeItem.disable();
                    removeItem.setTooltip(Uni.I18n.translate('usagepoint.actionMenu.removeQtip', 'MDC', 'Future version only can be removed'));
                }
            } else {
                edititem.enable();
                edititem.setTooltip(false);
                removeItem.enable();
                removeItem.setTooltip(false);
            }
        }
    }
});