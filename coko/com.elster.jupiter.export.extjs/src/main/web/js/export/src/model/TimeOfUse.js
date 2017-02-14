/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.TimeOfUse', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'tou', type: 'integer', useNull: true}
    ]
});
