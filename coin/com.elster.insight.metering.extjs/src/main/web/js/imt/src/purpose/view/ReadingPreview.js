/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.ReadingPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.reading-preview',
    requires: [
        'Cfg.view.field.ReadingQualities'
    ],
    outputType: null,
    output: null,
    withOutAppName: false,
    frame: false,

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('readingTime'),
            dataQualities = record.get('readingQualities'),
            title;

        switch (me.output.get('outputType')) {
            case 'channel':
            {
                title = Uni.DateTime.formatDateTime(intervalEnd,'long','short')
            }
                break;
            case 'register':
            {
                title = Uni.DateTime.formatDateTimeShort(new Date(record.get('timeStamp')));
            }
                break;
        }


        Ext.suspendLayouts();
        me.down('#general-panel').setTitle(title);
        me.down('#values-panel').setTitle(me.output.get('name'));
        me.down('#qualities-panel').setTitle(title);
        me.down('#general-panel').loadRecord(record);
        me.down('#values-panel').loadRecord(record);
        me.down('#formula-field').setValue(me.output.get('formula').description);
        me.down('#noReadings-msg').setVisible(Ext.isEmpty(dataQualities));
        me.setDataQualityFields(me.down('#device-quality'), me.down('#multiSense-quality'), me.down('#insight-quality'), me.down('#thirdParty-quality'), dataQualities);
        Ext.resumeLayouts(true);
    },

    setValueWithResult: function (value) {
        var me = this,
            record = me.down('form').getRecord(),
            validationResult = record.get('validationResult'),
            readingType = me.output.get('readingType'),
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
                        validationResultText += '<span class="icon-flag5" style="margin-left:10px; color:#dedc49;"></span>';
                    break;
            }
        }

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

    getValidationResult: function (validationResult) {
        var me = this,
            validationResultText = '',
            record = me.down('form').getRecord(),
            estimatedByRule,
            editedAndProjected;

        if (record) {
            estimatedByRule = record.get('estimatedByRule');
            editedAndProjected = record.get('modificationFlag') && record.get('modificationDate') && record.get('isProjected') === true
        }

        if (!Ext.isEmpty(record) && record.get('isConfirmed')) {
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

        validationResultText += estimatedByRule ? me.getEstimationFlagWithTooltip(estimatedByRule, record) : '';
        return validationResultText;
    },

    getEstimationFlagWithTooltip: function (estimatedByRule, record) {
        var icon;

        icon = '<span class="icon-flag5" style="margin-left:10px; color:#33CC33;" data-qtip="'
            + Uni.I18n.translate('reading.estimatedWithTime', 'IMT', 'Estimated in {0} on {1} at {2}', [
                estimatedByRule.application.name,
                Uni.DateTime.formatDateLong(new Date(estimatedByRule.when)),
                Uni.DateTime.formatTimeLong(new Date(estimatedByRule.when))
            ], false) + '"></span>';
        if (record.get('isProjected') === true) {
            icon += '<span style="margin-left:3px; font-weight:bold; cursor: default" data-qtip="'
                + Uni.I18n.translate('reading.estimated.projected', 'IMT', 'Projected') + '">P</span>';
        }

        return icon;
    },

    setDataQualityFields: function (deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities) {
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

    initComponent: function () {
        var me = this,
            generalItems = [],
            valuesItems = [],
            qualityItems = [],
            generalTimeField;

        switch (me.output.get('outputType')) {
            case 'channel':
            {
                generalTimeField = {
                    fieldLabel: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                    name: 'interval',
                    itemId: 'interval-field',
                    renderer: function (value) {
                        return value
                            ? Uni.DateTime.formatDateTimeLong(new Date(value.start))
                        + ' - ' +
                        Uni.DateTime.formatDateTimeLong(new Date(value.end))
                            : '-';
                    },
                    htmlEncode: false
                }
            } break;
            case 'register':{
                if((me.output.get('deliverableType')==='numerical' || me.output.get('deliverableType')==='billing') && (me.output.get('isCummulative') || me.output.get('isBilling'))){
                    generalTimeField = {
                        fieldLabel: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                        name: 'interval',
                        itemId: 'interval-field',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value) && !!value.start) {
                                return value
                                    ? Uni.DateTime.formatDateTimeLong(new Date(value.start))
                                + ' - ' +
                                Uni.DateTime.formatDateTimeLong(new Date(value.end))
                                    : '-';
                            } else if (!Ext.isEmpty(value) && !!value.end){
                                return Uni.DateTime.formatDateLong(new Date(value.end))
                            }
                            return '-';
                        },
                        htmlEncode: false
                    }
                } else if (!me.output.get('hasEvent')){
                    generalTimeField = {
                        fieldLabel: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                        name: 'timeStamp',
                        itemId: 'measurement-time-field',
                        renderer: function (value) {
                            return value
                                ? Uni.DateTime.formatDateTimeShort(new Date(value))
                                : '-';
                        }
                    }
                }
            }
                break;
        }
        generalItems.push(generalTimeField);

        if(me.output.get('hasEvent')){
            generalItems.push(
                {
                    fieldLabel: Uni.I18n.translate('device.registerData.eventTime', 'IMT', 'Event time'),
                    dataIndex: 'eventDate',
                    itemId: 'eventTime',
                    renderer: function (value) {
                        return value
                            ? Uni.DateTime.formatDateTimeLong(new Date(value))
                            : '-';
                    }
                }
            );
        }


        generalItems.push(
            {
                fieldLabel: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                name: 'reportedDateTime',
                itemId: 'reading-time-field',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('reading.dataValidated.title', 'IMT', 'Data validated'),
                name: 'dataValidated',
                itemId: 'data-validated-field',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                fieldLabel: Uni.I18n.translate('reading.validationResult', 'IMT', 'Validation result'),
                name: 'validationResult',
                itemId: 'validation-result-field',
                renderer: function (value) {
                    return me.getValidationResult(value);
                }
            }
        );

        valuesItems.push(
            {
                fieldLabel: Uni.I18n.translate('general.value', 'IMT', 'Value'),
                name: 'value',
                itemId: 'reading-value-field',
                width: 400,
                renderer: function (value) {
                    return me.setValueWithResult(value);
                }
            });
        if(me.output.get('isCummulative')){
            valuesItems.push({
                fieldLabel: Uni.I18n.translate('device.registerData.deltaValue', 'IMT', 'Delta value'),
                dataIndex: 'deltaValue'
            })
        }
        valuesItems.push(
            {
                fieldLabel: Uni.I18n.translate('general.formula', 'IMT', 'Formula'),
                itemId: 'formula-field'
            },
            {
                xtype: 'reading-qualities-field',
                router: me.router,
                itemId: 'reading-qualities-field',
                usedInInsight: true,
                name: 'validationRules',
                withOutAppName: me.withOutAppName,
                renderer: function (value, field) {
                    var rec = field.up('form').getRecord(),
                        validationRules = Ext.isArray(value) ? value : value.validationRules;
                    field.show();
                    if (rec.get('isConfirmed')) {
                        return this.getConfirmed(value.confirmedInApps);
                    } else if (!Ext.isEmpty(validationRules)) {
                        var valueToRender = this.getValidationRules(validationRules);
                        if (Ext.isEmpty(valueToRender)) {
                            field.hide();
                        }
                        return valueToRender;
                    } else if (!Ext.isEmpty(rec.get('estimatedByRule'))) {
                        return this.getEstimatedByRule(rec.get('estimatedByRule'));
                    } else {
                        field.hide();
                    }
                }
            }
        );

        valuesItems.push(
            {
                itemId: 'estimation-comment-field',
                name: 'mainCommentValue',
                fieldLabel: Uni.I18n.translate('general.estimationComment', 'IMT', 'Estimation comment'),
                renderer: function (value) {
                    if (!value) {
                        this.hide();
                    } else {
                        this.show();
                        return value;
                    }
                }
            }
        );

        qualityItems.push(
            {
                xtype: 'uni-form-info-message',
                itemId: 'noReadings-msg',
                text: Uni.I18n.translate('general.noDataQualitiesMsg', 'IMT', 'There are no reading qualities for this data.'),
                padding: '10'
            },
            {
                fieldLabel: Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality'),
                itemId: 'device-quality'
            },
            {
                fieldLabel: Uni.I18n.translate('general.MDCQuality', 'IMT', 'MDC quality'),
                itemId: 'multiSense-quality'
            },
            {
                fieldLabel: Uni.I18n.translate('general.MDMQuality', 'IMT', 'MDM quality'),
                itemId: 'insight-quality'
            },
            {
                fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'IMT', 'Third party quality'),
                itemId: 'thirdParty-quality'
            }
        );

        me.items = [
            {
                title: Uni.I18n.translate('reading.generaltab.title', 'IMT', 'General'),
                itemId: 'general-tab',
                items: {
                    xtype: 'form',
                    itemId: 'general-panel',
                    frame: true,
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: generalItems
                }
            },
            {
                title: Uni.I18n.translate('reading.readingvaluetab.title', 'IMT', 'Reading value'),
                itemId: 'reading-value-tab',
                items: {
                    xtype: 'form',
                    itemId: 'values-panel',
                    frame: true,
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: valuesItems
                }
            },
            {
                title: Uni.I18n.translate('general.readingQuality', 'IMT', 'Reading quality'),
                itemId: 'qualities-tab',
                items: {
                    xtype: 'form',
                    itemId: 'qualities-panel',
                    frame: true,
                    items: qualityItems,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200,
                        htmlEncode: false
                    },
                    layout: 'vbox'
                }
            }
        ];
        me.callParent(arguments);
    }
});