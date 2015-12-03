Ext.define('Imt.validation.controller.UsagePointDataValidation', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.validation.store.UsagePointDataValidationRulesSet',
        'Imt.validation.model.UsagePointDataValidationRulesSet',
        'Imt.validation.model.UsagePointDataValidationStatus',
        'Imt.validation.view.RulesSetMainView'
    ],
    stores: [
        'Imt.validation.store.UsagePointDataValidationRulesSet',
    ],
    views: [
        'Imt.validation.view.RulesSetMainView'
    ],
    mixins: [
        'Imt.validation.util.UsagePointDataValidationActivation'
    ],
    refs: [
        {ref: 'page', selector: '#usagePointDataValidationRulesSetMainView'},
        {ref: 'rulesSetGrid', selector: '#usagePointDataValidationRulesSetGrid'},
        {ref: 'rulesSetPreviewCt', selector: '#usagePointDataValidationRulesSetPreviewCt'},
        {ref: 'rulePreview', selector: '#usagePointDataValidationRulePreview'},
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeRuleSetStateActionMenuItem'},
        {ref: 'ruleSetVersionsGrid', selector: '#usagePointDataValidationRuleSetVersionsGrid'},
        {ref: 'rulesSetVersionPreviewCt', selector: '#usagePointDataValidationRuleSetVersionsPreviewCt'},
        {ref: 'ruleSetVersionsPreview', selector: '#usagePointDataValidationRulesSetVersionPreview'}
    ],
    mRID: null,
    ruleSetId: null,

    init: function () {
        this.control({
            '#usagePointDataValidationRulesSetGrid': {
                afterrender: this.onRulesSetGridAfterRender,
                //itemclick: this.onRulesSetGridItemClick,
                selectionchange: this.onRulesSetGridSelectionChange
            },

            '#usagePointDataValidationRuleSetVersionsGrid': {
                afterrender: this.onRulesSetVersionGridAfterRender,
                selectionchange: this.onRulesSetVersionsGridSelectionChange
            },

            '#usagePointDataValidationRulesGrid': {
                afterrender: this.onRulesGridAfterRender,
                selectionchange: this.onRulesGridSelectionChange
            },
            '#changeRuleSetStateActionMenuItem': {
                click: this.changeRuleSetStatus
            },
            '#usagePointDataValidationStateChangeBtn': {
                click: this.changeDataValidationStatus
            },
            '#validationFromDate': {
                change: this.onValidationFromDateChange
            }
        });
    },

    showUsagePointDataValidationMainView: function (mRID) {
    	  var me = this,
    	  router = me.getController('Uni.controller.history.Router'),
          viewport = Ext.ComponentQuery.query('viewport')[0];

	      me.mRID = mRID;
	
	      viewport.setLoading();
	
	      Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
	          success: function (usagePoint) {
	              me.getApplication().fireEvent('loadUsagePoint', usagePoint);
	              Ext.Ajax.request({
	                  url: '/api/udr/usagepoints/' + encodeURIComponent(mRID) + '/validationrulesets/validationstatus',
	                  method: 'GET',
	                  timeout: 60000,
	                  success: function () {
	                      var widget = Ext.widget('usagePointDataValidationRulesSetMainView', {router: router, usagePoint: usagePoint});
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
        grid.store.getProxy().setExtraParam('mRID', encodeURIComponent(me.mRID));
        grid.store.load({
            callback: function () {
                grid.getSelectionModel().doSelect(0);
            }
        });
    },
    onRulesSetGridSelectionChange: function (grid) {
        this.getRulesSetPreviewCt().removeAll(true);
        var validationRuleSet = grid.lastSelected,
            rulesSetPreview = Ext.widget('usagePointDataValidationRulesSetPreview', {
                ruleSetId: validationRuleSet.get('id'),
                title: validationRuleSet.get('name')
            });
        this.getRulesSetPreviewCt().add(rulesSetPreview);
        var menuItem = this.getChangeRuleSetStateActionMenuItem();
        if (!!menuItem) {
            menuItem.setText(validationRuleSet.get('isActive') ?
                Uni.I18n.translate('general.deactivate', 'IMT', 'Deactivate') :
                Uni.I18n.translate('general.activate', 'IMT', 'Activate'))
        }
    },


    onRulesSetVersionGridAfterRender: function (grid) {
        var me = this,
            ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');

        grid.store.getProxy().setExtraParam('ruleSetId', ruleSetId);


        grid.store.load({
            ruleSetId: ruleSetId,
            callback: function () {

                var rec = grid.store.find('status', 'CURRENT');
                if ((rec >= 0) || (me.getView())) {
                    grid.getSelectionModel().select(rec);
                } else {
                    grid.getSelectionModel().doSelect(0);
                }
            }
        });
    },
    onRulesSetVersionsGridSelectionChange: function (grid) {
        this.getRulesSetVersionPreviewCt().removeAll(true);
        var selectedRuleSetVersion = grid.lastSelected,
            rulesSetVersionPreview = Ext.widget('usagePointDataValidationRulesSetVersionPreview', {
            	ruleSetId: this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id'),
                versionId: selectedRuleSetVersion.get('id'),
                title: selectedRuleSetVersion.get('name')
            });
        this.getRulesSetVersionPreviewCt().add(rulesSetVersionPreview);

    },

    onRulesGridAfterRender: function (grid) {
        var ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');
        var versionId = this.getRuleSetVersionsGrid().getSelectionModel().getLastSelected().get('id');

        grid.store.getProxy().setExtraParam('ruleSetId', ruleSetId);
        grid.store.getProxy().setExtraParam('versionId', versionId);
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
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('general.readingTypes', 'IMT', 'Reading types'),
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
            ruleSetIsActive = record.get('isActive'),
            page = me.getPage();
        Ext.Ajax.request({
            url: '/api/udr/usagepoints/' + encodeURIComponent(me.mRID) + '/validationrulesets/' + ruleSetId + '/status',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                isActive: !ruleSetIsActive,
 //               device: _.pick(page.device.getRecordData(), 'mRID', 'version', 'parent')
                usagePoint: record.get('usagePoint'),
            },
            success: function () {
                me.getRulesSetGrid().getStore().reload({
                    callback: function () {
                        me.getRulesSetGrid().getSelectionModel().doSelect(me.getRulesSetGrid().getStore().indexOf(record));
                        me.getApplication().fireEvent('acknowledge', ruleSetIsActive ?
                            Uni.I18n.translate('usagepoint.dataValidation.ruleSet.deactivated', 'IMT', 'Validation rule set deactivated') :
                            Uni.I18n.translate('usagepoint.dataValidation.ruleSet.activated', 'IMT', 'Validation rule set activated'));
                    }
                });
            }
        });
    }
});