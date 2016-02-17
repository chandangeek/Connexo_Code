Ext.define('Scs.controller.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Scs.view.Setup',
        'Scs.view.Landing',
        'Scs.view.SetupOverview',
        'Scs.view.ServiceCallPreviewContainer'
    ],
    stores: [
        'Scs.store.ServiceCalls',
        'Scs.store.Logs'
    ],
    models: [
    ],

    refs: [
        {
            ref: 'page',
            selector: 'service-call-preview-container'
        }
    ],

    init: function () {
        this.control({
            'service-call-preview-container servicecalls-grid': {
                select: this.showPreview
            },
            'scs-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showServiceCalls: function() {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls'),
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
            me.getApplication().fireEvent('servicecallload', arguments);
            record = store.getAt(store.find('internalId', servicecallId));
            if (record.get('hasChildren')) {
                view = Ext.widget('servicecalls-setup-overview', {
                    router: me.getController('Uni.controller.history.Router'),
                    serviceCallId: servicecallId
                });
            } else {
                view = Ext.widget('scs-landing-page', {serviceCallId: servicecallId});

            }
            view.down('scs-landing-page-form').updateLandingPage(record);
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