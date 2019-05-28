/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.webservice.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.wss-webservice-history-grid',
    store: 'Wss.store.endpoint.Occurrence',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.startedOn', 'WSS', 'Started on'),
                dataIndex: 'startTime',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var route = 'administration/webserviceendpoints/view/history/occurrence';
                    var url = me.router.getRoute(route).buildUrl({
                        endpointId: record.getEndpoint().get('id'),
                        occurenceId: record.get('id'),
                    });
                    var date = value ? Uni.DateTime.formatDateTimeShort(value) : '-';

                    return '<a href="' + url + '">' + date + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.endpoint', 'WSS', 'Web service endpoint'),
                dataIndex: 'endpoint',
                flex: 1,
                renderer: function(value, metaData, record) {
                    var endpoint = record.getEndpoint();
                    var url = me.router.getRoute('administration/webserviceendpoints/view').buildUrl({
                        endpointId: endpoint.get('id')
                    });
                    var webservice = endpoint.get('webServiceName');
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(webservice) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.application', 'WSS', 'Application'),
                dataIndex: 'applicationName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.direction', 'WSS', 'Direction'),
                dataIndex: 'endpoint',
                flex: 1,
                renderer: function(value, metaData, record) {
                    const direction = record.getEndpoint().get('direction');
                    return direction
                        ? direction.localizedValue
                        : '-'
                }
            },
            {
                header: Uni.I18n.translate('general.duration', 'WSS', 'Duration'),
                flex: 1,
                renderer: function (value, metaData, record) {
                    return Uni.util.String.formatDuration(record.get('startTime') - record.get('endTime'));
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'WSS', 'Status'),
                dataIndex: 'status',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate(
                    'webservices.history.pagingtoolbartop.displayMsg',
                    'WSS',
                    '{0} - {1} of {2} history lines'
                ),
                displayMoreMsg: Uni.I18n.translate(
                    'webservices.history.pagingtoolbartop.displayMoreMsg',
                    'WSS',
                    '{0} - {1} of more than {2} history lines'
                ),
                emptyMsg: Uni.I18n.translate(
                    'webservices.history.pagingtoolbartop.emptyMsg',
                    'WSS',
                    'There are no history lines to display'
                )
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate(
                    'webservices.history.pagingtoolbarbottom.itemsPerPage',
                    'WSS',
                    'History lines per page'
                ),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});