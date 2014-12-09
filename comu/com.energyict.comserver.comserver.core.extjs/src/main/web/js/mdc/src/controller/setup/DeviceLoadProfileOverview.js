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

    showOverview: function (mRID, loadProfileId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            timeUnitsStore = me.getStore('TimeUnits'),
            widget,
            defer = {
                param: null,
                callback: null,
                resolve: function (arg) {
                    arg && this.callback.apply(this, this.param)
                },
                setCallback: function (fn) {
                    this.callback = fn;
                    this.resolve(this.param)
                },
                setParam: function () {
                    this.param = arguments;
                    this.resolve(this.callback)
                }
            },
            showPage = function () {
                defer.setCallback(function (device) {
                    loadProfileOfDeviceModel.getProxy().setUrl(mRID);
                    widget = Ext.widget('deviceLoadProfilesOverview', {
                        mRID: mRID,
                        router: me.getController('Uni.controller.history.Router'),
                        device: device
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.setLoading(true);

                    loadProfileOfDeviceModel.load(loadProfileId, {
                        success: function (record) {
                            if (!widget.isDestroyed) {
                                me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                                widget.down('#deviceLoadProfilesPreviewForm').loadRecord(record);
                                widget.down('deviceLoadProfilesActionMenu').record = record;
                                widget.setLoading(false);
                            }
                        }
                    });
                });
            };

        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                defer.setParam(record)
            }
        });

        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
    }
});