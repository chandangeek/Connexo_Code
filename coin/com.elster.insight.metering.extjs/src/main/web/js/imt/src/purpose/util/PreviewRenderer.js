/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Imt.purpose.util.PreviewRenderer', {

    singleton: true,

    renderValueWithResult: function (value, record, output) {
        var me = this,
            validationResult = record.get('validationResult'),
            readingType = output.get('readingType'),
            unitOfMeasure = readingType.names ? readingType.names.unitOfMeasure : readingType.unit,
            estimatedByRule = record.get('estimatedByRule'),
            validationResultText = '',
            editedAndProjected = record.get('modificationFlag') && record.get('modificationDate') && record.get('isProjected') === true;

        if (!Ext.isEmpty(record) && record.get('isConfirmed')) {
            validationResultText = '(' + Uni.I18n.translate('reading.validationResult.notsuspect', 'IMT', 'Not suspect') + ')' +
                '<span class="icon-checkmark" style="margin-left:10px; display:inline-block; vertical-align:top;"></span>';
        } else if (validationResult) {
            switch (validationResult.split('.')[1]) {
                case 'notValidated':
                    validationResultText = '(' + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + ')' +
                        '<span class="icon-flag6" style="margin-left:10px; display:inline-block; vertical-align:top;"></span>';
                    break;
                case 'suspect':
                    validationResultText = '(' + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + ')' +
                        '<span class="icon-flag5" style="margin-left:10px; display:inline-block; vertical-align:top; color:red;"></span>';
                    break;
                case 'ok':
                    validationResultText = '(' + Uni.I18n.translate('reading.validationResult.notsuspect', 'IMT', 'Not suspect') + ')';
                    if (record.get('action') == 'WARN_ONLY') {
                        validationResultText += '<span class="icon-flag5" style="margin-left:10px; color:#dedc49;"></span>';
                    }
                    break;
            }
        }

        unitOfMeasure = unitOfMeasure ? unitOfMeasure : '';
        validationResultText += estimatedByRule ? me.getEstimationFlagWithTooltip(estimatedByRule, record) : '';
        if (editedAndProjected) {
            validationResultText = '<span style="margin-left:5px; font-weight:bold; cursor: default" data-qtip="'
                + Uni.I18n.translate('reading.estimated.projected', 'IMT', 'Projected') + '">P</span>';
        }
        if (!Ext.isEmpty(value)) {
            return value + ' ' + unitOfMeasure + ' ' + validationResultText;
        } else {
            return Uni.I18n.translate('general.missingx', 'IMT', 'Missing {0}', [validationResultText], false);
        }
    },

    renderValidationResult: function (validationResult, record) {
        var me = this,
            validationResultText = '',
            estimatedByRule;

        if (!record) {
            return validationResultText;
        }

        if (record.get('isConfirmed')) {
            validationResultText = Uni.I18n.translate('reading.validationResult.notsuspect', 'IMT', 'Not suspect');
            validationResultText += '<span class="icon-checkmark" style="margin-left:10px; position:absolute;"></span>';
            return validationResultText;
        }

        switch (validationResult.split('.')[1]) {
            case 'notValidated':
                validationResultText = Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') +
                    '<span class="icon-flag6" style="margin-left:10px; display:inline-block; vertical-align:top;"></span>';
                break;
            case 'suspect':
                validationResultText = Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') +
                    '<span class="icon-flag5" style="margin-left:10px; display:inline-block; vertical-align:top; color:red;"></span>';
                break;
            case 'ok':
                validationResultText = Uni.I18n.translate('reading.validationResult.notsuspect', 'IMT', 'Not suspect');
                if (record.get('isConfirmed')) {
                    validationResultText += '<span class="icon-checkmark" style="margin-left:10px; position:absolute;"></span>';
                } else if (record.get('action') == 'WARN_ONLY') {
                    validationResultText += '<span class="icon-flag5" style="margin-left:10px; color:#dedc49;"></span>';
                }
                break;
        }

        estimatedByRule = record.get('estimatedByRule');
        validationResultText += estimatedByRule ? me.getEstimationFlagWithTooltip(estimatedByRule, record) : '';
        return validationResultText;
    },

    renderDataQualityFields: function (deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities) {
        var me = this,
            showDeviceQuality = false,
            showMultiSenseQuality = false,
            showInsightQuality = false,
            show3rdPartyQuality = false,
            field = undefined;

        deviceQualityField.setValue('');
        multiSenseQualityField.setValue('');
        insightQualityField.setValue('');
        thirdPartyQualityField.setValue('');

        if (!Ext.isEmpty(dataQualities)) {
            dataQualities.sort(function (a, b) {
                if (a.indexName > b.indexName) {
                    return 1;
                }
                if (a.indexName < b.indexName) {
                    return -1;
                }
                return 0;
            });
            Ext.Array.forEach(dataQualities, function (readingQuality) {
                switch (readingQuality.cimCode.slice(0, 2)) {
                    case '1.':
                        showDeviceQuality |= true;
                        field = deviceQualityField;
                        break;
                    case '2.':
                        showMultiSenseQuality |= true;
                        field = multiSenseQualityField;
                        break;
                    case '3.':
                        showInsightQuality |= true;
                        field = insightQualityField;
                        break;
                    case '4.':
                    case '5.':
                        show3rdPartyQuality |= true;
                        field = thirdPartyQualityField;
                        break;
                }
                if (!Ext.isEmpty(field)) {
                    field.setValue(field.getValue()
                        + (Ext.isEmpty(field.getValue()) ? '' : '<br>')
                        + '<span style="display:inline-block; float: left; margin-right:7px;" >' + readingQuality.indexName + ' (' + readingQuality.cimCode + ')' + '</span>'
                        + '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="'
                        + me.getTooltip(readingQuality.systemName, readingQuality.categoryName, readingQuality.indexName) + '"></span>'
                    );
                }
            });
        }

        Ext.suspendLayouts();
        deviceQualityField.setVisible(showDeviceQuality);
        multiSenseQualityField.setVisible(showMultiSenseQuality);
        insightQualityField.setVisible(showInsightQuality);
        thirdPartyQualityField.setVisible(show3rdPartyQuality);
        Ext.resumeLayouts(true);
    },

    /**
     * @private
     */
    getTooltip: function (systemName, categoryName, indexName) {
        var tooltip = '<table><tr><td>';
        tooltip += '<b>' + Uni.I18n.translate('general.system', 'IMT', 'System') + ':</b></td>';
        tooltip += '<td>' + systemName + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.category', 'IMT', 'Category') + ':</b></td>';
        tooltip += '<td>' + categoryName + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.index', 'IMT', 'Index') + ':</b></td>';
        tooltip += '<td>' + indexName + '</td></tr></table>';
        return tooltip;
    },

    /**
     * @private
     */
    getEstimationFlagWithTooltip: function (estimatedByRule, record) {
        var icon, tooltip;
        if(Ext.isEmpty(estimatedByRule.application.name)) {
            tooltip = Uni.I18n.translate('reading.estimatedWithTime', 'IMT', 'Estimated on {0}', Uni.DateTime.formatDateTimeLong(new Date(estimatedByRule.when)));
        } else {
            tooltip = Uni.I18n.translate('reading.estimatedWithApplicationAndTime', 'IMT', 'Estimated in {0} on {1}', [
                estimatedByRule.application.name,
                Uni.DateTime.formatDateTimeLong(new Date(estimatedByRule.when))], false);
        }
        icon = '<span class="icon-flag5" style="margin-left:10px; color:#33CC33;" data-qtip="'
            + tooltip + '"></span>';
        if (record.get('isProjected') === true) {
            icon += '<span style="margin-left:3px; font-weight:bold; cursor: default" data-qtip="'
                + Uni.I18n.translate('reading.estimated.projected', 'IMT', 'Projected') + '">P</span>';
        }
        return icon;
    }
});
