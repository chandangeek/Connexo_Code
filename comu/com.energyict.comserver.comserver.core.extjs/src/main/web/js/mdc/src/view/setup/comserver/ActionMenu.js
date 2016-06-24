Ext.define('Mdc.view.setup.comserver.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comserver-actionmenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'edit',
            text:  Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            privileges: Mdc.privileges.Communication.admin,
            action: 'edit'
        },
        {
            itemId: 'activate',
            text: Uni.I18n.translate('comserver.activate', 'MDC', 'Activate'),
            privileges: Mdc.privileges.Communication.admin,
            action: 'activate'
        },
        {
            itemId: 'deactivate',
            text: Uni.I18n.translate('comserver.deactivate', 'MDC', 'Deactivate'),
            privileges: Mdc.privileges.Communication.admin,
            action: 'deactivate'
        },
        {
            itemId: 'monitor',
            text: Uni.I18n.translate('comserver.monitor', 'MDC', 'Monitor'),
            privileges: Mdc.privileges.Monitor.monitor,
            action: 'monitor'
        },
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            privileges: Mdc.privileges.Communication.admin,
            action: 'remove'
        }
    ]
});
