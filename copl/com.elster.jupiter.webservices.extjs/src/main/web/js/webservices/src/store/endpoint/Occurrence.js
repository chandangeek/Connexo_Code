/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.endpoint.Occurrence', {
  extend: 'Ext.data.Store',
  model: 'Wss.model.endpoint.Occurrence',
  autoLoad: false,
  proxy: {
    type: 'rest',
    url: '/api/ws/endpointconfigurations/occurrences/',
    timeout: 120000,
    reader: {
        type: 'json',
        root: 'occurrences'
    }
  }
});
