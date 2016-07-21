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
            if (record.get('editable')) {
                removeItem.enable();
                removeItem.clearTip();
                edititem.enable();
                edititem.clearTip();
            } else {
                if (record.get('current')) {
                    edititem.enable();
                    edititem.clearTip();
                } else {
                    edititem.disable();
                    edititem.setTooltip(Uni.I18n.translate('usagepoint.actionMenu.editQtip', 'MDC', 'Future version only can be modified'));
                }
                removeItem.disable();
                removeItem.setTooltip(Uni.I18n.translate('usagepoint.actionMenu.removeQtip', 'MDC', 'Future version only can be removed'));
            }
        }
    }
});