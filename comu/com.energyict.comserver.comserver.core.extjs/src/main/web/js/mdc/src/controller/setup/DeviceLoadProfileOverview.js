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

    showOverview: function (mRID, loadProfileId, tabController, loadProfile) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            timeUnitsStore = me.getStore('TimeUnits'),
            widget,
            router = me.getController('Uni.controller.history.Router'),
            loadProfilesStore = me.getStore('Mdc.store.LoadProfilesOfDevice'),
            tabWidget;

        timeUnitsStore.load();
        deviceModel.load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                tabWidget = Ext.widget('tabbedDeviceLoadProfilesView', {
                    device: device,
                    loadProfileId: loadProfileId,
                    toggleId: 'loadProfileLink',
                    router: router
                });
                widget = Ext.widget('deviceLoadProfilesOverview', {
                    mRID: mRID,
                    router: router,
                    device: device
                });
                var func = function () {
                    me.getApplication().fireEvent('changecontentevent', tabWidget);
                    Ext.suspendLayouts();
                    tabWidget.down('#loadProfile-specifications').add(widget);
                    tabController.showTab(0);

                    var updateView = function (record) {
                        if (!widget.isDestroyed) {
                            me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                            widget.down('#deviceLoadProfilesPreviewForm').loadRecord(record);
                            tabWidget.down('#loadProfileTabPanel').setTitle(record.get('name'));
                            widget.down('deviceLoadProfilesActionMenu').record = record;
                            Ext.resumeLayouts(true);
                        }
                    };

                    tabWidget.setLoading(true);
                    if (loadProfile) {
                        me.setLoadProfile(loadProfile);
                        updateView(loadProfile);
                        tabWidget.setLoading(false);
                    } else {
                        loadProfileOfDeviceModel.getProxy().setUrl(mRID);
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
                    loadProfilesStore.getProxy().setUrl(mRID);
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