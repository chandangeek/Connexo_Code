Ext.define('Imt.metrologyconfiguration.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrologyConfigurationActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit',
            text: Uni.I18n.translate('general.menu.edit', 'IMT', 'Edit'),
            action: 'editMetrologyConfiguration'
        },
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
//            privileges: Cfg.privileges.Validation.validateManual,
           action: 'removeMetrologyConfiguration'
        }
    ]
});
