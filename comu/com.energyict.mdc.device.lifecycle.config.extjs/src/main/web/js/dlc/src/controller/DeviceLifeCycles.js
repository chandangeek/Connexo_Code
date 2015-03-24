Ext.define('Dlc.controller.DeviceLifeCycles', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.view.devicelifecycles.Setup'
    ],

    stores: [
        'Dlc.store.DeviceLifeCycles'
    ],

    models: [
        'Dlc.model.DeviceLifeCycle'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-life-cycles-setup'
        }
    ],

    init: function () {
        this.control({
            'device-life-cycles-setup device-life-cycles-grid': {
                select: this.showDeviceLifeCyclePreview
            }
        });
    },

    showDeviceLifeCycles: function () {
        var me = this,
            view = Ext.widget('device-life-cycles-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', view);
        view.down('device-life-cycles-grid').getSelectionModel().select(0);
    },

    showDeviceLifeCyclePreview: function (selectionModel, record, index) {
        var me = this,
            page = me.getPage(),
            preview = page.down('device-life-cycles-preview'),
            previewForm = page.down('device-life-cycles-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});