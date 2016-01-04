Ext.define('Imt.registerdata.controller.ViewData', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.registerdata.store.RegisterData',
        'Imt.registerdata.view.RegisterTabbedView',
        'Imt.registerdata.view.RegisterDataSetup',
        'Imt.registerdata.view.RegisterDataPreview'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.registerdata.model.RegisterData',
    ],
    stores: [
        'Imt.registerdata.store.RegisterData',
        'Imt.registerdata.store.RegisterDataDurations',
    ],
    views: [
        'Imt.registerdata.view.RegisterDataList'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'registerDataOverviewLink', selector: '#register-data-overview-link'},
        {ref: 'registerDataList', selector: '#registerDataList'},
        {ref: 'registerDataSetup', selector: '#registerDataSetup'},
        {ref: 'registerTabbedView', selector: '#registerTabbedView'},
        {ref: 'registerDataPreview', selector: '#registerDataPreview'}
    ],
    init: function () {
        var me = this;
        me.control({
            '#registerDataList': {
                select: me.onRegisterDataListSelect
            },
            'registerDataActionMenu': {
                click: this.chooseAction
            },
            '#registerTabbedView #registerTabPanel': {
                tabchange: this.onTabChange
            },
        });
    },

    showUsagePointRegisterData: function(mRID, registerId) {
	     var me = this,
         dataStore = me.getStore('Imt.registerdata.store.RegisterData'),
         router = me.getController('Uni.controller.history.Router'),
         registerModel = me.getModel('Imt.registerdata.model.Register'),
         durationsStore = me.getStore('Imt.registerdata.store.RegisterDataDurations'),
         pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
	   
	     pageMainContent.setLoading(true);
	     
	     Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
		        success: function (usagepoint) {
		        	me.getApplication().fireEvent('usagePointLoaded', usagepoint);
		        }
	     });
	     registerModel.getProxy().setUrl({mRID: mRID, registerId: registerId});
	     registerModel.load(registerId, {
            success: function (record) {
            	  var widget = Ext.widget('registerTabbedView', {
                        router: router, 
                        mRID: mRID, 
                        registerId: registerId,
                        filter: {
                           fromDate: new Date().getTime(),
                           duration: '1months',
                           durationStore: durationsStore
                        },
                });
                
                me.getApplication().fireEvent('registerDataLoaded', record);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(mRID); 
	            
	            if (me.fromSpecification === false || me.getController('Imt.registerdata.controller.View').fromSpecification === false) {
	                widget.down('#registerTabPanel').down('#registerDataSetupPanel').show();
	                widget.down('#registerTabPanel').setTitle(record.get('readingTypeFullAliasName'));
	                dataStore.getProxy().setUrl({mRID: mRID, registerId: registerId});
		            dataStore.load(function() {
		            	me.getRegisterDataList().getSelectionModel().select(0);
		                pageMainContent.setLoading(false);
		            });
	            } 	            
	            
	            
            }
	     });
    },
    onRegisterDataListSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewRegisterData(record);
    },
    previewRegisterData: function (record) {
        var me = this,
            widget = Ext.widget('registerDataPreview'), 
            form = widget.down('#registerDataPreviewForm'),
        	previewContainer = me.getRegisterTabbedView().down('#registerTabPanel').down('#registerDataSetupPanel').down('#previewComponentContainer')
        form.loadRecord(record);
        widget.tools[0].menu.record=record;
        var datestr = Uni.I18n.translate('general.dateattime', 'IMT', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(record.get('readingTime'))),Uni.DateTime.formatTimeLong(new Date(record.get('readingTime')))], false);
        widget.setTitle(datestr);
        previewContainer.removeAll();
        previewContainer.add(widget);
   	
    },
    chooseAction: function (menu, item) {
    	var me = this,
            router = me.getController('Uni.controller.history.Router'),
            grid = me.getRegisterDataList(),
            record = grid.getView().getSelectionModel().getLastSelected();
    	
    	router.arguments.timestamp =  record.getData().readingTime;
    	
        switch (item.action) {
            case 'confirmValue':
            	route = 'usagepoints/view/registers/registerdata',
              //  me.getPage().setLoading();
                me.getRegisterDataList().setLoading(),
                record.getProxy().setUrl({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
                record.set('isConfirmed', true);
                record.save({
                    callback: function (rec, operation, success) {
                        if (success) {
                            rec.set('validationResult', 'validationStatus.ok');
                            grid.getView().refreshNode(grid.getStore().indexOf(rec));
                       //     me.getRegisterDataList().down('form').loadRecord(rec);
                        }
                        me.getRegisterDataList().setLoading(false);
                    }
                });
                break;
            case 'removeData':
            	me.removeRegisterData();
            	return;
            case 'editData':
            	route = 'usagepoints/view/registers/register/edit';
            	break;
        }
        
        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
    },
    removeRegisterData() {
    	var me = this,
    	controller = me.getController('Imt.registerdata.controller.EditData');
    	controller.removeRegisterData();
    },

    showRegisterSpecifications: function (mRID, registerId) {
	     var me = this,
         router = me.getController('Uni.controller.history.Router'),
         registerModel = me.getModel('Imt.registerdata.model.Register'),
         durationsStore = me.getStore('Imt.registerdata.store.RegisterDataDurations'),
         pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
	   
	     pageMainContent.setLoading(true);
	     
	     Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
		        success: function (usagepoint) {
		        	router.arguments.version = usagepoint.get('version');
		        	me.getApplication().fireEvent('usagePointLoaded', usagepoint);
		        }
	     });
	     registerModel.getProxy().setUrl({mRID: mRID, registerId: registerId});
	     registerModel.load(registerId, {
            success: function (record) {
            	  var widget = Ext.widget('registerTabbedView', {
                        router: router, 
                        mRID: mRID, 
                        registerId: registerId,
                        filter: {
                           fromDate: new Date().getTime(),
                           duration: '1months',
                           durationStore: durationsStore
                        },
                });
                
                me.getApplication().fireEvent('registerDataLoaded', record);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(mRID); 
                widget.down('#registerTabPanel').setTitle(record.get('readingTypeFullAliasName'));
                widget.down('#registerTabPanel').down('#registerOverview').down('#registerPreviewForm').loadRecord(record); //.record = record
                this.fromSpecification = true;
                widget.down('#registerActionMenu').record = record;
                pageMainContent.setLoading(false);
            }
	     });
	     
        
    },

    onTabChange: function (tabPanel, newTab) {
        var router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};
        if (newTab.itemId === 'registerDataSetupPanel') {
        	this.fromSpecification = false;
            filterParams.onlySuspect = false;
            route = 'usagepoints/view/registers/registerdata';
            route && (route = router.getRoute(route));
            route && route.forward(routeParams, filterParams);

        } else if (newTab.itemId === 'register-specifications') {
        	this.fromSpecification = true;
            route = 'usagepoints/view/registers/register';
            route && (route = router.getRoute(route));
            route && route.forward(routeParams, filterParams);
        }
    },
 
});

