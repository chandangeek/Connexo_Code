/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.AutoclosureExclusionIssueType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uid',
            type: 'text'
        },
        {
            name: 'name',
            type: 'text'
        }
    ],

    idProperty: 'uid',

});