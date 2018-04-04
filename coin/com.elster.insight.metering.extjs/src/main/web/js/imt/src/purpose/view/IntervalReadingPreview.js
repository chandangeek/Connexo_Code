/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Imt.purpose.view.IntervalReadingPreview', {
    extend: 'Imt.purpose.view.summary.PurposeDataPreview',
    alias: 'widget.interval-reading-preview',

    getGeneralItems: function () {
        var me = this;
        return [
            {
                fieldLabel: Uni.I18n.translate('general.interval', 'IMT', 'Interval'),
                name: 'interval',
                itemId: 'interval-preview-interval-field',
                renderer: Imt.purpose.util.DataFormatter.formatIntervalLong
            },
            {
                fieldLabel: Uni.I18n.translate('general.lastUpdate', 'IMT', 'Last update'),
                name: 'reportedDateTime',
                itemId: 'interval-preview-lastUpdate-field',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('reading.dataValidated.title', 'IMT', 'Data validated'),
                name: 'dataValidated',
                itemId: 'interval-preview-dataValidated-field',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                fieldLabel: Uni.I18n.translate('reading.validationResult', 'IMT', 'Validation result'),
                name: 'validationResult',
                itemId: 'interval-preview-validationResult-field',
                renderer: function (value) {
                    var record = me.down('form').getRecord();
                    return Imt.purpose.util.PreviewRenderer.renderValidationResult(value, record);
                }
            }
        ];
    }


});