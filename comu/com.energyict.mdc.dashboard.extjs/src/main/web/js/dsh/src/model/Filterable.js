/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.Filterable', {
    extend: 'Ext.data.Model',
    requires: ['Ext.data.writer.Json'],

    inheritableStatics: {
        setFilter: function (model) {
            var proxy = this.getProxy();
            var writer = Ext.create('Ext.data.writer.Json', {
                writeRecordId: false
            });
            var data = _.map(writer.getRecordData(model), function (value, key) {
                return {property: key, value: value};
            });

            proxy.setExtraParam('filter', Ext.encode(_.filter(data, function(item) {return !!item.value})));
        }
    }
});