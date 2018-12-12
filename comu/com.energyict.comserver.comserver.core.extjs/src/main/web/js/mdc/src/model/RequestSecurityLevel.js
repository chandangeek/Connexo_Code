/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RequestSecurityLevel', {
    extend: 'Ext.data.Model',
    statics: {
        noRequestSecurity: function()  {
            var level = new this();
            level.set('id', -1);
            level.set('name', Uni.I18n.translate('RequestSecurityLevel.noRequestSecurity', 'MDC', 'No request security'));
            return level;
        }
    },
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true}
    ]
});