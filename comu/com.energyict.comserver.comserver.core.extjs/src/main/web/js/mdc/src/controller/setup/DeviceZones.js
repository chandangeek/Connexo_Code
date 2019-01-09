/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceZones', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.view.setup.devicezones.ZonesSetup',
        'Mdc.view.setup.devicezones.ZonesGrid',
        'Mdc.view.setup.devicezones.ZonesPreview',
        'Mdc.view.setup.devicezones.ZoneAdd',
        'Mdc.view.setup.devicezones.ZoneEdit',
        'Mdc.model.Device',
        'Mdc.model.DeviceZone',
        'Mdc.model.DeviceZones',
        'Cfg.zones.model.Zone'

    ],

    views: [
        'Mdc.view.setup.devicezones.ZonesSetup',
        'Mdc.view.setup.devicezones.ZonesGrid'
    ],

    stores: [
        'Mdc.store.DeviceZones'
    ],
    models: [
        'Mdc.model.DeviceZones'
    ],

    refs: [
        {ref: 'deviceZonesGrid', selector: '#grd-device-zones'},
        {ref: 'deviceZonesSetup', selector: '#device-zones-setup'},
        {ref: 'deviceZonesPreview', selector: "#device-zones-preview"},
        {ref: 'deviceZonesPreviewForm', selector: "#device-zones-preview-form"},
        {ref: 'addPropertyForm', selector: 'device-zone-add #device-zone-add-property-form'},
        {ref: 'addZoneForm', selector: '#device-zone-add-form'},
        {ref: 'editPropertyForm', selector: 'device-zone-edit #device-zone-edit-property-form'},
        {ref: 'editZoneForm', selector: '#device-zone-edit-form'},
    ],

    init: function () {
        this.control({
                '#grd-device-zones': {
                    select: this.showDeviceZonePreview
                },
                '#grd-device-zones #deviceAddZoneButton': {
                    click: this.navigateAdd
                },
                'device-zone-add #mdc-zone-cancel-button': {
                    click: this.cancelClick
                },
                'device-zone-add #mdc-zone-add-button': {
                    click: this.addZone
                },
                'device-zone-edit #mdc-zone-save-button': {
                    click: this.saveZone
                },
                '#device-zones-action-menu': {
                    click: this.chooseAction
                }
            }
        );
    },

    showDeviceZoneView: function (deviceId) {
        var me = this;
            viewport = Ext.ComponentQuery.query('viewport')[0],
            zonesStore = me.getStore('Mdc.store.DeviceZones');

        zonesStore.getProxy().setUrl(deviceId);
        viewport.setLoading();


        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                zonesStore.load({
                    callback: function (records) {

                        me.getStore('Mdc.store.DeviceZones').getProxy().setExtraParam('deviceId', device.get('name'));

                        var widget = Ext.widget('device-zones-setup', {device: device});

                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        viewport.setLoading(false);

                        //me.getDeviceZonesGrid().getSelectionModel().doSelect(0);
                    }
                })

            }
        });
    },

    showDeviceZonePreview: function(records,record) {
        Ext.suspendLayouts();
        this.getDeviceZonesPreview().setTitle(Ext.String.htmlEncode(record.get('zoneName')));
        this.getDeviceZonesPreviewForm().loadRecord(record);
        Ext.resumeLayouts();
    },

    navigateAdd: function (btn) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/zones/add').forward({deviceId: encodeURIComponent(btn.deviceId)});
    },

    addZone: function (btn) {

        var me = this,
            propertyForm = this.getAddPropertyForm(),
            zoneForm = this.getAddZoneForm();

        if (propertyForm && propertyForm.isValid()) {
            zoneForm.down('#form-errors').hide();
            propertyForm.updateRecord();
            var newRecord = zoneForm.getRecord() || Ext.create('Mdc.model.DeviceZone');

            newRecord.beginEdit();
            newRecord.set('zoneId', zoneForm.down('#zone-name').getValue());


            newRecord.endEdit();
            newRecord.save({
                url: '/api/ddr/devices/' + encodeURIComponent(btn.deviceId) + '/zones',
                method: 'POST',
                success: function (record, operation) {
                    if (operation.success) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceZone.overview.addSuccess', 'MDC', 'Zone added'));
                        var router = me.getController('Uni.controller.history.Router');
                        router.getRoute('devices/device/zones').forward();
                    }
                },
                failure: function (record, operation) {
                    if (operation && operation.response && operation.response.status === 400) {
                        me.formMarkInvalid(Ext.decode(operation.response.responseText));
                        zoneForm.down('#form-errors').show();
                        newRecord.set('id', messageSpecification.id);
                    }
                }
            });
        } else {
            zoneForm.down('#form-errors').show();
        }

    },

    saveZone: function (btn) {

        var me = this,
            propertyForm = this.getEditPropertyForm(),
            zoneForm = this.getEditZoneForm();

        if (propertyForm && propertyForm.isValid()) {
            zoneForm.down('#form-errors').hide();
            propertyForm.updateRecord();

            var jsonData = {
                zoneId: zoneForm.down('#zone-name').getValue()
            };
            Ext.Ajax.request({
                url: '/api/ddr/devices/' + encodeURIComponent(btn.deviceId) + '/zones/' + encodeURIComponent(btn.deviceZoneId) ,
                method: 'PUT',
                jsonData: Ext.encode(jsonData),
                success: function (response) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceZone.overview.addSuccess', 'MDC', 'Zone added'));
                    var router = me.getController('Uni.controller.history.Router');
                    router.getRoute('devices/device/zones').forward();
                },
                failure: function (response) {
                    if (response && response.status === 400) {
                        //me.formMarkInvalid(Ext.decode(response.responseText));
                        //zoneForm.down('#form-errors').show();
                    }
                }
            });


        } else {
            zoneForm.down('#form-errors').show();
        }

    },

    showEditOverview: function (deviceId, zoneId) {
        var me = this,
            deviceTypesStore = me.getStore('Mdc.store.DeviceZonesTypes'),
            zoneModel =  Ext.ModelManager.getModel('Cfg.zones.model.Zone'),
            zones = this.getDeviceZonesGrid().getSelectionModel().getSelection(),
            deviceZoneId = zones[0].get('id'),
            deviceZoneTypeId = zones[0].get('zoneTypeId');

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                zoneModel.load(zoneId, {
                    success: function(zone) {
                        var widget = Ext.widget('device-zone-edit', {
                            device: device,
                            deviceZoneId: deviceZoneId,
                            deviceZoneTypeId: deviceZoneTypeId,
                            edit: true,
                            title: Uni.I18n.translate('deviceZone.add.title', 'MDC', "Edit '{0}'", zone.get('name'))
                        });
                        if (deviceId) {
                            deviceTypesStore.getProxy().setUrl(deviceId);
                            deviceTypesStore.load();
                        }
                        widget.down('#device-zone-edit-form').loadRecord(zone);
                        me.getApplication().fireEvent('loadZonesOnDevice', zone);
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('changecontentevent', widget);

                    }
                });

            }
        });
    },

    showAddOverview: function (deviceId) {
        var me = this,
            deviceTypesStore = me.getStore('Mdc.store.DeviceZonesTypes');

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getStore('Mdc.store.DeviceZones').getProxy().setExtraParam('deviceId', device.get('name'));
                widget = Ext.widget('device-zone-add', {
                    device: device,
                    edit: false,
                    title: Uni.I18n.translate('deviceZone.add.title', 'MDC', 'Add zone')
                });
                if (deviceId) {
                    deviceTypesStore.getProxy().setUrl(deviceId);
                    deviceTypesStore.load();
                }
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    cancelClick: function (btn) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/zones').forward({deviceId: encodeURIComponent(btn.deviceId)});
    },

    removeZone: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            router = me.getController('Uni.controller.history.Router'),
            form =  this.getDeviceZonesGrid();


        form.setLoading(true);
        confirmationWindow.show({
            msg: Uni.I18n.translate('devicezone.general.remove.msg', 'MDC', 'The device will be unlinked from this zone.'),
            title: Uni.I18n.translate('devicezone.remove', 'MDC', "Remove from zone '{0}'?", [record.data.zoneName]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    record.destroy({
                        url: '/api/ddr/devices/' + encodeURIComponent(form.deviceId) + '/zones',
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicezone.remove.success.msg', 'MDC', "Device removed from zone '{0}'",[record.data.zoneName]));
                            router.getRoute('devices/device/zones').forward();
                        },
                        callback: function () {
                            form.setLoading(false);
                        }
                    });
                } else if (state === 'cancel') {
                    this.close();
                    form.setLoading(false);
                }
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments;

        var zones = this.getDeviceZonesGrid().getSelectionModel().getSelection();
        routeParams.deviceZoneId = menu.record.get("id");
        routeParams.zoneId = menu.record.get("zoneId");

        switch (item.action) {
            case 'editZone':
                router.getRoute('devices/device/zones/edit').forward(routeParams);
                break;
            case 'deleteZone':
                me.removeZone(zones[0]);
                break;
        }
    },

});