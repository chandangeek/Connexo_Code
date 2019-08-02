/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.webservice-issue-log-grid',
    store: null,
    ui: 'medium',
    requires: [ 'Uni.DateTime' ],
    maxHeight: 364,
    columns: [
        {
            text: Uni.I18n.translate('general.label.timestamp', 'IWS', 'Timestamp'),
            dataIndex: 'timestamp',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
            }
        },
        {
            text: Uni.I18n.translate('general.label.logLevel', 'IWS', 'Log level'),
            dataIndex: 'logLevel',
            flex: 1,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        },
        {
            text: Uni.I18n.translate('general.label.message', 'IWS', 'Message'),
            dataIndex: 'details',
            flex: 3,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        } 
    ]
});
