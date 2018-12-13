/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.Channel', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.RegisterType',
        'Mdc.model.MeasurementType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'overflowValue', type: 'integer', useNull: true},
        {
            name: 'measurementType',
            persist: false
        },
        {   name: 'readingType',
            persist: false,
            mapping: 'measurementType.readingType'
        },
        'collectedReadingType',
        'calculatedReadingType',
        'multipliedCalculatedReadingType',
        'possibleCalculatedReadingTypes',
        'useMultiplier',
        {name: 'loadProfileId', type: 'number', useNull: true},
        {name: 'loadProfileName', type: 'string', useNull: true},
        {name: 'nbrOfFractionDigits', type: 'int'},
        {
            name: 'registerTypeName',
            type: 'string',
            persist: false,
            mapping: 'measurementType.readingType.fullAliasName'
        },
        {
            name: 'extendedChannelName',
            type: 'string',
            persist: false,
            convert: function extendedChannelName(valueAsReadByReader, record){
                return record.get('readingType').fullAliasName + ' - ' + record.get('loadProfileName');
            }
        }
    ],
    idProperty: 'id',
    associations: [
        {
            name: 'measurementType',
            type: 'hasOne',
            model: 'Mdc.model.MeasurementType',
            associationKey: 'measurementType',
            getterName: 'getMeasurementType',
            setterName: 'setMeasurementType',
            foreignKey: 'measurementType'
        },
        {
            name: 'collectedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'collectedReadingType',
            getterName: 'getCollectedReadingType',
            setterName: 'setCollectedReadingType',
            foreignKey: 'collectedReadingType'
        },
        {
            name: 'calculatedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'calculatedReadingType',
            getterName: 'getCalculatedReadingType',
            setterName: 'setCalculatedReadingType',
            foreignKey: 'calculatedReadingType'
        },
        {
            name: 'multipliedCalculatedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'multipliedCalculatedReadingType',
            getterName: 'getMultipliedCalculatedReadingType',
            setterName: 'setMultipliedCalculatedReadingType',
            foreignKey: 'multipliedCalculatedReadingType'
        },
        {
            name: 'possibleCalculatedReadingTypes',
            associationKey: 'possibleCalculatedReadingTypes',
            type: 'hasMany',
            model: 'Mdc.model.ReadingType',
            foreignKey: 'possibleCalculatedReadingTypes'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/loadprofileconfigurations/{loadProfileConfig}/channels'
    }
});
