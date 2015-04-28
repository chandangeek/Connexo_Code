Ext.define('Mdc.controller.setup.DeviceValidationResults', {
    extend: 'Ext.app.Controller',	
	requires: [
        'Mdc.store.ValidationResultsDurations'     
    ],
	models: [
		'Mdc.model.ValidationResultsDataFilter',
		'Mdc.model.ValidationResults',
        'Mdc.model.ValidationResultsDataView'
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
        {ref: 'loadProfileGrid', selector: 'loadProfileList'},
        {ref: 'registerGrid', selector: 'registerList'}
		
    ],    
    mRID: null,
	dataValidationLastChecked: null,
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
			viewport = Ext.ComponentQuery.query('viewport')[0],
			confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowConfirmationWindow',
                confirmText: Uni.I18n.translate('validationResults.validate.confirm', 'MDC', 'Validate'),				
				msg: 'message',
                confirmation: function () {
                    me.onValidateNow(this);
                }
            });
		
        viewport.setLoading();		
		Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
            method: 'GET',
            timeout: 60000,
            success: function (response) {
			
				viewport.setLoading(false);		
				
                var res = Ext.JSON.decode(response.responseText);				
				if (!res.isActive || res.allDataValidated){
					return;
				}
				
                if (res.lastChecked) {
                    me.dataValidationLastChecked = new Date(res.lastChecked);
                } else {
                    me.dataValidationLastChecked = new Date();
                }
				
				confirmationWindow.insert(1,me.getActivationConfirmationContent());
				confirmationWindow.show({
					title: Ext.String.format(Uni.I18n.translate('validationResults.validate.title', 'MDC', 'Validate data of device {0}?'), me.mRID)
				});
                
            },
			failure: function (record) {
				viewport.setLoading(false);
			}
        });
		
        confirmationWindow.on('close', function () {
            this.destroy();
        });		
	},
	
	 onValidateNow: function (confWindow) {
	 		
        var me = this;
		
		var isFromLastValidation = confWindow.down('#validationRun').getValue().validation === 'lastValidation';
        var isFromNewValidation = confWindow.down('#validationRun').getValue().validation === 'newValidation';
	
        me.confirmationWindowButtonsDisable(true);
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
            method: 'PUT',
            jsonData: {
                isActive: 'true',
                lastChecked: (isFromNewValidation ? confWindow.down('#validationFromDate').getValue().getTime() : me.dataValidationLastChecked.getTime())
            },
            success: function () {
                //me.updateDataValidationStatusSection(me.mRID, view);
                me.validateData(confWindow);                
            },
            failure: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                me.showValidationActivationErrors(res.errors[0].msg);
                me.confirmationWindowButtonsDisable(false);
            }
        });
    },

	 validateData: function (confWindow) {
        var me = this;

        confWindow.down('#validationProgress').add(Ext.create('Ext.ProgressBar', {
                margin: '5 0 15 0'
            })).wait({
            duration: 120000,
            text: Uni.I18n.translate('device.dataValidation.isInProgress', 'MDC', 'Data validation is in progress. Please wait...'),
            fn: function () {
                me.destroyConfirmationWindow();
                Ext.widget('messagebox', {
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.close', 'MDC', 'Close'),
                            ui: 'remove',
                            handler: function () {
                                this.up('window').close();
                            }
                        }
                    ],
                    listeners: {
                        close: function () {
                            this.destroy();
                        }
                    }
                }).show({
                    ui: 'notification-error',
                    title: Uni.I18n.translate('device.dataValidation.timeout.title', 'MDC', 'Data validation takes longer as expected'),
                    msg: Uni.I18n.translate('device.dataValidation.timeout.msg', 'MDC', 'Data validation takes longer as expected. Data validation will continue in the background'),
                    icon: Ext.MessageBox.ERROR
                });
            }
        });

        Ext.Ajax.suspendEvent('requestexception');

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validate',
            method: 'PUT',
            timeout: 600000,
            success: function () {
                me.destroyConfirmationWindow();
                me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('device.dataValidation.activation.validated', me.mRID, 'MDC', 'Data validation completed'));   

				me.showDeviceValidationResultsMainView(me.mRID, 0);
            },
            failure: function (response) {
                if (confWindow) {
                    var res = Ext.JSON.decode(response.responseText);
                    confWindow.down('#validationProgress').removeAll(true);
                    me.showValidationActivationErrors(res.errors[0].msg);
                    me.confirmationWindowButtonsDisable(false);
                }
            },
            callback: function () {
                Ext.Ajax.resumeEvent('requestexception');
            }
        });
    },
	
	getActivationConfirmationContent: function () {
        var me = this;
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left'
            },
            items: [
				{
                    xtype: 'radiogroup',
                    itemId: 'validationRun',
                    columns: 1,
					padding: '-10 0 0 60',
                    defaults: {
                        name: 'validationRun',                    
                    },
                    items: [
						{
							boxLabel: Uni.I18n.translate('validationResults.validate.fromLast', 'MDC', 'Validate data from last validation'),
							inputValue: 'lastValidation',
							itemId: 'validateFromLast',
							xtype: 'radiofield',
							checked: true,
							name: 'validation'
						}, 
						{
							xtype: 'container',
							layout: {
								type: 'hbox',
								align: 'stretch'
							},
							width: 300,							
							items: [	
								{
									boxLabel: Uni.I18n.translate('validationResults.validate.from', 'MDC', 'Validate data from'),
									inputValue: 'newValidation',
									itemId: 'validateFromDate',
									xtype: 'radiofield',									
									name: 'validation'
								},
								{
									xtype: 'datefield',
									itemId: 'validationFromDate',
									editable: false,
									showToday: false,
									value: me.dataValidationLastChecked,
									fieldLabel: '  ',
									labelWidth: 10,
									width: 150,
									listeners: {
										focus: {
											fn: function () {														
												var radioButton = Ext.ComponentQuery.query('#validationRun #validateFromDate')[0];
												radioButton.setValue(true);
											}
										}
                                    }									
								}
							]
						}   
                    ]
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
			configurationViewValidationResultsBrowse.setVisible(record.get('detailedRuleSets') && record.get('detailedRuleSets').length >0);
			
			var configurationViewValidateNowBtn = me.getConfigurationViewValidateNowBtn();
			configurationViewValidateNowBtn.setDisabled(!record.get('isActive') || record.get('allDataValidated'));
			
			ruleSetGrid.getStore().on('datachanged', function (){ruleSetGrid.getSelectionModel().select(0); return true;}, this);
			ruleSetGrid.getStore().loadData(record.get('detailedRuleSets'));		
	},

    loadValidationResultsData: function(){
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            models = me.getModel('Mdc.model.ValidationResultsDataView'),
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


        //loadProfileGrid.getStore().on('datachanged', function (){loadProfileGrid.getSelectionModel().select(0); return true;}, this);
        loadProfileGrid.getStore().loadData(record.get('detailedValidationLoadProfile'));
        loadProfileGrid.getSelectionModel().select(0);

        //registerGrid.getStore().on('datachanged', function (){registerGrid.getSelectionModel().select(0); return true;}, this);
        registerGrid.getStore().loadData(record.get('detailedValidationRegister'));
        registerGrid.getSelectionModel().select(0);
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