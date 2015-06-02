Ext.define('Fim.view.importservices.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fim-import-services-setup',

    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Fim.view.importservices.ActionMenu',
        'Fim.view.importservices.Grid',
        'Fim.view.importservices.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.importServices', 'FIM', 'Import services'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'fim-import-services-grid',
                        itemId: 'grd-import-services',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-import-service',
                        title: Uni.I18n.translate('importServices.empty.title', 'FIM', 'No import services found'),
                        reasons: [
                            Uni.I18n.translate('importServices.empty.list.item1', 'FIM', 'No import services have been defined yet.'),
                            Uni.I18n.translate('importServices.empty.list.item2', 'FIM', 'Import services exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addImportService', 'FIM', 'Add import service'),
                                privileges: Fim.privileges.DataImport.getAdminPrivilege(),
                                ui: 'action',
                                href: '#/administration/importservices/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'fim-import-service-preview',
                        itemId: 'pnl-import-service-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});