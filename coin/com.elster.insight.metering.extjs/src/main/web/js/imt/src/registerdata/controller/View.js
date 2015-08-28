Ext.define('Imt.registerdata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.registerdata.store.Register',
        'Imt.registerdata.view.Setup',
        'Imt.registerdata.view.Preview'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint'
    ],
    stores: [
        'Imt.registerdata.store.Register'
    ],
    views: [
        'Imt.registerdata.view.RegisterList'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'registerList', selector: '#registerList'},
        {ref: 'registerListSetup', selector: '#registerListSetup'},
        {ref: 'registerPreview', selector: '#registerPreview'},
        {ref: 'stepsMenu', selector: '#stepsMenu'}
    ],
    init: function () {
        var me = this;
        me.control({
            '#registerList': {
                select: me.onRegisterListSelect
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
    
    showUsagePointRegister: function(mRID, id) {
        
    },
    
    
    
    
});

