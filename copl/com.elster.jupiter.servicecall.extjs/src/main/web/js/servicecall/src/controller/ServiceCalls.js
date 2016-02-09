Ext.define('Scs.controller.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Scs.view.Setup',
        'Scs.view.Landing'
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

    showServiceCalls: function() {
        var me = this,
            view = Ext.widget('servicecalls-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showServiceCallOverview: function() {
        var me = this,
            record,
            store = Ext.getStore('Scs.store.ServiceCalls'),
            view,
            servicecallId = arguments[arguments.length - 1];

        //TODO: check if the service call has children or not using the correct rest info, then decide what screen to show
        if (servicecallId) {
            record = store.getAt(store.find('internalId', servicecallId));
            if (record.get('hasChildren')) {
                view = Ext.widget('servicecalls-setup', {
                    router: me.getController('Uni.controller.history.Router')
                });
            } else {
                view = Ext.widget('servicecall-landing', {});

            }

            me.getApplication().fireEvent('changecontentevent', view);
        }
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
            case 'cancel':
                debugger;
                break;
            case 'pause':
                break;
            case 'resume':
                break;
            case 'retry':
                break;
        }
    }
});