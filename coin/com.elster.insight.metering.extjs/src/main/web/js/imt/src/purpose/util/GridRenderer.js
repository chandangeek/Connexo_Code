/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.util.GridRenderer', {
    singleton: true,
    requires: [
        'Imt.purpose.util.DataFormatter',
        'Imt.purpose.util.TooltipRenderer'
    ],

    renderValueAndUnit: function (value, metaData, record) {
        if (value && record) {
            return Uni.Number.formatNumber(value, -1) + ' ' + record.get('unitWithMultiplier');
        }
        return '-';
    },

    renderMeasurementPeriodColumn: function (value, metaData, record) {
        var interval = Imt.purpose.util.DataFormatter.formatIntervalShort(value);
        if (interval === '-'){
            return interval
        }
        return interval + Imt.purpose.util.TooltipRenderer.prepareIcon(record);
    },

    renderMeasurementTimeColumn: function (valuem, metaData, record){
        if (value){
            return Uni.DateTime.formatDateTimeShort(new Date(value))  + Imt.purpose.util.TooltipRenderer.prepareIcon(record);
        }
        return '-';
    },

    renderEventTimeColumn: function (value, metaData, record) {
        if (Ext.isEmpty(value)) {
            return '-';
        }
        var date = new Date(value),
            showDeviceQualityIcon = false,
            tooltipContent = '',
            icon = '';

        if (!Ext.isEmpty(record.get('readingQualities'))) {
            Ext.Array.forEach(record.get('readingQualities'), function (readingQualityObject) {
                if (Ext.String.startsWith(readingQualityObject.cimCode, '1.')) {
                    showDeviceQualityIcon |= true;
                    tooltipContent += readingQualityObject.indexName + '<br>';
                }
            });
            if (tooltipContent.length > 0) {
                tooltipContent += '<br>';
                tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'IMT', 'View reading quality details for more information.');
            }
            if (showDeviceQualityIcon) {
                icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtitle="'
                    + Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality') + '" data-qtip="'
                    + tooltipContent + '"></span>';
            }
        }
        return Uni.DateTime.formatDateTimeShort(date)  + icon;
    },

    renderValueColumn: function (value, metaData, record) {
        if (Ext.isEmpty(value)) {
            return '-';
        }
        var me = this,
            validationResult = record.get('validationResult'),
            status = validationResult ? validationResult.split('.')[1] : '',
            icon = '';

        if (status === 'notValidated') {
            icon = me.getNotValidatedStatus();
        } else if (record.get('confirmedNotSaved')) {
            metaData.tdCls = 'x-grid-dirty-cell';
        } else if (status === 'suspect') {
            icon = me.getSuspectStatus();
        } else if (status === 'ok' && record.get('action') == 'WARN_ONLY') {
            icon = me.getOkStatus();
        }
        if (record.get('isConfirmed') && !record.isModified('value')) {
            icon = me.getConfirmedStatus();
        }
        return value + icon;
    },

    renderLastUpdateColumn: function(value){
        return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
    },

    getNotValidatedStatus: function () {
        return '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
            + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + '"></span>';
    },

    getSuspectStatus: function () {
        return '<span class="icon-flag5" style="margin-left:10px; color:red; position:absolute;" data-qtip="'
            + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '"></span>';
    },

    getOkStatus: function () {
        return '<span class="icon-flag5" style="margin-left:10px; color: #dedc49; position:absolute;" data-qtip="'
            + Uni.I18n.translate('validationStatus.informative', 'IMT', 'Informative') + '"></span>';
    },

    getConfirmedStatus: function () {
        return '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
            + Uni.I18n.translate('reading.validationResult.confirmed', 'IMT', 'Confirmed') + '"></span>';
    }

});
