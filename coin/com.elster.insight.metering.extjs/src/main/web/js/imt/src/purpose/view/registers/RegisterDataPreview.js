/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.registers.RegisterDataPreview', {
    extend: 'Imt.purpose.view.summary.PurposeRegisterDataPreview',
    alias: 'widget.register-data-preview',


    /**
     * @override
     * @param record
     */
    updateForm: function (record) {
        var me = this,
            dataQualities = record.get('readingQualities'),
            title = me.getTitle(record),
            formula = me.getFormulaValue(me.output);

        Ext.suspendLayouts();
        me.down('#register-preview-general-panel').setTitle(title);
        me.down('#register-preview-validation-panel').setTitle(title);
        me.down('#register-preview-qualities-panel').setTitle(title);
        me.down('#register-preview-general-panel').loadRecord(record);
        me.down('#register-preview-validation-panel').loadRecord(record);
        me.down('#register-preview-formula-field').setValue(formula);
        me.down('#register-preview-noReadings-msg').setVisible(Ext.isEmpty(dataQualities));
        Imt.purpose.util.PreviewRenderer.renderDataQualityFields(
            me.down('#register-preview-deviceQuality-field'),
            me.down('#register-preview-multiSenseQuality-field'),
            me.down('#register-preview-insightQuality-field'),
            me.down('#register-preview-thirdPartyQuality-field'),
            dataQualities);
        Ext.resumeLayouts(true);
    },

    /**
     * @private
     * @override
     * @param value
     */
    renderValueAndUnit: function (value) {
        if (value) {
            var readingType = this.output.get('readingType'),
                unitOfMeasure = readingType.names ? readingType.names.unitOfMeasure : readingType.unit;
            return value + ' ' + unitOfMeasure;
        }
        return '-'
    }
});