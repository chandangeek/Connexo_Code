Ext.define('Imt.metrologyconfiguration.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrologyConfigurationActionMenu',
    itemId: 'metrologyConfigurationActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit',
            text: Uni.I18n.translate('general.menu.edit', 'IMT', 'Edit')
//            action: 'edit'
        },
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove')
//            privileges: Cfg.privileges.Validation.validateManual,
//           action: 'remove'
        }
    ]
});
