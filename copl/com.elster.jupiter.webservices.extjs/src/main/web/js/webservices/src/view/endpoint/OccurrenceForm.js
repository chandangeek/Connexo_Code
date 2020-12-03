/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.OccurrenceForm', {
  extend: 'Ext.form.Panel',
  frame: false,
  alias: 'widget.webservice-history-occurence-form',

  defaults: {
      xtype: 'displayfield',
      labelWidth: 250
  },

  loadRecord: function(record) {
    this.callParent(arguments);
    var endpoint = record.getEndpoint();

    this.getForm().setValues({
        endpoint: endpoint,
        webServiceName: endpoint.get('webServiceName'),
    });
  },

  initComponent: function () {
      var me = this;

      me.items = [
            {
              name: 'endpoint',
              fieldLabel: Uni.I18n.translate('general.webserviceEndpoint', 'WSS', 'Web service endpoint'),
              renderer: function (value) {
                return value ? value.get('name') : '-';
              }
          },
          {
              name: 'webServiceName',
              fieldLabel: Uni.I18n.translate('general.webservice', 'WSS', 'Web service')
          },
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
          {
            name: 'appServerName',
            fieldLabel: Uni.I18n.translate('general.appServerName', 'WSS', 'Application server')
          },
      ];

      me.callParent(arguments);

      if (me.record) {
        me.loadRecord(me.record);
      }
  },
});