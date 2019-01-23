/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.zones.controller.Zones',{
    extend: 'Cfg.zones.controller.Zones',
    requires:['Mdc.view.setup.devicezones.Details'],

    views: [
        'Mdc.view.setup.devicezones.DevicesOfZoneGrid',
        'Mdc.view.setup.devicezones.Details',
        'Mdc.view.setup.devicezones.PreviewForm'
    ],

    stores: [
        'Mdc.store.Zones',
    ],

    mixins: [],


    refs: [

        {ref: 'devicesOfZoneGrid', selector: '#allDevicesOfZoneGrid'},
        {ref: 'deviceZonePreview', selector: '#device-zone-preview'},
        {ref: 'deviceZonePreviewForm', selector: '#device-zone-preview-form'},
        {ref: 'editDeviceZoneMenuItem', selector: '#edit-device-zone'},
        {ref: 'deviceZoneDetails', selector: '#deviceZoneDetailsForm'},
        //{ref: 'countButton', selector: 'group-details button[action=countDevicesOfZone]'}
    ],

    init: function () {
        this.control({
            '#allDevicesOfZoneGrid': {
                select: this.showDeviceZonePreview
            },
            'zone-details devicesOfZoneGrid': {
                zoneDeviceRemoveEvent: this.onRemoveDeviceZone,
            },
            'device-zones-action-menu-from-zone': {
                click: this.chooseActionZone
            },
            'device-of-zone-action-menu': {
                click: this.chooseActionGrid
            },
        });
    },

    viewZone: function (currentZoneId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            model = me.getModel('Cfg.zones.model.Zone'),
            widget;

        var service = Ext.create('Mdc.service.Search', {
            router: router
        });


        widget = Ext.widget('device-zone-details', {
            router: router,
            deviceZoneId: currentZoneId,
            service: service,
            searchLink: "#search"
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        model.load(currentZoneId, {
            success: function (record) {
                var domainsStore = service.getSearchDomainsStore();
                domainsStore.load(function () {
                    service.applyState({
                        domain: 'com.energyict.mdc.device.data.Device',
                        filters: [
                            {
                                property: 'device.zoneType',
                                value: [{
                                    criteria: record.get("zoneTypeId"),
                                    operator: '=='
                                }]
                            },
                            {
                                property: 'device.zoneName',
                                value: [{
                                    criteria: currentZoneId,
                                    operator: '=='
                                }]

                            }
                        ]
                    });
                });

                widget.searchLink = service.getRouter().getRoute('search').buildUrl();
                Ext.suspendLayouts();
                widget.down('#device-zone-title').setTitle(record.get('name'));
                widget.down('form').loadRecord(record);

                me.getApplication().fireEvent('loadDeviceZone', record);
                Ext.resumeLayouts(true);
            }
        });
    },
    showDeviceZonePreview: function(records,record) {
        Ext.suspendLayouts();
        this.getDeviceZonePreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        this.getDeviceZonePreviewForm().loadRecord(record);
        Ext.resumeLayouts();
    },

    onRemoveDeviceZone: function(deviceRecord)
    {
        var zoneRecord = this.getDeviceZoneDetails().getRecord();
        this.removeDeviceZone(zoneRecord, deviceRecord);
    },

    removeDeviceZone: function (zoneRecord, deviceRecord) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            gridDevices =  this.getDevicesOfZoneGrid();

        confirmationWindow.show({
            msg: Uni.I18n.translate('devicezone.general.removeDevice.msg', 'MDC', 'The device will be unlinked from this zone.'),
            title: Uni.I18n.translate('devicezone.removeDevice', 'MDC', "Remove device '{0}'?", deviceRecord.get("name")),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    gridDevices.setLoading(true);
                    zoneRecord.destroy({
                        url: '/api/ddr/devices/' + encodeURIComponent(deviceRecord.get("name")) + '/zones/byZoneId/',
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicezone.remove.success.msg', 'MDC', "Device removed from zone '{0}'", zoneRecord.get("name")));
                            gridDevices.getStore().load();
                        },
                        callback: function () {
                            gridDevices.setLoading(false);
                        }
                    });
                } else if (state === 'cancel') {
                    this.close();
                    gridDevices.setLoading(false);
                }
            }
        });
    },

    chooseActionGrid: function (menu, item) {
        var me = this,
            zoneRecord = this.getDeviceZoneDetails().getRecord(),
            deviceRecord = menu.record || this.getDevicesOfZoneGrid().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'deleteDeviceZone':
                me.removeDeviceZone(zoneRecord,deviceRecord);
                break;
        }
    },

    chooseActionZone: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        var zoneRecord =  this.getDeviceZoneDetails().getRecord();
        router.arguments.id = zoneRecord.get('id');

        switch (item.action) {
            case 'editZone':
                router.getRoute('administration/zones/edit').forward({zoneId: router.arguments.id});
                break;
            case 'deleteZone':
                me.removeZone(zoneRecord);
                break;
        }
    },

    removeZone: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            router = me.getController('Uni.controller.history.Router');

        confirmationWindow.show({
            msg: Uni.I18n.translate('zone.general.remove.msg', 'MDC', 'This zone will no longer be available.'),
            title: Uni.I18n.translate('zone.remove', 'MDC', "Remove '{0}'?", [record.data.name]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    record.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('zone.remove.success.msg', 'MDC', 'Zone removed'));
                            router.getRoute('administration/zones').forward();
                        },
                        callback: function () {
                        }
                    });
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },


});