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

    listeners: {
        show: {
            fn: function (menu) {
                var me = menu,
                    record = menu.record,
                    edititem = me.down('#action-menu-item-mc-edit'),
                    removeItem = me.down('#action-menu-item-mc-remove');
                if (record.get('editable')) {
                    removeItem.enable();
                    removeItem.itemEl.dom.removeAttribute(removeItem.getTipAttr());
                    edititem.enable();
                    edititem.itemEl.dom.removeAttribute(edititem.getTipAttr());
                } else {
                    if (record.get('current')) {
                        edititem.enable();
                        edititem.itemEl.dom.removeAttribute(edititem.getTipAttr());
                    } else {
                        edititem.disable();
                        edititem.setTooltip(Uni.I18n.translate('usagepoint.actionMenu.editQtip', 'MDC', 'Version in the past can\'t be modified'));
                    }
                    removeItem.disable();
                    removeItem.setTooltip(Uni.I18n.translate('usagepoint.actionMenu.removeQtip', 'MDC', 'Future version only can be removed'));
                }
            }
        }
    },

    setMenuItems: function (record) {
        this.record = record;
    }
});