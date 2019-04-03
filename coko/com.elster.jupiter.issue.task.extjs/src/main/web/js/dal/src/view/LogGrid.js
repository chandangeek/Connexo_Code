/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.issue-details-log-grid',
    store: null,
    ui: 'medium',
    requires: ['Uni.DateTime'],
    maxHeight: 408,
    columns: [
        {
            text: Uni.I18n.translate('general.eventDate', 'ITK', 'Event date'),
            dataIndex: 'eventDate',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
            }
        },
        {
            text: Uni.I18n.translate('general.deviceCode', 'ITK', 'Device code'),
            dataIndex: 'deviceCode',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.deviceType', 'ITK', 'Device type'),
            dataIndex: 'deviceType',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.domain', 'ITK', 'Domain'),
            dataIndex: 'domain',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.subdomain', 'ITK', 'Subdomain'),
            dataIndex: 'subDomain',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.eventOrAction', 'ITK', 'Event or action'),
            dataIndex: 'eventOrAction',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.message', 'ITK', 'Message'),
            dataIndex: 'message',
            flex: 1
        }
    ]
});
