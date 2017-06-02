/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.util.ViewHistoryActionListener', {
    moveToHistoryPage: function (record, isBulk) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            outputFilterParams;

        if (isBulk) {
            outputFilterParams = {
                interval: me.getOutputReadingsFilterPanel().down('#output-readings-topfilter-duration').getParamValue(),
                changedDataOnly: 'yes'
            }
        } else {
            outputFilterParams = {
                endInterval: Number(record.get('interval').start) + '-' + Number(record.get('interval').end)
            }
        }
        router.getRoute('usagepoints/view/purpose/output/history').forward(router.arguments, outputFilterParams);
    }
});

