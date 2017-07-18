/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RegisterMapping', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'isLinkedByDeviceType', type: 'boolean', useNull: true},
        {name: 'isLinkedByActiveRegisterConfig', type: 'boolean', useNull: true},
        {name: 'isLinkedByInactiveRegisterConfig', type: 'boolean', useNull: true},
        'readingType',
        {name: 'name', type: 'string', persist: false, mapping: 'readingType.fullAliasName'}
    ],
    associations: [
        {
            name: 'readingType',
            associationKey: 'readingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType',
            foreignKey: 'readingType'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceType}/registertypes',
        setUrl: function (deviceType) {
            this.url = this.urlTpl.replace('{deviceType}', encodeURIComponent(deviceType));
        }
    }
});