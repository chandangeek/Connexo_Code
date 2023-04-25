/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.controller.Webservices', {
    extend: 'Ext.app.Controller',

    views: [
        'Wss.view.Setup',
        'Wss.view.History',
        'Uni.view.window.Confirmation',
        'Wss.view.Add',
        'Wss.view.LandingPage',
        'Wss.view.endpoint.Status',
        'Wss.view.endpoint.History',
        'Wss.view.endpoint.HistoryOccurrence'
    ],
    stores: [
        'Wss.store.Endpoints',
        'Wss.store.Webservices',
        'Wss.store.endpoint.Type',
        'Wss.store.endpoint.Status',
        'Wss.store.endpoint.Occurrence',
        'Wss.store.endpoint.OccurrenceLog',
        'Wss.store.LogLevels',
        'Wss.store.AuthenticationMethods',
        'Wss.store.Logs',
        'Wss.store.Roles',
        'Wss.store.RelatedAttributeStore',
        'Wss.store.PayloadSaveStrategy'
    ],
    models: [
        'Wss.model.Endpoint',
        'Wss.model.Webservice',
        'Wss.model.endpoint.Occurrence',
        'Wss.model.Log',
        'Wss.model.RelatedAttributeModel',
        'Wss.model.PayloadSaveStrategy'
    ],

    refs: [
        {ref: 'preview', selector: 'webservices-preview'},
        {ref: 'historyPreview', selector: 'webservices-webservice-history-preview'},
        {ref: 'addForm', selector: '#addForm'},
        {ref: 'propertyForm', selector: 'endpoint-add property-form'},
        {ref: 'landingPageForm', selector: 'webservice-landing-page webservices-preview-form form'},
        {ref: 'paging', selector: 'webservices-grid pagingtoolbartop'}
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
            'webservices-endpoint-action-menu': {
                click: this.chooseEndpointAction
            },
            'webservices-historygrid-action-menu': {
                click: this.chooseEndpointAction
            },
            'webservices-endpoint-occurrence-action-menu': {
                click: this.chooseOccurrenceLogAction
            },
            '#wss-no-webservice-endpoints-add-btn': {
                click: this.goToAddView
            },
            '#add-webservice-endpoint': {
                click: this.goToAddView
            },
            'wss-webservice-history-grid': {
                select: this.showHistoryPreview
            }
        });
    },

    showWebservicesOverview: function () {
        var me = this,
            view,
            store = me.getStore('Wss.store.Endpoints');
        view = Ext.widget('webservices-setup', {
            router: me.getController('Uni.controller.history.Router'),
            adminView: Uni.util.Application.getAppNamespace() === 'SystemApp'
        });
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showWebservicesHistoryOverview: function () {
        var me = this;
        var store = me.getStore('Wss.store.endpoint.Occurrence');
        var view = Ext.widget('webservices-history', {
            router: me.getController('Uni.controller.history.Router'),
            adminView: Uni.util.Application.getAppNamespace() === 'SystemApp'
        });
        store.load();
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showEndpointHistoryOverview: function () {
        crossroads.parse("/error/notvisible");
    },

    showWebserviceHistory: function (endpointId) {
        var me = this;
        var store = me.getStore('Wss.store.endpoint.Occurrence');

        me.getModel('Wss.model.Endpoint').load(endpointId, {
            success: function (record) {
                var view = Ext.widget('webservice-history', {
                    router: me.getController('Uni.controller.history.Router'),
                    record: record,
                    adminView: Uni.util.Application.getAppNamespace() === 'SystemApp'
                });
                me.getApplication().fireEvent('changecontentevent', view);
                me.getApplication().fireEvent('endpointload', record.get('name'));
                store.load();
            }
        });
    },

    showWebserviceHistoryOccurrence: function (occurenceId, endpoint) {
        var me = this;

        var logStore = me.getStore('Wss.store.endpoint.OccurrenceLog');
        logStore.getProxy().setUrl(occurenceId);

        me.getModel('Wss.model.endpoint.Occurrence').load(occurenceId, {
            success: function (occurrence) {
                var view = Ext.widget('webservice-history-occurence', {
                    router: me.getController('Uni.controller.history.Router'),
                    endpoint: endpoint,
                    occurrence: occurrence,
                    time: occurrence.data.startTime
                });

                var endpointName = occurrence.getEndpoint() && occurrence.getEndpoint().get('name');

                me.getApplication().fireEvent('changecontentevent', view);
                me.getApplication().fireEvent('occurenceload', endpointName);
                me.getApplication().fireEvent('endpointlogdate', occurrence.data.startTime);

            }
        });
    },

    showWebserviceEndPoint: function (endpointId, occurrence) {
        var me = this;

        if ((Uni.Auth.hasPrivilege('privilege.administrate.webservices')) || (Uni.Auth.hasPrivilege('privilege.view.webservices')) || (Uni.Auth.hasPrivilege('privilege.retry.webservices'))) {
            me.getModel('Wss.model.Endpoint').load(endpointId, {
                success: function (endpoint) {
                    me.showWebserviceHistoryOccurrence(occurrence, endpoint);
                    me.getApplication().fireEvent('endpointload', endpoint.get('name'));

                }
            });
        } else {
            me.showWebserviceHistoryOccurrence(occurenceId);
        }
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
                me.getApplication().fireEvent('endpointload', record.get('name'));
            }
        });
    },

    showAddEditView: function (record, type) {
        var me = this,
            authenticationMethodStore = me.getStore('Wss.store.AuthenticationMethods'),
            logLevelsStore = me.getStore('Wss.store.LogLevels'),
            rolesStore = me.getStore('Wss.store.Roles'),
            webservicesStore = me.getStore('Wss.store.Webservices'),
            payloadSaveStrategyStore = me.getStore('Wss.store.PayloadSaveStrategy'),
            dependenciesCounter = 4,
            onDependenciesLoaded = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    rolesStore.insert(0, {
                        id: 'all',
                        name: Uni.I18n.translate('endPointAdd.all', 'WSS', 'All')
                    });
                    var previousPath = me.getController('Uni.controller.history.EventBus').getPreviousPath();
                    var view = Ext.widget('endpoint-add', {
                        action: type,
                        record: record,
                        returnLink: previousPath ? '#' + previousPath : me.getController('Uni.controller.history.Router').getRoute('administration/webserviceendpoints').buildUrl(),
                        authenticationMethodStore: authenticationMethodStore,
                        rolesStore: rolesStore,
                        logLevelsStore: logLevelsStore,
                        payloadStrategyStore: payloadSaveStrategyStore
                    });
                    me.getApplication().fireEvent('changecontentevent', view);
                }
            };
        authenticationMethodStore.load({
            callback: onDependenciesLoaded
        });
        logLevelsStore.load({
            callback: onDependenciesLoaded
        });
        payloadSaveStrategyStore.load({
            callback: onDependenciesLoaded
        });
        rolesStore.load({
            callback: onDependenciesLoaded
        });
        webservicesStore.load({
            callback: onDependenciesLoaded
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
        this.saveEndPoint(button, record, Uni.I18n.translate('webservices.endpoint.added', 'WSS', 'Web service endpoint added.'));
    },

    updateEndpoint: function (button) {
        this.saveEndPoint(button, this.record, Uni.I18n.translate('webservices.endpoint.saved', 'WSS', 'Web service endpoint saved.'));
    },

    saveEndPoint: function (button, record, acknowledgement) {
        var form = this.getAddForm(),
            me = this,
            formErrorsPanel = form.down('#addEndPointFormErrors');
        if (!form.isValid() && (form.down('#logLevelCombo') === null || form.down('#url-path').lastValue == null)) {
            formErrorsPanel.show();
            return;
        } else {
            formErrorsPanel.hide();
        }
        record.set(form.getForm().getFieldValues());
        if (form.down('#logLevelCombo')) {
            var logLevel = form.down('#logLevelCombo').findRecordByValue(record.get('logLevel'));
        }
        logLevel ? record.setLogLevel(logLevel) : record.set('logLevel', null);
        if (form.down('#storePayloadCombo')) {
            var payloadStrategy = form.down('#storePayloadCombo').findRecordByValue(record.get('payloadStrategy'));
        }
        payloadStrategy ? record.setPayloadStrategy(payloadStrategy) : record.set('payloadStrategy', null);
        if (form.down('#authenticationCombo')) {
            var authenticationMethod = form.down('#authenticationCombo').findRecordByValue(record.get('authenticationMethod'));
        }
        authenticationMethod ? record.setAuthenticationMethod(authenticationMethod) : record.set('authenticationMethod', null);
        if (record.getAuthenticationMethod().data.id === 'NONE') {
            record.set('username', null);
            record.set('password', null);
        }
        if (record.getAuthenticationMethod().data.id === 'OAUTH2_FRAMEWORK') {
            record.set('clientId', form.down('#clientIdField').getValue());
            record.set('clientSecret', form.down('#clientSecretField').getValue());
        }
        if (form.down('#userRoleField')) {
            var userGroup = form.down('#userRoleField').findRecordByValue(record.get('group'));
            if (userGroup && userGroup.get('id') === 'all') {
                record.setGroup(null);
                record.set('group', null);
            } else {
                userGroup ? record.setGroup(userGroup) : record.set('group', null);
            }
        } else {
            record.set('group', null);
        }
        record.set('direction', null);
        var properties = [];
        this.getPropertyForm().getRecord().properties().each(function (property) {
            properties.push(property.raw);
        });
        this.getPropertyForm().updateRecord();
        if (!Ext.isEmpty(this.getPropertyForm().getRecord())) {
            record.propertiesStore = this.getPropertyForm().getRecord().properties();
            record.set('properties', this.getPropertyForm().getFieldValues().properties);
        }
        record.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', acknowledgement);
                me.getController('Uni.controller.history.Router').getRoute('administration/webserviceendpoints').forward();
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.getForm().clearInvalid();
                    me.getPropertyForm().clearInvalid();
                    me.getPropertyForm().markInvalid(json.errors);
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
        this.getAddForm().down('#addEndPointFormErrors').show();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('webservices-preview-form'),
            form = previewForm.down('form');

        form.loadRecord(record);
        form.down('#property-form').loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        if (preview.down('webservices-action-menu')) {
            preview.down('webservices-action-menu').record = record;
        }
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getHistoryPreview(),
            previewForm = preview.down('webservices-webservice-history-form');

        previewForm.loadRecord(record);
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

    chooseEndpointAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'cancel':
                var confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                    confirmText: Uni.I18n.translate('general.yes', 'WSS', 'Yes'),
                    cancelText: Uni.I18n.translate('general.no', 'WSS', 'No'),
                    green: true
                });
                confirmationWindow.show({
                    title: Uni.I18n.translate('webservices.cancel.title', 'WSS', "Cancel occurrence '{0}'?", Uni.DateTime.formatDateTimeShort(menu.record.data.startTime)),
                    msg: Uni.I18n.translate(
                        'webservices.cancel.msg',
                        'WSS',
                        'The occurrence will be cancelled.'
                    ),
                    fn: function (state) {
                        if (state === 'confirm') {
                            me.cancel(menu.record);
                        }
                    }
                });
                break;
            case 'view-payload':
                var showPayload = function (payloadText) {
                    var win = window.open();
                    win.document.open('content-type: text/xml');
                    var payload = Ext.String.htmlEncode(payloadText);
                    win.document.write("<pre>" + payload + "</pre>");
                    win.document.close();
                    win.focus();
                };
                if (menu.record.get('hasPayload')) {
                    Ext.Ajax.request({
                        method: 'GET',
                        url: '/api/ws/occurrences/' + menu.record.getId() + '/payload',
                        success: function (operation) {
                            showPayload(operation.responseText);
                        }
                    });
                } else {
                    showPayload("");
                }
                break;
            case 'retry':
                var confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                    confirmText: Uni.I18n.translate('webservices.retry.action', 'WSS', "Retry now"),
                    green: true,
                });
                confirmationWindow.show({
                    title: Uni.I18n.translate('webservices.retry.title', 'WSS', "Retry now?"),
                    msg: Uni.I18n.translate(
                        'webservices.retry.msg',
                        'WSS',
                        'The response will be resent.'
                    ),
                    fn: function (state) {
                        if (state === 'confirm') {
                            me.retry(menu.record);
                        }
                    }
                });
                break;
        }
    },

    chooseOccurrenceLogAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'view-stackTrace':
                var win = window.open();
                var occurrenceLogRecord = menu.record.data;
                var stackTrace = occurrenceLogRecord.stackTrace;
                win.document.write("<pre>" + stackTrace + "</pre>");
                win.focus();
                break;
        }
    },

    cancel: function (occurrence) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var adminView = Uni.util.Application.getAppNamespace() === 'SystemApp';
        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ws/occurrences/' + occurrence.getId() + '/cancel',
            success: function () {
                if (router.arguments && router.arguments.endpointId && router.arguments.occurenceId) {
                    var basename = adminView ? 'administration' : 'workspace';
                    router.getRoute(basename + '/webserviceendpoints/view/history/occurrence').forward({
                        endpointId: occurrence.getEndpoint().getId(),
                        occurenceId: occurrence.getId()
                    });
                } else {
                    me.getStore('Wss.store.endpoint.Occurrence').load();
                }
                me.getApplication().fireEvent(
                    'acknowledge',
                    Uni.I18n.translate(
                        'webservices.cancel.success',
                        'WSS',
                        'Occurrence cancelled'
                    )
                );
            },
            failure: function () {
                var errorWindow = Ext.create('Uni.view.window.Confirmation', {
                    noConfirmBtn: true
                });
                errorWindow.show({
                    title: Uni.I18n.translate('webservices.cancel.error', 'WSS', "Couldn't perform your action"),
                    msg: Uni.I18n.translate(
                        'webservices.cancel.error.msg',
                        'WSS',
                        'The occurrence couldn\'t be cancelled. Possible reasons: the occurrence\'s state has already been changed, the web service is unavailable, unexpected error has occurred while processing.'
                    )
                });
            }
        });
    },

    retry: function (occurrence) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var adminView = Uni.util.Application.getAppNamespace() === 'SystemApp';

        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ws/occurrences/' + occurrence.getId() + '/retry',
            success: function () {
                var basename = adminView ? 'administration' : 'workspace';

                router.getRoute(basename + '/webserviceendpoints/view/history').forward({
                    endpointId: occurrence.getEndpoint().getId()
                })
                me.getApplication().fireEvent(
                    'acknowledge',
                    Uni.I18n.translate(
                        'webservices.retry.success',
                        'WSS',
                        'The response is successfully resent'
                    )
                );
            },
            failure: function () {
                var errorWindow = Ext.create('Uni.view.window.Confirmation', {
                    noConfirmBtn: true
                });
                errorWindow.show({
                    title: Uni.I18n.translate('webservices.retry.error', 'WSS', "Couldn't perform your action"),
                    msg: Uni.I18n.translate(
                        'webservices.retry.error.msg',
                        'WSS',
                        'The response couldn\'t be resent. Please check the web service configuration'
                    )
                });
            }
        });
    },

    removeEndpoint: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            store = me.getStore('Wss.store.Endpoints');

        confirmationWindow.show(
            {
                title: Uni.I18n.translate('general.removeX', 'WSS', "Remove '{0}'?", [record.get('name')]),
                msg: Uni.I18n.translate('webservices.remove.msg', 'WSS', 'This web service endpoint will be removed and no longer be available.'),
                fn: function (state) {
                    if (state === 'confirm') {
                        if (record.get('group') === '') {
                            record.setGroup(null);
                            record.set('group', null);
                        }
                        record.destroy({
                            success: function () {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('webservices.endpoint.removed', 'WSS', 'Web service endpoint removed'));
                                me.showWebservicesOverview();
                            }
                        });
                    }
                }
            });
    },

    activateOrDeactivate: function (record) {
        var me = this,
            toState = !record.get('active');

        var actionToPerform = toState ? "activate" : "deactivate";

        record.beginEdit();
        if (record.get('group') === '') {
            record.setGroup(null);
            record.set('group', null);
        }

        record.endEdit();

        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ws/endpointconfigurations/' + record.getId() + '/' + actionToPerform,

            jsonData: {
                version: record.get('version')
            },

            success: function () {
                record.set('active', toState);
                tmpVersion = record.get('version');
                tmpVersion++;
                record.set('version', tmpVersion);
                me.getApplication().fireEvent('acknowledge', record.get('active') ?
                    Uni.I18n.translate('webservices.endpoint.activated', 'WSS', 'Web service endpoint activated') :
                    Uni.I18n.translate('webservices.endpoint.deactivated', 'WSS', 'Web service endpoint deactivated')
                );
                if (me.getLandingPageForm()) {
                    me.getLandingPageForm().loadRecord(record);
                }
            }
        });
    },

    showEndpointOverview: function (endpointId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;

        me.getModel('Wss.model.Endpoint').load(endpointId, {
            success: function (record) {
                view = Ext.widget('webservice-landing-page', {
                    router: router,
                    record: record,
                    adminView: Uni.util.Application.getAppNamespace() === 'SystemApp'
                });
                if (view.down('webservices-action-menu')) {
                    view.down('webservices-action-menu').record = record;
                }
                me.getApplication().fireEvent('changecontentevent', view);
                me.getApplication().fireEvent('endpointload', record.get('name'));
            }
        });
    },

    showEndpointStatusHistory: function (endpointId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view,
            store = me.getStore('Wss.store.Logs');

        store.getProxy().setUrl(endpointId);

        me.getModel('Wss.model.Endpoint').load(endpointId, {
            success: function (record) {
                view = Ext.widget('webservice-endpoint-status', {
                    router: router,
                    record: record
                });
                me.getApplication().fireEvent('changecontentevent', view);
                me.getApplication().fireEvent('endpointload', record.get('name'));
            }
        });
    }
});
