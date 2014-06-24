Ext.define('Mdc.controller.setup.ValidationRuleSets', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.DeviceConfigValidationRuleSets',
        'Cfg.store.ValidationRuleSets'
    ],

    views: [
        'setup.validation.RulesOverview',
        'setup.validation.AddRuleSets'
    ],

    stores: [
        'DeviceConfigValidationRuleSets'
    ],

    refs: [
        {ref: 'validationRuleSetsOverview', selector: 'validation-rules-overview'},
        {ref: 'validationRuleSetsGrid', selector: 'validation-rules-overview validation-rulesets-grid'},
        {ref: 'validationRulesGrid', selector: 'validation-rules-overview validation-rules-grid'},
        {ref: 'addValidationRuleSets', selector: 'validation-add-rulesets'},
        {ref: 'addValidationRuleSetsGrid', selector: 'validation-add-rulesets validation-add-rulesets-grid'},
        {ref: 'addValidationRulesGrid', selector: 'validation-add-rulesets validation-add-rules-grid'},
        {ref: 'addValidationRulesPreview', selector: 'validation-add-rulesets validation-rule-preview'},
        {ref: 'validationRulesPreview', selector: 'validation-rules-overview validation-rule-preview'}
    ],

    deviceTypeId: null,
    deviceConfigId: null,

    init: function () {
        this.callParent(arguments);

        this.control({
            'validation-add-rulesets validation-add-rulesets-grid': {
                selectionchange: this.onAddValidationRuleSetsSelectionChange
            },
            'validation-add-rulesets button[action=addValidationRuleSets]': {
                click: this.onAddValidationRuleSets
            },
            'validation-add-rulesets button[action=uncheckAll]': {
                click: this.onUncheckAll
            },
            'validation-add-rulesets uni-actioncolumn': {
                menuclick: this.onAddValidationActionMenuClick
            },
            'validation-rules-overview validation-rulesets-grid': {
                selectionchange: this.onValidationRuleSetsSelectionChange
            },
            'validation-rules-overview uni-actioncolumn': {
                menuclick: this.onValidationActionMenuClick
            },
            'validation-add-rulesets validation-rule-actionmenu': {
                click: this.onAddValidationPreviewActionClick
            },
            'validation-rules-overview validation-rule-actionmenu': {
                click: this.onValidationPreviewActionClick
            },
            'validation-add-rulesets validation-rules-grid': {
                selectionchange: this.onAddValidationRuleSelectionChange
            },
            'validation-rules-overview validation-rules-grid': {
                selectionchange: this.onValidationRuleSelectionChange
            }
        });
    },

    showValidationRuleSetsOverview: function (deviceTypeId, deviceConfigId) {
        var me = this;

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigId = deviceConfigId;

        me.getDeviceConfigValidationRuleSetsStore().getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        var widget = Ext.widget('validation-rules-overview', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId});

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);

                // Load the store in asynchronously.
                me.getDeviceConfigValidationRuleSetsStore().load();

                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getValidationRuleSetsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    showAddValidationRuleSets: function (deviceTypeId, deviceConfigId) {
        var me = this,
            deviceConfigRuleSetsStore = me.getDeviceConfigValidationRuleSetsStore(),
            ruleSetsStore;

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigId = deviceConfigId;

        deviceConfigRuleSetsStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        var widget = Ext.widget('validation-add-rulesets', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId});

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);

                // This can be loaded asynchronously with the device configuration model.
                deviceConfigRuleSetsStore.load({
                    callback: function () {
                        ruleSetsStore = me.getAddValidationRuleSetsGrid().getStore();
                        ruleSetsStore.load(
                            {
                                callback: function () {
                                    ruleSetsStore.remove(deviceConfigRuleSetsStore.data.items);
                                }
                            }
                        );
                    }
                });

                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getAddValidationRuleSetsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    onValidationRuleSetsSelectionChange: function (grid) {
        var view = this.getValidationRuleSetsOverview(),
            selection = grid.view.getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.updateValidationRuleSet(selection[0]);
        }
    },

    onAddValidationRuleSetsSelectionChange: function (grid) {
        var view = this.getAddValidationRuleSets(),
            selection = grid.view.getSelectionModel().getSelection(),
            counter = Ext.ComponentQuery.query('validation-add-rulesets #selection-counter')[0],
            selectionText = Uni.I18n.translatePlural(
                'validation.validationRuleSetSelection',
                selection.length,
                'MDC',
                '{0} validation rule sets selected'
            );

        counter.setText(selectionText);

        if (selection.length > 0) {
            view.updateValidationRuleSet(selection[0]);
        }
    },

    onAddValidationRuleSets: function () {
        var me = this,
            view = me.getAddValidationRuleSets(),
            grid = me.getAddValidationRuleSetsGrid(),
            selection = grid.view.getSelectionModel().getSelection(),
            url = '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets',
            loadMask = Ext.create('Ext.LoadMask', {
                target: view
            }),
            ids = [];

        Ext.Array.each(selection, function (item) {
            ids.push(item.internalId);
        });

        loadMask.show();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: Ext.encode(ids),
            success: function () {
                location.href = '#/administration/devicetypes/'
                    + me.deviceTypeId + '/deviceconfigurations/'
                    + me.deviceConfigId + '/validationrulesets';

                var message = Uni.I18n.translatePlural(
                    'validation.ruleSetAdded',
                    selection.length,
                    'MDC',
                    'Succesfully added validation rule sets.'
                );

                me.getApplication().fireEvent('acknowledge', message);
            },
            failure: function (response) {
                if (response.status === 400) {
                    var result = Ext.decode(response.responseText, true),
                        title = Uni.I18n.translate('general.failedToAdd', 'MDC', 'Failed to add'),
                        message = Uni.I18n.translatePlural(
                            'validation.failedToAddMessage',
                            selection.length,
                            'MDC',
                            'Validation rule sets could not be added. There was a problem accessing the database'
                        );

                    if (result !== null) {
                        title = result.error;
                        message = result.message;
                    }

                    me.getApplication().getController('Uni.controller.Error').showError(title, message);
                }
            },
            callback: function () {
                loadMask.destroy();
            }
        });
    },

    onUncheckAll: function () {
        var grid = this.getAddValidationRuleSetsGrid();
        grid.getView().getSelectionModel().deselectAll();
    },

    onAddValidationActionMenuClick: function (menu, item) {
        var action = item.action,
            record = menu.record;

        switch (action) {
            case 'viewRule':
                // TODO Link to the rule overview when [JP-3505] is done.
                break;
            case 'viewRuleSet':
                window.location.href = '#/administration/validation/overview/' + record.getId();
                break;
        }
    },

    onValidationActionMenuClick: function (menu, item) {
        var me = this,
            action = item.action,
            record = menu.record;

        switch (action) {
            case 'viewRuleSet':
                window.location.href = '#/administration/validation/overview/' + record.getId();
                break;
            case 'removeRuleSet':
                me.removeValidationRuleSet(record);
                break;
        }
    },

    onAddValidationPreviewActionClick: function (menu, item) {
        var me = this,
            record = menu.record || me.getAddValidationRulesPreview().getRecord(),
            action = item.action;

        switch (action) {
            case 'viewRule':
                // TODO Link to the rule overview when [JP-3505] is done.
                break;
        }
    },

    onValidationPreviewActionClick: function (menu, item) {
        var me = this,
            record = menu.record || me.getValidationRulesPreview().getRecord(),
            action = item.action;

        switch (action) {
            case 'viewRule':
                // TODO Link to the rule overview when [JP-3505] is done.
                break;
        }
    },

    removeValidationRuleSet: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('validation.remove.confirmation.title', 'MDC', 'Remove {0}?', [record.data.name]),
            msg: Uni.I18n.translate('validation.deviceconfig.remove.confirmation.msg', 'MDC',
                'This validation rule set type will no longer be available on the device configuration.'),
            config: {
                me: me,
                record: record
            },
            fn: me.doRemoveValidationRuleSet
        });
    },

    doRemoveValidationRuleSet: function (state, text, cfg) {
        var me = this;

        if (state === 'confirm') {
            var scope = cfg.config.me,
                store = scope.getValidationRuleSetsGrid().getStore(),
                preloader = Ext.create('Ext.LoadMask', {
                    target: scope.getValidationRuleSetsOverview()
                });

            preloader.show();

            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + scope.deviceTypeId
                    + '/deviceconfigurations/' + scope.deviceConfigId
                    + '/validationrulesets/' + cfg.config.record.getId(),
                method: 'DELETE',
                success: function () {
                    scope.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.remove.success', 'MDC', 'Successfully removed.'));
                    store.load();
                },
                failure: function (response) {
                    if (response.status === 400) {
                        var record = cfg.config.record,
                            result = Ext.decode(response.responseText, true),
                            title = Uni.I18n.translate('validation.remove.failed', 'MDC', 'Failed to remove {0}', [record.data.name]),
                            message = Uni.I18n.translate('general.server.error', 'MDC', 'Server error');
                        if (!Ext.isEmpty(response.statusText)) {
                            message = response.statusText;
                        }
                        if (result && result.message) {
                            message = result.message;
                        } else if (result && result.error) {
                            message = result.error;
                        }
                        self.getApplication().getController('Uni.controller.Error').showError(title, message);
                    }
                },
                callback: function () {
                    preloader.destroy();
                }
            });
        }
    },

    onAddValidationRuleSelectionChange: function (grid) {
        var view = this.getAddValidationRulesPreview(),
            selection = grid.view.getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.updateValidationRule(selection[0]);
        }
    },

    onValidationRuleSelectionChange: function (grid) {
        var view = this.getValidationRulesPreview(),
            selection = grid.view.getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.updateValidationRule(selection[0]);
        }
    }
});
