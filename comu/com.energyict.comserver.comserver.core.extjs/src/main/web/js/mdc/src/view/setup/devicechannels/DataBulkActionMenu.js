Ext.define('Mdc.view.setup.devicechannels.DataBulkActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.channel-data-bulk-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'confirm-value',
                hidden: true,
                text: Uni.I18n.translate('general.confirmValue', 'MDC', 'Confirm value'),
                action: 'confirmValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'estimate-value',
                hidden: true,
                text: Uni.I18n.translate('general.estimateValue', 'MDC', 'Estimate value'),
                action: 'estimateValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'remove-readings',
                hidden: true,
                text: Uni.I18n.translate('general.removeReadings', 'MDC', 'Remove readings'),
                action: 'removeReadings',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
