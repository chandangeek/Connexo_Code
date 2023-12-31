/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.controller.mixins.AddPurposesCommon', {
    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreviewPanel();

        Ext.suspendLayouts();
        preview.setTitle(record.get('metrologyConfigurationInfo').name);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    addPurposesToRuleSet: function (grid, records) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        Ext.Ajax.request({
            method: 'PUT',
            url: Ext.String.format(me.addPurposesLink, router.arguments.ruleSetId),
            jsonData: {
                contracts: formatData()
            },
            success: onSuccessAdd,
            callback: addCallback
        });

        function formatData() {
            return _.map(records, function (record) {
                return record.getRecordData();
            });
        }

        function onSuccessAdd() {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.add.success.msg', 'IMT', '{0} purposes of metrology configuirations added',
                records.length));
            if (grid.rendered) {
                window.location.href = grid.cancelHref;
            }
        }

        function addCallback() {
            mainView.setLoading(false);
        }
    },

    addPurposesAndStatesToRuleSet: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            purposesGrid = me.getPurposesGrid(),
            statesGrid = me.getStatesGrid(),
            contractsRecords = purposesGrid.getSelectedRecords(),
            statesRecords = statesGrid.getSelectedRecords();

        if(!statesGrid.allStatesSelected && !statesRecords.length){
            me.getStatesErrorMessage().show()
        } else {
            me.getStatesErrorMessage().hide();
            mainView.setLoading();
            Ext.Ajax.request({
                method: 'PUT',
                url: Ext.String.format(me.addPurposesLink, router.arguments.ruleSetId),
                jsonData: {
                    contracts: formatData(contractsRecords),
                    lifeCycleStates: formatData(statesRecords)
                },
                success: onSuccessAdd,
                callback: addCallback
            });
        }

        function formatData(records) {
            return _.map(records, function (record) {
                return record.getRecordData();
            });
        }

        function onSuccessAdd() {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.add.success.msg', 'IMT', '{0} purposes of metrology configuirations added',
                contractsRecords.length));
            if (purposesGrid.rendered) {
                window.location.href = purposesGrid.cancelHref;
            }
        }

        function addCallback() {
            mainView.setLoading(false);
        }
    }
});