/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.model.webservice.History', {
  extend: 'Ext.data.Model',
  requires: [
    'Wss.model.Webservice',
  ],

  fields: [
      {name: 'startDateTime', type: 'date'},
      {name: 'endDateTime', type: 'date'},
      {name: 'status', type: 'string'}
  ],

  hasOne: [
    {
        model: 'Wss.model.Webservice',
        associationKey: 'webservice',
        name: 'webservice',
        getterName: 'getWebservice',
        setterName: 'setWebservice'
    },
  ],
});