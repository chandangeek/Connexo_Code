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
            title;

        switch (me.output.get('outputType')) {
            case 'channel':
            {
                title = Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',
                    [Uni.DateTime.formatDateLong(intervalEnd), Uni.DateTime.formatTimeShort(intervalEnd)],
                    false);
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
        me.down('#general-panel').loadRecord(record);
        me.down('#values-panel').loadRecord(record);
        me.down('#formula-field').setValue(me.output.get('formula').description);
        Ext.resumeLayouts(true);
    },

    setValueWithResult: function (value) {
        var me = this,
            record = me.down('form').getRecord(),
            validationResult = record.get('validationResult'),
            readingType = me.output.get('readingType'),
            unitOfMeasure = readingType.names ? readingType.names.unitOfMeasure : readingType.unit,
            validationResultText = '';

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
        var me = this,
            validationResultText = '',
            record = me.down('form').getRecord();
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
                }

                break;
        }

        return validationResultText;
    },

    initComponent: function () {
        var me = this,
            generalItems = [],
            valuesItems = [],
            generalTimeField;

        switch (me.output.get('outputType')) {
            case 'channel':
            {
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
            }
                break;
            case 'register':
            {
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
                break;
        }

        generalItems.push(
            generalTimeField,
            {
                fieldLabel: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                name: 'reportedDateTime',
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
                    } else if (value.estimatedByRule) {
                        return this.getEstimatedByRule(value.estimatedByRule);
                    } else {
                        field.hide();
                    }
                }
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
            }
        ];
        me.callParent(arguments);
    }
});