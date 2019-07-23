/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.servicecall-issue-log-grid',
    store: null,
    ui: 'medium',
    requires: [ 'Uni.DateTime' ],
    maxHeight: 364,
    columns: [
        {
            text: Uni.I18n.translate('general.timestamp', 'ISC', 'Timestamp'),
            dataIndex: 'timestamp',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
            }
        },
        {
            text: Uni.I18n.translate('general.logLevel', 'ISC', 'Log level'),
            dataIndex: 'logLevel',
            flex: 1,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        },
        {
            text: Uni.I18n.translate('general.details', 'ISC', 'Message'),
            dataIndex: 'details',
            flex: 3,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        } 
    ]
});
