Ext.define('Mdc.view.setup.devicechannels.DataBulkActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.channel-data-bulk-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'bulk-estimate-value',
            text: Uni.I18n.translate('general.estimateValue', 'MDC', 'Estimate value'),
            action: 'bulkEstimateValue'
        }
    ]
});
