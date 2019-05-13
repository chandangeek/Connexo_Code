/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.webservice.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.wss-webservice-history-grid',
    store: 'Wss.store.webservice.History',
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
                dataIndex: 'timestampDisplay',
                flex: 1.5
            },
            {
                header: Uni.I18n.translate('general.endpoint', 'WSS', 'Web service endpoint'),
                dataIndex: 'endpoint',
                renderer: function(value, metaData, record) {
                    var endpoint = record.getEndpoint();
                    var url = me.router.getRoute('administration/webserviceendpoints/view').buildUrl({
                        endpointId: endpoint.get('id')
                    });
                    var webservice = endpoint.get('webServiceName');
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(webservice) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.application', 'WSS', 'Application'),
                dataIndex: 'endpoint',
                renderer: function(value, metaData, record) {
                    return record.getEndpoint().get('application');
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'WSS', 'Type'),
                dataIndex: 'endpoint',
                renderer: function(value, metaData, record) {
                    return record.getEndpoint().get('type');
                },
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
                defaultPageSize: 50,
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