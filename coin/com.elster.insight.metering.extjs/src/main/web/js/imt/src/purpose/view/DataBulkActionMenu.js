Ext.define('Imt.purpose.view.DataBulkActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-readings-data-action-menu',
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
            itemId: 'edit-value',
            text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
            action: 'editValue',
            // dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
        },
        {
            itemId: 'estimate-value',
            hidden: true,
            text: Uni.I18n.translate('general.estimateValue', 'IMT', 'Estimate value'),
            action: 'estimateValue'
        },
        {
            itemId: 'reset-value',
            hidden: true,
            text: Uni.I18n.translate('general.resetReadings', 'IMT', 'Reset readings'),
            action: 'resetValue'
        }
    ]
});
