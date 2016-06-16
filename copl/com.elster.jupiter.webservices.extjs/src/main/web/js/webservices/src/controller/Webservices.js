Ext.define('Wss.controller.Webservices', {
    extend: 'Ext.app.Controller',

    views: [
        'Wss.view.Setup',
        'Uni.view.window.Confirmation',
        'Wss.view.Add',
        'Wss.view.LandingPage',
        'Wss.view.LoggingPage'
    ],
    stores: [
        'Wss.store.Endpoints',
        'Wss.store.Webservices',
        'Wss.store.Logs'
    ],
    models: [
        'Wss.model.Endpoint',
        'Wss.model.Webservice',
        'Wss.model.Log'
    ],

    refs: [
        {ref: 'preview', selector: 'webservices-preview'},
        {ref: 'addForm', selector: '#addForm'}
    ],

    init: function () {
        this.control({
            'endpoint-add button[action=add]': {
                click: this.addEndpoint
            },
            'webservices-setup webservices-grid': {
                select: this.showPreview
            },
            'webservices-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showWebservicesOverview: function () {
        var me = this,
            view,
            store = me.getStore('Wss.store.Endpoints');

        view = Ext.widget('webservices-setup', {
            router: me.getController('Uni.controller.history.Router')
        });
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showAddWebserviceEndPoint: function(){
        var me = this,
            view,
            store = me.getStore('Wss.store.Webservices');

        view = Ext.widget('endpoint-add',{
            action: 'add',
            returnLink: me.getController('Uni.controller.history.Router').getRoute('administration/webserviceendpoints').buildUrl()
        });
        me.getApplication().fireEvent('changecontentevent', view);

    },

    addEndpoint: function(button){
        var form = button.up('form');
        var record = Ext.create('Wss.model.Endpoint');
        record.set(form.getValues());
        //record.phantom = true;
        //record.getProxy().appendId = false;
        record.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('endPointAdd.endpointAdded', 'WSS', 'Webservice endpoint added'));
                //location.href = '#/administration/devicetypes/' + encodeURIComponent(record.get('id'));
                //record.phantom = true;       // force 'POST' method for request otherwise 'PUT' will be performed
                //record.getProxy().appendId = false;
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.markInvalid(json.errors);
                    me.showErrorPanel();
                }
            }
        });
    },

    showErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getAddFrom().down('#addEndPointFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('general.formErrors', 'WSS', 'There are errors on this page that require your attention.')
        });
        formErrorsPlaceHolder.show();
    },

    showPreview: function(selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('webservices-preview-form'),
            form = previewForm.down('form');

        form.loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        preview.down('webservices-action-menu').record = record;
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'remove':
                me.removeEndpoint(menu.record);
                break;
            case 'activate': {
                me.activateOrDeactivate(menu.record);
            }
        }
    },

    removeEndpoint: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            store = me.getStore('Wss.store.Endpoints');

        confirmationWindow.show(
            {
                title: Uni.I18n.translate('general.removeX', 'WSS', "Remove '{0}'?", [record.get('name')]),
                msg: Uni.I18n.translate('webservices.remove.msg', 'WSS', 'This webservice endpoint will be removed and no longer be available.'),
                fn: function (state) {
                    if (state === 'confirm') {
                        record.destroy({
                            success: function () {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('webservices.remove.success.msg', 'WSS', 'Webservice endpoint removed'));
                                store.load();
                            }
                        });
                    }
                }
            });
    },

    activateOrDeactivate: function (record) {
        var me = this,
            toState = !record.get('active');
        record.beginEdit();
        record.set('active', toState);
        record.endEdit();
        record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', record.get('active') ?
                        Uni.I18n.translate('general.xActivated', 'WSS', '{0} activated ', [record.get('name')])  :
                        Uni.I18n.translate('general.xDeactivated', 'WSS', '{0} deactivated ', [record.get('name')])
                    );
                }
            }
        );
    },
    showEndpointOverview: function (endpointId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;


        me.getModel('Wss.model.Endpoint').load(endpointId, {
            success: function (record) {
                view = Ext.widget('webservice-landing-page', {
                    router: router,
                    record: record
                });
                if (view.down('webservices-action-menu')) {
                    view.down('webservices-action-menu').record = record;
                }
                me.getApplication().fireEvent('changecontentevent', view);
                me.getApplication().fireEvent('endpointload', record.get('name'));
            }
        });
    },

    showLoggingPage: function (endpointId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view,
            store = me.getStore('Wss.store.Logs');

        store.getProxy().setUrl(endpointId);

        me.getModel('Wss.model.Endpoint').load(endpointId, {
            success: function (record) {
                view = Ext.widget('webservice-logging-page', {
                    router: router,
                    record: record
                });
                me.getApplication().fireEvent('changecontentevent', view);
                me.getApplication().fireEvent('endpointload', record.get('name'));
            }
        });
    }
});