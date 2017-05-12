/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ResponseSecurityLevel', {
    extend: 'Ext.data.Model',
    statics: {
        noResponseSecurity: function()  {
            var level = new this();
            level.set('id', -1);
            level.set('name', Uni.I18n.translate('ResponseSecurityLevel.noResponseSecurity', 'MDC', 'No response security'));
            return level;
        }
    },
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true}
    ]
});