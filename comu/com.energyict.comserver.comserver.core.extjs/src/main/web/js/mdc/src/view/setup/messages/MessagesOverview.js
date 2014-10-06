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
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigId,
                        toggle: 9
                    }
                ]
            }
        ];
        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('messages.overview.title', 'MDC', 'Messages'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'messages-categories-grid'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('messages.categoriesGrid.empty.title', 'MDC', 'No message categories found'),
                            reasons: [
                                Uni.I18n.translate('messages.CategoriesGrid.emptyCmp.item1', 'MDC', 'No message categories have been defined yet.'),
                                Uni.I18n.translate('messages.CategoriesGrid.emptyCmp.item2', 'MDC', 'No message categories available for this device configuration.')
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