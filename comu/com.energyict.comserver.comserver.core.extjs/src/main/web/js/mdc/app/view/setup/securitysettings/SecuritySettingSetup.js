Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.securitySettingSetup',
    itemId: 'securitySettingSetup',
    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
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
                        xtype: 'deviceConfigurationMenu',
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
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'left'
                            },
                            minHeight: 20,
                            items: [
                                {
                                    xtype: 'image',
                                    margin: '0 10 0 0',
                                    src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: "<b>No security settings found</b><br>\
                                            There are no security settings. This could be because:\
                                            <lv>\
                                            <li>No security settings have been defined yet.</li>\
                                            <li>No security settings comply to the filter.</li>\
                                            </lv><br>\
                                            Possible steps:"
                                        },
                                        {
                                            xtype: 'button',
                                            margin: '10 0 0 0',
                                            text: 'Add security setting',
                                            hrefTarget: '',
                                            href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/securitysettings/create'
                                        }
                                    ]
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


