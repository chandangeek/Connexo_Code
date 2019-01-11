/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Tou.view.ActionMenu'
    ],
    alias: 'widget.tou-campaigns-detail-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5,
        defaults: {
            xtype: 'displayfield',
            labelWidth: 250
        }
    },
    router: null,
    isPreview: false,
    stores: ['Tou.store.DeviceTypes'],
    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                itemId: 'tou-campaigns-detail-action-menu-button',
                menu: {
                    xtype: 'tou-campaigns-action-menu',
                    itemId: 'tou-campaigns-action-menu',
                    returnToCampaignOverview: !me.isPreview
                }
            }
        ];

        me.items = [
            {
                items: [
                    {
                        itemId: 'name-field',
                        fieldLabel: 'Name',
                        name: 'name',
                        renderer: function (value, field) {
                            var result = '';
                            if (me.isPreview) {
                                result = value ? '<a href="' + me.router.getRoute('workspace/toucampaigns/toucampaign').buildUrl({touCampaignName : value}) + '">' + Ext.String.htmlEncode(value) + '</a>' : '-';
                            } else {
                                result = Ext.String.htmlEncode(value) || '';
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'device-type-field',
                        fieldLabel: 'Device type',
                        name: 'deviceType',
                        renderer: function (value) {
                            return value && value.id ? '<a href="' + me.router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: value.id}) + '">' + Ext.String.htmlEncode(value.name) + '</a>' : '-'
                        }
                    },
                    {
                        itemId: 'activation-field',
                        xtype: 'displayfield',
                        name: 'timeBoundary',
                        fieldLabel: 'Time boundary',
                        renderer: function (value) {
                            var res = '-';
                            if (value){
                                res = value;
                            }
                            return res;
                        }
                    },
                    {
                        itemId: 'update-type-field',
                        fieldLabel: 'Update type',
                        name: 'updateType',
                        renderer: function (value) {
                            var res = '-';
                            if (value){
                                 switch(value){
                                    case 'fullCalendar':
                                       res = 'Full calendar';
                                       break;
                                    case 'specialDays':
                                       res = 'Only special days';
                                       break;
                                    default:
                                       res = value;
                                       break;
                                 }
                            }
                            return res;
                        }
                    },
                    {
                          itemId: 'calendar-field',
                          fieldLabel: 'Calendar',
                          name: 'calendar',
                          renderer: function (value) {
                                return value && value.name ? '<span>' + value.name + '</span>' : '-'
                          }
                    },
                    {
                          itemId: 'activation-date-field',
                          fieldLabel: 'Activate calendar',
                          name: 'activationDate',
                          renderer: function (value) {
                                if (value){
                                    if (!isNaN(value) &&  parseInt(value) == value ) return  Uni.DateTime.formatDateTimeShort(parseInt(value));
                                    return Ext.String.htmlEncode(value)
                                }
                                return '-';
                          }
                    },
                    {
                          itemId: 'time-validation-field',
                          fieldLabel: 'Timeout before validation',
                          name: 'timeValidation',
                          renderer: function (value) {
                               return value ? '<span>' + Uni.DateTime.formatTimeShort(new Date(value)) + '</span>' : '-'
                          }
                    }
                    ]
             },
            {
                items: [
                    {
                        itemId: 'status-field',
                        fieldLabel: 'Status',
                        name: 'status',
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                          itemId: 'devices-field',
                          fieldLabel: 'Devices',
                          name: 'devices',
                          renderer: function (value, field) {
                              var result = '';

                              if (!Ext.isArray(value)) {
                                  return result;
                              }

                              field.addCls('firmware-campaign-status');
                              Ext.Array.each(value, function (devicesStatus, index) {
                                  var iconCls = '';

                                  switch (devicesStatus.status) {
                                       case 'Failed':
                                           iconCls = 'icon-cancel-circle';
                                       break;
                                       case 'Successful':
                                            iconCls = 'icon-checkmark-circle';
                                       break;
                                       case 'Ongoing':
                                            iconCls = 'icon-spinner3';
                                       break;
                                       case 'Pending':
                                            iconCls = 'icon-forward2';
                                       break;
                                       case 'Configuration Error':
                                            iconCls = 'icon-notification';
                                       break;
                                       case 'Canceled':
                                            iconCls = 'icon-blocked';
                                       break;
                                  }

                                  if (index) {
                                        result += '<br>';
                                  }

                                  result += '<span class="' + iconCls + '" data-qtip="' + devicesStatus.status + '"></span>' + devicesStatus.quantity;
                              });
                              return result;
                          }
                    },
                    {
                          itemId: 'started-on-field',
                          fieldLabel: 'Started on',
                          name: 'startedOn',
                          renderer: function (value) {
                                return value ? '<span>' + Uni.DateTime.formatDateTimeShort(value) + '</span>' : '-'
                          }
                    },
                    {
                          itemId: 'finished-on-field',
                          fieldLabel: 'Finished on',
                          name: 'finishedOn',
                          renderer: function (value) {
                                 return value ? '<span>' + Uni.DateTime.formatDateTimeShort(value) + '</span>' : '-'
                          }
                      }
                ]
            }
        ];

        me.callParent(arguments);
    }
});