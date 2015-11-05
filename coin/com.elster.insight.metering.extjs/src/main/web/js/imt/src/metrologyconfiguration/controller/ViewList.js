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

        switch (item.action) {
        	case 'editMetrologyConfiguration':
        		router.arguments.mcid = menu.record.getId();
        		route = 'metrologyconfiguration/edit';
        		break;
        	case 'viewMetrologyConfiguration':
        		router.arguments.mcid = menu.record.getId();
        		route = 'metrologyconfiguration/view';
        		break;
        	case 'deleteMetrologyConfiguration':
        		router.arguments.mcid = menu.record.getId();
        		alert('delete not yet implemented -- ' + ' ' + menu.record.getId());
 //       		route = 'metrologyconfiguration/delete';
        		break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
    },
    createMetrologyConfiguration: function() {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        route;
   		route = 'metrologyconfiguration/add';

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
});

