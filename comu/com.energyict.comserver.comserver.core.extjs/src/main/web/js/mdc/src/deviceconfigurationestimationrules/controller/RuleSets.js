Ext.define('Mdc.deviceconfigurationestimationrules.controller.RuleSets', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.deviceconfigurationestimationrules.view.Setup',
        'Mdc.deviceconfigurationestimationrules.view.RuleSetup'
    ],

    requires: [
        'Mdc.model.DeviceType',
        'Mdc.model.DeviceConfiguration'
    ],

    stores: [
        'Mdc.deviceconfigurationestimationrules.store.EstimationRuleSets',
        'Mdc.deviceconfigurationestimationrules.store.EstimationRules'
    ],


    refs: [
        {
            ref: 'page',
            selector: 'device-configuration-estimation-rule-sets-setup'
        },
        {
            ref: 'rulesPage',
            selector: 'device-configuration-estimation-rules-setup'
        },
        {
            ref: 'rulesPlaceholder',
            selector: 'device-configuration-estimation-rule-sets-setup #rulesPlaceholder'
        },
        {
            ref: 'ruleSetsGrid',
            selector: 'device-configuration-estimation-rule-sets-setup device-configuration-estimation-rule-sets-grid'
        }
    ],

    init: function () {
        this.control({
            'device-configuration-estimation-rule-sets-setup device-configuration-estimation-rule-sets-grid': {
                selectionchange: this.showRulesForSelectedSet
            },
            'device-configuration-estimation-rules-setup device-configuration-estimation-rules-grid': {
                select: this.showRulePreview
            },
            'device-configuration-estimation-rule-sets-setup button[action=addEstimationRuleSet]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets/add')
                        .forward();
                }
            },
            'device-configuration-estimation-rule-sets-setup button[action=editRuleSetsOrder]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets')
                        .forward(null, {editOrder: true});
                }
            },
            'device-configuration-estimation-rule-sets-setup button[action=undoOrderingEstimationRuleSet]': {
                click: function () {
                    this.getController('Uni.controller.history.Router')
                        .getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets')
                        .forward();
                }
            },
            'device-configuration-estimation-rule-sets-setup button[action=saveRuleSetsOrder]': {
                click: this.saveEstimationRuleSetsOrder
            },
            '#statesActionMenu menuitem[action=remove]': {
                click: this.removeEstimationRuleSet
            }
        });
    },

    saveEstimationRuleSetsOrder: function () {
        var me = this,
            view = me.getPage(),
            router = me.getController('Uni.controller.history.Router'),
            grid = me.getRuleSetsGrid(),
            store = grid.getStore(),
            url = store.getProxy().url,
            ruleSets = [];

        view.setLoading(true);

        store.each(function (item) {
            ruleSets.push({
                id: item.get('id'),
                parent: item.get('parent')
            });
        });

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: Ext.encode(ruleSets),
            success: function () {
                var message = Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.add.success', 'MDC', 'Estimation rule sets order saved.');
                router.getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets').forward();
                me.getApplication().fireEvent('acknowledge', message);
            },
            callback: function () {
                view.setLoading(false);
            }
        });
    },

    removeEstimationRuleSet: function (menu) {
        var me = this,
            ruleSetGrid = this.getPage().down('device-configuration-estimation-rule-sets-grid'),
            router = this.getController('Uni.controller.history.Router'),
            record = ruleSetGrid.getSelectionModel().getLastSelected();

        record.getProxy().setUrl(router.arguments);

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
            msg: Uni.I18n.translate('estimationrules.ruleset.remove.confirmation.msg', 'MDC',
                'This estimation rule set type will no longer be available on the device configuration.'),
            config: {
                me: me,
                record: record
            },
            fn: me.doRemoveEstimationRuleSet
        });
    },

    doRemoveEstimationRuleSet: function (state, text, cfg) {
        var me = this;

        if (state === 'confirm') {

            var scope = cfg.config.me,
                router = scope.getController('Uni.controller.history.Router'),
                page = scope.getPage();

            page.setLoading();

            cfg.config.record.destroy({
                success: function () {
                    scope.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.remove.success', 'MDC', 'Successfully removed.'));
                    router.getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets').forward();
                },
                failure: function (response) {
                    if (response.status === 400) {
                        var record = cfg.config.record,
                            result = Ext.decode(response.responseText, true),
                            title = Uni.I18n.translate('general.failedToRemove', 'MDC', 'Failed to remove {0}', [record.data.name]),
                            message = Uni.I18n.translate('general.serverError', 'MDC', 'Server error');
                        if (!Ext.isEmpty(response.statusText)) {
                            message = response.statusText;
                        }
                        if (result && result.message) {
                            message = result.message;
                        } else if (result && result.error) {
                            message = result.error;
                        }
                        me.getApplication().getController('Uni.controller.Error').showError(title, message);
                    }
                },
                callback: function () {
                    page.setLoading(false);
                }
            });
        }
    },

    showRulesForSelectedSet: function (selectionModel, record) {
        var me = this,
            rulesContainer = me.getRulesPlaceholder(),
            selectedRecord,
            store = Ext.getStore('Mdc.deviceconfigurationestimationrules.store.EstimationRules');

        rulesContainer.removeAll();
        if (selectionModel.getSelection().length === 1) {

            selectedRecord = record[0] ? record[0] : record;

            store.getProxy().setUrl(selectedRecord.get('id'));
            rulesContainer.add([
                {
                    xtype: 'panel',
                    title: selectedRecord.get('name')
                },
                {
                    xtype: 'device-configuration-estimation-rules-setup',
                    router: me.getController('Uni.controller.history.Router')
                }
            ]);
        }

    },

    showRulePreview: function (selectionModel, record) {
        var me = this,
            page = me.getRulesPage(),
            preview = page.down('device-configuration-estimation-rules-preview'),
            previewForm = page.down('device-configuration-estimation-rules-preview-form');

        Ext.suspendLayouts();
        preview.down('property-form').loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.loadRecord(record);
        previewForm.fillReadings(record);
        Ext.resumeLayouts(true);
    },

    showEstimationRuleSets: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget;

        if (router.queryParams.editOrder && !Mdc.privileges.DeviceConfigurationEstimations.canAdministrate()) {
            return crossroads.parse("/error/notfound");
        }

        Ext.getStore('Mdc.deviceconfigurationestimationrules.store.EstimationRuleSets').getProxy().setUrl(router.arguments);

        widget = Ext.widget('device-configuration-estimation-rule-sets-setup', { router: router });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.down('device-configuration-estimation-rule-sets-grid').getStore().load();
        me.loadDeviceTypeAndConfiguration(deviceTypeId, deviceConfigurationId, widget);
    },

    loadDeviceTypeAndConfiguration: function (deviceTypeId, deviceConfigurationId, widget) {
        var me = this;

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        if (widget.down('#stepsMenu #deviceConfigurationOverviewLink')) {
                            widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
                        }
                    }
                });
            }
        });
    }

});

