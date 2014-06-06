Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.connection-method-action-menu',
    plain: true,
    border: false,
    itemId: 'connection-method-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editConnectionMethod',
            action: 'editConnectionMethod'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteConnectionMethod',
            action: 'deleteConnectionMethod'
        },
        {
            text: 'default',
            itemId: 'toggleDefaultMenuItem',
            action: 'toggleDefault'
        }
    ]
});

