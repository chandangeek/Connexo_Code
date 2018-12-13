/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.MeterRegistrationIssueDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device',
        'Mdc.privileges.DeviceType',
        'Idc.store.Gateways',
        'Uni.util.FormEmptyMessage'
    ],
    alias: 'widget.meter-registration-issue-details-form',
    router: null,
    store: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'meter-resitration-issue-details-container',
                xtype: 'data-collection-details-container'
            },
            {
                itemId: 'Device-details-panel-title',
                title: Uni.I18n.translate('general.deviceDetails', 'IDC', 'Device details'),
                ui: 'medium'
            },
            {
                xtype: 'container',
                itemId: 'communication-issue-other-details-container',
                layout: 'column',
                items: [
                    {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5,
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + Uni.I18n.translate('general.slaveDetails', 'IDC', 'Slave details'),
                                labelAlign: 'top',
                                layout: 'vbox',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 200
                                },
                                items: [
                                    {
                                        itemId: 'meter-registration-issue-device',
                                        fieldLabel: Uni.I18n.translate('general.title.device', 'IDC', 'Device'),
                                        name: 'device',
                                        renderer: function (value) {
                                            var url = '',
                                                result = '';

                                            if (value) {
                                                if (value.name && Mdc.privileges.Device.canView()) {
                                                    url = me.router.getRoute('devices/device').buildUrl({deviceId: value.name});
                                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                                } else {
                                                    result = Ext.String.htmlEncode(value.name);
                                                }
                                            }

                                            return result;
                                        }
                                    },
                                    {
                                        itemId: 'meter-registration-issue-usage-point',
                                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDC', 'Usage point'),
                                        name: 'usage_point'
                                    },
                                    {
                                        itemId: 'meter-registration-issue-device-type',
                                        fieldLabel: Uni.I18n.translate('general.title.deviceType', 'IDC', 'Device type'),
                                        name: 'deviceType',
                                        renderer: function (value) {
                                            var url = '',
                                                result = '';

                                            if (value) {
                                                if (Mdc.privileges.DeviceType.canView()) {
                                                    url = me.router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: value.id});
                                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                                } else {
                                                    result = Ext.String.htmlEncode(value.name);
                                                }
                                            }

                                            return result;
                                        }
                                    },
                                    {
                                        itemId: 'meter-registration-issue-device-configuration',
                                        fieldLabel: Uni.I18n.translate('general.title.deviceConfiguration', 'IDC', 'Device configuration'),
                                        name: 'deviceConfiguration',
                                        renderer: function (value) {
                                            var url = '',
                                                result = '';

                                            if (value) {
                                                if (me.getRecord() && Mdc.privileges.DeviceType.canView()) {
                                                    url = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({
                                                        deviceTypeId: me.getRecord().get('deviceType').id,
                                                        deviceConfigurationId: value.id
                                                    });
                                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                                } else {
                                                    result = Ext.String.htmlEncode(value.name);
                                                }
                                            }

                                            return result;
                                        }
                                    },
                                    {
                                        itemId: 'meter-registration-issue-device-state',
                                        fieldLabel: Uni.I18n.translate('general.title.deviceState', 'IDC', 'Device state'),
                                        name: 'deviceState_name'
                                    }
                                ]
                            }

                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5,
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.latestConnectedMasterDetails', 'IDC', 'Latest connected master details'),
                                labelAlign: 'top',
                                layout: 'vbox',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 200
                                },
                                items: [
                                    {
                                        xtype: 'uni-form-empty-message',
                                        itemId: 'no-master',
                                        text: Uni.I18n.translate('general.masterDeviceHasBeenRemoved', 'IDC', 'Master device has been removed'),
                                        hidden: true
                                    },
                                    {
                                        itemId: 'meter-registration-issue-master',
                                        fieldLabel: Uni.I18n.translate('general.title.device', 'IDC', 'Device'),
                                        name: 'master',
                                        renderer: function (value) {
                                            var url = '',
                                                result = '';
                                            if (value) {
                                                if (Mdc.privileges.Device.canView()) {
                                                    url = me.router.getRoute('devices/device').buildUrl({deviceId: value});
                                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                                } else {
                                                    result = Ext.String.htmlEncode(value);
                                                }
                                            }

                                            return result;
                                        }
                                    },
                                    {
                                        itemId: 'meter-registration-issue-master-usage-point',
                                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDC', 'Usage point'),
                                        name: 'masterUsagePoint'
                                    },
                                    {
                                        itemId: 'meter-registration-issue-master-device-type',
                                        fieldLabel: Uni.I18n.translate('general.title.deviceType', 'IDC', 'Device type'),
                                        name: 'masterDeviceType',
                                        renderer: function (value) {
                                            var url = '',
                                                result = '';

                                            if (value) {
                                                if (Mdc.privileges.DeviceType.canView()) {
                                                    url = me.router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: value.id});
                                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                                } else {
                                                    result = Ext.String.htmlEncode(value.name);
                                                }
                                            }

                                            return result;
                                        }
                                    },
                                    {
                                        itemId: 'meter-registration-issue-master-configuration',
                                        fieldLabel: Uni.I18n.translate('general.title.deviceConfiguration', 'IDC', 'Device configuration'),
                                        name: 'masterDeviceConfig',
                                        renderer: function (value) {
                                            var url = '',
                                                result = '';

                                            if (value) {
                                                if (me.getRecord() && Mdc.privileges.DeviceType.canView()) {
                                                    url = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({
                                                        deviceTypeId: me.getRecord().get('deviceType').id,
                                                        deviceConfigurationId: value.id
                                                    });
                                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                                } else {
                                                    result = Ext.String.htmlEncode(value.name);
                                                }
                                            }

                                            return result;
                                        }
                                    },
                                    {
                                        itemId: 'meter-registration-issue-master-state',
                                        fieldLabel: Uni.I18n.translate('general.title.deviceState', 'IDC', 'Device state'),
                                        name: 'masterState_name'
                                    },
                                    {
                                        itemId: 'last-connected-master-period',
                                        fieldLabel: Uni.I18n.translate('general.Period', 'IDC', 'Period'),
                                        name: 'period'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            {
                itemId: 'Gateway-details-panel-title',
                title: Uni.I18n.translate('general.latestConnectedMasters', 'IDC', 'Latest connected masters'),
                ui: 'medium'
            },
            {
                xtype: 'uni-form-empty-message',
                itemId: 'no-masters',
                text: Uni.I18n.translate('general.masterDevicesHaveBeenRemoved', 'IDC', 'Master devices have been removed'),
                margin: '0 0 0 19',
                hidden: true
            },
            {
                xtype: 'grid',
                itemId: 'gateways-grid',
                ui: 'medium',
                store: 'Idc.store.Gateways',
                requires: ['Uni.DateTime'],
                columns: [
                    {
                        text: Uni.I18n.translate('general.start', 'IDC', 'From'),
                        dataIndex: 'start',
                        flex: 2,
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                        }
                    },
                    {
                        text: Uni.I18n.translate('general.end', 'IDC', 'To'),
                        dataIndex: 'end',
                        flex: 2,
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                        }
                    },
                    {
                        text: Uni.I18n.translate('general.name', 'IDC', 'Name'),
                        dataIndex: 'name',
                        flex: 2,
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value && Mdc.privileges.Device.canView()) {
                                    url = me.router.getRoute('devices/device').buildUrl({deviceId: value});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                } else {
                                    result = Ext.String.htmlEncode(value);
                                }
                            }

                            return result;
                        }
                    }

                ]
            }
        ];
        if (me.store) {
            me.items.push({
                xtype: 'issue-details-log-grid',
                title: Uni.I18n.translate('general.mostRecentCommunicationLog', 'IDC', 'Most recent communication log'),
                itemId: 'communication-issue-log-grid',
                store: me.store
            });
        }

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            grid = me.down('#gateways-grid'),
            emptyMsg = me.down('#no-masters'),
            masterVisible = !Ext.isEmpty(record.get('master'));
        if (record.gateways().count() <= 0) {
            grid.hide();
            emptyMsg.show();
        } else {
            emptyMsg.hide();
            grid.show();
            grid.store.removeAll();
            grid.store.add(record.gateways().getRange(0, record.gateways().count()));
        }

        me.down('#no-master').setVisible(!masterVisible);
        me.down('#meter-registration-issue-master').setVisible(masterVisible);
        me.down('#meter-registration-issue-master-usage-point').setVisible(masterVisible);
        me.down('#meter-registration-issue-master-device-type').setVisible(masterVisible);
        me.down('#meter-registration-issue-master-configuration').setVisible(masterVisible);
        me.down('#meter-registration-issue-master-state').setVisible(masterVisible);
        me.down('#last-connected-master-period').setVisible(masterVisible);

        me.callParent(arguments);
    }
});