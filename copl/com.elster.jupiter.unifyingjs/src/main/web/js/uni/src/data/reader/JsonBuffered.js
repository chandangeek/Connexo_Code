/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.data.reader.JsonBuffered', {
    extend: 'Ext.data.reader.Json',
    alias : 'reader.jsonBuffered',

    totalCount: 0,
    buildExtractors: function() {
        this.callParent(arguments);
        this.getTotal = function (data) {
            var me = this;
            var total = data[me.totalProperty];
            me.totalCount = total < me.totalCount ? me.totalCount : total;
            return me.totalCount;
        }
    }
});
