/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.store.AvailableQueues', {
  extend: 'Ext.data.Store',
  fields: ['name', 'isDefault'],
  autoLoad: false,
  proxy: {
      type: 'rest',
      reader: 'json',
      url: '/api/scs/servicecalltypes/compatiblequeues',
      timeout: 120000
  }
});
