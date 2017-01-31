/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.model.task.Priority', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'value',
        'label'
    ],
    idProperty: 'name'
});