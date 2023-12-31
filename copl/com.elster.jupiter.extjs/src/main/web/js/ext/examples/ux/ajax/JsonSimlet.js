/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * JSON Simlet.
 */
Ext.define('Ext.ux.ajax.JsonSimlet', {
    extend: 'Ext.ux.ajax.DataSimlet',
    alias: 'simlet.json',

    doGet: function (ctx) {
        var me = this,
            data = me.getData(ctx),
            page = me.getPage(ctx, data),
            reader = ctx.xhr.options.proxy && ctx.xhr.options.proxy.reader,
            ret = me.callParent(arguments), // pick up status/statusText
            response = {};

        if (reader && reader.root && Ext.isArray(page)) {
            response[reader.root] = page;
            response[reader.totalProperty] = data.length;
        } else {
            response = page;
        }

        if (ctx.groupSpec) {
            response.summaryData = me.getSummary(ctx, data, page);
        }

        ret.responseText = Ext.encode(response);
        return ret;
    }
});
