/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DataLoggerSlaveChannelHistory', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'startDate', type: 'number', useNull: true},
        {name: 'endDate', type: 'number', useNull: true},
        {name: 'deviceName', type: 'string', useNull: true},
        {name: 'channelId', type: 'number', useNull: true},

        {
            name: 'periodName',
            persist: false,
            mapping: function (data) {
                var result, startDate, endDate;

                startDate = data.startDate;
                endDate = data.endDate;
                if (startDate && endDate) {
                    result = Uni.I18n.translate('slaveHistoryPeriod.fromXUntilY', 'MDC', "From {0} - Until {1}",
                        [Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.SHORT, Uni.DateTime.SHORT),
                         Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.SHORT, Uni.DateTime.SHORT)],
                        false
                    );
                } else if (data.startDate) {
                    result = Uni.I18n.translate('slaveHistoryPeriod.fromX', 'MDC', "From {0}",
                        Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.SHORT, Uni.DateTime.SHORT),
                        false
                    );
                } else if (data.endDate) {
                    result = Uni.I18n.translate('slaveHistoryPeriod.untilX', 'MDC', "Until {0}",
                        Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                        false
                    );
                } else {
                    result = Uni.I18n.translate('general.always', 'MDC', 'Always')
                }
                return result;
            }
        }
    ]
});