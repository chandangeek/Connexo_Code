/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.service.UsagePointGroupSearch', {
    extend: 'Imt.service.Search',
    stateful: false,
    stateId: 'usagePointGroup',
    addProperty: function (property) {
        var me = this,
            excludedCriteria;

        if (Ext.isArray(me.excludedCriteria)) {
            excludedCriteria = me.excludedCriteria;
        } else {
            excludedCriteria = me.excludedCriteria ? [me.excludedCriteria] : [];
        }
        
        if (!Ext.Array.contains(excludedCriteria, property.get('name'))) {
            return me.callParent(arguments);
        }
    }
});