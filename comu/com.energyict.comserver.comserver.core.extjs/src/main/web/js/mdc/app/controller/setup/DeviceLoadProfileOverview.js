Ext.define('Mdc.controller.setup.DeviceLoadProfileOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Overview'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice'
    ],

    stores: [
        'Mdc.store.TimeUnits'
    ],

    showOverview: function (mRID, loadProfileId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            proxy = loadProfileOfDeviceModel.getProxy(),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            widget,
            showPage = function () {
                proxy.url = proxy.url.replace('{mRID}', mRID);
                widget = Ext.widget('deviceLoadProfilesOverview', {mRID: mRID});
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);
                deviceModel.load(mRID, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadDevice', record);
                    }
                });
                loadProfileOfDeviceModel.load(loadProfileId, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                        widget.down('#deviceLoadProfilesPreviewForm').loadRecord(record);
                        widget.down('#deviceLoadProfilesSubMenuPanel').setParams(mRID, record);
                        widget.setLoading(false);
                    }
                });
            };

        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
    }
});