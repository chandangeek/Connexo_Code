Ext.define('Imt.purpose.view.SingleReadingActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-readings-data-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'confirm-value',
            hidden: true,
            text: Uni.I18n.translate('general.confirm', 'IMT', 'Confirm'),
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
            text: Uni.I18n.translate('general.estimate', 'IMT', 'Estimate'),
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
