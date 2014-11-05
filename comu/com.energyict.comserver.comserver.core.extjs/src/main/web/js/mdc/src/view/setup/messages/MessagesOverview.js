Ext.define('Mdc.view.setup.messages.MessagesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.messages-overview',
    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Mdc.view.setup.messages.MessagesCategoriesGrid',
        'Mdc.view.setup.messages.MessagesGrid'
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
                        toggle: 9,
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigId
                    }
                ]
            }
        ];
        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('commands.overview.title', 'MDC', 'Commands'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'messages-categories-grid',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigId: me.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('commands.categoriesGrid.empty.title', 'MDC', 'Device protocol did not specify any commands.'),
                            reasons: [
                                Uni.I18n.translate('commands.CategoriesGrid.emptyCmp.item1', 'MDC', 'No command categories have been defined yet.'),
                                Uni.I18n.translate('commands.CategoriesGrid.emptyCmp.item2', 'MDC', 'No command categories available for this device configuration.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'messages-grid'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});