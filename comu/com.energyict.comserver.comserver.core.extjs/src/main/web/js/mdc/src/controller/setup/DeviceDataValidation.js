Ext.define('Mdc.controller.setup.DeviceDataValidation', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],
    stores: [
        'DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],
    views: [
        'Mdc.view.setup.devicedatavalidation.RulesSetMainView'
    ],
    mixins: [
        'Mdc.util.DeviceDataValidationActivation'
    ],
    refs: [
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
    init: function () {
        this.control({
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
        this.callParent();
    },
    showDeviceDataValidationMainView: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.mRID = mRID;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                Ext.Ajax.request({
                    url: '/api/ddr/devices/' + encodeURIComponent(mRID) + '/validationrulesets/validationstatus',
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
        var me = this,
			ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');
		
		grid.store.getProxy().setExtraParam('ruleSetId',ruleSetId);


        grid.store.load({
            ruleSetId: ruleSetId,
            callback: function () {

                var rec = grid.store.find('status', 'CURRENT');
                if ((rec>=0)|| (me.getView())) {
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
    }
});