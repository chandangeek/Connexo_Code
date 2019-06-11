/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.endpoint.EndpointOccurrence', {
  extend: 'Ext.data.Store',
  model: 'Wss.model.endpoint.Occurrence',
  autoLoad: false,
  proxy: {
    type: 'rest',

    urlTpl: '/api/ws/endpointconfigurations/{endpointId}/occurrences',
    setUrl: function (endpointId) {
      this.url = this.urlTpl.replace('{endpointId}', endpointId);
    },
    timeout: 120000,
    reader: {
        type: 'json',
        root: 'occurrences'
    }
  }
});
