Ext.define('Mdc.controller.setup.DeviceLoadProfileChannels', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofilechannels.Setup'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
    ],

    stores: [
        'Mdc.store.ChannelsOfLoadProfilesOfDevice',
        'Mdc.store.TimeUnits'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'deviceLoadProfileChannelsSetup #deviceLoadProfileChannelsPreview'
        }
    ],

    init: function () {
        this.control({
            'deviceLoadProfileChannelsSetup #deviceLoadProfileChannelsGrid': {
                select: this.showPreview
            },
            '#deviceLoadProfileChannelsActionMenu': {
                click: this.chooseAction
            }
        });
    },

    showOverview: function (mRID, loadProfileId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            channelsOfLoadProfilesOfDeviceStore = me.getStore('Mdc.store.ChannelsOfLoadProfilesOfDevice'),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            widget,
            showPage = function () {
                channelsOfLoadProfilesOfDeviceStore.getProxy().setUrl({
                    mRID: mRID,
                    loadProfileId: loadProfileId
                });
                channelsOfLoadProfilesOfDeviceStore.load();
                widget = Ext.widget('deviceLoadProfileChannelsSetup', {
                    mRID: mRID,
                    loadProfileId: loadProfileId,
                    router: me.getController('Uni.controller.history.Router')
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                deviceModel.load(mRID, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadDevice', record);
                    }
                });
                loadProfileOfDeviceModel.getProxy().setUrl(mRID);
                loadProfileOfDeviceModel.load(loadProfileId, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                        widget.down('#deviceLoadProfilesSubMenuPanel').setParams(mRID, record);
                        widget.down('#deviceLoadProfileChannelsIntervalAndLastReading').loadRecord(record);
                    }
                });
            };

        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        preview.setTitle(record.get('name'));
        preview.down('#deviceLoadProfileChannelsPreviewForm').loadRecord(record);
        preview.down('#deviceLoadProfileChannelsActionMenu').record = record;
    },

    chooseAction: function (menu, item) {
        var router = this.getController('Uni.controller.history.Router'),
            route;

        switch (item.action) {
            case 'viewData':
                route = 'devices/device/loadprofiles/loadprofile/channels/channel/data';
                break;
            case 'viewDetails':
                route = 'devices/device/loadprofiles/loadprofile/channels/channel/overview';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward({
            channelId: menu.record.getId()
        });
    }
});