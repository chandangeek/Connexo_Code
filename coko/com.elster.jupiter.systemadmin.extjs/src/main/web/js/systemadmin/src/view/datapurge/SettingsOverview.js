Ext.define('Sam.view.datapurge.SettingsOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-purge-settings-overview',
    requires: [
        'Sam.view.datapurge.SettingGrid',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('datapurge.settings.title', 'SAM', 'Data purge settings'),
                ui: 'large',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            itemId: 'data-purge-settings-grid',
                            xtype: 'data-purge-settings-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            itemId: 'data-purge-settings-no-items-found-panel',
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('datapurge.settings.empty.title', 'SAM', 'No data purge settings found'),
                            reasons: [
                                Uni.I18n.translate('datapurge.settings.empty.list.item1', 'SAM', 'No data purge settings have been defined yet.')
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});