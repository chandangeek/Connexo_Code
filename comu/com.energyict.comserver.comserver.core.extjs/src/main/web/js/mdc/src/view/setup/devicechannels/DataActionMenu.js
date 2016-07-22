Ext.define('Mdc.view.setup.devicechannels.DataActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLoadProfileChannelDataActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewHistory',
            text: Uni.I18n.translate('deviceloadprofiles.viewHistory', 'MDC', 'View history'),
            action: 'viewHistory',
            hidden: true
        },
        {
            itemId: 'edit-value',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editValue',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
        },
        {
            itemId: 'confirm-value',
            hidden: true,
            text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
            action: 'confirmValue'
        },
        {
            itemId: 'estimate-value',
            hidden: true,
            text: Uni.I18n.translate('general.estimate', 'MDC', 'Estimate'),
            action: 'estimateValue'
        },
        {
            itemId: 'remove-reading',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'removeReading',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
        }       
    ]
});
