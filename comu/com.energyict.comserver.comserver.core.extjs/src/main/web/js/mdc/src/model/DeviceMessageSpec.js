/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceMessageSpec', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],
    idProperty: 'id',
    fields: [
        {name: 'id', persist: false, useNull: true},
        {name: 'willBePickedUpByComTask', type: 'boolean', persist: false, useNull: true},
        {name: 'willBePickedUpByPlannedComTask', type: 'boolean', persist: false, useNull: true},
        {name: 'name', persist: false},
        'properties'
    ],
    hasMany: [{
        name: 'properties',
        model: 'Uni.property.model.Property'
    }]
});