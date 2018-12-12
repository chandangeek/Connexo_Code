/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.model.Transition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'transitionNow', type: 'auto'}
    ],
    associations: [
        {name: 'customPropertySets', type: 'hasMany', model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject', associationKey: 'customPropertySets', foreignKey: 'customPropertySets'}
    ]
});
