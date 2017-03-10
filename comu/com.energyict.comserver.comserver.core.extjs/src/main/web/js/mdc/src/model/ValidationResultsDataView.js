/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ValidationResultsDataView', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ValidationResultsLoadProfile'
    ],
    fields: [
        {
            name: 'validationStatus'
        },
        {
            name: 'allDataValidated',
            convert: function (value, record) {
                return record.get('validationStatus').allDataValidated;
            }
        },
        {
            name: 'isActive',
            convert: function (value, record) {
                return record.get('validationStatus').isActive;
            }
        },
        {
            name: 'allDataValidatedDisplay',
            convert: function (value, record) {
                if (record.get('validationStatus').allDataValidated) {
                    return Uni.I18n.translate('validationResults.dataValidatedYes', 'MDC', 'Yes');
                }
                return Uni.I18n.translate('validationResults.dataValidatedNo', 'MDC', 'No');
            }
        },
        {
            name: 'total',
            convert: function (value, record) {
                return Ext.String.format(Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects'), value);
            }
        },
        {
            name: 'detailedValidationLoadProfile'
        },
        {
            name: 'detailedValidationRegister'
        }
    ],

    associations: [
        {
            name: 'detailedValidationLoadProfile',
            type: 'hasMany',
            model: 'Mdc.model.ValidationResultsLoadProfile',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
        {
            name: 'detailedValidationRegister',
            type: 'hasMany',
            model: 'Mdc.model.ValidationResultsRegister',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
});
