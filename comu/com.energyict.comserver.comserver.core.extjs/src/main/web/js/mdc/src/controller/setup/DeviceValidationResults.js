Ext.define('Mdc.controller.setup.DeviceValidationResults', {
    extend: 'Ext.app.Controller',	
	requires: [
        'Mdc.store.ValidationResultsDurations'     
    ],
    /*requires: [
        'Mdc.store.DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],*/
	models: [
		'Mdc.model.ValidationResultsDataFilter'
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
	/*
    stores: [
        'DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],*/
	views: [
        'Mdc.view.setup.devicevalidationresults.ValidationResultsMainView'
    ],
	/*
    views: [
        'Mdc.view.setup.devicedatavalidation.RulesSetMainView'
    ],
    mixins: [
        'Mdc.util.DeviceDataValidationActivation'
    ],*/
	refs: [
        {ref: 'page', selector: '#deviceValidationResultsMainView'},
		{ref: 'validationResultsTabPanel', selector: '#validationResultsTabPanel'},
		{ref: 'sideFilterForm', selector: '#deviceValidationResultsFilterForm'},
		{ref: 'filterPanel', selector: 'deviceValidationResultsMainView filter-top-panel'}

		/*,
        {ref: 'rulesSetGrid', selector: '#deviceDataValidationRulesSetGrid'},
        {ref: 'rulesSetPreviewCt', selector: '#deviceDataValidationRulesSetPreviewCt'},
        {ref: 'rulePreview', selector: '#deviceDataValidationRulePreview'},
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeRuleSetStateActionMenuItem'},
        {ref: 'ruleSetVersionsGrid', selector:'#deviceDataValidationRuleSetVersionsGrid'},
        {ref: 'rulesSetVersionPreviewCt', selector: '#deviceDataValidationRuleSetVersionsPreviewCt'},
        {ref: 'ruleSetVersionsPreview', selector:'#deviceDataValidationRulesSetVersionPreview'}*/
    ],
    /*refs: [
        {ref: 'page', selector: '#deviceDataValidationRulesSetMainView'},
        {ref: 'rulesSetGrid', selector: '#deviceDataValidationRulesSetGrid'},
        {ref: 'rulesSetPreviewCt', selector: '#deviceDataValidationRulesSetPreviewCt'},
        {ref: 'rulePreview', selector: '#deviceDataValidationRulePreview'},
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeRuleSetStateActionMenuItem'},
        {ref: 'ruleSetVersionsGrid', selector:'#deviceDataValidationRuleSetVersionsGrid'},
        {ref: 'rulesSetVersionPreviewCt', selector: '#deviceDataValidationRuleSetVersionsPreviewCt'},
        {ref: 'ruleSetVersionsPreview', selector:'#deviceDataValidationRulesSetVersionPreview'}
    ],
    mRID: null,
    ruleSetId: null,
    */init: function () {
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

        });


     /*   this.control({
            '#deviceDataValidationRulesSetGrid': {
                afterrender: this.onRulesSetGridAfterRender,
                //itemclick: this.onRulesSetGridItemClick,
                selectionchange: this.onRulesSetGridSelectionChange
            },

            '#deviceDataValidationRuleSetVersionsGrid': {
                afterrender: this.onRulesSetVersionGridAfterRender,
                selectionchange: this.onRulesSetVersionsGridSelectionChange
            },

            '#deviceDataValidationRulesGrid': {
                afterrender: this.onRulesGridAfterRender,
                selectionchange: this.onRulesGridSelectionChange
            },
            '#changeRuleSetStateActionMenuItem': {
                click: this.changeRuleSetStatus
            },
            '#deviceDataValidationStateChangeBtn': {
                click: this.changeDataValidationStatus
            },
            '#validationFromDate': {
                change: this.onValidationFromDateChange
            }
        });
     */   this.callParent();
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
/*
		if (tab.itemId === 'validationResults-configuration') {
			 widget = Ext.widget('deviceLoadProfilesData', {
                router: me.getController('Uni.controller.history.Router'),
                device: device
            });

		} else if (tab.itemId === 'validationResults-data') {
			return;
		}
*/
		if (Ext.isEmpty(router.filter.data.intervalStart)) {
			me.setDefaults();
		}
		me.getSideFilterForm().loadRecord(router.filter);
		me.setFilterView();


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



	/*,
    showDeviceDataValidationMainView: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.mRID = mRID;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                Ext.Ajax.request({
                    url: '/api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/validationstatus',
                    method: 'GET',
                    timeout: 60000,
                    success: function () {
                        var widget = Ext.widget('deviceDataValidationRulesSetMainView', { device: device });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.updateDataValidationStatusSection(mRID, widget);
                        viewport.setLoading(false);
                    }
                });
            }
        });
    },
    changeDataValidationStatus: function (btn) {
        btn.action === 'activate' ? this.showActivationConfirmation(this.getPage()) : this.showDeactivationConfirmation(this.getPage());
    },
    onRulesSetGridAfterRender: function (grid) {
        var me = this;
        grid.store.getProxy().setExtraParam('mRID', me.mRID);
        grid.store.load({
            callback: function () {
                grid.getSelectionModel().doSelect(0);
            }
        });
    },
    onRulesSetGridSelectionChange: function (grid) {
        this.getRulesSetPreviewCt().removeAll(true);
        var validationRuleSet = grid.lastSelected,
            rulesSetPreview = Ext.widget('deviceDataValidationRulesSetPreview', {
                ruleSetId: validationRuleSet.get('id'),
                title: validationRuleSet.get('name')
            });
        this.getRulesSetPreviewCt().add(rulesSetPreview);
        var menuItem = this.getChangeRuleSetStateActionMenuItem();
        menuItem.setText(validationRuleSet.get('isActive') ?
            Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
            Uni.I18n.translate('general.activate', 'MDC', 'Activate'))
    },




    onRulesSetVersionGridAfterRender: function (grid) {
        var ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');
        grid.store.getProxy().setExtraParam('ruleSetId',ruleSetId);


        grid.store.load({
            ruleSetId: ruleSetId,
            callback: function () {

                var rec = grid.store.find('status', 'CURRENT');
                if ((rec>=0)|| (this.getView())) {
                    grid.getSelectionModel().select(rec);
                }else{
                    grid.getSelectionModel().doSelect(0);
                }
            }
        });
    },
    onRulesSetVersionsGridSelectionChange: function (grid) {
        this.getRulesSetVersionPreviewCt().removeAll(true);
        var selectedRuleSetVersion = grid.lastSelected,
            rulesSetVersionPreview = Ext.widget('deviceDataValidationRulesSetVersionPreview', {
                versionId: selectedRuleSetVersion.get('id'),
                title: selectedRuleSetVersion.get('name')
            });
        this.getRulesSetVersionPreviewCt().add(rulesSetVersionPreview);

    },

    onRulesGridAfterRender: function (grid) {
        var ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');
        var versionId = this.getRuleSetVersionsGrid().getSelectionModel().getLastSelected().get('id');

        grid.store.getProxy().setExtraParam('ruleSetId',ruleSetId);
        grid.store.getProxy().setExtraParam('versionId',versionId);
        //var versionId = this.getRuleSetVersionsGrid().getSelectionModel().getLastSelected().get('id');
        grid.store.load({
            ruleSetId: ruleSetId,
            versionId: versionId,
            callback: function () {
                grid.getSelectionModel().doSelect(0);
            }
        });
    },
    onRulesGridSelectionChange: function (grid) {
        var rulePreview = this.getRulePreview(),
            validationRule = grid.lastSelected,
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
    },
    changeRuleSetStatus: function () {
        var me = this,
            ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id'),
            record = this.getRulesSetGrid().getStore().getById(ruleSetId),
            ruleSetName = record.get('name'),
            ruleSetIsActive = record.get('isActive');
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/validationrulesets/' + ruleSetId + '/status',
            method: 'PUT',
            jsonData: (!ruleSetIsActive).toString(),
            success: function () {
                me.getRulesSetGrid().getStore().reload({
                    callback: function () {
                        me.getRulesSetGrid().getSelectionModel().doSelect(me.getRulesSetGrid().getStore().indexOf(record));
                        me.getApplication().fireEvent('acknowledge', ruleSetIsActive ?
                            Uni.I18n.translatePlural('device.dataValidation.ruleSet.deactivated', ruleSetName, 'MDC', 'Rule set {0} was deactivated successfully') :
                            Uni.I18n.translatePlural('device.dataValidation.ruleSet.activated', ruleSetName, 'MDC', 'Rule set {0} was activated successfully'));
                    }
                });
            }
        });
    }*/
});