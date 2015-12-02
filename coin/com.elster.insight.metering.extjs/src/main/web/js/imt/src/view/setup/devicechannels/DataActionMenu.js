Ext.define('Imt.view.setup.devicechannels.DataActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.deviceLoadProfileChannelDataActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewHistory',
            text: Uni.I18n.translate('deviceloadprofiles.viewHistory', 'IMT', 'View history'),
            action: 'viewHistory',
            hidden: true
        },
        {
            itemId: 'edit-value',
            text: Uni.I18n.translate('devicechannels.editReadings.editValue', 'IMT', 'Edit value'),
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
            text: Uni.I18n.translate('devicechannels.editReadings.removeReading', 'IMT', 'Remove reading'),
            action: 'removeReading',
//            dynamicPrivilege: Imt.dynamicprivileges.DeviceState.deviceDataEditActions
        }       
    ]
});
