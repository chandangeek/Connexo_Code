/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.ComTasks', {
  extend: 'Ext.data.Store',
  requires: [
      'Fwc.firmwarecampaigns.model.ComTask'
  ],
  autoLoad: false,
  model: 'Fwc.firmwarecampaigns.model.ComTask',
  proxy: {
      type: 'rest',
      urlTpl: '/api/tou/toucampaigns/comtasks?type={deviceTypeId}',
      setUrl: function (deviceTypeId) {
          this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
      },
      reader: {
        type: 'json'
      }
  }
});