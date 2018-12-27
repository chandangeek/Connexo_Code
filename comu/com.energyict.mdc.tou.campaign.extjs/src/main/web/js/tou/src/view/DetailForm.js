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
                                result = value ? '<a href="' + me.router.getRoute('workspace/toucampaigns') + '">' + Ext.String.htmlEncode(value) + '</a>' : '';
                                console.log(result);
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
                            var devStore = Ext.getStore('Tou.store.DeviceTypes');
                            devStore.load();
                            devStore.on('load',function (store, records, successful, eOpts ){
                                 //Block of codes
                                 var access=records[0].data.access;
                                 //Block of codes
                            });
                            return value ? '<a href="' + me.router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: value}) + '">' + Ext.String.htmlEncode(value) + '</a>' : '-'
                        }
                    },
                    {
                        itemId: 'activation-field',
                        xtype: 'displayfield',
                        name: 'activationStart',
                        fieldLabel: 'Activation start',
                        renderer: function (value) {
                            return value ? '<span>' + value + '</span>' : '-';
                        }
                    },
                    {
                        itemId: 'update-type-field',
                        fieldLabel: 'Update type',
                        name: 'updateType',
                        renderer: function (value) {
                            return value ? '<span>' + value + '</span>' : '-'
                        }
                    },
                    {
                          itemId: 'calendar-field',
                          fieldLabel: 'Calendar',
                          name: 'calendar',
                          renderer: function (value) {
                                return value ? '<span>' + value + '</span>' : '-'
                          }
                    },
                    ]
             },
            {
                items: [
                    {
                        itemId: 'status-field',
                        fieldLabel: 'Status',
                        name: 'status',
                        renderer: function (value) {
                            return value ? value.localizedValue : '-';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});