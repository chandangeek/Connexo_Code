Ext.define('Imt.channeldata.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.channelsActionMenu',
    itemId: 'channelsActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'viewSuspects',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'IMT', 'View suspects'),
            action: 'viewSuspects'
        },
        {
            itemId: 'validateNowChannel',
            text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'IMT', 'Validate now'),
//            privileges:Cfg.privileges.Validation.validateManual,
//            dynamicPrivilege: Imt.dynamicprivileges.DeviceState.validationActions,
            action: 'validateNow'
        }
    ]
});
