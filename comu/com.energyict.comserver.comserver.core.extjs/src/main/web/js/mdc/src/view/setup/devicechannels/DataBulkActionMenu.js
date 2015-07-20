Ext.define('Mdc.view.setup.devicechannels.DataBulkActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.channel-data-bulk-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'confirm-value',
            hidden: true,
            text: Uni.I18n.translate('general.confirmValue', 'MDC', 'Confirm value'),
            action: 'confirmValue'
        },
        {
            itemId: 'estimate-value',
            hidden: true,
            text: Uni.I18n.translate('general.estimateValue', 'MDC', 'Estimate value'),
            action: 'estimateValue'
        }
    ]
});
