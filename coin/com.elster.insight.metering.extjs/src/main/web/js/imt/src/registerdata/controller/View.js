Ext.define('Imt.registerdata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.registerdata.store.Register',
        'Imt.registerdata.view.Setup',
        'Imt.registerdata.view.Preview',
        'Imt.registerdata.store.Reading',
        'Imt.registerdata.view.ReadingsSetup',
        'Imt.registerdata.view.ReadingPreview'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.registerdata.model.Register',
        'Imt.registerdata.model.Reading'
    ],
    stores: [
        'Imt.registerdata.store.Register',
        'Imt.registerdata.store.Reading'
    ],
    views: [
        'Imt.registerdata.view.RegisterList',
        'Imt.registerdata.view.ReadingList'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'registerList', selector: '#registerList'},
        {ref: 'registerListSetup', selector: '#registerListSetup'},
        {ref: 'registerPreview', selector: '#registerPreview'},
        
        {ref: 'readingOverviewLink', selector: '#reading-overview-link'},
        {ref: 'readingList', selector: '#readingList'},
        {ref: 'readingListSetup', selector: '#readingListSetup'},
        {ref: 'readingPreview', selector: '#readingPreview'},
        
        {ref: 'stepsMenu', selector: '#stepsMenu'}
    ],
    init: function () {
        var me = this;
        me.control({
            '#registerList': {
                select: me.onRegisterListSelect
            },
            '#readingList': {
                select: me.onReadingListSelect
            },
            '#deviceRegisterConfigurationActionMenu': {
                click: this.chooseAction
            },
            '#registerActionMenu': {
                click: this.chooseAction
            }
        });
    },
    showUsagePointRegisters: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePoint = Ext.create('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);
        var widget = Ext.widget('registerListSetup', {router: router, mRID: mRID});
        usagePoint.set('mRID', mRID);
        me.getApplication().fireEvent('usagePointLoaded', usagePoint);
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getOverviewLink().setText(mRID);
        me.getRegisterList().getSelectionModel().select(0);
        pageMainContent.setLoading(false);
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
        widget.setTitle(record.get('readingTypeAlias'));
        previewContainer.removeAll();
        previewContainer.add(widget);
   	
    },
    
    showUsagePointReading: function(mRID, readingTypemRID) {
	     var me = this,
         router = me.getController('Uni.controller.history.Router'),
         usagePoint = Ext.create('Imt.usagepointmanagement.model.UsagePoint'),
         pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
	     
	     pageMainContent.setLoading(true);
	     var widget = Ext.widget('readingListSetup', {router: router, mRID: mRID, readingTypemRID: readingTypemRID});
	     usagePoint.set('mRID', mRID);
	     me.getApplication().fireEvent('usagePointLoaded', usagePoint);
	     me.getApplication().fireEvent('changecontentevent', widget);
	     me.getOverviewLink().setText(mRID);
	     me.getReadingList().getSelectionModel().select(0);
	     pageMainContent.setLoading(false);
    },
    onReadingListSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewReadingData(record);
    },
    previewReadingData: function (record) {
        var me = this,
            widget = Ext.widget('readingPreview'), 
            form = widget.down('#readingPreviewForm'),
            previewContainer = me.getReadingListSetup().down('#previewComponentContainer');
        
        form.loadRecord(record);
        var datestr = Uni.DateTime.formatDateLong(new Date(record.get('utcTimestamp')))
        			+ ' ' + Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase() + ' '
        				+ Uni.DateTime.formatTimeLong(new Date(record.get('utcTimestamp')));
        widget.setTitle(datestr);
        previewContainer.removeAll();
        previewContainer.add(widget);
   	
    },
    
    
    
});

