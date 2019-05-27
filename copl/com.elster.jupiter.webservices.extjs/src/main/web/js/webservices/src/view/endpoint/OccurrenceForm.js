/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.OccurrenceForm', {
  extend: 'Ext.form.Panel',
  frame: false,
  alias: 'widget.webservice-history-occurence-form',
  // layout: {
  //     type: 'column'
  // },
  defaults: {
      xtype: 'displayfield',
      labelWidth: 250
  },
  initComponent: function () {
      var me = this;

      me.items = [
          {
              name: 'startTime',
              fieldLabel: Uni.I18n.translate('general.startedOn', 'WSS', 'Started on'),
              renderer: function (value) {
                  return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
              }
          },
          {
            name: 'status',
            fieldLabel: Uni.I18n.translate('general.status', 'WSS', 'Status')
        },
      ];

      me.callParent(arguments);

      if (me.record) {
        me.loadRecord(me.record);
      }
  },
});