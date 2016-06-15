Ext.define('Wss.controller.Webservices', {
    extend: 'Ext.app.Controller',

    views: [
        'Wss.view.Setup'
    ],
    stores: [
        'Wss.store.Endpoints'
    ],
    models: [
        'Wss.model.Endpoint'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'webservices-preview'
        }
    ],

    init: function () {
        this.control({
            'webservices-setup webservices-grid': {
                select: this.showPreview
            },
        });
    },

    showWebservicesOverview: function () {
        var me = this,
            view,
            store = me.getStore('Wss.store.Endpoints');

        view = Ext.widget('webservices-setup');
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function(selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('webservices-preview-form'),
            form = previewForm.down('form');

        form.loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        preview.down('webservices-action-menu').record = record;
    }
});