/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.endpoint.Type', {
  extend: 'Ext.data.Store',
  fields: ['value', 'display'],
  data : [
    {"value":"INBOUND", "display": Uni.I18n.translate('general.inbound', 'WSS', 'Inbound') },
    {"value":"OUTBOUND", "display": Uni.I18n.translate('general.outbound', 'WSS', 'Outbound') },
  ]
});