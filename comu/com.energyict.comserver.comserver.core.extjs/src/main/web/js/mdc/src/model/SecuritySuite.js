/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.SecuritySuite', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true}
    ]
});