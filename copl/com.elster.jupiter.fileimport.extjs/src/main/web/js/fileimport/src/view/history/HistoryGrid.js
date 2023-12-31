/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.history.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.fim-history-grid',
    store: 'Fim.store.ImportServicesHistory',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    showImportService: false,
    fromWorkSpace: false,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('importService.history.startedOn', 'FIM', 'Started on'),
                dataIndex: 'startedOn',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var url = me.fromWorkSpace ?
                        me.router.getRoute('workspace/importhistory/occurrence').buildUrl({
                            occurrenceId: record.get('id')
                        }) :
                        me.router.getRoute('administration/importservices/importservice/history/occurrence').buildUrl({
                            importServiceId: record.get('importServiceId'),
                            occurrenceId: record.get('id')
                        });
                    return value ? '<a href="' + url + '">' + Uni.DateTime.formatDateTimeShort(new Date(value)) + '</a>' : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
                dataIndex: 'importServiceName',
                flex: 2,
                hidden: !me.showImportService,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/importservices/importservice').buildUrl({
                        importServiceId: record.get('importServiceId')
                    });
                    return Fim.privileges.DataImport.canView()
                        ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                        : Ext.String.htmlEncode(value);
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                dataIndex: 'duration',
                shortFormat: true,
                textAlign: 'center',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'FIM', 'Status'),
                dataIndex: 'status',
                textAlign: 'center',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                needCustomExporter: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('importService.history.pagingtoolbartop.displayMsg', 'FIM', '{0} - {1} of {2} history lines'),
                displayMoreMsg: Uni.I18n.translate('importService.history.pagingtoolbartop.displayMoreMsg', 'FIM', '{0} - {1} of more than {2} history lines'),
                emptyMsg: Uni.I18n.translate('importService.history.pagingtoolbartop.emptyMsg', 'FIM', 'There are no history to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('importService.history.pagingtoolbarbottom.itemsPerPage', 'FIM', 'History lines per page'),
                dock: 'bottom',
                needExtendedData: true,
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});
