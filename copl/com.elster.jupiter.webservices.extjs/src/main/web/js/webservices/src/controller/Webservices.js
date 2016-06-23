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
        'Wss.store.LogLevels',
        'Wss.store.AuthenticationMethods',
        'Wss.store.Logs',
        'Wss.store.Roles'
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
            'endpoint-add button[action=edit]': {
                click: this.updateEndpoint
            },
            'webservices-setup webservices-grid': {
                select: this.showPreview
            },
            'webservices-action-menu': {
                click: this.chooseAction
            },
            '#wss-no-webservice-endpoints-add-btn': {
                click: this.goToAddView
            },
            '#add-webservice-endpoint': {
                click: this.goToAddView
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

    showAddWebserviceEndPoint: function () {
        var me = this,
            store = me.getStore('Wss.store.Webservices');
        me.showAddEditView(null, 'add');
    },

    showEditPage: function (endpointId) {
        var me = this,
            store = me.getStore('Wss.store.Webservices');
        var model = Ext.ModelManager.getModel('Wss.model.Endpoint');
        model.load(endpointId, {
            success: function (record) {
                me.record = record;
                me.showAddEditView(record, 'edit');
            }
        });
    },

    showAddEditView: function (record, type) {
        var me = this;
        var authenticationMethodStore = me.getStore('Wss.store.AuthenticationMethods');
        var logLevelsStore = me.getStore('Wss.store.LogLevels');
        var rolesStore = me.getStore('Wss.store.Roles');
        authenticationMethodStore.load({
            callback: function () {
                logLevelsStore.load({
                    callback: function () {
                        rolesStore.load({
                            callback: function () {
                                rolesStore.insert(0,{
                                    id: 'all',
                                    name: Uni.I18n.translate('endPointAdd.all', 'WSS', 'All')
                                });
                                view = Ext.widget('endpoint-add', {
                                    action: type,
                                    record: record,
                                    returnLink: me.getController('Uni.controller.history.Router').getRoute('administration/webserviceendpoints').buildUrl(),
                                    authenticationMethodStore: authenticationMethodStore,
                                    rolesStore: rolesStore,
                                    logLevelsStore: logLevelsStore
                                });
                                me.getApplication().fireEvent('changecontentevent', view);
                            }
                        });
                    }
                });
            }
        });
    },

    goToAddView: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/webserviceendpoints/add').forward();
    },

    addEndpoint: function (button) {
        var me = this,
            form = button.up('form'),
            record = Ext.create('Wss.model.Endpoint');
        this.saveEndPoint(button, record, Uni.I18n.translate('endPointAdd.endpointAdded', 'WSS', 'Webservice endpoint added'));
    },

    updateEndpoint: function (button) {
        this.saveEndPoint(button, this.record, Uni.I18n.translate('endPointAdd.endpointSaved', 'WSS', 'Webservice endpoint saved'));
    },

    saveEndPoint: function (button, record, acknowledgement) {
        var form = button.up('form'),
            me = this;
        record.set(form.getValues());
        var logLevel = form.down('#logLevelCombo').findRecordByValue(record.get('logLevel'));
        logLevel?record.setLogLevel(logLevel):record.set('logLevel',null);
        var authenticationMethod = form.down('#authenticationCombo').findRecordByValue(record.get('authenticationMethod'));
        authenticationMethod?record.setAuthenticationMethod(authenticationMethod):record.set('authenticationMethod',null);
        if(form.down('#userRoleField')) {
            var userGroup = form.down('#userRoleField').findRecordByValue(record.get('group'));
            if(userGroup.get('id')==='all'){;
                record.setGroup(null);
                record.set('group', null);
            } else {
                userGroup ? record.setGroup(userGroup) : record.set('group', null);
            }
        } else {
            record.set('group', null);
        }
        record.set('direction', null);
        record.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', acknowledgement);
                me.getController('Uni.controller.history.Router').getRoute('administration/webserviceendpoints').forward();
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.getForm().clearInvalid();
                    form.getForm().markInvalid(json.errors);
                    me.showErrorPanel();
                }
            }
        });
    },

    editEndpoint: function (record) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/webserviceendpoints/view/edit').forward({endpointId: record.get('id')});
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

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('webservices-preview-form'),
            form = previewForm.down('form');

        form.loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        if (preview.down('webservices-action-menu')) {
            preview.down('webservices-action-menu').record = record;
        }
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'remove':
                me.removeEndpoint(menu.record);
                break;
            case 'activate':
                me.activateOrDeactivate(menu.record);
                break;
            case 'edit':
                me.editEndpoint(menu.record);
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
        if(record.get('group')===''){
            record.setGroup(null);
            record.set('group', null);
        }
        record.endEdit();
        record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', record.get('active') ?
                        Uni.I18n.translate('general.xActivated', 'WSS', '{0} activated ', [record.get('name')]) :
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