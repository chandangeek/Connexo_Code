/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.model.webservice.History', {
  extend: 'Ext.data.Model',
  requires: [
    'Wss.model.Endpoint',
  ],

  fields: [
      {name: 'startDateTime', type: 'date'},
      {name: 'endDateTime', type: 'date'},
      {name: 'status', type: 'string'}
  ],

  hasOne: [
    {
        model: 'Wss.model.Endpoint',
        associatedName: 'endpoint',
        associationKey: 'endpoint',
        getterName: 'getEndpoint',
        setterName: 'setEndpoint'
    },
  ],
});