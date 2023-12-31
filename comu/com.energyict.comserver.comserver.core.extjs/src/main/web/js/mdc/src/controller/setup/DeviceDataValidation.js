/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        {ref: 'ruleSetVersionsGrid', selector: '#deviceDataValidationRuleSetVersionsGrid'},
        {ref: 'rulesSetVersionPreviewCt', selector: '#deviceDataValidationRuleSetVersionsPreviewCt'},
        {ref: 'ruleSetVersionsPreview', selector: '#deviceDataValidationRulesSetVersionPreview'}
    ],
    deviceId: null,
    ruleSetId: null,

    init: function () {
        this.control({
            '#deviceDataValidationRulesSetGrid': {
                afterrender: this.onRulesSetGridAfterRender,
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
    },

    showDeviceDataValidationMainView: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceId = deviceId;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                if (record.get('hasLogBooks')
                    || record.get('hasLoadProfiles')
                    || record.get('hasRegisters')) {
                    me.getApplication().fireEvent('loadDevice', record);
                    Ext.Ajax.request({
                        url: '/api/ddr/devices/' + encodeURIComponent(deviceId) + '/validationrulesets/validationstatus',
                        method: 'GET',
                        timeout: 60000,
                        success: function () {
                            var widget = Ext.widget('deviceDataValidationRulesSetMainView', {device: record});
                            me.getApplication().fireEvent('changecontentevent', widget);
                            me.updateDataValidationStatusSection(deviceId, widget);
                            viewport.setLoading(false);
                        }
                    });
                } else {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                    viewport.setLoading(false);
                }
            }
        });
    },
    changeDataValidationStatus: function (btn) {
        btn.action === 'activate' ? this.showActivationConfirmation(this.getPage()) : this.showDeactivationConfirmation(this.getPage());
    },
    onRulesSetGridAfterRender: function (grid) {
        var me = this;
        grid.setLoading(true);
        grid.store.getProxy().setExtraParam('deviceId', me.deviceId);
        grid.store.load({
            callback: function () {
                grid.setLoading(false);
            }
        });
    },
    onRulesSetGridSelectionChange: function (grid) {
        Ext.suspendLayouts();
        this.getRulesSetPreviewCt().removeAll(true);
        var validationRuleSet = grid.lastSelected,
            rulesSetPreview = Ext.widget('deviceDataValidationRulesSetPreview', {
                ruleSetId: validationRuleSet.get('id'),
                title: validationRuleSet.get('name')
            });
        this.getRulesSetPreviewCt().add(rulesSetPreview);
        var menuItem = this.getChangeRuleSetStateActionMenuItem();
        if (!!menuItem) {
            menuItem.setText(validationRuleSet.get('isActive') ?
                Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                Uni.I18n.translate('general.activate', 'MDC', 'Activate'))
        }
        Ext.resumeLayouts(true);
    },


    onRulesSetVersionGridAfterRender: function (grid) {
        var me = this,
            ruleSetId = me.getRulesSetGrid().getSelectionModel().getLastSelected().get('id');

        grid.store.getProxy().setExtraParam('ruleSetId', ruleSetId);
        grid.store.load({
            ruleSetId: ruleSetId,
            callback: function () {
                var recIndex = grid.store.find('status', 'CURRENT');
                if ((recIndex >= 0) || (me.getView())) {
                    grid.getSelectionModel().select(recIndex);
                } else {
                    grid.getSelectionModel().select(0);
                }
            }
        });
    },
    onRulesSetVersionsGridSelectionChange: function (grid) {
        Ext.suspendLayouts();
        this.getRulesSetVersionPreviewCt().removeAll(true);
        var selectedRuleSetVersion = grid.lastSelected,
            rulesSetVersionPreview = Ext.widget('deviceDataValidationRulesSetVersionPreview', {
                ruleSetId: selectedRuleSetVersion.get('ruleSetId'),
                versionId: selectedRuleSetVersion.get('id'),
                title: selectedRuleSetVersion.get('name')
            });
        this.getRulesSetVersionPreviewCt().add(rulesSetVersionPreview);
        Ext.resumeLayouts(true);
    },

    onRulesGridAfterRender: function (grid) {
        var selectedRuleSetVersion = this.getRuleSetVersionsGrid().getSelectionModel().getLastSelected(),
            ruleSetId = selectedRuleSetVersion.get('parent').id,
            versionId = selectedRuleSetVersion.get('id');

        grid.store.getProxy().setExtraParam('ruleSetId', ruleSetId);
        grid.store.getProxy().setExtraParam('versionId', versionId);
        grid.store.load({
            ruleSetId: ruleSetId,
            versionId: versionId
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
            ruleSetIsActive = record.get('isActive'),
            page = me.getPage();

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.deviceId) + '/validationrulesets/' + ruleSetId + '/status',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                isActive: !ruleSetIsActive,
                device: _.pick(page.device.getRecordData(), 'name', 'version', 'parent')
            },
            success: function (res) {
                var data = Ext.decode(res.responseText);
                page.device.set(data.device);
                me.getRulesSetGrid().getStore().reload({
                    callback: function () {
                        me.getApplication().fireEvent('acknowledge', ruleSetIsActive ?
                            Uni.I18n.translate('device.dataValidation.ruleSet.deactivated', 'MDC', 'Validation rule set deactivated') :
                            Uni.I18n.translate('device.dataValidation.ruleSet.activated', 'MDC', 'Validation rule set activated'));
                    }
                });
            }
        });
    }
});