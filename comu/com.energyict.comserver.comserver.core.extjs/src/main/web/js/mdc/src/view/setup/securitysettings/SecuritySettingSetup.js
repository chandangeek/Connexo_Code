Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.securitySettingSetup',
    itemId: 'securitySettingSetup',
    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.securitysettings.SecuritySettingGrid',
        'Mdc.view.setup.securitysettings.SecuritySettingPreview',
        'Uni.view.container.PreviewContainer'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigId,
                        toggle: 5
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: 'Security settings',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'securitySettingGrid',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigId: me.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: 'No security settings found',
                            reasons: [
                                'No security settings have been defined yet.',
                                'No security settings comply to the filter.'
                            ],
                            stepItems: [
                                {
                                    text: 'Add security setting',
                                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/securitysettings/create'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'securitySettingPreview'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


