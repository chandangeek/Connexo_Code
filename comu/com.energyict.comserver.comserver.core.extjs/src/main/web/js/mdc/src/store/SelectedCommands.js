/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.SelectedCommands',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Command'
    ],
    model: 'Mdc.model.Command',
    autoLoad: false
});
