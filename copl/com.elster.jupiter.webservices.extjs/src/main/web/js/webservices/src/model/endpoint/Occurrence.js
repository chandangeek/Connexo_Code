/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.model.endpoint.Occurrence', {
  extend: 'Ext.data.Model',
  requires: [
    'Wss.model.Endpoint',
  ],

  fields: [
    {name: 'id', type: 'number'},
    {
      name: 'startTime',
      type: 'date',
      convert: function (value) {
        return value && new Date(value);
      }
    },
    {
      name: 'endTime',
      type: 'date',
      convert: function (value) {
        return value && new Date(value);
      }
    },
    {name: 'applicationName', type: 'string'},
    {name: 'request', type: 'string'},
    {
        name: 'status',
        type: 'auto',
        mapping: function (data) {
            return (data && data.status && data.status.name) ? data.status.name : null
        },
    },
    {
        name: 'statusId',
        type: 'auto',
        mapping: function (data) {
            return (data && data.status && data.status.id) ? data.status.id : null
        },
    },
    {name: 'payload', type: 'string'},
    {name: 'appServerName', type: 'string'}
  ],

  hasOne: [
    {
        model: 'Wss.model.Endpoint',
        associatedName: 'endpoint',
        associationKey: 'endPointConfigurationInfo',
        getterName: 'getEndpoint',
        setterName: 'setEndpoint'
    },
  ],

  proxy: {
    type: 'rest',
    url: '/api/ws/occurrences/',
    timeout: 120000,
    reader: {
        type: 'json',
    }
  }
});