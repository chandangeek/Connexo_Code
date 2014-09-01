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
            widget;

        me.getStore('Mdc.store.LogbooksOfDevice').getProxy().setUrl(mRID);
        widget = Ext.widget('deviceLogbooksSetup', {
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

        route && (route = router.getRoute(route));
        route && route.forward(routeParams);
    }
});