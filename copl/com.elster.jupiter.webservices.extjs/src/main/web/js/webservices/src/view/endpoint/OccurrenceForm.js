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
        webServiceName: endpoint.get('webServiceName'),
    });
  },

  initComponent: function () {
      var me = this;

      me.items = [
          {
              name: 'webServiceName',
              fieldLabel: Uni.I18n.translate('general.webservice', 'WSS', 'Web service'),
              renderer: function (value) {
                var record = me.getRecord();
                if (!value || !record) {
                  return '-';
                }

                var route = 'administration/webserviceendpoints/view';
                var url = me.router.getRoute(route).buildUrl({
                    endpointId: record.getEndpoint().get('id')
                });

                return '<a href="' + url + '">' + value + '</a>';
            }
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
      ];

      me.callParent(arguments);

      if (me.record) {
        me.loadRecord(me.record);
      }
  },
});