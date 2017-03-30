/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RegisterConfiguration', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'obisCodeDescription', type: 'string', useNull: true},
        {name: 'numberOfFractionDigits', type: 'number', useNull: true},
        {name: 'overflow', type: 'number', useNull: true},
        {name: 'registerType', type:'number', useNull: true},
        {name: 'readingType', persist:false},
        {name: 'collectedReadingType', persist:false},
        {name: 'calculatedReadingType'},
        {name: 'possibleCalculatedReadingTypes', persist:false},
        {
            name: 'registerTypeName',
            type: 'string',
            persist: false,
            convert: function registerTypeName(valueAsReadByReader, record){
                if (record.get('readingType')) {
                    return record.get('readingType').fullAliasName;
                }
            }
        },
        {name: 'asText', type:'boolean'},
        {name: 'useMultiplier', type:'boolean'}
    ],
    associations: [
        {
            name: 'readingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'readingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType',
            foreignKey: 'readingType'
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
            name: 'possibleCalculatedReadingTypes',
            type: 'hasMany',
            model: 'Mdc.model.ReadingType',
            associationKey: 'possibleCalculatedReadingTypes',
            getterName: 'getPossibleCalculatedReadingTypes',
            setterName: 'setPossibleCalculatedReadingTypes',
            foreignKey: 'possibleCalculatedReadingTypes'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/registerconfigurations'
    }
});