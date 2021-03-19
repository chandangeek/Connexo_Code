/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.webservice.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.wss-webservice-history-grid',
    store: 'Wss.store.endpoint.Occurrence',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Wss.view.webservice.GridActionMenu'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.startedOn', 'WSS', 'Started on'),
                dataIndex: 'startTime',
                flex: 10,
                minWidth: 150,
                renderer: function (value, metaData, record) {
                    var basename = me.adminView ? 'administration' : 'workspace';
                    var curOccurrenceRoute = (Uni.Auth.hasPrivilege('privilege.administrate.webservices') || Uni.Auth.hasPrivilege('privilege.view.webservices') ?
                        '/webserviceendpoints/view/history/occurrence' : '/webservicehistory/view/occurrence');
                    var route = basename + curOccurrenceRoute;
                    var date = value ? Uni.DateTime.formatDateTimeShort(value) : '-';

                    var url = me.router.getRoute(route).buildUrl({
                        endpointId: record.getEndpoint().get('id'),
                        occurenceId: record.get('id'),
                    });

                    return '<a href="' + url + '">' + date + '</a>';
                }
            },
            {
                dataIndex: 'endTime',
                header: Uni.I18n.translate('general.finishedOn', 'WSS', 'Finished on'),
                flex: 10,
                minWidth: 150,
                renderer: function (value, metaData, record) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.endpoint', 'WSS', 'Web service endpoint'),
                dataIndex: 'endpoint',
                flex: 49,
                hidden: Boolean(me.endpoint),
                renderer: function (value, metaData, record) {
                    var endpoint = record.getEndpoint();
                    if (Uni.Auth.hasPrivilege('privilege.administrate.webservices') || Uni.Auth.hasPrivilege('privilege.view.webservices')) {
                        var basename = me.adminView ? 'administration' : 'workspace';
                        var url = me.router.getRoute(basename + '/webserviceendpoints/view').buildUrl({
                            endpointId: endpoint.get('id')
                        });

                        return '<span data-qtip="' + Ext.String.htmlEncode(endpoint.get('name')) + '"><a href="' + url + '">' + Ext.String.htmlEncode(endpoint.get('name')) + '</a></span>';
                    } else {
                        return '<span data-qtip="' + Ext.String.htmlEncode(endpoint.get('name')) + '">' + Ext.String.htmlEncode(endpoint.get('name')) + '</span>';
                    }
                }
            },
            {
                dataIndex: 'applicationName',
                hidden: Boolean(me.endpoint) || !me.adminView,
                header: Uni.I18n.translate('general.application', 'WSS', 'Application'),
                flex: 10
            },
            {
                dataIndex: 'endpoint',
                hidden: Boolean(me.endpoint),
                header: Uni.I18n.translate('general.type', 'WSS', 'Type'),
                flex: 7,
                renderer: function (value, metaData, record) {
                    const direction = record.getEndpoint().get('direction');
                    return direction
                        ? direction.localizedValue
                        : '-'
                }
            },
            {
                flex: 7,
                header: Uni.I18n.translate('general.duration', 'WSS', 'Duration'),
                name: Uni.I18n.translate('general.duration', 'WSS', 'Duration'),
                renderer: function (value, metaData, record) {
                    if (!record.get('endTime')) {
                        return '-';
                    }

                    return Uni.util.String.formatDuration(record.get('startTime') - record.get('endTime'), true);
                }
            },
            {
                dataIndex: 'status',
                header: Uni.I18n.translate('general.status', 'WSS', 'Status'),
                flex: 7
            },
            {
                dataIndex: 'action',
                xtype: 'uni-actioncolumn',
                flex: 7,
                isDisabled: function(menu, item) {
                    var payloadStore = menu.getStore('Wss.store.PayloadSaveStrategy');
                    if(payloadStore){
                        hasPayloadRecord = payloadStore.getAt(item);
                        var hasPayload = hasPayloadRecord.get('hasPayload');
                        return !hasPayload;
                    }
                    return true;
                },
                menu: {
                    itemId: 'webservices-historygrid-action-menu-id',
                    xtype: 'webservices-historygrid-action-menu',
                    endpoint: me.endpoint
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                needCustomExporter: true,
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
                deferLoading: true,
                needExtendedData: true,
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