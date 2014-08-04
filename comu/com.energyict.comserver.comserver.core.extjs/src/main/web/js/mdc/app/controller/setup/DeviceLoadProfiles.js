Ext.define('Mdc.controller.setup.DeviceLoadProfiles', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Setup'
    ],

    models: [
        'Mdc.model.Device'
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
            'deviceLoadProfilesSetup #deviceLoadProfilesActionMenu': {
                click: this.chooseAction
            }
        });
    },

    showView: function (mRID) {
        var me = this,
            model = me.getModel('Mdc.model.Device'),
            proxy = me.getStore('Mdc.store.LoadProfilesOfDevice').getProxy(),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            widget,
            showPage = function () {
                proxy.url = proxy.url.replace('{mRID}', mRID);
                widget = Ext.widget('deviceLoadProfilesSetup', {mRID: mRID});
                me.getApplication().fireEvent('changecontentevent', widget);
                model.load(mRID, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadDevice', record);
                        widget.down('#stepsMenu').setTitle(record.get('mRID'));
                    }
                });
            };

        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        preview.setTitle(record.get('name'));
        preview.down('#deviceLoadProfilesPreviewForm').loadRecord(record);
        preview.down('#deviceLoadProfilesActionMenu').record = record;
    },

    chooseAction: function (menu, item) {
        var router = this.getController('Uni.controller.history.Router'),
            id = menu.record.getId(),
            route;

//        todo: change routes when respective features be implemented
        switch (item.action) {
            case 'viewChannels':
                route = '';
                break;
            case 'viewData':
                route = '';
                break;
            case 'viewDetails':
                route = '';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward({id: id});
    }
});