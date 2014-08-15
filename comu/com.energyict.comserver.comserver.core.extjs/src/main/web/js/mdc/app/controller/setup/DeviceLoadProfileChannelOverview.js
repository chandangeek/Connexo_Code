Ext.define('Mdc.controller.setup.DeviceLoadProfileChannelOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofilechannels.Overview'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
    ],

    showOverview: function (mRID, loadProfileId, channelId) {
        var me = this,
            loadProfileModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            channelModel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            widget = Ext.widget('deviceLoadProfileChannelOverview', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });

        loadProfileModel.getProxy().setUrl(mRID);
        loadProfileModel.load(loadProfileId, {
            success: function (record) {
                me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
            }
        });

        channelModel.getProxy().setUrl({
            mRID: mRID,
            loadProfileId: loadProfileId
        });
        channelModel.load(channelId, {
            success: function (record) {
                me.getApplication().fireEvent('channelOfLoadProfilesOfDeviceLoad', record);
                widget.down('#deviceLoadProfileChannelsPreviewForm').loadRecord(record);
                widget.down('#deviceLoadProfileChannelSubMenuPanel').setParams(mRID, loadProfileId, record);
                widget.setLoading(false);
            }
        });
    }
});