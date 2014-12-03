Ext.define('Mdc.controller.setup.DeviceLogbooks', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicelogbooks.Setup'
    ],

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.LogbooksOfDevice'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'deviceLogbooksSetup #deviceLogbooksPreview'
        }
    ],

    init: function () {
        this.control({
            'deviceLogbooksSetup #deviceLogbooksGrid': {
                select: this.showPreview
            },
            '#deviceLogbooksActionMenu': {
                click: this.chooseAction
            }
        });
    },

    showView: function (mRID) {
        var me = this,
            model = me.getModel('Mdc.model.Device'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewport.setLoading();

        me.getStore('Mdc.store.LogbooksOfDevice').getProxy().setUrl(mRID);
        model.load(mRID, {
            success: function (record) {
                widget = Ext.widget('deviceLogbooksSetup', {
                    device: record,
                    router: me.getController('Uni.controller.history.Router')
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', record);
                viewport.setLoading(false);

            }
        });
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        preview.setTitle(record.get('name'));
        preview.down('#deviceLogbooksPreviewForm').loadRecord(record);
        preview.down('#deviceLogbooksActionMenu').record = record;
    },

    chooseAction: function (menu, item) {
        var router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route;

        routeParams.logbookId = menu.record.getId();

        switch (item.action) {
            case 'viewData':
                route = 'devices/device/logbooks/logbook/data';
                break;
            case 'viewDetails':
                route = 'devices/device/logbooks/logbook/overview';
                break;
        }

        if (route) {
            route = router.getRoute(route);
            if (route) {
                route.forward(routeParams);
            }
        }
    }
});