/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.issue-details-log-grid',
    store: null,
    ui: 'medium',
    requires: [ 'Uni.DateTime' ],
    maxHeight: 408,
    columns: [
        {
            text: Uni.I18n.translate('general.timestamp', 'IDC', 'Timestamp'),
            dataIndex: 'timestamp',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
            }
        },
        {
            text: Uni.I18n.translate('general.description', 'IDC', 'Description'),
            dataIndex: 'details',
            flex: 3,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        },
        {
            text: Uni.I18n.translate('general.logLevel', 'IDC', 'Log level'),
            dataIndex: 'logLevel',
            flex: 1,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        }
    ]
});
