/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.webservice.HistoryForm', {
    extend: 'Ext.form.Panel',
    frame: false,
    alias: 'widget.webservices-webservice-history-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },

    loadRecord: function(record) {
        this.callParent(arguments);
        var endpoint = record.getEndpoint();
        var duration;
        var time = Uni.DateTime.formatDateTimeShort(record.get('startTime'));
        if (record.get('endTime')){
            duration = record.get('endTime') - record.get('startTime');
        }else{
            duration = "-"
        }

        this.getForm().setValues({
            endpoint: endpoint,
            webServiceName: endpoint.get('webServiceName'),
            direction: endpoint.get('direction'),
            logLevel: endpoint.get('logLevel'),
            duration: duration
        });

        this.setTitle(time);
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                name: 'endpoint',
                fieldLabel: Uni.I18n.translate('general.webserviceEndpoint', 'WSS', 'Web service endpoint'),
                renderer: function (endpoint) {
                    if (!endpoint) {
                        return '-';
                    }

                    if (me.endpoint || !(Uni.Auth.hasPrivilege('privilege.administrate.webservices') || Uni.Auth.hasPrivilege('privilege.view.webservices'))) {
                        return Ext.String.htmlEncode(endpoint.get('name'));
                    }

                    var basename = me.adminView ? 'administration' : 'workspace';
                    var url = me.router.getRoute(basename + '/webserviceendpoints/view').buildUrl({
                        endpointId: endpoint.get('id')
                    });

                    return '<a href="' + url + '">' + Ext.String.htmlEncode(endpoint.get('name')) + '</a>';
                }
            },
            {
                name: 'webServiceName',
                fieldLabel: Uni.I18n.translate('general.webservice', 'WSS', 'Web service')
            },
            {
                name: 'request',
                fieldLabel: Uni.I18n.translate('general.request', 'WSS', 'Request')
            },
            {
                name: 'startTime',
                fieldLabel: Uni.I18n.translate('general.startedOn', 'WSS', 'Started on'),
                renderer: function (value) {
                    if (value){
                        var date = new Date(value);
                        return Uni.DateTime.formatDateLong(date) + ' at ' + Uni.DateTime.formatTimeLong(date);
                    }
                    return '-';
                }
            },
            {
                name: 'endTime',
                fieldLabel: Uni.I18n.translate('general.finishedOn', 'WSS', 'Finished on'),
                renderer: function (value) {
                    if (value){
                        var date = new Date(value);
                        return Uni.DateTime.formatDateLong(date) + ' at ' + Uni.DateTime.formatTimeLong(date);
                    }
                    return '-';
                }
            },
            {
                name: 'duration',
                fieldLabel: Uni.I18n.translate('general.duration', 'WSS', 'Duration'),
                renderer: function (value) {
                    if (value && value !== "-") {
                        return Uni.util.String.formatDuration(value) ;
                    }

                    return '-';
                }
            },
            {
                name: 'applicationName',
                hidden: !me.adminView,
                fieldLabel: Uni.I18n.translate('general.application', 'WSS', 'Application')
            },
            {
                name: 'direction',
                fieldLabel: Uni.I18n.translate('general.type', 'WSS', 'Type'),
                renderer: function (value) {
                    return value ? value.localizedValue : '-';
                }
            },
            {
                name: 'logLevel',
                fieldLabel: Uni.I18n.translate('general.logLevel', 'WSS', 'Log level'),
                renderer: function (value) {
                    return value ? value.localizedValue : '-';
                }
            },
            {
                name: 'status',
                fieldLabel: Uni.I18n.translate('general.status', 'WSS', 'Status')
            },
            {
                name: 'appServerName',
                fieldLabel: Uni.I18n.translate('general.appServerName', 'WSS', 'Application server')
            }
        ];

        me.callParent(arguments);
    }
});
