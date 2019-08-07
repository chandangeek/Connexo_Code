/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.model.endpoint.OccurrenceLog', {
  extend: 'Ext.data.Model',
  requires: [
    'Wss.model.Endpoint',
  ],

  fields: [
    {name: 'id', type: 'number'},
    { name: 'logLevel', type: 'string' },
    {
      name: 'timestamp',
      type: 'date',
      convert: function (value) {
        return new Date(value);
      }
    },
    {name: 'message', type: 'string'},
    {name: 'stackTrace', type: 'string'},
  ],

  hasOne: [
    {
        model: 'Wss.model.Endpoint',
        associatedName: 'endpoint',
        associationKey: 'endPointConfiguration',
        getterName: 'getEndpoint',
        setterName: 'setEndpoint'
    },
  ],
});