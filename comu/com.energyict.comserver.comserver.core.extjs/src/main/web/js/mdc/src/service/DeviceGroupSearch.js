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
            filter = me.createWidgetForProperty(property);
        }
        if (Ext.isDefined(filter)) {
            me.filters.add(property.get('sticky') ? filter : filter.widget);
            me.fireEvent('add', me.filters, filter, property);
        }
    }
});