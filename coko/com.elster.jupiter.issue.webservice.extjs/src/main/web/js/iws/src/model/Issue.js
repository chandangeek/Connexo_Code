/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.model.Issue', {
    extend: 'Isu.model.Issue',

    fields: [
        {name: 'status', persist: false, mapping: 'webServiceCallOccurrence.status'},
        {name: 'webServiceEndpoint', persist: false, mapping: 'webServiceCallOccurrence.endpoint'},
        {name: 'webServiceName', persist: false, mapping: 'webServiceCallOccurrence.webServiceName'},
        {
            name: 'occurrenceLink',
            persist: false,
            mapping: function (data) {
                if (data.webServiceCallOccurrence) {
                    return {
                        ednpointId: data.webServiceCallOccurrence.endpoint.id,
                        occurrenceId: data.webServiceCallOccurrence.id,
                        startTime: data.webServiceCallOccurrence.startTime
                    }
                }
                return null;
            }
        },
    ],

    proxy: {
        type: 'rest',
        url: '/api/wsi/issues',
        reader: {
            type: 'json',
        }
    }
});
