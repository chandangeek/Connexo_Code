Ext.define('Mdc.controller.setup.DeviceLoadProfileOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Overview'
    ],

    requires: [
        'Mdc.store.TimeUnits'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice'
    ],

    stores: [
        'TimeUnits',
        'Mdc.store.LoadProfilesOfDevice'
    ],

    showOverview: function (deviceId, loadProfileId, tabController, loadProfile) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            timeUnitsStore = me.getStore('TimeUnits'),
            widget,
            router = me.getController('Uni.controller.history.Router'),
            loadProfilesStore = me.getStore('Mdc.store.LoadProfilesOfDevice'),
            tabWidget;

        if(loadProfile){
            if (loadProfile.data.id != loadProfileId || loadProfile.data.parent.id != deviceId) {
                loadProfile = null;
            }
        }

        timeUnitsStore.load();
        deviceModel.load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                tabWidget = Ext.widget('tabbedDeviceLoadProfilesView', {
                    device: device,
                    loadProfileId: loadProfileId,
                    toggleId: 'loadProfileLink',
                    router: router
                });
                widget = Ext.widget('deviceLoadProfilesOverview', {
                    deviceId: deviceId,
                    router: router,
                    device: device
                });
                var func = function () {
                    me.getApplication().fireEvent('changecontentevent', tabWidget);
                    Ext.suspendLayouts();
                    tabWidget.down('#loadProfile-specifications').add(widget);
                    tabController.showTab(0);

                    var updateView = function (record) {
                        var menu = widget.down('deviceLoadProfilesActionMenu');
                        if (!widget.isDestroyed) {
                            me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                            widget.down('#deviceLoadProfilesPreviewForm').loadRecord(record);
                            tabWidget.down('#loadProfileTabPanel').setTitle(record.get('name'));
                            if (menu) {
                                menu.record = record;
                                var validateNowLoadProfile = menu.down('#validateNowLoadProfile');
                                if (validateNowLoadProfile) {
                                    validateNowLoadProfile.setVisible(record.get('validationInfo').validationActive);
                                }
                            }
                            Ext.resumeLayouts(true);
                        }
                    };

                    tabWidget.setLoading(true);
                    if (loadProfile) {
                        me.setLoadProfile(loadProfile);
                        updateView(loadProfile);
                        tabWidget.setLoading(false);
                    } else {
                        loadProfileOfDeviceModel.getProxy().setExtraParam('deviceId', deviceId);
                        loadProfileOfDeviceModel.load(loadProfileId, {
                            success: function (record) {
                                me.setLoadProfile(record);
                                updateView(record);
                            },
                            callback: function () {
                                tabWidget.setLoading(false);
                            }
                        });
                    }
                };
                if (loadProfilesStore.getTotalCount() === 0) {
                    loadProfilesStore.getProxy().setExtraParam('deviceId', deviceId);
                    loadProfilesStore.load(function () {
                        func();
                    });
                } else {
                    func();
                }
            }
        });
    },

    getLoadProfile: function () {
        return this.loadProfile;
    },

    setLoadProfile: function (loadProfile) {
        this.loadProfile = loadProfile;
    }
});