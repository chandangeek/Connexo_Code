Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationListPreview', {
    extend: 'Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm',
    alias: 'widget.metrology-config-details',
    requires: [
        'Imt.metrologyconfiguration.view.ActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            privileges: Imt.privileges.MetrologyConfig.admin,
            menu: {
                xtype: 'metrology-configuration-action-menu',
                itemId: 'metrology-configuration-action-menu'
            }
        }
    ]
});


