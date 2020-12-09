/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.store.endpoint.Status', {
  extend: 'Ext.data.Store',
  fields: ['value', 'display'],
  data : [
      {"value":"failed", "display": Uni.I18n.translate('general.failed', 'WSS', 'Failed')},
      {"value":"ongoing", "display": Uni.I18n.translate('general.ongoing', 'WSS', 'Ongoing')},
      {"value":"successful", "display": Uni.I18n.translate('general.successful', 'WSS', 'Successful')},
      {"value":"cancelled", "display": Uni.I18n.translate('general.cancelled', 'WSS', 'Cancelled')}
  ]
});