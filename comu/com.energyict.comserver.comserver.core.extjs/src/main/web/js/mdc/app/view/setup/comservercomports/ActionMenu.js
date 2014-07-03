Ext.define('Mdc.view.setup.comservercomports.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comServerComPortsActionMenu',
    itemId: 'comServerComPortsActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
//        {
//            itemId: 'edit',
//            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
//            action: 'edit'
//        },
        {
            itemId: 'activate',
            text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
            action: 'activate'
        },
        {
            itemId: 'deactivate',
            text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
            action: 'deactivate'
        },
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'remove'
        }
    ]
});
