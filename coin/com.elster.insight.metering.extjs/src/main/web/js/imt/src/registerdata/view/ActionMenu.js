Ext.define('Imt.registerdata.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.registerActionMenu',
    itemId: 'registerActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'viewSuspects',
            text: Uni.I18n.translate('registerdata.menu.viewsuspects', 'IMT', 'View suspects'),
//            action: 'viewSuspects'
        },
        {
            itemId: 'validateNowRegister',
            text: Uni.I18n.translate('registerdata.menu.validate', 'IMT', 'Validate now'),
//            privileges: Cfg.privileges.Validation.validateManual,
//           action: 'validate'
        }
    ]
});
