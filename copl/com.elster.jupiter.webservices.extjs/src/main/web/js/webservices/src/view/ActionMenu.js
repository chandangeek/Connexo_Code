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
            action: 'edit',
            privileges: Wss.privileges.Webservices.admin
        },
        {
            itemId: 'activate-webservice',
            action: 'activate',
            privileges: Wss.privileges.Webservices.admin
        },
        {
            itemId: 'remove-webservice',
            text: Uni.I18n.translate('general.remove', 'WSS', 'Remove'),
            action: 'remove',
            privileges: Wss.privileges.Webservices.admin
        }
    ],
    listeners: {
        beforeshow: function () {
            var me = this;
            me.down('#activate-webservice').setText(me.record.get('active') ? Uni.I18n.translate('general.deactivate', 'WSS', 'Deactivate')
                : Uni.I18n.translate('general.activate', 'WSS', 'Activate'));
        }
    }
});