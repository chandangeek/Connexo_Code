/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Fwc.firmwarecampaigns.view.ActionMenu'
    ],
    alias: 'widget.firmware-campaigns-detail-form',
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
    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                itemId: 'firmware-campaigns-detail-action-menu-button',
                menu: {
                    xtype: 'firmware-campaigns-action-menu',
                    itemId: 'firmware-campaigns-action-menu',
                    returnToCampaignOverview: !me.isPreview
                }
            }
        ];

        me.items = [
            {
                items: [
                    {
                        itemId: 'name-field',
                        fieldLabel: Uni.I18n.translate('general.name', 'FWC', 'Name'),
                        name: 'name',
                        renderer: function (value, field) {
                            var result = '';

                            if (me.isPreview) {
                                result = value ? '<a href="' + me.router.getRoute('workspace/firmwarecampaigns/firmwarecampaign').buildUrl({firmwareCampaignId: field.up('form').getRecord().getId()}) + '">' + Ext.String.htmlEncode(value) + '</a>' : '';
                            } else {
                                result = Ext.String.htmlEncode(value) || '';
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'device-type-field',
                        fieldLabel: Uni.I18n.translate('general.deviceType', 'FWC', 'Device type'),
                        name: 'deviceType',
                        renderer: function (value) {
                            return value ? '<a href="' + me.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl({deviceTypeId: value.id}) + '">' + Ext.String.htmlEncode(value.localizedValue) + '</a>' : '-'
                        }
                    },
                    {
                        xtype: 'displayfield',
                        name: 'timeBoundaryAsText',
                        fieldLabel: Uni.I18n.translate('general.timeBoundary', 'FWC', 'Time boundary'),
                        renderer: function (value){
                             return value ? Uni.I18n.translate('general.betweenXandY', 'FWC', 'Between {0} and {1}', value ) : '-';
                        }
                    },
                    {
                        itemId: 'firmware-type-field',
                        fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
                        name: 'firmwareType',
                        renderer: function (value) {
                            return value ? value.localizedValue : '-'
                        }
                    },
                    {
                        itemId: 'firmware-management-option-field',
                        fieldLabel: Uni.I18n.translate('firmware.campaigns.firmwareManagementOption', 'FWC', 'Firmware management option'),
                        name: 'managementOption',
                        renderer: function (value) {
                            return value ? value.localizedValue : '-'
                        }
                    },
                    {
                        itemId: 'firmware-validation-timeout-field',
                        fieldLabel: Uni.I18n.translate('general.firmwareTimeout', 'FWC', 'Timeout before validation'),
                        name: 'validationTimeout',
                        renderer: function (value) {
                            return value ? Ext.String.format('{0} {1}', value.count, value.localizedTimeUnit) : '-'
                        }
                    },
                    {
                        itemId: 'firmware-service-call-field',
                        fieldLabel: Uni.I18n.translate('general.firmwareServiceCall', 'FWC', 'Service call'),
                        name: 'serviceCall',
                        renderer: function (value) {
                            return value ?  '<a href="' + me.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id})+ '">' + Ext.String.htmlEncode(value.name) + '</a>' : '-'
                        }
                    },
                    {
                        itemId: 'firmware-activation-date-field',
                        fieldLabel: Uni.I18n.translate('general.activationDate', 'FWC', 'Activation date'),
                        name: 'activationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-'
                        }
                    },
                    {
                        xtype: 'property-form',
                        itemId: 'property-form',
                        bodyStyle: {
                            background: 'transparent'
                        },
                        isEdit: false,
                        defaults: {
                            labelWidth: me.defaults.defaults.labelWidth
                        },
                        renderer: function (value) {
                            debugger;
                            return value;
                        }
                    }
                ]
            },
            {
                items: [
                    {
                        itemId: 'status-field',
                        fieldLabel: Uni.I18n.translate('general.status', 'FWC', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            //TODO: format should be changed
                            return value ? value : '-';
                        }
                    },
                    {
                        itemId: 'devices-field',
                        fieldLabel: Uni.I18n.translate('general.devices', 'FWC', 'Devices'),
                        name: 'devices',
                        renderer: function (value, field) {
                            var result = '';

                            if (!Ext.isArray(value)) {
                                return result;
                            }

                            field.addCls('firmware-campaign-status');
                            Ext.Array.each(value, function (devicesStatus, index) {
                                var iconCls = '';
                                //TODO: format should be changed
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
                                    case 'Configuration error':
                                        iconCls = 'icon-notification';
                                        break;
                                    case 'Cancelled':
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
                        fieldLabel: Uni.I18n.translate('general.startedOn', 'FWC', 'Started on'),
                        name: 'startedOn',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                        }
                    },
                    {
                        itemId: 'finished-on-field',
                        fieldLabel: Uni.I18n.translate('general.finishedOn', 'FWC', 'Finished on'),
                        name: 'finishedOn',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});