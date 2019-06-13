/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.store.AvailableQueues', {
  extend: 'Ext.data.Store',
  requires: [
    'Uni.data.reader.StringArray'
  ],
  fields: ['name'],
  autoLoad: false,
  proxy: {
      type: 'rest',
      urlTpl: '/api/scs/servicecalltypes/compatiblequeues/{serviceCallTypeId}',
      timeout: 120000,
      setUrl: function (servicecallType) {
        this.url = this.urlTpl.replace('{serviceCallTypeId}', servicecallType.getId());
      },
      reader: 'stringArray'
  }
});
