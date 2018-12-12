/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.model.ServiceCallType', {
    extend: 'Uni.model.Version',
    fields: [
        'name', 'versionName', 'logLevel', 'status', 'serviceCallLifeCycle', 'customPropertySets',
        {
            name: 'version',
            defaultValue: 0
        },
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'lifecycle',
            persist: false,
            mapping: function (data) {
                if(data.serviceCallLifeCycle) {
                    return data.serviceCallLifeCycle.name;
                } else {
                    return 'undefined';
                }
            }
        },
        {
            name: 'logLevelName',
            persist: false,
            mapping: function (data) {
                if(data.logLevel) {
                    return data.logLevel.displayValue;
                } else {
                    return 'undefined';
                }
            }
        }
        ,
        {
            name: 'statusName',
            persist: false,
            mapping: function (data) {
                if(data.status) {
                    return data.status.displayValue;
                } else {
                    return 'undefined';
                }
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/scs/servicecalltypes',
        reader: {
            type: 'json'
        }
    }
});