/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.endpoint.OccurrenceLog', {
  extend: 'Ext.data.Store',
  model: 'Wss.model.endpoint.OccurrenceLog',
  autoLoad: false,
  proxy: {
    type: 'rest',
    urlTpl: '/api/ws/endpointconfigurations/occurrences/{occurrenceId}/log',
    setUrl: function (occurrenceId) {
      this.url = this.urlTpl.replace('{occurrenceId}', occurrenceId);
    },
    timeout: 120000,
    reader: {
        type: 'json',
        root: 'logs'
    }
  }
});
