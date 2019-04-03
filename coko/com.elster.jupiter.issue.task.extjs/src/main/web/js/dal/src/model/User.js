/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.model.User', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    fields: [
        {
            name: 'id',
            displayValue: Uni.I18n.translate('general.id','ITK','ID'),
            type: 'int'
        },
        {
            name: 'type',
            displayValue: Uni.I18n.translate('general.type','ITK','Type'),
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: Uni.I18n.translate('general.name','ITK','Name'),
            type: 'auto'
        }
    ],

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/users',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});