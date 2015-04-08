Ext.define('Dsh.view.connectionsbulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.connections-bulk-navigation',
    jumpForward: false,
    jumpBack: true,
    items: [
        {
            itemId: 'cnbn-select-connections',
            action: 'select-connections',
            text: Uni.I18n.translate('connection.bulk.selectConnections', 'DSH', 'Select connections')
        },
        {
            itemId: 'cnbn-select-action',
            action: 'select-action',
            text: Uni.I18n.translate('general.selectAction', 'DSH', 'Select action')
        },
        {
            itemId: 'cnbn-confirmation',
            action: 'confirmation',
            text: Uni.I18n.translate('general.confirmation', 'DSH', 'Confirmation')
        },
        {
            itemId: 'cnbn-status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'DSH', 'Status')
        }
    ]
});