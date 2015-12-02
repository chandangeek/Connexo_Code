Ext.define('Imt.view.setup.devicechannels.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceloadprofilechannelspreview-validation',
    itemId: 'deviceloadprofilechannelspreviewvalidation',

    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation', 'IMT', 'Validation'),
    labelAlign: 'top',
    layout: 'vbox',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('device.channelData.validationStatus', 'IMT', 'Validation status'),
            name: 'validationStatus',
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.active', 'IMT', 'Active') : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive');
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.registerData.dataValidated', 'IMT', 'Data validated'),
            name: 'dataValidated',
            htmlEncode: false,
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes')
                    : Uni.I18n.translate('general.no', 'IMT', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'IMT', 'Validation result'),
            name: 'validationResult',
            renderer: function (value, field) {
                if (!Ext.isEmpty(value)) {
                    field.show();
                    switch (value) {
                        case 'validationStatus.notValidated':
                            field.hide();
                            break;
                        case 'validationStatus.ok':
                            return Uni.I18n.translate('general.notSuspect', 'IMT', 'Not suspect');
                            break;
                        case 'validationStatus.suspect':
                            return Uni.I18n.translate('validationStatus.suspect', 'IMT', 'Suspect') + ' ' + '<span class="icon-validation icon-validation-red"></span>';
                            break;
                        default:
                            field.hide();
                            break;
                    }
                } else {
                    field.hide();
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.channelData.failedValidationRules', 'IMT', 'Failed validation rules'),
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
                                        failEqualDataValue = Uni.I18n.translate('general.yes', 'IMT', 'Yes');
                                    } else {
                                        failEqualDataValue = Uni.I18n.translate('general.no', 'IMT', 'No');
                                    }
                                    prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'IMT', 'Fail equal data') + ': ' + failEqualDataValue;
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
                                    prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'IMT', 'Interval flags') + ': ' + intervalFlagsValue;
                                    break;
                                default:
                                    prop = '';
                                    break;
                            }
                        } else {
                            prop = '';
                        }
                        if (rule.deleted) {
                            str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">' + rule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'IMT', '(removed rule)') + prop + '</span>' + '<br>';
                        } else {
                            str = '<span style="word-wrap: break-word; display: inline-block; width: 800px">';
                            if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                                str += '<a href="#/administration/validation/rulesets/' + rule.ruleSetVersion.id + '/rules/' + rule.id + '">' + rule.name + '</a>';
                            } else {
                                str += rule.name;
                            }
                            str += prop + '</span>' + '<br>';
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
