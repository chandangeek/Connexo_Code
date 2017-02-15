/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.controller.AddRuleSets', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.deviceconfigurationestimationrules.view.AddRuleSetsSetup',
        'Mdc.deviceconfigurationestimationrules.view.RuleSetup'
    ],

    requires: [
        'Mdc.model.DeviceType',
        'Mdc.model.DeviceConfiguration'
    ],

    stores: [
        'Mdc.deviceconfigurationestimationrules.store.EstimationRuleSets',
        'Mdc.deviceconfigurationestimationrules.store.EstimationRules',
        'Mdc.deviceconfigurationestimationrules.store.BulkEstimationRuleSets'
    ],


    refs: [
        {
            ref: 'page',
            selector: 'device-configuration-estimation-add-rule-sets-setup'
        },
        {
            ref: 'rulesPage',
            selector: 'device-configuration-estimation-add-rule-sets-setup device-configuration-estimation-rules-setup'
        },
        {
            ref: 'bulkRuleSets',
            selector: 'device-configuration-estimation-add-rule-sets-setup'
        },
        {
            ref: 'rulesPlaceholder',
            selector: 'device-configuration-estimation-add-rule-sets-setup #rulesPlaceholder'
        },
        {
            ref: 'bulkSelectionGrid',
            selector: 'device-configuration-estimation-add-rule-sets-setup device-configuration-estimation-add-rule-sets-bulk-selection'
        }
    ],

    init: function () {
        this.control({
            'device-configuration-estimation-add-rule-sets-setup device-configuration-estimation-add-rule-sets-bulk-selection': {
                selectionchange: this.showRulesForSelectedSet
            },
            'device-configuration-estimation-add-rule-sets-bulk-selection radiogroup': {
                change: this.checkSelection
            },
            'device-configuration-estimation-add-rule-sets-bulk-selection': {
                allitemsadd: this.addAllEstimationRuleSets,
                selecteditemsadd: this.addSelectedEstimationRuleSets
            },
            'device-configuration-estimation-add-rule-sets-setup button[action=addEstimationRuleSet]': {
                click: this.moveToAdminAddRuleSet
            }
        });
    },

    moveToAdminAddRuleSet: function() {
     var router = this.getController('Uni.controller.history.Router');

     router.getRoute('administration/estimationrulesets/addruleset').forward(null, {previousRoute: router.getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets/add').buildUrl() });

    },

    addAllEstimationRuleSets: function () {
        this.addEstimationRuleSets(false, null)
    },

    addSelectedEstimationRuleSets: function (selection) {
        this.addEstimationRuleSets(true, selection)
    },

    addEstimationRuleSets: function (isSelected, selection) {
        var me = this,
            view = me.getBulkRuleSets(),
            router = me.getController('Uni.controller.history.Router'),
            grid = me.getBulkSelectionGrid(),
            store = grid.getStore(),
            url = store.getProxy().url,
            ruleSets = [];

        view.setLoading(true);

        if (isSelected) {
            Ext.Array.each(selection, function (item) {
                ruleSets.push({
                    id: item.get('id'),
                    parent: item.get('parent')
                });
            });
        }

        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: Ext.encode(ruleSets),
            params: {
                all: !isSelected
            },
            success: function () {
                var message = Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.add.success', 'MDC', 'Estimation rule sets added.');
                router.getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets').forward();
                me.getApplication().fireEvent('acknowledge', message);
            },
            callback: function () {
                view.setLoading(false);
            }
        });
    },

    checkSelection: function () {
        var me = this,
            bulkSelectionGrid = me.getBulkSelectionGrid();

        me.getRulesPlaceholder().setVisible(!bulkSelectionGrid.isAllSelected());
    },

    showRulesForSelectedSet: function (selectionModel, record) {
        var me = this,
            rulesContainer = me.getRulesPlaceholder();

        Ext.suspendLayouts();
        rulesContainer.removeAll();
        if (selectionModel.getSelection().length === 1) {

            Ext.getStore('Mdc.deviceconfigurationestimationrules.store.EstimationRules').getProxy().setUrl(record[0].get('id'));

            rulesContainer.setTitle(Ext.String.htmlEncode(record[0].get('name')));
            rulesContainer.add([
                {
                    xtype: 'device-configuration-estimation-rules-setup',
                    router: me.getController('Uni.controller.history.Router')
                }
            ]);
        }
        Ext.resumeLayouts(true);
    },

    showAddEstimationRuleSetsView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store,
            widget;

        widget = Ext.widget('device-configuration-estimation-add-rule-sets-setup', { router: router });
        store = widget.down('device-configuration-estimation-add-rule-sets-bulk-selection').getStore();
        store.removeAll();
        store.getProxy().setUrl(router.arguments);
        store.load({
            success: function () {
                me.getBulkSelectionGrid().down('radiogroup').items.items[1].setValue(true);
            }
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        me.getApplication().getController('Mdc.deviceconfigurationestimationrules.controller.RuleSets').loadDeviceTypeAndConfiguration(deviceTypeId, deviceConfigurationId, widget);
    }

});

