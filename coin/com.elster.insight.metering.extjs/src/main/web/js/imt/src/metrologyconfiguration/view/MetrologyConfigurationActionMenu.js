Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrology-configuration-action-menu',
    plain: true,
    border: false,
    itemId: 'metrology-configuration-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('metrologyconfiguration.edit', 'IMT', 'Edit'),
            action: 'editMetrologyConfiguration',
            itemId: 'editMetrologyConfiguration'
        },
        {
            text: Uni.I18n.translate('metrologyconfiguration.remove', 'IMT', 'Remove'),
            action: 'removeMetrologyConfiguration',
            itemId: 'removeMetrologyConfiguration'
        }
    ]
});
