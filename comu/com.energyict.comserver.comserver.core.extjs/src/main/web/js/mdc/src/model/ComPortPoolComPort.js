/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ComPortPoolComPort', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'direction',
        'comServer_id',
        'comServerName',
        'comPortType',
        'description',
        'active',
        'bound',
        {name:'numberOfSimultaneousConnections',type:'int', defaultValue: 1},
        {name: 'type', type: 'string'},
        {name:'comPortPool_id',type: 'auto',useNull: true, defaultValue: null}
    ]
});
