Ext.define('Fwc.firmwarecampaigns.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Fwc.firmwarecampaigns.view.ActionMenu'
    ],
    alias: 'widget.firmware-campaigns-detail-form',
    tools: [
        {
            xtype: 'button',
            itemId: 'firmware-campaigns-detail-action-menu-button',
            text: Uni.I18n.translate('general.actions', 'FWC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'firmware-campaigns-action-menu',
                itemId: 'firmware-campaigns-action-menu'
            }
        }
    ],
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
                                result = value ? '<a href="' + me.router.getRoute('workspace/firmwarecampaigns/firmwarecampaign').buildUrl({firmwareCampaignId: field.up('form').getRecord().getId()}) + '">' + value + '</a>' : '';
                            } else {
                                result = value || '';
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'device-type-field',
                        fieldLabel: Uni.I18n.translate('general.deviceType', 'FWC', 'Device type'),
                        name: 'deviceType',
                        renderer: function (value) {
                            return value ? '<a href="' + me.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl({deviceTypeId: value.id}) + '">' + value.localizedValue + '</a>' : ''
                        }
                    },
                    {
                        itemId: 'firmware-type-field',
                        fieldLabel: Uni.I18n.translate('firmware.campaigns.firmwareType', 'FWC', 'Firmware type'),
                        name: 'firmwareType',
                        renderer: function (value) {
                            return value ? value.localizedValue : ''
                        }
                    },
                    {
                        itemId: 'firmware-management-option-field',
                        fieldLabel: Uni.I18n.translate('firmware.campaigns.firmwareManagementOption', 'FWC', 'Firmware management option'),
                        name: 'managementOption',
                        renderer: function (value) {
                            return value ? value.localizedValue : ''
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
                            return value ? value.localizedValue : '';
                        }
                    },
                    {
                        itemId: 'devices-field',
                        fieldLabel: Uni.I18n.translate('general.devices', 'FWC', 'Devices'),
                        name: 'devicesStatus',
                        renderer: function (value, field) {
                            var result = '';

                            if (!Ext.isArray(value)) {
                                return result;
                            }

                            field.addCls('firmware-campaign-status');
                            Ext.Array.each(value, function (devicesStatus, index) {
                                var iconCls = '';

                                switch (devicesStatus.status.id) {
                                    case 'failed':
                                        iconCls = 'icon-cancel-circle';
                                        break;
                                    case 'success':
                                        iconCls = 'icon-checkmark-circle';
                                        break;
                                    case 'ongoing':
                                        iconCls = 'icon-spinner3';
                                        break;
                                    case 'pending':
                                        iconCls = 'icon-forward2';
                                        break;
                                    case 'configurationError':
                                        iconCls = 'icon-notification';
                                        break;
                                    case 'cancelled':
                                        iconCls = 'icon-blocked';
                                        break;
                                }

                                if (index) {
                                    result += '<br>';
                                }

                                result += '<span class="' + iconCls + '" data-qtip="' + devicesStatus.status.localizedValue + '"></span>' + devicesStatus.amount;
                            });
                            return result;
                        }
                    },
                    {
                        itemId: 'started-on-field',
                        fieldLabel: Uni.I18n.translate('general.startedOn', 'FWC', 'Started on'),
                        name: 'startedOn',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    },
                    {
                        itemId: 'finished-on-field',
                        fieldLabel: Uni.I18n.translate('general.finishedOn', 'FWC', 'Finished on'),
                        name: 'finishedOn',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});