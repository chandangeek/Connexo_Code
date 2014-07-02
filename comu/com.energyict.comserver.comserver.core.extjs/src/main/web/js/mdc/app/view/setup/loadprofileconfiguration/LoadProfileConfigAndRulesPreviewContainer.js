Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigAndRulesPreviewContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigAndRulesPreviewContainer',
    itemId: 'loadProfileConfigAndRulesPreviewContainer',
    xtype: 'loadprofile-config-and-rules-preview-container',
    deviceTypeId: null,
    deviceConfigId: null,
    loadProfileId: null,
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview',
        'Mdc.view.setup.loadprofileconfiguration.RulesForLoadProfileConfigGrid'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'loadProfileConfigurationPreview'/*,
            deviceTypeId: this.deviceTypeId,
            deviceConfigId: this.deviceConfigId */
        }/*,

        {
            xtype: 'panel',
            ui: 'medium',
            padding: '32 0 0 0',
            itemId: 'rulesForLoadProfileConfigPreview',
            title: 'test validation rules',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'validation-rules-for-loadprofileconfig-grid',
                    deviceTypeId: this.deviceTypeId,
                    deviceConfigId: this.deviceConfigId,
                    loadProfileId: this.loadProfileId
                },
                {
                    xtype: 'validation-rule-preview'
                }
            ]

        }  */






    ]



});