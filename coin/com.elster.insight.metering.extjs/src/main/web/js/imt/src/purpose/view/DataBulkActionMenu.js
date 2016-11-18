Ext.define('Imt.purpose.view.DataBulkActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-channel-data-bulk-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'confirm-value',
            hidden: true,
            text: Uni.I18n.translate('general.confirmValue', 'IMT', 'Confirm value'),
            action: 'confirmValue'
        },
        {
            itemId: 'estimate-value',
            hidden: true,
            text: Uni.I18n.translate('general.estimateValue', 'IMT', 'Estimate value'),
            action: 'estimateValue'
        },
        {
            itemId: 'remove-value',
            hidden: true,
            text: Uni.I18n.translate('general.removeReadings', 'IMT', 'Remove readings'),
            action: 'removeValue'
        }
    ]
});
