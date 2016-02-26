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
            view,
            servicecallIds = Array.prototype.slice.call(arguments),
            servicecallId = arguments[arguments.length - 1],
            compareParentsArray;

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
                    compareParentsArray = record.get('parents').slice();
                    compareParentsArray.push(servicecallId);
                    if(me.getDiff(me.uniqueValuesArray(servicecallIds), me.uniqueValuesArray(compareParentsArray)).length !== 0) {
                        view = Ext.widget('errorNotFound');
                        me.getBreadcrumbs().hide();
                    } else if (record.get('hasChildren')) {
                        me.getApplication().fireEvent('servicecallload', servicecallIds);
                        view = Ext.widget('servicecalls-setup-overview', {
                            router: me.getController('Uni.controller.history.Router'),
                            serviceCallId: servicecallId,
                            store: store
                        });
                    } else {
                        me.getApplication().fireEvent('servicecallload', servicecallIds);
                        view = Ext.widget('scs-landing-page', {serviceCallId: servicecallId});
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
            serviceCallName = record.get('number'),
            previewForm = page.down('servicecalls-preview-form');

        preview.setTitle(serviceCallName);
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
    },

    getDiff: function (array1, array2) {
        var diff = [];

        if(array1.length !== array2.length) {
            return ["FALSE"];
        }

        for (var i = array1.length - 1; i >= 0; i--) {
            var key = array1[i];
            if (-1 === array2.indexOf(key)) {
                diff.push(key);
            }
        }

        return diff;
    },

    uniqueValuesArray: function (a) {
        return a.sort().filter(function(item, pos, ary) {
            return !pos || item != ary[pos - 1];
        })
    }
});