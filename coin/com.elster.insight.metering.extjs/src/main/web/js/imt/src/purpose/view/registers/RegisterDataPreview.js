/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.registers.RegisterDataPreview', {
    extend: 'Imt.purpose.view.summary.PurposeRegisterDataPreview',
    alias: 'widget.register-data-preview',

    getGeneralItems: function (){
        var me = this;
        return [
            {
                fieldLabel: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                name: 'interval',
                itemId: 'register-preview-measurementPeriod-field',
                renderer: Imt.purpose.util.DataFormatter.formatIntervalLong
            },
            {
                fieldLabel: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                name: 'timeStamp',
                itemId: 'register-preview-measurementTime-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            },
            {
                fieldLabel: Uni.I18n.translate('general.eventTime', 'IMT', 'Event time'),
                name: 'eventDate',
                itemId: 'register-preview-eventTime-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            },
            {
                fieldLabel: Uni.I18n.translate('general.value', 'IMT', 'Value'),
                name: 'value',
                itemId: 'register-preview-value-field',
                renderer: function (value,displayField){
                    return me.renderValueAndUnit(value,displayField,me.output);
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.deltaValue', 'IMT', 'Delta value'),
                name: 'deltaValue',
                itemId: 'register-preview-deltaValue-field',
                renderer: function (value,displayField){
                    return me.renderValueAndUnit(value,displayField,me.output);
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.formula', 'IMT', 'Formula'),
                itemId: 'register-preview-formula-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.lastUpdate', 'IMT', 'Last update'),
                name: 'reportedDateTime',
                itemId: 'register-preview-lastUpdate-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            }
        ];
    },

    getFormulaValue: function(){
        return this.output.get('formula').description;
    },

    /**
     * We override the method here because this will also be used for History
     * which doesn't have the 'unitWithMultiplier' field
     */
    renderValueAndUnit: function (value, displayField, output) {
        if (value) {
            var readingType = output.get('readingType'),
                unitOfMeasure = readingType.names ? readingType.names.unitOfMeasure : readingType.unit;
            return value + ' ' + unitOfMeasure;
        }
        return '-'
    }
});