Ext.define('Mdc.view.setup.datacollectionkpis.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dataCollectionKpisActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'edit'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'remove'
        }
    ]
});
