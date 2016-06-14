Ext.define('Wss.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.webservices-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-webservice',
            text: Uni.I18n.translate('general.edit', 'WSS', 'Edit'),
            action: 'edit'
        },
        {
            itemId: 'activate-webservice',
            text: Uni.I18n.translate('general.activate', 'WSS', 'Activate'),
            action: 'activate'
        },
        {
            itemId: 'remove-webservice',
            text: Uni.I18n.translate('general.remove', 'WSS', 'Remove'),
            action: 'remove'
        }
    ],
    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ? item.show() : item.hide();
                }
            })
        }
    }
});