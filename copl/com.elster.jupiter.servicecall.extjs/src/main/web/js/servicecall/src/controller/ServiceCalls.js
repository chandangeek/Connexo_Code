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
        'Scs.model.ServiceCall'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'service-call-preview-container'
        },
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
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
            view;

        store.setProxy({
            type: 'rest',
            url: '/api/scs/servicecalls',
            timeout: 120000,
            reader: {
                type: 'json',
                root: 'serviceCalls'
            }
        });
        view= Ext.widget('servicecalls-setup', {
                router: me.getController('Uni.controller.history.Router'),
                store: store
        });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showServiceCallOverview: function() {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls'),
            logStore = Ext.getStore('Scs.store.Logs'),
            view,
            servicecallIds = Array.prototype.slice.call(arguments),
            servicecallId = arguments[arguments.length - 1],
            parentsIdArray = [],
            parentsNameArray = [];
        if (servicecallId) {
            store.setProxy({
                type: 'rest',
                url: '/api/scs/servicecalls/' + servicecallId + '/children',
                timeout: 120000,
                reader: {
                    type: 'json',
                    root: 'serviceCalls'
                }
            });
            me.getModel('Scs.model.ServiceCall').load(servicecallId, {
                success: function (record) {
                    logStore.getProxy().setUrl(servicecallId);
                    //logStore.load({
                    //
                    //})
                    Ext.each(record.get('parents'),function(item){
                        parentsIdArray.push(item.id+'');
                        parentsNameArray.push(item.name);
                    });


                 //   compareParentsArray = record.get('parents').slice();
                    parentsNameArray.push(record.get('name'));
                    parentsIdArray.push(servicecallId);
                    if(!me.isEqual(servicecallIds, parentsIdArray)) {
                        view = Ext.widget('errorNotFound');
                        me.getBreadcrumbs().hide();
                    } else if (record.get('hasChildren')) {
                        me.getApplication().fireEvent('servicecallload', parentsNameArray);
                        view = Ext.widget('servicecalls-setup-overview', {
                            router: me.getController('Uni.controller.history.Router'),
                            serviceCallId: record.get('name'),
                            store: store
                        });
                    } else {
                        me.getApplication().fireEvent('servicecallload', parentsNameArray);
                        view = Ext.widget('scs-landing-page', {serviceCallId: record.get('name')});
                        view.down('scs-landing-page-form').updateLandingPage(record);
                    }
                    me.getApplication().fireEvent('changecontentevent', view);
                },
                failure: function (record, operation) {
                    view = Ext.widget('errorNotFound');
                    me.getBreadcrumbs().hide();
                    me.getApplication().fireEvent('changecontentevent', view);
                }
            });
        }
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('servicecalls-preview'),
            serviceCallName = record.get('name'),
            previewForm = page.down('servicecalls-preview-form');

        preview.setTitle(serviceCallName);
        previewForm.updatePreview(record);
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'cancel':
                break;
            case 'pause':
                break;
            case 'resume':
                break;
            case 'retry':
                break;
        }
    },

    isEqual: function (array1, array2) {
        var i;
        if(array1.length !== array2.length) {
            return false;
        }

        for(i = 0; i < array1.length; i++) {
            if(array1[i] !== array2[i]) {
                return false;
            }
        }

        return true;
    }
});