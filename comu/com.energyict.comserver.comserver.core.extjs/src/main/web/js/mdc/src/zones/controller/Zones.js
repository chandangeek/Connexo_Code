/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.zones.controller.Zones',{
    extend: 'Cfg.zones.controller.Zones',

    views: [
        'Mdc.view.setup.devicezones.DevicesOfZoneGrid',
        'Mdc.view.setup.devicezones.Details',
        'Mdc.view.setup.devicezones.PreviewForm',
        'Mdc.view.setup.devicezones.DeviceZonePreview'
    ],

    stores: [
        'Mdc.store.Zones',
        'Mdc.store.DevicesOfZone'
    ],

    refs: [
        {ref: 'devicesOfZoneGrid', selector: '#zone-details-devices-grid'},
        {ref: 'deviceZonePreview', selector: '#device-zone-preview'},
        {ref: 'deviceZonePreviewForm', selector: '#device-zone-preview-form'},
        {ref: 'editDeviceZoneMenuItem', selector: '#edit-device-zone'},
        {ref: 'deviceZoneDetailsForm', selector: '#deviceZoneDetailsForm'}
    ],

    init: function () {
        this.control({
            'zone-details #zone-details-devices-grid': {
                select: this.showDeviceZonePreview
            },
            '#zone-details-devices-grid': {
                zoneDeviceRemoveEvent: this.onRemoveDeviceZone
            },
            'device-zones-action-menu-from-zone': {
                click: this.chooseActionZone
            },
            'device-of-zone-action-menu': {
                click: this.chooseActionGrid
            },
            '#deviceZoneDetails button[action=viewDevicesInSearch]': {
                click: this.viewDevicesInSearch
            }
        });
    },

    viewZone: function (currentZoneId) {
        var me = this,
            model = me.getModel('Cfg.zones.model.Zone'),
            deviceZonesStore = Ext.getStore('Mdc.store.DevicesOfZone'),
            widget;

        model.load(currentZoneId, {
            success: function (record) {
                deviceZonesStore.getProxy().extraParams = {
                    zoneTypeId: record.get("zoneTypeId"),
                    zoneId: currentZoneId,
                    filter: Ext.JSON.encode([{"property": "device.zoneType", "value": [{"operator": "==", "criteria": record.get("zoneTypeId"), "filter": ""}]},
                                            {"property": "device.zoneName", "value": [{"operator": "==", "criteria": record.get("id"), "filter": ""}]}])
                };

                widget = Ext.widget('device-zone-details', {
                    itemId: 'deviceZoneDetails',
                    deviceZoneId: currentZoneId,
                    zoneTypeId: record.get("zoneTypeId")
                });
                widget.down('#deviceZoneDetailsForm').loadRecord(record);
                widget.down('#device-zone-title').setTitle(record.get('name'));
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDeviceZone', record);
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
        var zoneRecord = this.getDeviceZoneDetailsForm().getRecord();
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
            zoneRecord = this.getDeviceZoneDetailsForm().getRecord(),
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

        var zoneRecord =  this.getDeviceZoneDetailsForm().getRecord();
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

    viewDevicesInSearch: function () {
        var me = this,
            deviceZoneDetailsForm = me.getDeviceZoneDetailsForm(),
            zoneTypeId = deviceZoneDetailsForm.getRecord().get("zoneTypeId"),
            zoneId = deviceZoneDetailsForm.deviceZoneId,
            search = me.getController('Mdc.controller.Search'),
            router = me.getController('Uni.controller.history.Router'),
            service = search.service, filters = [];


        filters.push(new Ext.util.Filter({
            property: 'device.zoneType',
            value: [
                {
                    operator: "==",
                    criteria: zoneTypeId
                }],
            id: 'device.zoneType'
        }));

        filters.push(new Ext.util.Filter({
            property: 'device.zoneName',
            value: [
                {
                    operator: "==",
                    criteria: zoneId
                }],
            id: 'device.zoneName'
        }));

        var domainsStore = service.getSearchDomainsStore();
        domainsStore.load(function () {
            service.applyState({
                domain: 'com.energyict.mdc.common.device.data.Device',
                filters: filters
            }, function () {
                service.setFilters(filters);
                router.getRoute('search').forward(null, Ext.apply(router.queryParams, {restore: true}));
            });
        });
    }
});