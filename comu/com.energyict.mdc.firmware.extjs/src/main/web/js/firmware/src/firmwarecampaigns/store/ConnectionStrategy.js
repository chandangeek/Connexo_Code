/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.ConnectionStrategy', {
  extend: 'Ext.data.Store',
  autoLoad: false,
  fields: ['id', 'name'],
  data: [
    {
      id: 1,
      name: Uni.I18n.translate(
        'general.connectionStrategy.minimizeConnections',
        'FWC',
        'Minimize connections'
      )
    },
    {
      id: 2,
      name: Uni.I18n.translate(
        'general.connectionStrategy.asSoonAsPossible',
        'FWC',
        'As soon as possible'
      )
    }
  ]
});