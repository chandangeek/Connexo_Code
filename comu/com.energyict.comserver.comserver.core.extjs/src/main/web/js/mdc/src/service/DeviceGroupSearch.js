Ext.define('Mdc.service.DeviceGroupSearch', {
    extend: 'Mdc.service.Search',

    stateful: false,
    stateId: 'deviceGroup',

    addProperty: function (property) {
        var me = this,
            excludedCriteria,
            filter;

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