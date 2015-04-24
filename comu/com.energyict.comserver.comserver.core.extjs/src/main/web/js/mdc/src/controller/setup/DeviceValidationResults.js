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
		'Mdc.store.ValidationResultsVersions',
        'Mdc.store.ValidationResultsLoadProfiles',
        'Mdc.store.ValidationResultsRegisters'
    ],
	views: [
        'Mdc.view.setup.devicevalidationresults.ValidationResultsMainView'
    ],	
	refs: [
        {ref: 'page', selector: '#deviceValidationResultsMainView'},
		{ref: 'validationResultsTabPanel', selector: '#validationResultsTabPanel'},
		{ref: 'sideFilterForm', selector: '#deviceValidationResultsFilterForm'},
		{ref: 'filterPanel', selector: 'deviceValidationResultsMainView filter-top-panel'},
        {ref: 'filterDataPanel', selector: 'deviceValidationResultsMainView #validation-results-data-filter'},
		{ref: 'validationResultsRulesetForm', selector: '#deviceValidationResultsRulesetForm'},
        {ref: 'validationResultsLoadProfileRegisterForm', selector: '#deviceValidationResultsLoadProfileRegisterForm'},
		{ref: 'configurationViewValidationResultsBrowse', selector: '#configurationViewValidationResultsBrowse'},		
		{ref: 'ruleSetGrid', selector: '#ruleSetList'},
		{ref: 'ruleSetVersionGrid', selector: '#ruleSetVersionList'},
		{ref: 'ruleSetVersionRuleGrid', selector: '#ruleSetVersionRuleList'},
		{ref: 'ruleSetVersionRulePreview', selector: '#ruleSetVersionRulePreview'},		
		{ref: 'configurationViewValidateNowBtn', selector: 'deviceValidationResultsRuleset #configurationViewValidateNow'},
        {ref: 'loadProfileGrid', selector: '#loadProfileList'},
        {ref: 'registerGrid', selector: '#registerList'}
		
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
			'deviceValidationResultsSideFilter #deviceValidationResultsFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceValidationResultsSideFilter #deviceValidationResultsFilterResetBtn': {
                click: this.clearFilter
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
			'deviceValidationResultsRuleset #configurationViewValidateNow': {
                click: this.validateNow
            }			
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
				viewport.setLoading(false);
				
				var widget = Ext.widget('deviceValidationResultsMainView', { device: device });
				me.getApplication().fireEvent('changecontentevent', widget);
				me.getValidationResultsTabPanel().setActiveTab(activeTab);                        
            },
			failure: function (response) {
				viewport.setLoading(false);
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
        me.loadValidationResultsData();
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
		router.filter.endEdit();
	},

	 getIntervalStart: function (intervalEnd, item) {
        return moment(intervalEnd).subtract(item.get('timeUnit'), item.get('count')).toDate();
    },

	setFilterView: function () {
        var filterForm = this.getSideFilterForm(),
            filterView = this.getFilterPanel(),
            filterDataView = this.getFilterDataPanel(),
            intervalStartField = filterForm.down('[name=intervalStart]'),
            intervalEndField = filterForm.down('[name=duration]'),
            intervalStart = intervalStartField.getValue(),
            intervalEnd = intervalEndField.getRawValue(),
            eventDateText = '';
			
        eventDateText += intervalEnd + ' ' + intervalStartField.getFieldLabel().toLowerCase() + ' '
            + Uni.DateTime.formatDateShort(intervalStart);
        filterView.setFilter('eventDateChanged', filterForm.down('#dateContainer').getFieldLabel(), eventDateText, true);
        filterView.down('#Reset').setText('Reset');

        filterDataView.setFilter('eventDateChanged', filterForm.down('#dateContainer').getFieldLabel(), eventDateText, true);
	},

	clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilterItem: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;
			
        record.save();
    },

	applyFilter: function () {
        var filterForm = this.getSideFilterForm();
		
        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

	validateNow : function () {		
		 var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowConfirmationWindow',
                confirmText: Uni.I18n.translate('validationResults.validate.confirm', 'MDC', 'Validate'),				
				msg: 'message',
                confirmation: function () {
                    me.onValidateNow(this);
                }
            });
        
		confirmationWindow.insert(1,me.getActivationConfirmationContent());
        confirmationWindow.show({
			title: Ext.String.format(Uni.I18n.translate('validationResults.validate.title', 'MDC', 'Perform validation on device {0}?'), me.mRID)
		});
        confirmationWindow.on('close', function () {
            this.destroy();
        });		
	},
	
	 onValidateNow: function (confWindow) {
	 		
        var me = this;
        if (me.hasValidation) {
            var isValidationRunImmediately = confWindow.down('#validationRunRg').getValue().validationRun === 'now';
            var isWaitForNewData = confWindow.down('#validationRunRg').getValue().validationRun === 'waitForNewData';
        }
        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: {
                isActive: 'true',
                lastChecked: (me.hasValidation ? confWindow.down('#validationFromDate').getValue().getTime() : null)
            },
            success: function () {
                me.updateDataValidationStatusSection(me.mRID, view);
                if (isValidationRunImmediately) {
                    me.isValidationRunImmediately = true;
                    me.validateData(confWindow);
                } else {
                    me.destroyConfirmationWindow();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.activated', me.mRID, 'MDC', 'Data validation activated'));
                }
            },
            failure: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                me.showValidationActivationErrors(res.errors[0].msg);
                me.confirmationWindowButtonsDisable(false);
            }
        });
    },

	getActivationConfirmationContent: function () {
        var me = this;
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left',
                labelStyle: 'font-weight: normal; padding-left: 50px'
            },
            items: [
                {
                    xtype: 'datefield',
                    itemId: 'validationFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item1', 'MDC', '1. Validate data from'),
                    labelWidth: 175,
                    labelPad: 1
                },
                {
                    xtype: 'panel',
                    itemId: 'validationDateErrors',
                    hidden: true,
                    bodyStyle: {
                        color: '#eb5642',
                        padding: '0 0 15px 65px'
                    },
                    html: ''
                },
                {
                    xtype: 'displayfield',
                    value: '',
                    padding: '0 0 -10 0',
                    fieldLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item2', 'MDC', '2. When do you want to run the data validation?'),
                    labelWidth: 350
                },
                {
                    xtype: 'radiogroup',
                    itemId: 'validationRunRg',
                    columns: 1,
                    defaults: {
                        name: 'validationRun',
                        padding: '-10 0 0 60'
                    },
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item2.1', 'MDC', 'Run now'),
                            inputValue: 'now'
                        },
                        {
                            boxLabel: Uni.I18n.translate('device.dataValidation.activateConfirmation.item2.2', 'MDC', 'Wait for new data'),
                            inputValue: 'waitForNewData',
                            checked: true
                        }
                    ]
                },
                {
                    xtype: 'panel',
                    itemId: 'validationProgress',
                    layout: 'fit',
                    padding: '0 0 0 50'
                }
            ]
        });
    },

	
	destroyConfirmationWindow: function () {
        if (Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].removeAll(true);
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].destroy();
        }
    },

    confirmationWindowButtonsDisable: function (value) {
        if (Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('button[name=confirm]').setDisabled(value);
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('button[name=cancel]').setDisabled(value);
        }
    },
	
	 showValidationActivationErrors: function (errors) {
        if (Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('#validationDateErrors').update(errors);
            Ext.ComponentQuery.query('#validateNowConfirmationWindow')[0].down('#validationDateErrors').setVisible(true);
        }
    },
	
	loadConfigurationData: function(){
		var me = this,		
			viewport = Ext.ComponentQuery.query('viewport')[0],
			models = me.getModel('Mdc.model.ValidationResults'),
			router = me.getController('Uni.controller.history.Router');
			
		models.getProxy().setUrl(me.mRID);
		models.getProxy().setFilterModel(router.filter);
		
        viewport.setLoading();		
		models.load('', {
			success: function (record) {
				me.loadConfigurationDataItems(record);				
				viewport.setLoading(false);
            },
			failure: function (response) {
				viewport.setLoading(false);
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
			
			var configurationViewValidateNowBtn = me.getConfigurationViewValidateNowBtn();
			configurationViewValidateNowBtn.setDisabled(!record.get('dataValidated'));
			
			ruleSetGrid.getStore().on('datachanged', function (){ruleSetGrid.getSelectionModel().select(0); return true;}, this);
			ruleSetGrid.getStore().loadData(record.get('detailedRuleSets'));		
	},

    loadValidationResultsData: function(){
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            models = me.getModel('Mdc.model.ValidationResultsLoadProfile'),
            router = me.getController('Uni.controller.history.Router');

        models.getProxy().setUrl(me.mRID);
        models.getProxy().setFilterModel(router.filter);

        viewport.setLoading();
        models.load('', {
            success: function (record) {
                me.loadValidationResultsDataItems(record);
                viewport.setLoading(false);
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    loadValidationResultsDataItems : function(record){
        var me = this,
            validationResultsDataForm = me.getValidationResultsLoadProfileRegisterForm();
            loadProfileGrid = me.getLoadProfileGrid(),
            registerGrid = me.getRegisterGrid(),

            validationResultsDataForm.loadRecord(record);


        //loadProfileGrid.getStore().on('datachanged', function (){ruleSetGrid.getSelectionModel().select(0); return true;}, this);
        //loadProfileGrid.getStore().loadData(record.get('detailedRuleSets'));
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