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
            showPage = function () {
                loadProfileOfDeviceModel.getProxy().setUrl(mRID);
                widget = Ext.widget('deviceLoadProfilesOverview', {
                    mRID: mRID,
                    router: me.getController('Uni.controller.history.Router')
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);
                deviceModel.load(mRID, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadDevice', record);
                    }
                });
                loadProfileOfDeviceModel.load(loadProfileId, {
                    success: function (record) {
                        if (!widget.isDestroyed) {
                            me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                            widget.down('#deviceLoadProfilesPreviewForm').loadRecord(record);
                            widget.down('#deviceLoadProfilesSubMenuPanel').setParams(mRID, record);
                            widget.setLoading(false);
                        }
                    }
                });
            };

        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
    }
});