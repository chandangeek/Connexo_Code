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
                    var basename = me.adminView ? 'administration' : 'workspace';
                    var route = basename + '/webserviceendpoints/view/history/occurrence';
                    var date = value ? Uni.DateTime.formatDateTimeShort(value) : '-';

                    if (Wss.privileges.Webservices.canViewHistory()) {
                        var url = me.router.getRoute(route).buildUrl({
                            endpointId: record.getEndpoint().get('id'),
                            occurenceId: record.get('id'),
                        });

                        return '<a href="' + url + '">' + date + '</a>';
                    }

                    return date;
                }
            },
            {
                dataIndex: 'endTime',
                hidden: !Boolean(me.endpoint),
                header: Uni.I18n.translate('general.finishedOn', 'WSS', 'Finished on'),
                flex: 1,
                renderer: function (value, metaData, record) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.endpoint', 'WSS', 'Web service endpoint'),
                dataIndex: 'endpoint',
                flex: 1,
                hidden: Boolean(me.endpoint),
                renderer: function(value, metaData, record) {
                    var endpoint = record.getEndpoint();
                    var basename = me.adminView ? 'administration' : 'workspace';
                    var url = me.router.getRoute(basename + '/webserviceendpoints/view').buildUrl({
                        endpointId: endpoint.get('id')
                    });
                    var webservice = endpoint.get('webServiceName');
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(webservice) + '</a>';
                }
            },
            {
                dataIndex: 'applicationName',
                hidden: Boolean(me.endpoint) || !me.adminView,
                header: Uni.I18n.translate('general.application', 'WSS', 'Application'),
                flex: 1
            },
            {
                dataIndex: 'endpoint',
                hidden: Boolean(me.endpoint),
                header: Uni.I18n.translate('general.direction', 'WSS', 'Direction'),
                flex: 1,
                renderer: function(value, metaData, record) {
                    const direction = record.getEndpoint().get('direction');
                    return direction
                        ? direction.localizedValue
                        : '-'
                }
            },
            {
                flex: 1,
                header: Uni.I18n.translate('general.duration', 'WSS', 'Duration'),
                renderer: function (value, metaData, record) {
                    if (!record.get('endTime')) {
                        return '-';
                    }

                    return Uni.util.String.formatDuration(record.get('startTime') - record.get('endTime'));
                }
            },
            {
                dataIndex: 'status',
                header: Uni.I18n.translate('general.status', 'WSS', 'Status'),
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