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
        'TimeUnits'
    ],

    showOverview: function (mRID, loadProfileId, tabController, loadProfile) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            timeUnitsStore = me.getStore('TimeUnits'),
            widget,
            router = me.getController('Uni.controller.history.Router'),
            tabWidget,
            deffered = function () {
                var self = this;
                self.param = null;
                self.callback = null;
                self.resolve = function (arg) {
                    arg && self.callback.apply(self, self.param)
                };
                self.setCallback = function (fn) {
                    self.callback = fn;
                    self.resolve(self.param)
                };
                self.setParam = function () {
                    self.param = arguments;
                    self.resolve(self.callback)
                }
            },
            defer = new deffered(),
            loadProfileDefer = new deffered();

        if (loadProfile) {
            me.setLoadProfile(loadProfile);
            loadProfileDefer.setParam(loadProfile);
        } else {
            loadProfileOfDeviceModel.getProxy().setUrl(mRID);
            loadProfileOfDeviceModel.load(loadProfileId, {
                success: function (record) {
                    me.setLoadProfile(record);
                    loadProfileDefer.setParam(record);
                }
            });
        }

        showPage = function () {
            defer.setCallback(function (device) {
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
                me.getApplication().fireEvent('changecontentevent', tabWidget);
                Ext.suspendLayouts();
                tabWidget.down('#deviceLoadProfileDataSideFilter').setVisible(false);
                tabWidget.down('#loadProfile-specifications').add(widget);
                tabController.showTab(0);
                widget.setLoading(true);
                updateView = function (record) {
                    if (!widget.isDestroyed) {
                        me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                        widget.down('#deviceLoadProfilesPreviewForm').loadRecord(record);
                        tabWidget.down('#loadProfileTabPanel').setTitle(record.get('name'));
                        widget.down('deviceLoadProfilesActionMenu').record = record;
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                };
                loadProfileDefer.setCallback(updateView);
            });
        };

        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                defer.setParam(record)
            }
        });

        if (timeUnitsStore.getCount() > 0) {
            showPage();
        } else {
            timeUnitsStore.on('load', showPage, me, {single: true});
            timeUnitsStore.load();
        }
    },

    getLoadProfile: function () {
        return this.loadProfile
    },

    setLoadProfile: function (loadProfile) {
        this.loadProfile = loadProfile
    }
});