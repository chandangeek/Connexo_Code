/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.util.DataFormatter', {
    singleton: true,

    formatIntervalShort: function (value) {
        if (value) {
            if (value.start && value.end) {
                return Imt.purpose.util.DataFormatter.getIntervalShort(new Date(value.start), new Date(value.end))
            } else if (value.end) {
                return Uni.DateTime.formatDateTimeShort(new Date(value.end))
            }
        }
        return '-';
    },

    formatIntervalLong: function (value) {
        if (value) {
            if (value.start && value.end) {
                return Imt.purpose.util.DataFormatter.getIntervalLong(new Date(value.start), new Date(value.end))
            } else if (value.end) {
                return Uni.DateTime.formatDateTimeLong(new Date(value.end))
            }
        }
        return '-';
    },


    formatDateLong: function (value) {
        return value ? Uni.DateTime.formatDateLong(new Date(value)) : '-';
    },

    getIntervalShort: function (startDate, endDate) {
        return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate)
    },

    getIntervalLong: function (startDate, endDate) {
        return Uni.DateTime.formatDateTimeLong(startDate) + ' - ' + Uni.DateTime.formatDateTimeLong(endDate)
    }

});
