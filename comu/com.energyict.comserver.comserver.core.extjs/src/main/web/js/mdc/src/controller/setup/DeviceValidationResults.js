Ext.define('Mdc.controller.setup.DeviceValidationResults', {
    extend: 'Ext.app.Controller',	
	requires: [
        'Mdc.store.ValidationResultsDurations'     
    ],
	models: [
		'Mdc.model.ValidationResultsDataFilter',
		'Mdc.model.ValidationResults'
	],
	
	stores: [
        'Mdc.store.DataIntervalAndZoomLevels',
        'Mdc.store.ValidationResultsDurations',   
		'Mdc.store.ValidationResultsRuleSets',		
		'Mdc.store.ValidationResultsRules',
		'Mdc.store.ValidationResultsVersions'
    ],
	views: [
        'Mdc.view.setup.devicevalidationresults.ValidationResultsMainView'
    ],
	refs: [
        {ref: 'page', selector: '#deviceValidationResultsMainView'},
		{ref: 'validationResultsTabPanel', selector: '#validationResultsTabPanel'},
		{ref: 'sideFilterForm', selector: '#deviceValidationResultsFilterForm'},
		{ref: 'filterPanel', selector: 'deviceValidationResultsMainView filter-top-panel'},
		{ref: 'validationResultsRulesetForm', selector: '#deviceValidationResultsRulesetForm'},
		{ref: 'configurationViewValidationResultsBrowse', selector: '#configurationViewValidationResultsBrowse'},		
		{ref: 'ruleSetGrid', selector: '#ruleSetList'},
		{ref: 'ruleSetVersionGrid', selector: '#ruleSetVersionList'},
		{ref: 'ruleSetVersionRuleGrid', selector: '#ruleSetVersionRuleList'},
		{ref: 'ruleSetVersionRulePreview', selector: '#ruleSetVersionRulePreview'},
		
		
	
    ],    
    mRID: null,
    init: function () {
		var me = this;
        me.control({
            '#validationResultsTabPanel': {
                tabChange: this.changeTab
            },
			
			'#devicevalidationresultsfilterpanel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            },
			
			'#configurationViewValidationResultsBrowse #ruleSetList': {				
                selectionchange: this.onRuleSetGridSelectionChange
            },
			
			'#configurationViewValidationResultsBrowse #ruleSetVersionList': {
                selectionchange: this.onRuleSetVersionGridSelectionChange
            },
			
			'#configurationViewValidationResultsBrowse #ruleSetVersionRuleList': {
                selectionchange: this.onRuleSetVersionRuleGridSelectionChange
            },
			
			'deviceValidationResultsSideFilter #deviceValidationResultsFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceValidationResultsSideFilter #deviceValidationResultsFilterResetBtn': {
                click: this.clearFilter
            },

        });
		
		
		this.callParent();
    },
	
	showDeviceValidationResultsMainView: function (mRID, activeTab) {
		 var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.mRID = mRID;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                Ext.Ajax.request({
                    url: '/api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus', /*modify*/
                    method: 'GET',
                    timeout: 60000,
                    success: function () {
                        var widget = Ext.widget('deviceValidationResultsMainView', { device: device });
                        me.getApplication().fireEvent('changecontentevent', widget);
						me.getValidationResultsTabPanel().setActiveTab(activeTab);
                        //me.updateDataValidationStatusSection(mRID, widget);
                        viewport.setLoading(false);
                    }
                });
            }
        });
	},
	
	changeTab: function (tabPanel, tab) {
	
		var me = this,			        
            router = me.getController('Uni.controller.history.Router');

		if (Ext.isEmpty(router.filter.data.intervalStart)) {
			me.setDefaults();
		}
		me.getSideFilterForm().loadRecord(router.filter);
		me.setFilterView();
		me.loadConfigurationData();
	},
	
	setDefaults : function(){
		 var me = this,
            router = me.getController('Uni.controller.history.Router'),            
            intervalStart = new Date().getTime(),
			durationsStore = me.getStore('Mdc.store.ValidationResultsDurations'),
			firstItem = durationsStore.first();
        router.filter.beginEdit();
        router.filter.set('intervalStart', me.getIntervalStart(intervalStart, firstItem));
        router.filter.set('duration', firstItem.get('count') + firstItem.get('timeUnit'));        
		router.filter.set('count', firstItem.get('count')); 
		router.filter.set('timeUnit', firstItem.get('timeUnit')); 
        router.filter.endEdit();	
	},
	
	 getIntervalStart: function (intervalEnd, item) {
        return moment(intervalEnd).subtract(item.get('timeUnit'), item.get('count')).toDate();
    },
	
	setFilterView: function () {
        var filterForm = this.getSideFilterForm(),
            filterView = this.getFilterPanel(),
            intervalStartField = filterForm.down('[name=intervalStart]'),
            intervalEndField = filterForm.down('[name=duration]'),
            intervalStart = intervalStartField.getValue(),
            intervalEnd = intervalEndField.getRawValue(),
            eventDateText = '';
        eventDateText += intervalEnd + ' ' + intervalStartField.getFieldLabel().toLowerCase() + ' '
            + Uni.DateTime.formatDateShort(intervalStart);
        filterView.setFilter('eventDateChanged', filterForm.down('#dateContainer').getFieldLabel(), eventDateText, true);
        filterView.down('#Reset').setText('Reset');        
	},
	
	clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilterItem: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        if (key === 'onlySuspect' || key === 'onlyNonSuspect') {
            record.set(key, false);
        }
        record.save();
    },
	
	applyFilter: function () {
        var filterForm = this.getSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

	loadConfigurationData: function(){
		var me = this,		
			models = me.getModel('Mdc.model.ValidationResults'),
			router = me.getController('Uni.controller.history.Router');
			
			models.getProxy().setUrl(me.mRID);
			models.getProxy().setFilterModel(router.filter);
			
			models.load('', {
				success: function (record) {
					me.loadConfigurationDataItems(record);
            }
        });
	},
	
	loadConfigurationDataItems : function(record){
		var me = this,
			validationResultsRulesetForm = me.getValidationResultsRulesetForm();
			ruleSetGrid = me.getRuleSetGrid(),
			ruleSetVersionGrid = me.getRuleSetVersionGrid(),
			ruleSetVersionRuleGrid = me.getRuleSetVersionRuleGrid();
			
			validationResultsRulesetForm.loadRecord(record);
			
			var configurationViewValidationResultsBrowse = me.getConfigurationViewValidationResultsBrowse();				
			configurationViewValidationResultsBrowse.setVisible(record.get('dataValidated'));
			
			ruleSetGrid.getStore().on('datachanged', function (){ruleSetGrid.getSelectionModel().select(0); return true;}, this);
			ruleSetGrid.getStore().loadData(record.get('detailedRuleSets'));
		
	},
	
	onRuleSetGridSelectionChange : function(grid, record){	
		var me = this,
			ruleSetVersionGrid = me.getRuleSetVersionGrid();
		
		ruleSetVersionGrid.getStore().on('datachanged', function (){ruleSetVersionGrid.getSelectionModel().select(0); return true;}, this);
		ruleSetVersionGrid.getStore().loadData(record[0].get('detailedRuleSetVersions'));		
	},

	onRuleSetVersionGridSelectionChange : function(grid, record){
		var me = this,
			ruleSetVersionRuleGrid = me.getRuleSetVersionRuleGrid();
		
		ruleSetVersionRuleGrid.getStore().on('datachanged', function (){ruleSetVersionRuleGrid.getSelectionModel().select(0); return true;}, this);
		ruleSetVersionRuleGrid.getStore().loadData(record[0].get('detailedRules'));	
	},
      
	onRuleSetVersionRuleGridSelectionChange : function(grid, record){
		var me = this,			
			rulePreview = me.getRuleSetVersionRulePreview(),
			validationRule = record[0],
			readingTypes = validationRule.data.readingTypes;
		
        rulePreview.loadRecord(validationRule);
        rulePreview.setTitle(validationRule.get('name'));
        Ext.suspendLayouts();
        rulePreview.down('property-form').removeAll();
        if (validationRule.properties() && validationRule.properties().count()) {
            rulePreview.down('property-form').loadRecord(validationRule);
        }
        rulePreview.down('#readingTypesArea').removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('general.readingTypes', 'MDC', 'Reading types'),
                readingType = readingTypes[i];

            rulePreview.down('#readingTypesArea').add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'reading-type-displayfield',
                            fieldLabel: fieldlabel,
                            labelWidth: 260,
                            value: readingType
                        }
                    ]
                }
            );
        }
        Ext.resumeLayouts(true);
	}
	
});