Ext.define('Imt.purpose.view.MultipleReadingsActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-bulk-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'confirm-value',
            hidden: true,
            text: Uni.I18n.translate('general.confirmValue', 'IMT', 'Confirm'),
            action: 'confirmValue'
        },
        {
            itemId: 'estimate-value',
            hidden: true,
            text: Uni.I18n.translate('general.estimateValue', 'IMT', 'Estimate'),
            action: 'estimateValue'
        },
        {
            itemId: 'reset-value',
            hidden: true,
            text: Uni.I18n.translate('general.resetReadings', 'IMT', 'Reset'),
            action: 'resetValue'
        }
    ]
});
