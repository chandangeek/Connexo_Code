Ext.define('Imt.registerdata.view.RegisterDataValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.registerDataValidationPreview',
    itemId: 'registerDataValidationPreview',
    requires:[
        'Cfg.privileges.Validation'
    ],

    fieldLabel: Uni.I18n.translate('registerdata.validation', 'IMT', 'Validation'),
    labelAlign: 'top',
    layout: 'vbox',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('registerdata.validationStatus', 'IMT', 'Validation status'),
            name: 'validationStatus',
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.active', 'IMT', 'Active') : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
            }
        },
        {
            fieldLabel: Uni.I18n.translate('registerdata.dataValidated', 'IMT', 'Data validated'),
            name: 'dataValidated',
            htmlEncode: false,
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes')
                    : Uni.I18n.translate('general.no', 'IMT', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
            }
        },
        {
            fieldLabel: Uni.I18n.translate('registerdata.validationResult', 'IMT', 'Validation result'),
            name: 'validationResult',
            renderer: function (value, field) {
                if (!Ext.isEmpty(value)) {
                    field.show();
                    switch (value) {
                        case 'validationStatus.notValidated':
                            field.hide();
                            break;
                        case 'validationStatus.ok':
                            if (field.up('form').getRecord().get('isConfirmed')) {
                                return Uni.I18n.translate('general.notSuspect', 'IMT', 'Not suspect') + ' ' + '<span style="margin-left: 5px; vertical-align: top" class="icon-checkmark3"</span>';
                            } else {
                                return Uni.I18n.translate('general.notSuspect', 'IMT', 'Not suspect');
                            }
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
            fieldLabel: Uni.I18n.translate('general.readingQualities', 'IMT', 'Reading qualities'),
            name: 'suspectReason',
            renderer: function (value, field) {
                if (field.up('form').getRecord() && field.up('form').getRecord().get('isConfirmed')) {
                    field.show();
                    return Uni.I18n.translate('general.confirmed', 'IMT', 'Confirmed');
                } else if (!Ext.isEmpty(value)) {
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
                                    prop = ' - ' + Uni.I18n.translate('registerdata.failEqualData', 'IMT', 'Fail equal data') + ': ' + failEqualDataValue;
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
                                    prop = ' - ' + Uni.I18n.translate('registerdata.intervalFlags', 'IMT', 'Interval flags') + ': ' + intervalFlagsValue;
                                    break;
                                default:
                                    prop = '';
                                    break;
                            }
                        } else {
                            prop = '';
                        }
                        if (rule.deleted) {
                            str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">' + rule.name + ' ' + Uni.I18n.translate('registerdata.removedRule', 'IMT', '(removed rule)') + prop + '</span>' + '<br>';
                        } else {
                            str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">';
                            if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                                str += '<a href="#/administration/validation/rulesets/' + rule.ruleSetVersion.ruleSet.id + '/versions/' + rule.ruleSetVersion.id + '/rules/' + rule.id + '">' + rule.name + '</a>';
                            } else {
                                str += rule.name;
                            }
                            str += prop + '</span>' + '<br>';
                        }
                    });
                    return str;
                } else {
                    field.hide();
                    return '';
                }
            }
        }
    ]
});
