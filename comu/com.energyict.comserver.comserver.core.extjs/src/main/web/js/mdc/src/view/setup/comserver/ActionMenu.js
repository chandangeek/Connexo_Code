Ext.define('Mdc.view.setup.comserver.ActionMenu', {
    extend: 'Ext.menu.Menu',
    requires:[
        'Mdc.privileges.Monitor'
    ],
    alias: 'widget.comserver-actionmenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'edit',
            text:  Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'edit'
        },
        {
            itemId: 'activate',
            text: Uni.I18n.translate('comserver.activate', 'MDC', 'Activate'),
            action: 'activate'
        },
        {
            itemId: 'deactivate',
            text: Uni.I18n.translate('comserver.deactivate', 'MDC', 'Deactivate'),
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
            action: 'remove'
        }
    ]
});
