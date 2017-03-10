/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.controller.ViewList', {
    extend: 'Ext.app.Controller',

    models: [
        'Imt.metrologyconfiguration.model.MetrologyConfiguration'
    ],

    stores: [
        'Imt.metrologyconfiguration.store.MetrologyConfiguration'
    ],

    views: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationListSetup',
        'Imt.metrologyconfiguration.view.Setup',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu'
    ],

    refs: [
        {ref: 'metrologyConfigurationList', selector: '#metrologyConfigurationList'},
        {ref: 'metrologyConfigurationListSetup', selector: '#metrologyConfigurationListSetup'},
        {ref: 'metrologyConfigurationListPreview', selector: '#metrology-config-preview'},
        {ref: 'metrologyConfigurationEdit', selector: '#metrologyConfigurationEdit'}
    ],

    init: function () {
        var me = this;
        me.control({
            '#metrologyConfigurationList': {
                select: me.onMetrologyConfigurationListSelect
            },
            'metrology-configuration-action-menu': {
                click: this.chooseAction
            },
            '#createMetrologyConfiguration': {
                click: this.createMetrologyConfiguration
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;
        router.arguments.mcid = menu.record.getId();

        if(item){
            switch (item.action) {
                case 'removeMetrologyConfiguration':
                    me.removeMetrologyConfiguration(menu.record);
                    break;
                case 'editMetrologyConfiguration':
                    route = 'administration/metrologyconfiguration/view/edit';
                    break;
                case 'viewMetrologyConfiguration':
                    route = 'administration/metrologyconfiguration';
                    break;
                case 'activateMetrologyConfiguration':
                    me.activateMetrologyConfiguration(menu.record);
                    break;
                case 'deprecateMetrologyConfiguration':
                    me.deprecateMetrologyConfiguration(menu.record);
                    break;
            }
        }

        if(route){
            router.getRoute(route).forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
        }
    },

    createMetrologyConfiguration: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;
        route = 'administration/metrologyconfiguration/add';

        if(route){
            router.getRoute(route).forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
        }
    },

    showMetrologyConfigurationList: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget = Ext.widget('metrologyConfigurationListSetup', {
                itemId: 'metrologyConfigurationListSetup',
                router: router
            });

        viewport.setLoading(true);
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getStore('Imt.metrologyconfiguration.store.MetrologyConfiguration').load();
        viewport.setLoading(false);
    },

    onMetrologyConfigurationListSelect: function (selectionModel, record) {
        var me = this,
            previewPanel = me.getMetrologyConfigurationListPreview(),
            menu = previewPanel.down('#metrology-configuration-list-action-menu');

        Ext.suspendLayouts();
        previewPanel.setVisibleActionsButton(record.get('status').id != "deprecated");
        previewPanel.setTitle(Ext.htmlEncode(record.get('name')));
        previewPanel.loadRecord(record);
        Ext.resumeLayouts(true);
        if (menu) {
            menu.record = record;
        }
    },

    removeMetrologyConfiguration: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show({
            msg: Uni.I18n.translate('metrologyconfiguration.general.remove.msg', 'IMT', 'This metrology configuration will be removed.'),
            title: Uni.I18n.translate('general.remove.count', 'IMT', "Remove '{0}'", [record.data.name]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeOperation(record);
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    removeOperation: function (record) {
        var me = this;
        record.destroy({
            success: function () {
                if (me.getMetrologyConfigurationListSetup()) {
                    var grid = me.getMetrologyConfigurationListSetup().down('metrologyConfigurationList');
                    grid.down('pagingtoolbartop').totalCount = 0;
                    grid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/metrologyconfiguration').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.general.remove.confirm.msg', 'IMT', 'Metrology configuration removed'));
            },
            failure: function (record, operation) {
                if (operation.response.status === 409) {
                    return
                }
                var json = Ext.decode(operation.response.responseText, true);
                var errorText = Uni.I18n.translate('general.error.unknown', 'IMT', 'Unknown error occurred');
                if (json && json.errors) {
                    errorText = json.errors[0].msg;
                }

                if (!Ext.ComponentQuery.query('#remove-error-messagebox')[0]) {
                    Ext.widget('messagebox', {
                        itemId: 'remove-error-messagebox',
                        buttons: [
                            {
                                text: Uni.I18n.translate('general.button.retry', 'IMT', 'Retry'),
                                ui: 'remove',
                                handler: function (button, event) {
                                    me.removeOperation(record);
                                }
                            },
                            {
                                text: Uni.I18n.translate('general.button.cancel', 'IMT', 'Cancel'),
                                action: 'cancel',
                                ui: 'link',
                                href: '#/administration/metrologyconfiguration',
                                handler: function (button, event) {
                                    this.up('messagebox').destroy();
                                }
                            }
                        ]
                    }).show({
                        ui: 'notification-error',
                        title: Uni.I18n.translate('general.error.remove.msg', 'IMT', 'Remove operation failed'),
                        msg: errorText,
                        modal: false,
                        icon: Ext.MessageBox.ERROR
                    })
                }
            }
        });
    },

    activateMetrologyConfiguration: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation',{
                confirmText: Uni.I18n.translate('general.activate', 'IMT', 'Activate')
            });
        confirmationWindow.show({
            msg: Uni.I18n.translate(
                'metrologyconfiguration.general.activate.confirmation.msg',
                'IMT',
                'You will be able to define this metrology configuration for usage points. After that, you can\'t change the metrology configuration anymore.'
            ),
            title: Uni.I18n.translate('metrologyconfiguration.general.activate.title', 'IMT', "Activate '{0}'?", record.get('name')),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    me.metrologyConfigurationPutOperation(record, 'activate');
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    deprecateMetrologyConfiguration: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation',{
                confirmText: Uni.I18n.translate('general.deprecate', 'IMT', 'Deprecate')
            });
        confirmationWindow.show({
            msg: Uni.I18n.translate(
                'metrologyconfiguration.general.deprecate.msg',
                'IMT',
                'You will not be able to link this metrology configuration to usage points anymore.' +
                ' But all the existing usage points with such metrology configuration will not be modified and it will be still possible to monitor data on these usage points'
            ),
            title: Uni.I18n.translate('metrologyconfiguration.general.deprecate.title', 'IMT', "Deprecate '{0}'?", record.get('name')),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    me.metrologyConfigurationPutOperation(record, 'deprecate');
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    metrologyConfigurationPutOperation: function(record, action){
        var me =this,
            url,
            acknowledgeMsg,
            router = me.getController('Uni.controller.history.Router');

        switch (action){
            case 'activate': {
                url = '/api/ucr/metrologyconfigurations/{0}/activate';
                acknowledgeMsg = Uni.I18n.translate('metrologyconfiguration.general.activated', 'IMT', 'Metrology configuration activated')
            }
                break;
            case 'deprecate': {
                url = '/api/ucr/metrologyconfigurations/{0}/deprecate';
                acknowledgeMsg = Uni.I18n.translate('metrologyconfiguration.general.deprecated', 'IMT', 'Metrology configuration deprecated')
            }
        }
        Ext.Ajax.request({
            url: Ext.String.format(url, record.get('id')),
            method: 'PUT',
            jsonData: record.getData(),
            isNotEdit: true,
            success: function () {
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', acknowledgeMsg);
            }
        });
    }
});

