/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.RestOverride
 *
 * Formats reserved HTML tokens in the url builder.
 */
Ext.define('Uni.override.RestOverride', {
    override: 'Ext.data.proxy.Rest',

    setParams: function() {
        var
            me = this,
            params = arguments;

        if (this.url) {
            _.map(me.url.match(/\{[^\}]+\}/g), function(param, idx) {
                me.setExtraParam(param.slice(1, -1), params[idx]);
            });
        }
    },

    buildUrl: function (request) {
        var me = this,
            operation = request.operation,
            records = operation.records,
            record = Ext.isArray(records) ? records[0] : null,
            id = record ? record.getId() : operation.id;

        // Encodes HTML characters such as '/' and '@'.
        if (!Ext.isEmpty(id)) {
            operation.id = encodeURIComponent(id);
        }

        operation.records = [];
        var url = me.callParent(arguments);
        operation.records = records;

        var urlTemplate = new Ext.Template(url),
            params = _.object(
                _.keys(request.proxy.extraParams),
                _.map(request.proxy.extraParams || [],
                    encodeURIComponent
                )
            ),
            newUrl = urlTemplate.apply(params);

        //Remove variables embedded into URL
        Ext.Object.each(params, function (key, value) {
            var regex = new RegExp('{' + key + '.*?}');
            /*     if (regex.test(url)) {
             delete params[key];
             }*/
        });

        request.url = url;

        return newUrl;
    }
});