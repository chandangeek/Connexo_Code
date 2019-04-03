/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.model.CreationRuleActionPhase', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uuid',
            type: 'string'
        },
        {
            name: 'title',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        }
    ],

    idProperty: 'uuid'
});