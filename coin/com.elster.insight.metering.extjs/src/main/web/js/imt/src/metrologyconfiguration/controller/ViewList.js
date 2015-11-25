Ext.define('Imt.metrologyconfiguration.controller.ViewList', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.metrologyconfiguration.store.MetrologyConfiguration',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationListSetup',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationListPreview',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationList',
    ],
    models: [
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    ],
    stores: [
        'Imt.metrologyconfiguration.store.MetrologyConfiguration',
    ],
    views: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationList'
    ],
    refs: [
        {ref: 'metrologyConfigurationList', selector: '#metrologyConfigurationList'},
        {ref: 'metrologyConfigurationListSetup', selector: '#metrologyConfigurationListSetup'},
        {ref: 'metrologyConfigurationListPreview', selector: '#metrologyConfigurationListPreview'},
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
    chooseAction: function(menu, item) {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        route;
        router.arguments.mcid = menu.record.getId();
        
        switch (item.action) {
        	case 'editMetrologyConfiguration':
        		route = 'administration/metrologyconfiguration/edit';
        		break;
        	case 'viewMetrologyConfiguration':
        		route = 'administration/metrologyconfiguration/view';
        		break;
        	case 'removeMetrologyConfiguration':
                me.removeMetrologyConfiguration(menu.record);
        		break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
    },
    createMetrologyConfiguration: function() {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        route;
   		route = 'administration/metrologyconfiguration/add';

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
    },
    showMetrologyConfigurationList: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataStore = me.getStore('Imt.metrologyconfiguration.store.MetrologyConfiguration'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);
        var widget = Ext.widget('metrologyConfigurationListSetup', {router: router});
        me.getApplication().fireEvent('changecontentevent', widget);

        dataStore.load(function() {
	        if (this.getCount() === 0) {
	        	pageMainContent.setLoading(false);
	        	return;
	        }
        	me.getMetrologyConfigurationList().getSelectionModel().select(0);
        	pageMainContent.setLoading(false);
        })

    },
    onMetrologyConfigurationListSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewMetrologyConfiguration(record);
    },

    previewMetrologyConfiguration: function (record) {
        var me = this,
            widget = Ext.widget('metrologyConfigurationListPreview'),  
            form = widget.down('#metrologyConfigurationListPreviewForm'),
            previewContainer = me.getMetrologyConfigurationListSetup().down('#previewComponentContainer');
        
        form.loadRecord(record);
        widget.setTitle(record.get('name'));
        previewContainer.removeAll();
        previewContainer.add(widget);
   	
    },
    removeMetrologyConfiguration: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show({
            msg: Uni.I18n.translate('metrologyconfiguration.general.remove.msg', 'IMT', 'This metrology configuration will be removed.'),
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'",[record.data.name]),
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
                                text: Uni.I18n.translate('general.button.retry','IMT','Retry'),
                                ui: 'remove',
                                handler: function (button, event) {
                                    me.removeOperation(record);
                                }
                            },
                            {
                                text: Uni.I18n.translate('general.button.cancel','IMT','Cancel'),
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
});

