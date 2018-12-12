/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.ValidationInfo', {
    extend: 'Ext.data.Model',
    requires: ['Imt.usagepointmanagement.model.SuspectReason'],
    fields: [
        {name: 'allDataValidated', type: 'auto'},
        {name: 'hasSuspects', type: 'auto'},
        {name: 'validationActive', type: 'auto'},
        {name: 'lastChecked', type: 'auto'}
    ],
    hasMany: [{
        model: 'Imt.usagepointmanagement.model.SuspectReason',
        name: 'suspectReason',
        foreignKey: 'suspectReason',
        associationKey: 'suspectReason'
    }]
});