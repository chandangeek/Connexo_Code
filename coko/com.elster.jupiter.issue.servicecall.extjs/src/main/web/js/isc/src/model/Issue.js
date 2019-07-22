/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.model.Issue', {
    extend: 'Isu.model.Issue',

    fields: [
        {name: 'parentServiceCall',  persist: false, mapping: 'serviceCallInfo.parentServiceCall'},
        {name: 'serviceCallType', persist: false, mapping: 'serviceCallInfo.serviceCallType'},
        {name: 'receivedTime',  persist: false, mapping: 'serviceCallInfo.receivedTime'},
        {name: 'lastModifyTime', persist: false, mapping: 'serviceCallInfo.lastModifyTime'},
        {name: 'onState',  persist: false, mapping: 'serviceCallInfo.onState'},
        {
            name: 'serviceCall',
            persist: false,
            mapping: function (data) {
                return {
                    id: data.serviceCallInfo.id,
                    name: data.serviceCallInfo.name
                }
            }
        },
    ],

    proxy: {
        type: 'rest',
        url: '/api/isc/issues',
        reader: {
            type: 'json',
        }
    }
});
