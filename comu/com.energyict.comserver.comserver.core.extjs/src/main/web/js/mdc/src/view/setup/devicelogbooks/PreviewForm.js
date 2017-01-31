/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicelogbooks.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLogbooksPreviewForm',
    itemId: 'deviceLogbooksPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.LastEventTypeDisplay',
        'Uni.form.field.LastEventDateDisplay',
        'Uni.form.field.LastReadingDisplay'
    ],
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            name: 'name'
        },
        {
            xtype: 'obis-displayfield',
            name: 'overruledObisCode'
        },
        {
            xtype: 'last-event-type-displayfield',
            name: 'lastEventType',
            emptyText: '-'
        },
        {
            fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
            name: 'lastEventDate',
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
            }
        },
        {
            fieldLabel: Uni.I18n.translate('general.nextReadingBlockStart', 'MDC', 'Next reading block start'),
            name: 'lastReading',
            renderer: function (value) {
                if (value) {
                    var date = new Date(value);
                    return Uni.DateTime.formatDateLong(date) + ' - ' + Uni.DateTime.formatTimeShort(date);
                }
                return '-';
            }
        }
    ]
});
