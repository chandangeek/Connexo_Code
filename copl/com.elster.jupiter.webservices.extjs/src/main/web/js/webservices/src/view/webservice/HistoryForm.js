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

        this.getForm().setValues({
            endpoint: endpoint,
            webServiceName: endpoint.get('webServiceName'),
            direction: endpoint.get('direction'),
            logLevel: endpoint.get('logLevel')
        });
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                name: 'endpoint',
                fieldLabel: Uni.I18n.translate('general.webserviceEndpoint', 'WSS', 'Web service endpoint'),
                renderer: function (value) {
                    if (!value) {
                        return '-';
                    }

                    return value.get('name');
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
                name: 'startTime',
                fieldLabel: Uni.I18n.translate('general.startedOn', 'WSS', 'Started on'),
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            },
            {
                name: 'endTime',
                fieldLabel: Uni.I18n.translate('general.finishedOn', 'WSS', 'Finished on'),
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            }
        ];

        me.callParent(arguments);
    },

    renderYesOrNo: function (value) {
        if (value === true) {
            return Uni.I18n.translate('general.yes', 'WSS', 'Yes');
        } else {
            return Uni.I18n.translate('general.no', 'WSS', 'No')
        }
    }

});