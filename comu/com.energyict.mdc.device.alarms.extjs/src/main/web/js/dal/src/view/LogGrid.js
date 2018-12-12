/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.alarm-details-log-grid',
    store: null,
    ui: 'medium',
    requires: ['Uni.DateTime'],
    maxHeight: 408,
    columns: [
        {
            text: Uni.I18n.translate('general.eventDate', 'DAL', 'Event date'),
            dataIndex: 'eventDate',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
            }
        },
        {
            text: Uni.I18n.translate('general.deviceCode', 'DAL', 'Device code'),
            dataIndex: 'deviceCode',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.deviceType', 'DAL', 'Device type'),
            dataIndex: 'deviceType',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.domain', 'DAL', 'Domain'),
            dataIndex: 'domain',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.subdomain', 'DAL', 'Subdomain'),
            dataIndex: 'subDomain',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.eventOrAction', 'DAL', 'Event or action'),
            dataIndex: 'eventOrAction',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.message', 'DAL', 'Message'),
            dataIndex: 'message',
            flex: 1
        }
    ]
});
