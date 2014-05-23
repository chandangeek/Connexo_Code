Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.securitySettingSetup',
    itemId: 'securitySettingSetup',
    deviceTypeId: null,
    deviceConfigId: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: 'Security settings',
            itemId: 'stepsContainer',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
//                {
//                    xtype: 'securitySettingFiltering'
//                },
//                {
//                    xtype: 'securitySettingSorting'
//                },
                {
                    xtype: 'container',
                    itemId: 'SecuritySettingDockedItems'
                },
                {
                    xtype: 'container',
                    itemId: 'SecuritySettingEmptyList'
                },
                {
                    xtype: 'securitySettingGrid'
                },
                {
                    xtype: 'securitySettingPreview'
                }
            ]}
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceConfigurationMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigId,
                        toggle: 5
                    }
                ]
            }
        ];
        this.callParent(arguments);
        this.down('#SecuritySettingDockedItems').add(
            {
                xtype: 'securitySettingDockedItems',
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId
            }
        );
    }
});


