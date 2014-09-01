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
            fieldLabel: Uni.I18n.translate('devicelogbooks.preview.name', 'MDC', 'Name'),
            name: 'name'
        },
        {
            xtype: 'obis-displayfield',
            name: 'obisCode'
        },
        {
            xtype: 'last-event-type-displayfield',
            name: 'lastEventType'
        },
        {
            fieldLabel: Uni.I18n.translate('devicelogbooks.preview.lastEventDate', 'MDC', 'Last event date'),
            name: 'lastEventDate',
            renderer: function (value) {
                return value ? Uni.I18n.formatDate('devicelogbooks.preview.lastEventDate.dateFormat', value, 'UNI', 'F d Y, H:i:s') : '';
            }
        },
        {
            fieldLabel: Uni.I18n.translate('devicelogbooks.preview.lastReading', 'MDC', 'Last reading'),
            name: 'lastReading',
            renderer: function (value) {
                return value ? Uni.I18n.formatDate('devicelogbooks.preview.lastReading.dateFormat', value, 'UNI', 'F d Y H:i:s') : '';
            }
        }
    ]
});
