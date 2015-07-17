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
            text: Uni.I18n.translate('devicechannels.editReadings.editValue', 'MDC', 'Edit value'),
            action: 'editValue',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
        },
        {
            itemId: 'estimate-value',
            hidden: true,
            text: Uni.I18n.translate('general.estimateValue', 'MDC', 'Estimate value'),
            action: 'estimateValue'
        },
        {
            itemId: 'remove-reading',
            text: Uni.I18n.translate('devicechannels.editReadings.removeReading', 'MDC', 'Remove reading'),
            action: 'removeReading',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
        }       
    ]
});
