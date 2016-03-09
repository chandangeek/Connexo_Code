Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrology-configuration-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        //{
        //    text: Uni.I18n.translate('general.menu.activate', 'IMT', 'Activate'),
        //    action: 'activateMetrologyConfiguration',
        //    itemId: 'activateMetrologyConfiguration'
        //},
        //{
        //    text: Uni.I18n.translate('general.menu.addPurpose', 'IMT', 'Add purpose'),
        //    action: 'addPurposeMetrologyConfiguration',
        //    itemId: 'addPurposeMetrologyConfiguration'
        //},
        {
            text: Uni.I18n.translate('general.menu.editGeneralInformation', 'IMT', 'Edit general information'),
            action: 'editMetrologyConfiguration',
            itemId: 'editMetrologyConfiguration'
        },
        {
            text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
            action: 'removeMetrologyConfiguration',
            itemId: 'removeMetrologyConfiguration'
        }
    ]
});
