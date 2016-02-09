Ext.define('Scs.controller.ServiceCall', {
    extend: 'Ext.app.Controller',

    views: [
        'Scs.view.Setup'
    ],
    stores: [
        'Scs.store.ServiceCalls'
    ],
    models: [
    ],

    refs: [
        {
            ref: 'page',
            selector: 'servicecalls-setup'
        }
    ],

    init: function () {
        this.control({
            'servicecalls-setup servicecalls-grid': {
                select: this.showPreview
            },
            'scs-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showServiceCalls: function(){
        debugger;
        var me = this,
            view = Ext.widget('servicecalls-setup');

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('servicecalls-preview'),
            serviceCallTypeName = record.get('type'),
            previewForm = page.down('servicecalls-preview-form');

        preview.setTitle(serviceCallTypeName);
        previewForm.updatePreview(record);
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'test':
                debugger;
        }
    }
});