Ext.define('Fim.view.importservices.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.fim-import-services-grid',
    store: 'Fim.store.ImportServices',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'FIM', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/importservices/importservice').buildUrl({importServiceId: record.get('id')});
                    return '<a href="' + url + '">' + value + '</a>';
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('importService.fileImporter', 'FIM', 'File importer'),
                dataIndex: 'fileImporter',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.status', 'FIM', 'Status'),
                dataIndex: 'statusDisplay',
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('statusTooltip') + '"';
                    return value;
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.application', 'FIM', 'Application'),
                dataIndex: 'applicationDisplay',
                hidden: !Fim.privileges.DataImport.getAdmin(),
                flex: 1
            },

            {
                header: Uni.I18n.translate('general.importFolder', 'FIM', 'Imported folder'),
                dataIndex: 'importDirectory',
                flex: 3
            },
            {
                header: Uni.I18n.translate('importService.filePattern', 'FIM', 'File pattern'),
                dataIndex: 'pathMatcher',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'fim-import-service-action-menu',
                    itemId: 'fim-import-service-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('importServices.pagingtoolbartop.displayMsg', 'FIM', '{0} - {1} of {2} import services'),
                displayMoreMsg: Uni.I18n.translate('importServices.pagingtoolbartop.displayMoreMsg', 'FIM', '{0} - {1} of more than {2} import services'),
                emptyMsg: Uni.I18n.translate('importServices.pagingtoolbartop.emptyMsg', 'FIM', 'There are no import services to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addImportService', 'FIM', 'Add import service'),
                        privileges: Fim.privileges.DataImport.getAdmin,
                        href: '#/administration/importservices/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('importServices.pagingtoolbarbottom.itemsPerPage', 'FIM', 'Import services per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
