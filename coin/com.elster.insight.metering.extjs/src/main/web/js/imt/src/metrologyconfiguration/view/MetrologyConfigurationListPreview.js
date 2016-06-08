Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationListPreview', {
    extend: 'Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm',
    alias: 'widget.metrology-config-details',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu'
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
                itemId: 'metrology-configuration-list-action-menu'
            }
        }
    ],
    
    disableActionsButton: function(disabled){
        this.down('#actionButton').setDisabled(disabled)
    }    
});