/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.util.UsagePointType', {
    singleton: true,

    map: {
        'MEASURED_SDP': {
            isSdp: true,
            isVirtual: false
        },
        'UNMEASURED_SDP': {
            isSdp: true,
            isVirtual: true
        },
        'MEASURED_NON_SDP': {
            isSdp: false,
            isVirtual: false
        },
        'UNMEASURED_NON_SDP': {
            isSdp: false,
            isVirtual: true
        },
        'DEFAULT': {
            isSdp: null,
            isVirtual: null
        }
    },

    mapping: function (data) {
        var map = Imt.util.UsagePointType.map,
            key;

        for (key in map) {
            if (map.hasOwnProperty(key)
                && map[key].isSdp === data.isSdp
                && map[key].isVirtual === data.isVirtual) {
                return key;
            }
        }
    },

    convert: function (value, record) {
        record.set(Imt.util.UsagePointType.map[value] || Imt.util.UsagePointType.map['DEFAULT']);
        return value;
    }
});