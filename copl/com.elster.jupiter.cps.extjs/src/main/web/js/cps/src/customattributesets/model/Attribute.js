/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.model.Attribute', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean'},
        {name: 'order', type: 'integer'},
        {name: 'customAttributeType', type: 'auto'},
        {name: 'defaultValue', type: 'string'},
        {name: 'description', type: 'string'}
    ]
});
