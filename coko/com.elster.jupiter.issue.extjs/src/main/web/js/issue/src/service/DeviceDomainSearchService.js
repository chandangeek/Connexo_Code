/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.service.DeviceDomainSearchService', {

    extend: 'Isu.service.Search',

    stateful: false,

    stateId: 'deviceGroup',

    addProperty: function (property) {
        var me = this,
            excludedCriteria;

        if (Ext.isArray(me.excludedCriteria)) {
            excludedCriteria = me.excludedCriteria;
        } else {
            excludedCriteria = me.excludedCriteria ? [me.excludedCriteria] : [];
        }

        if (!Ext.Array.contains(excludedCriteria, property.get('name'))) {
            return this.callParent(arguments);
        }
        return property;
    }
});