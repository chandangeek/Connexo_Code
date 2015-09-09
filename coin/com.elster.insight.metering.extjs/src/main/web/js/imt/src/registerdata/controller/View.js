Ext.define('Imt.registerdata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.registerdata.store.Register',
        'Imt.registerdata.view.Setup',
        'Imt.registerdata.view.Preview',
        'Imt.registerdata.store.RegisterData',
        'Imt.registerdata.view.RegisterDataSetup',
        'Imt.registerdata.view.RegisterDataPreview'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.registerdata.model.Register',
        'Imt.registerdata.model.RegisterData',
        'Imt.model.DataIntervalAndZoomLevels',
        'Imt.model.ChannelDataDuration'
    ],
    stores: [
        'Imt.registerdata.store.Register',
        'Imt.registerdata.store.RegisterData',
        'Imt.store.DataIntervalAndZoomLevels',
        'Imt.store.ChannelDataDurations'
    ],
    views: [
        'Imt.registerdata.view.RegisterList',
        'Imt.registerdata.view.RegisterDataList'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'registerList', selector: '#registerList'},
        {ref: 'registerListSetup', selector: '#registerListSetup'},
        {ref: 'registerPreview', selector: '#registerPreview'},
        
        {ref: 'registerDataOverviewLink', selector: '#register-data-overview-link'},
        {ref: 'registerDataList', selector: '#registerDataList'},
        {ref: 'registerDataSetup', selector: '#registerDataSetup'},
        {ref: 'registerDataPreview', selector: '#registerDataPreview'},
    ],
    init: function () {
        var me = this;
        me.control({
            '#registerList': {
                select: me.onRegisterListSelect
            },
            '#registerDataList': {
                select: me.onRegisterDataListSelect
            },
        });
    },
    showUsagePointRegisters: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataStore = me.getStore('Imt.registerdata.store.Register'),
            usagePoint = Ext.create('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);
        var widget = Ext.widget('registerListSetup', {router: router, mRID: mRID});
        usagePoint.set('mRID', mRID);
        me.getApplication().fireEvent('usagePointLoaded', usagePoint);
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getOverviewLink().setText(mRID);
        dataStore.getProxy().setUrl(mRID);
        dataStore.load(function() {
        	me.getRegisterList().getSelectionModel().select(0);
        	pageMainContent.setLoading(false);
        })

    },
     onRegisterListSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewRegisterData(record);
    },

    previewRegisterData: function (record) {
        var me = this,
            widget = Ext.widget('registerPreview'), 
            form = widget.down('#registerPreviewForm'),
            previewContainer = me.getRegisterListSetup().down('#previewComponentContainer');
        
        form.loadRecord(record);
        widget.setTitle(record.get('readingTypeFullAliasName'));
        previewContainer.removeAll();
        previewContainer.add(widget);
   	
    },
    
    showUsagePointRegisterData: function(mRID, registerId) {
	     var me = this,
         dataStore = me.getStore('Imt.registerdata.store.RegisterData'),
         router = me.getController('Uni.controller.history.Router'),
         registerModel = me.getModel('Imt.registerdata.model.Register'),
         durationsStore = me.getStore('Imt.registerdata.store.RegisterDataDurations'),
         pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
	   
	     pageMainContent.setLoading(true);
	     registerModel.getProxy().setUrl({mRID: mRID, registerId: registerId});
	     registerModel.load(registerId, {
            success: function (record) {
                var widget = Ext.widget('registerDataSetup', {
                        router: router, 
                        mRID: mRID, 
                        registerId: registerId,
                        filter: {
                           fromDate: new Date().getTime(),
                           duration: '1months',
                           durationStore: durationsStore
                        }
                });
                me.getApplication().fireEvent('registerDataLoaded', record);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(mRID); 
                dataStore.getProxy().setUrl({mRID: mRID, registerId: registerId});
	            dataStore.load(function() {
	            	me.getRegisterDataList().getSelectionModel().select(0);
	            	me.getRegisterDataList().setTitle(record.get('readingTypeFullAliasName'));
	            	pageMainContent.setLoading(false);
	            });
            }
	     });
    },
    onRegisterDataListSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewRegisterData2(record);
    },
    previewRegisterData2: function (record) {
        var me = this,
            widget = Ext.widget('registerDataPreview'), 
            form = widget.down('#registerDataPreviewForm'),
            previewContainer = me.getRegisterDataSetup().down('#previewComponentContainer');
        
        form.loadRecord(record);
        var datestr = Uni.DateTime.formatDateLong(new Date(record.get('readingTime')))
        			+ ' ' + Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase() + ' '
        				+ Uni.DateTime.formatTimeLong(new Date(record.get('readingTime')));
        widget.setTitle(datestr);
        previewContainer.removeAll();
        previewContainer.add(widget);
   	
    },
    
    
});

