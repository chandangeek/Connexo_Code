Ext.define('Mdc.view.setup.logbooktype.LogbookTypeActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.logbookTypeActionMenu',
    itemId: 'logbookTypeActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'logbookTypeActionEdit',
            action: 'editLogbookType'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'logbookTypeActionRemove',
            action: 'removeLogbookType'
        }
    ]
});
