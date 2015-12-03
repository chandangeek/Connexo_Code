Ext.define('Imt.channeldata.view.DataActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.channelDataActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewHistory',
            text: Uni.I18n.translate('channels.viewHistory', 'IMT', 'View history'),
            action: 'viewHistory',
            hidden: true
        },
        {
            itemId: 'edit-value',
            text: Uni.I18n.translate('channels.editReadings.editValue', 'IMT', 'Edit value'),
            action: 'editValue',
//            dynamicPrivilege: Imt.dynamicprivileges.DeviceState.deviceDataEditActions
        },
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
            itemId: 'remove-reading',
            text: Uni.I18n.translate('channels.editReadings.removeReading', 'IMT', 'Remove reading'),
            action: 'removeReading',
//            dynamicPrivilege: Imt.dynamicprivileges.DeviceState.deviceDataEditActions
        }       
    ]
});
