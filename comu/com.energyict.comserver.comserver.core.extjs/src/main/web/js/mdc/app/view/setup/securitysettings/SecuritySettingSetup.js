Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.securitySettingSetup',
    itemId: 'securitySettingSetup',
    deviceTypeId: null,
    deviceConfigId: null,
    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            itemId: 'stepsContainer',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Security settings</h1>'
                },
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
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'securitySettingPreview'
                }
            ]}
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'deviceConfigurationMenu',
                itemId: 'stepsMenu',
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId,
                toggle: 5
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


