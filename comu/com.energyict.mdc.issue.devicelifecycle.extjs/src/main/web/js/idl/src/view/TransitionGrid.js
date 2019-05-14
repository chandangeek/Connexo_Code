/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.view.TransitionGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.transition-details-grid',
    store: null,
    ui: 'medium',
    maxHeight: 408,
    columns: [
        {
            text: Uni.I18n.translate('general.devicelifecycle', 'IDL', 'Device lifecycle'),
            dataIndex: 'deviceLifecycle',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.title.creationDate', 'IDL', 'Creation date'),
            dataIndex: 'modTime',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
            }
        },
        {
            text: Uni.I18n.translate('general.transition', 'IDL', 'Transition'),
            dataIndex: 'transition',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.failedStateChange', 'IDL', 'Failed state change'),
            dataIndex: 'failedStateChange',
            flex: 2
        },
        {
            text: Uni.I18n.translate('general.cause', 'IDL', 'Cause'),
            dataIndex: 'cause',
            flex: 5
        }
    ]
});
