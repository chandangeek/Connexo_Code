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
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeRuleSetStateActionMenuItem'}
    ],
    mRID: null,
    init: function () {
        this.control({
            '#deviceDataValidationRulesSetGrid': {
                afterrender: this.onRulesSetGridAfterRender,
                itemclick: this.onRulesSetGridItemClick,
                selectionchange: this.onRulesSetGridSelectionChange
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
                    url: '/api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
                    method: 'GET',
                    success: function () {
                        var widget = Ext.widget('deviceDataValidationRulesSetMainView', { device: device });
                        me.updateDataValidationStatusSection(mRID, widget);
                        me.getApplication().fireEvent('changecontentevent', widget);
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
                rulesSetId: validationRuleSet.get('id'),
                title: validationRuleSet.get('name'),
                height: 750
            });
        this.getRulesSetPreviewCt().add(rulesSetPreview);
    },
    onRulesSetGridItemClick: function (gridView, record, el, idx, e) {
        var target = e.getTarget(null, null, true);
        if (target.hasCls('x-action-col-icon')) {
            var menuItem = this.getChangeRuleSetStateActionMenuItem();
            menuItem.setText(record.get('isActive') ?
                Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                Uni.I18n.translate('general.activate', 'MDC', 'Activate'))
        }
    },
    onRulesGridAfterRender: function (grid) {
        var ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');
        grid.store.load({
            id: ruleSetId,
            callback: function () {
                grid.getSelectionModel().doSelect(0);
            }
        });
    },
    onRulesGridSelectionChange: function (grid) {
        var rulePreview = this.getRulePreview(),
            validationRule = grid.lastSelected,
            properties = validationRule.data.properties,
            readingTypes = validationRule.data.readingTypes;

        rulePreview.loadRecord(validationRule);
        rulePreview.setTitle(validationRule.get('name'));

        rulePreview.down('#propertiesArea').removeAll();
        for (var i = 0; i < properties.length; i++) {
            var property = properties[i];
            var propertyName = property.name;
            var propertyValue = property.value;
            var required = property.required;
            var label = propertyName;
            if (!required) {
                label = label + ' (optional)';
            }
            rulePreview.down('#propertiesArea').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: label,
                    value: propertyValue,
                    labelWidth: 260
                }
            );
        }

        rulePreview.down('#readingTypesArea').removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var fieldlabel = i > 0 ? '&nbsp' : Uni.I18n.translate('validation.readingTypes', 'CFG', 'Reading type(s)'),
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
    },
    changeRuleSetStatus: function () {
        var me = this,
            ruleSetId = this.getRulesSetGrid().getSelectionModel().getLastSelected().get('id'),
            record = this.getRulesSetGrid().getStore().getById(ruleSetId),
            ruleSetName = record.get('name'),
            ruleSetIsActive = record.get('isActive');
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + me.mRID + '/validationrulesets/' + ruleSetId + '/status',
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