Ext.define('Mdc.controller.setup.DeviceLoadProfileChannelOverview', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.controller.setup.DeviceLoadProfileChannelData'
    ],

    views: [
        'Mdc.view.setup.deviceloadprofilechannels.Overview',
        'Mdc.view.setup.deviceloadprofilechannels.TabbedDeviceChannelsView'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
    ],

    showOverview: function (mRID, channelId, tabController) {
        var me = this,
            channelModel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router'),
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
            };

        defer.setCallback(function (device) {

            tabWidget = Ext.widget('tabbedDeviceChannelsView', {
                router: router,
                device: device,
                channelsListLink: me.getController('Mdc.controller.setup.DeviceLoadProfileChannelData').makeLinkToList(router)
            });

            widget = Ext.widget('deviceLoadProfileChannelOverview', {
                router: me.getController('Uni.controller.history.Router'),
                device: device
            });

            tabWidget.down('#channel-specifications').add(widget);
            tabController.showTab(0);
            me.getApplication().fireEvent('changecontentevent', tabWidget);
            widget.setLoading(true);
            channelModel.getProxy().setUrl({
                mRID: mRID
            });
            channelModel.load(channelId, {
                success: function (record) {
                    if (!widget.isDestroyed) {
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                        tabWidget.down('#channelTabPanel').setTitle(record.get('name'));
                        widget.down('#deviceLoadProfileChannelsOverviewForm').loadRecord(record);
                        widget.setLoading(false);
                        widget.down('deviceLoadProfileChannelsActionMenu').record = record;
                    }
                }
            });
        });


        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                defer.setParam(record)
            }
        });

    }
});