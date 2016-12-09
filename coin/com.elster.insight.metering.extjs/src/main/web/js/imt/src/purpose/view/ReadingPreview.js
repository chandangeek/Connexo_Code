Ext.define('Imt.purpose.view.ReadingPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.reading-preview',
    requires: [
        'Cfg.view.field.ReadingQualities'        
    ],
    outputType: null,
    output: null,    
    frame: false,

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('readingTime'),
            dataQualities = record.get('readingQualities'),
            title;

        switch(me.output.get('outputType')){
            case 'channel': {
                title = Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',
                    [Uni.DateTime.formatDateLong(intervalEnd), Uni.DateTime.formatTimeShort(intervalEnd)],
                    false);
            } break;
            case 'register':{
                title =  Uni.DateTime.formatDateTimeShort(new Date(record.get('timeStamp')));
            } break;
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
            validationResultText = '';            

        if (validationResult) {
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
                    break;
            }
        }

        if (!Ext.isEmpty(value)) {            
            return value + ' ' + unitOfMeasure + ' ' + validationResultText;
        } else {            
            return Uni.I18n.translate('general.missingx', 'IMT', 'Missing {0}', [validationResultText], false);            
        }
    },

    getValidationResult: function (validationResult) {
        var validationResultText = '';

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
                break;
        }

        return validationResultText;
    },

    setDataQualityFields: function(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities) {
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
            Ext.Array.forEach(dataQualities, function (readingQuality) {
                if (Ext.String.startsWith(readingQuality.cimCode, '1.')) {
                    showDeviceQuality |= true;
                    field = deviceQualityField;
                } else if (Ext.String.startsWith(readingQuality.cimCode, '2.')) {
                    showMultiSenseQuality |= true;
                    field = multiSenseQualityField;
                } else if (Ext.String.startsWith(readingQuality.cimCode, '3.')) {
                    showInsightQuality |= true;
                    field = insightQualityField;
                } else if (Ext.String.startsWith(readingQuality.cimCode, '4.') || Ext.String.startsWith(readingQuality.cimCode, '5.')) {
                    show3rdPartyQuality |= true;
                    field = thirdPartyQualityField;
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

        deviceQualityField.setVisible(showDeviceQuality);
        multiSenseQualityField.setVisible(showMultiSenseQuality);
        insightQualityField.setVisible(showInsightQuality);
        thirdPartyQualityField.setVisible(show3rdPartyQuality);
    },

    getTooltip: function(systemName, categoryName, indexName) {
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

        switch(me.output.get('outputType')){
            case 'channel': {
                generalTimeField = {
                    fieldLabel: Uni.I18n.translate('general.interval', 'IMT', 'Interval'),
                    name: 'interval',
                    itemId: 'interval-field',
                    renderer: function (value) {
                        return value
                            ? Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateLong(new Date(value.start)), Uni.DateTime.formatTimeLong(new Date(value.start))])
                        + ' - ' +
                        Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateLong(new Date(value.end)), Uni.DateTime.formatTimeLong(new Date(value.end))])
                            : '-';
                    },
                    htmlEncode: false
                }
            } break;
            case 'register':{
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
            } break;
        }

        generalItems.push(
            generalTimeField,
            {
                fieldLabel: Uni.I18n.translate('reading.readingTime', 'IMT', 'Reading time'),
                name: 'readingTime',
                itemId: 'reading-time-field',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))]) : '-';
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
                renderer: function (value) {
                    return me.setValueWithResult(value);
                }
            },
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
                withOutAppName: true
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
                fieldLabel: Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality'),
                itemId: 'device-quality'
            },
            {                
                fieldLabel: Uni.I18n.translate('general.MDCQuality', 'MDC', 'MDC quality'),
                itemId: 'multiSense-quality'
            },
            {                
                fieldLabel: Uni.I18n.translate('general.MDMQuality', 'MDC', 'MDM quality'),
                itemId: 'insight-quality'
            },
            {                
                fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'MDC', 'Third party quality'),
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