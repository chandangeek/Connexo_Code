/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.model.webservice.History', {
  extend: 'Ext.data.Model',
  requires: [
    'Wss.model.Endpoint',
  ],

  fields: [
      {
        name: 'startDate',
        type: 'date',
        convert: function (value) {
          return new Date(value);
        }
      },
      {name: 'duration', type: 'number'},
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