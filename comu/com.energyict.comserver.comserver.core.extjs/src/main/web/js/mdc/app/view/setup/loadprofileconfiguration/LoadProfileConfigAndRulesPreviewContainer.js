Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigAndRulesPreviewContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigAndRulesPreviewContainer',
    itemId: 'loadProfileConfigAndRulesPreviewContainer',
    xtype: 'loadprofile-config-and-rules-preview-container',
    deviceTypeId: null,
    deviceConfigId: null,
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'loadProfileConfigurationPreview',
            deviceTypeId: this.deviceTypeId,
            deviceConfigId: this.deviceConfigId
        }
    ]
});