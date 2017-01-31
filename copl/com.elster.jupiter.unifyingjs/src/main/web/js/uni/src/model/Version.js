/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.Version', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'version',
            type: 'int'
        }
    ],
    getRecordData: function () {
        return this.getProxy().getWriter().getRecordData(this);
    }
});