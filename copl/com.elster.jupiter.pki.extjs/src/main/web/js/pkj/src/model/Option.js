/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.Option', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: false},
        {name: 'name', type: 'string', useNull: false}
    ]
});
