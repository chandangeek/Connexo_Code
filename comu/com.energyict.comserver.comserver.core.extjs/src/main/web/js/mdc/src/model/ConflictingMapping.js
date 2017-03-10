/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ConflictingMapping', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.ConnectionMethodsAndSecuritySets',
        'Mdc.model.SolutionConnectionMethod',
        'Mdc.model.SolutionSecuritySet'
    ],
    fields: [
        {name: 'fromConfiguration', type: "auto"},
        {name: 'toConfiguration', type: "auto"},
        {name: 'isSolved'},
        {name: 'solved', type: "auto"},
        {name: 'id', type: 'int'}
    ],
    associations: [
        {
            name: 'connectionMethods',
            type: 'hasMany',
            model: 'Mdc.model.ConnectionMethodsAndSecuritySets',
            associationKey: 'connectionMethods',
            foreignKey: 'connectionMethods'
        },
        {
            name: 'securitySets',
            type: 'hasMany',
            model: 'Mdc.model.ConnectionMethodsAndSecuritySets',
            associationKey: 'securitySets',
            foreignKey: 'securitySets'
        },
        {
            name: 'connectionMethodSolutions',
            type: 'hasMany',
            model: 'Mdc.model.SolutionConnectionMethod',
            associationKey: 'connectionMethodSolutions',
            foreignKey: 'connectionMethodSolutions'
        },
        {
            name: 'securitySetSolutions',
            type: 'hasMany',
            model: 'Mdc.model.SolutionSecuritySet',
            associationKey: 'securitySetSolutions',
            foreignKey: 'securitySetSolutions'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceType}/conflictmappings',
        reader: {
            type: 'json'
        },
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceType}', encodeURIComponent(deviceTypeId));
        }
    }
});