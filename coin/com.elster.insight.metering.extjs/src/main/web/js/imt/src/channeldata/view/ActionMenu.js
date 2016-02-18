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
            text: Uni.I18n.translate('channels.menu.viewsuspects', 'IMT', 'View suspects'),
            action: 'viewSuspects'
        },
        {
            itemId: 'validateNowChannel',
            text: Uni.I18n.translate('channels.menu.validate', 'IMT', 'Validate now'),
            //privileges:Cfg.privileges.Validation.validateManual,
//            dynamicPrivilege: Imt.dynamicprivileges.UsagePointState.validationActions,
            action: 'validateNow'
        }
    ]
});
