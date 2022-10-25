/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.FWComTask', {
  extend: 'Ext.data.Store',
  requires: [
      'Fwc.firmwarecampaigns.model.FWComTask'
  ],
  autoLoad: false,
  model: 'Fwc.firmwarecampaigns.model.FWComTask',
  proxy: {
      type: 'rest',
      urlTpl: '/api/fwc/field/firmwareuploadcomtasks?type={deviceTypeId}',
      setUrl: function (deviceTypeId) {
          this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
      },
      reader: {
        type: 'json'
      }
  }
});
