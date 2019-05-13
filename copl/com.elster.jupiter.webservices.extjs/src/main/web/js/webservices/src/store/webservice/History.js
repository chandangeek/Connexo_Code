/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.webservice.History', {
  extend: 'Ext.data.Store',
  model: 'Wss.model.webservice.History',
  autoLoad: false,
  proxy: {
      type: 'rest',
      url: '/api/ws/webservice/history',
      timeout: 120000,
      reader: {
          type: 'json',
          root: 'webServices'
      }
  }
});
