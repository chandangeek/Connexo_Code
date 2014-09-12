Ext.define('Mdc.view.setup.deviceloadprofilechannels.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreviewForm',
    itemId: 'deviceLoadProfileChannelsPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'MDC', 'Name'),
            name: 'name'
        },
        {
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.unitOfMeasure', 'MDC', 'Unit of measure'),
            name: 'unitOfMeasure_formatted'
        },
        {
            xtype: 'reading-type-displayfield',
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.readingType', 'MDC', 'Reading type'),
            name: 'cimReadingType'
        },
        {
            xtype: 'obis-displayfield',
            name: 'obisCode'
        },
        {
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.multiplier', 'MDC', 'Multiplier'),
            name: 'multiplier'
        },
        {
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.overflowValue', 'MDC', 'Overflow value'),
            name: 'overflowValue'
        }
    ]
});
