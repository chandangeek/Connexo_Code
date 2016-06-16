Ext.define('Wss.controller.Webservices', {
    extend: 'Ext.app.Controller',

    views: [
        'Wss.view.Setup',
        'Wss.view.Add'
    ],
    stores: [
        'Wss.store.Endpoints',
        'Wss.store.Webservices',
        'Wss.store.LogLevels'
    ],
    models: [
        'Wss.model.Endpoint',
        'Wss.model.Webservice'
    ],

    refs: [
        {ref: 'preview', selector: 'webservices-preview'},
        {ref: 'addForm', selector: '#addForm'},
        {
            ref: 'preview',
            selector: 'webservices-preview'
        }
    ],

    init: function () {
        this.control({
            'endpoint-add button[action=add]': {
                click: this.addEndpoint
            },
            'webservices-setup webservices-grid': {
                select: this.showPreview
            }
        });
    },

    showWebservicesOverview: function () {
        var me = this,
            view,
            store = me.getStore('Wss.store.Endpoints');

        view = Ext.widget('webservices-setup');
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
        var me = this;
        var form = button.up('form');
        var record = Ext.create('Wss.model.Endpoint');
        record.set(form.getValues());
        var logLevel = form.down('#logLevelCombo').findRecordByValue(record.get('logLevel'));
        record.set('logLevel',{id: logLevel.get('logLevel'), displayValue: logLevel.get('localizedValue')});
        record.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('endPointAdd.endpointAdded', 'WSS', 'Webservice endpoint added'));
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    debugger;
                    form.getForm().markInvalid(json.errors);
                    me.showErrorPanel();
                }
            }
        });
    },

    showErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getAddForm().down('#addEndPointFormErrors');

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
    }
});