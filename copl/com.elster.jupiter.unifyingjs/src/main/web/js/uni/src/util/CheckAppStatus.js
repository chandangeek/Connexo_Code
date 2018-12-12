/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.CheckAppStatus', {
    singleton: true,

    insightStatus: undefined,

    checkInsightAppStatus: function (callback) {
        var me = this;

        if (!Ext.isDefined(me.insightStatus)) {
            Ext.Ajax.request({
                url: '/api/apps/apps/status/INS',
                method: 'GET',
                success: function (response) {
                    me.insightStatus = Ext.decode(response.responseText, true).status;

                    if (Ext.isFunction(callback)) {
                        callback();
                    }
                }
            });
        } else if (Ext.isFunction(callback)) {
            callback();
        }
    },

    insightAppIsActive: function (callback) {
        var me = this;

        return me.insightStatus === 'ACTIVE';
    }
});