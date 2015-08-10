Ext.define('Apr.view.appservers.MessageServicesActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.message-services-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'remove-message-service',
            text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
            action: 'removeMessageService'
        }
    ]
});