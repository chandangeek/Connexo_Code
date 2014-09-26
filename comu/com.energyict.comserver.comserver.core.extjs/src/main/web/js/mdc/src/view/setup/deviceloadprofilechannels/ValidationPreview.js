Ext.define('Mdc.view.setup.deviceloadprofilechannels.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceloadprofilechannelspreview-validation',
    itemId: 'deviceloadprofilechannelspreviewvalidation',

    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),
    labelAlign: 'top',
    layout: 'vbox',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('device.channelData.validationStatus', 'MDC', 'Validation status'),
            name: 'validationStatus',
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('communicationtasks.task.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('communicationtasks.task.inactive', 'MDC', 'Inactive');
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.channelData.dataValidated', 'MDC', 'Data validated'),
            name: 'dataValidated',
            renderer: function (value) {
                if (value == true) {
                    return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                } else if (value == false) {
                    return Uni.I18n.translate('general.no', 'MDC', 'No');
                } else {
                    return '';
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
            name: 'validationResult',
            renderer: function (value, field) {
                if (!Ext.isEmpty(value)) {
                    field.show();
                    switch (value) {
                        case 'validationStatus.notValidated':
                            return '<span class="icon-validation icon-validation-black"></span>&nbsp;&nbsp;' + ' ' + Uni.I18n.translate('device.registerData.notValidated', 'MDC', 'Not validated');
                            break;
                        case 'validationStatus.ok':
                            return Uni.I18n.translate('validationStatus.ok', 'MDC', 'OK');
                            break;
                        case 'validationStatus.suspect':
                            return '<span class="icon-validation icon-validation-red"></span>&nbsp;&nbsp;' + ' ' + Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect');
                            break;
                        default:
                            return '';
                            break;
                    }
                } else {
                    field.hide();
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.channelData.failedValidationRules', 'MDC', 'Failed validation rules'),
            name: 'suspectReason',
            renderer: function (value, field) {
                if (!Ext.isEmpty(value)) {
                    field.show();
                    var str = '',
                        prop,
                        failEqualDataValue,
                        intervalFlagsValue = '';

                    Ext.Array.each(value, function (rule) {
                        if (!Ext.isEmpty(rule.properties)) {
                            switch (rule.implementation) {
                                case 'com.elster.jupiter.validators.impl.ThresholdValidator':
                                    prop = ' - ' + rule.properties[0].key.charAt(0).toUpperCase() + rule.properties[0].key.substring(1) + ': ' + rule.properties[0].propertyValueInfo.value + ', ' +
                                        rule.properties[1].key.charAt(0).toUpperCase() + rule.properties[1].key.substring(1) + ': ' + rule.properties[1].propertyValueInfo.value;
                                    break;
                                case 'com.elster.jupiter.validators.impl.RegisterIncreaseValidator':
                                    if (rule.properties[0].propertyValueInfo.value) {
                                        failEqualDataValue = Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                                    } else {
                                        failEqualDataValue = Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                    prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'MDC', 'Fail equal data') + ': ' + failEqualDataValue;
                                    break;
                                case 'com.elster.jupiter.validators.impl.IntervalStateValidator':
                                    Ext.Array.each(rule.properties[0].propertyValueInfo.value, function (idValue) {
                                        Ext.Array.each(rule.properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues, function (item) {
                                            if (idValue === item.id) {
                                                intervalFlagsValue += item.name + ', ';
                                            }
                                        });
                                    });
                                    intervalFlagsValue = intervalFlagsValue.slice(0, -2);
                                    prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'MDC', 'Interval flags') + ': ' + intervalFlagsValue;
                                    break;
                                default:
                                    prop = '';
                                    break;
                            }
                        } else {
                            prop = '';
                        }
                        if (rule.name === 'removed rule') {
                            str += Uni.I18n.translate('device.registerData.removedRule', 'MDC', 'removed rule') + '<br>';
                        } else {
                            str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">' + '<a href="#/administration/validation/rulesets/' + rule.ruleSet.id + '/rules/' + rule.id + '">' + rule.name + '</a>' + prop + '</span>' + '<br>';
                        }
                    });
                    return str;
                }
                else {
                    field.hide();
                    return '';
                }
            }

        }
    ]
});
