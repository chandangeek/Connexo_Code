Ext.define('Mdc.controller.setup.DeviceLoadProfiles', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Setup'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice'
    ],

    stores: [
        'Mdc.store.LoadProfilesOfDevice',
        'Mdc.store.TimeUnits'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'deviceLoadProfilesSetup #deviceLoadProfilesPreview'
        }
    ],

    init: function () {
        this.control({
            'deviceLoadProfilesSetup #deviceLoadProfilesGrid': {
                select: this.showPreview
            },
            '#deviceLoadProfilesActionMenu': {
                click: this.chooseAction
            }
        });
    },

    showView: function (mRID) {
        var me = this,
            model = me.getModel('Mdc.model.Device'),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            widget,
            showPage = function () {
                me.getStore('Mdc.store.LoadProfilesOfDevice').getProxy().setUrl(mRID);
                widget = Ext.widget('deviceLoadProfilesSetup', {
                    mRID: mRID,
                    router: me.getController('Uni.controller.history.Router')
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                model.load(mRID, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadDevice', record);
                        widget.down('#stepsMenu').setTitle(record.get('mRID'));
                    }
                });
            };
        me.mRID = mRID;
        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            loadProfileId = record.get('id'),
            preview = me.getPreview();
        preview.setTitle(record.get('name'));
        loadProfileOfDeviceModel.getProxy().setUrl(me.mRID);
        preview.up('deviceLoadProfilesSetup').setLoading();
        loadProfileOfDeviceModel.load(loadProfileId, {
            success: function (rec) {
                preview.down('#deviceLoadProfilesPreviewForm').loadRecord(rec);
                preview.up('deviceLoadProfilesSetup').setLoading(false);
            }
        });
        preview.down('#deviceLoadProfilesActionMenu').record = record;
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route;

        routeParams.loadProfileId = menu.record.getId();

        switch (item.action) {
            case 'viewChannels':
                route = 'devices/device/loadprofiles/loadprofile/channels';
                break;
            case 'viewData':
                route = 'devices/device/loadprofiles/loadprofile/data';
                break;
            case 'viewDetails':
                route = 'devices/device/loadprofiles/loadprofile/overview';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams);
    }
});