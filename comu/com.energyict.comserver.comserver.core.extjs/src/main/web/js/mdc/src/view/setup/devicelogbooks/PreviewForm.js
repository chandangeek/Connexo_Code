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
            name: 'lastEventType'
        },
        {
            fieldLabel: Uni.I18n.translate('devicelogbooks.timestampLastEvent', 'MDC', 'Timestamp last event'),
            name: 'lastEventDate',
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeLong(value) : '';
            }
        },
        {
            fieldLabel: Uni.I18n.translate('devicelogbooks.lastReading', 'MDC', 'Last reading'),
            name: 'lastReading',
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeLong(value) : '';
            }
        }
    ]
});
