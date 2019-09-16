/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AutoclosureExclusions', {
    extend: 'Ext.data.Store',
    requires: ['Mdc.model.AutoclosureExclusion'],
    model: 'Mdc.model.AutoclosureExclusion',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/isu/creationrules/device/{deviceId}/excludedfromautoclosurerules',
        reader: {
            type: 'json',
            root: 'creationRules'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }
});