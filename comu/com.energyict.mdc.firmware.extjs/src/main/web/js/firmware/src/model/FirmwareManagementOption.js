/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by pdo on 8/05/2015.
 */
Ext.define('Fwc.model.FirmwareManagementOption', {
    extend: 'Ext.data.Model',
    alternateClassName: 'FirmwareManagementOption',
    alias: 'firmware-management-option',
    constructor: function(id, localizedValue){
        this.id = id;
        this.localizedValue = localizedValue;
    },
    associations: [
        {
            type: 'belongsTo',
            model: 'FirmwareManagementOptions'
        }
    ],
    fields: [
        {
            name: 'id',
            type: 'string',
            useNull: true
        },
        {
            name: 'localizedValue',
            type: 'string',
            useNull: true
        }
    ]

});
