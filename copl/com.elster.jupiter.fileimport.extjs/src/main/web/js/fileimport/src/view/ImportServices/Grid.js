Ext.define('Fim.view.importServices.Grid', {
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
                flex: 1
            },
			{
                header: Uni.I18n.translate('general.application', 'FIM', 'Application'),
                dataIndex: 'importFolder',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'FIM', 'Status'),
                dataIndex: 'status',
                /*renderer: function (value) {
                    var result;
                    if (value && value.statusDate && value.statusDate != 0) {
                        result = value.statusPrefix + ' ' + Uni.DateTime.formatDateTimeShort(new Date(value.statusDate));
                    } else if (value) {
                        result = value.statusPrefix
                    } else {
                        result = Uni.I18n.translate('general.notPerformed', 'FIM', 'Not performed yet');
                    }
                    return result;
                },*/
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.importFolder', 'FIM', 'Imported folder'),
                dataIndex: 'importFolder',
                flex: 1
            },
			{
                header: Uni.I18n.translate('importService.filePattern', 'FIM', 'File pattern'),
                dataIndex: 'filePattern',
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
                        privileges: Dxp.privileges.DataExport.admin,
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
