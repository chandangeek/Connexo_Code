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

    showOverview: function (mRID, loadProfileId, tabController) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            timeUnitsStore = me.getStore('TimeUnits'),
            widget,
            tabWidget,
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
//                    debugger;
                    loadProfileOfDeviceModel.getProxy().setUrl(mRID);
                    tabWidget = Ext.widget('tabbedDeviceLoadProfilesView',{
                        device: device,
                        loadProfileId: loadProfileId,
                        toggleId: 'loadProfileLink',
                        router: me.getController('Uni.controller.history.Router')
                    });
                    widget = Ext.widget('deviceLoadProfilesOverview', {
                        mRID: mRID,
                        router: me.getController('Uni.controller.history.Router'),
                        device: device
                    });
                    me.getApplication().fireEvent('changecontentevent', tabWidget);
                    tabWidget.down('#deviceLoadProfileDataSideFilter').setVisible(false);
                    tabWidget.down('#loadProfileTabPanel').setTitle(Uni.I18n.translate('general.overview', 'MDC', 'Overview'));
                    tabWidget.down('#loadProfile-specifications').add(widget);
                    tabController.showTab(0);
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
        if (timeUnitsStore.getCount() > 0) {
            showPage();
        } else {
            timeUnitsStore.on('load', showPage, me, {single: true});
            timeUnitsStore.load();
        }
    }
});