Ext.define('Mdc.controller.setup.ValidationRuleSets', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.DeviceConfigValidationRuleSets',
        'Cfg.store.ValidationRuleSets',
        'Mdc.store.ValidationRuleSetsForDeviceConfig',
        'Cfg.store.ValidationRuleSetVersions',
        'Cfg.view.validation.RulePreview'
    ],

    views: [
        'setup.validation.RulesOverview',
        'setup.validation.AddRuleSets',
        'Cfg.view.validation.RulePreview'

    ],

    stores: [
        'DeviceConfigValidationRuleSets',
        'Mdc.store.ValidationRuleSetsForDeviceConfig',
        'Cfg.store.ValidationRuleSets',
        'Cfg.store.ValidationRuleSetVersions'
    ],

    refs: [
        {ref: 'validationRuleSetsOverview', selector: 'validation-rules-overview'},
        {ref: 'validationRuleSetsGrid', selector: 'validation-rules-overview validation-rulesets-grid'},
        {ref: 'validationVersionsGrid', selector: 'validation-rules-overview validation-versions-grid'},
        {ref: 'validationRulesGrid', selector: 'validation-rules-overview validation-rules-grid'},
        {ref: 'addValidationRuleSets', selector: 'validation-add-rulesets'},
        {ref: 'addValidationRuleSetsGrid', selector: 'validation-add-rulesets validation-add-rulesets-grid'},
        {ref: 'addValidationRulesGrid', selector: 'validation-add-rulesets validation-add-rules-grid'},
        {ref: 'addValidationRulesPreview', selector: 'validation-add-rulesets validation-rule-preview'},
        {ref: 'addValidationVersionsPreview', selector: 'validation-ruleset-view validation-versions-grid'},
        {ref: 'validationRulesPreview', selector: 'validation-rules-overview validation-rule-preview'},
        {ref: 'validationVersionsPreview', selector: 'validation-rules-overview validation-versions-view'}
    ],

    deviceTypeId: null,
    deviceConfigId: null,

    init: function () {
        this.callParent(arguments);

        this.control({
            'validation-add-rulesets validation-add-rulesets-grid': {
                selectionchange: this.onAddValidationRuleSetsSelectionChange,
                allitemsadd: this.onAllValidationRuleSetsAdd,
                selecteditemsadd: this.onSelectedValidationRuleSetsAdd
            },
            'validation-add-rulesets validation-add-rulesets-grid uni-actioncolumn': {
                menuclick: this.onValidationActionMenuClick
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
            },
            'validation-rules-overview validation-versions-grid': {
                selectionchange: this.onValidationVersionsChange
            },
            'validation-ruleset-view validation-versions-grid': {
                selectionchange: this.onAddValidationVersionsChange
            },
            'validation-add-rulesets-grid': {
                selectionchange: this.onAddRuleSetsChange
            }
        });
    },

    showValidationRuleSetsOverview: function (deviceTypeId, deviceConfigId) {
        var me = this;

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigId = deviceConfigId;
        me.getDeviceConfigValidationRuleSetsStore().getProxy().extraParams = ({
            deviceType: deviceTypeId,
            deviceConfig: deviceConfigId
        });
        var widget = Ext.widget('validation-rules-overview', {
            deviceTypeId: deviceTypeId,
            deviceConfigId: deviceConfigId
        });

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);

                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
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
        var widget = Ext.widget('validation-add-rulesets', {
            deviceTypeId: deviceTypeId,
            deviceConfigId: deviceConfigId
        });
        if (me.getAddValidationRuleSetsGrid()) {
            me.getAddValidationRuleSetsGrid().getStore().removeAll();
        }
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);

                // This can be loaded asynchronously with the device configuration model.
                deviceConfigRuleSetsStore.load({
                    callback: function () {
                        ruleSetsStore = me.getAddValidationRuleSetsGrid().getStore();
                        ruleSetsStore.getProxy().extraParams =
                            ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
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
                        widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    onValidationRuleSetsSelectionChange: function (grid) {
        var view = this.getValidationRuleSetsOverview(),
            selection = grid.view.getSelectionModel().getSelection();

        this.onAddRuleSetsChange(grid);
        if (selection.length > 0) {
            view.updateValidationRuleSet(selection[0]);
        }
    },

    onAddValidationRuleSetsSelectionChange: function (grid) {
        var view = this.getAddValidationRuleSets(),
            selection = grid.view.getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.updateValidationRuleSet(selection[0]);
        }
    },

    onAllValidationRuleSetsAdd: function () {
        this.addValidationRuleSets([]);
    },

    onSelectedValidationRuleSetsAdd: function (selection) {
        this.addValidationRuleSets(selection);
    },

    addValidationRuleSets: function (selection) {
        var me = this,
            view = me.getAddValidationRuleSets(),
            url = '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets',
            loadMask = Ext.create('Ext.LoadMask', {
                target: view
            }),
            ids = [],
            allPressed = Ext.isEmpty(selection);

        if (!allPressed) {
            Ext.Array.each(selection, function (item) {
                ids.push(item.internalId);
            });
        }

        loadMask.show();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: Ext.encode(ids),
            params: {
                all: allPressed
            },
            success: function () {
                location.href = '#/administration/devicetypes/'
                + me.deviceTypeId + '/deviceconfigurations/'
                + me.deviceConfigId + '/validationrulesets';

                var message = Uni.I18n.translatePlural(
                    'validation.ruleSetAdded',
                    selection.length,
                    'MDC',
                    'Successfully added {0} validation rule sets.',
                    'Successfully added {0} validation rule set.',
                    'Successfully added {0} validation rule sets.'
                );

                me.getApplication().fireEvent('acknowledge', message);
            },
            failure: function (response) {
                if (response.status === 400) {
                    var result = Ext.decode(response.responseText, true),
                        title = Uni.I18n.translate('general.failedToAdd', 'MDC', 'Failed to add'),
                        message = Uni.I18n.translate('validation.failedToAddRuleSets', 'MDC',
                            'Validation rule sets could not be added. There was a problem accessing the database.'
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

    onAddValidationActionMenuClick: function (menu, item) {
        var action = item.action,
            record = menu.record;
        if (action === 'viewRule') {
            window.location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('ruleSetVersionId') + '/rules/' + record.getId();
        } else {
            window.location.href = '#/administration/validation/rulesets/' + record.getId();
        }
    },

    onValidationActionMenuClick: function (menu, item) {
        var me = this,
            action = item.action,
            record = menu.record;

        switch (action) {
            case 'viewRule':
                window.location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('ruleSetVersionId') + '/rules/' + record.getId();
                break;
            case 'viewVersion':
                window.location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.getId();
                break;
            case 'viewRuleSet':
                window.location.href = '#/administration/validation/rulesets/' + record.getId();
                break;
            case 'removeRuleSet':
                me.removeValidationRuleSet(record);
                break;
            default:
                window.location.href = '#/administration/validation/rulesets/' + record.getId();
                break;
        }
    },

    onAddValidationPreviewActionClick: function (menu, item) {
        var me = this,
            record = menu.record || me.getAddValidationRulesPreview().getRecord(),
            action = item.action;

        switch (action) {
            case 'viewRule':
                window.location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('ruleSetVersionId') + '/rules/' + record.getId();
                break;
        }
    },

    onValidationPreviewActionClick: function (menu, item) {
        var me = this,
            record = menu.record || me.getValidationRulesPreview().getRecord(),
            action = item.action;

        switch (action) {
            case 'viewRule':
                window.location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('ruleSetVersionId') + '/rules/' + record.getId();
                break;
        }
    },

    removeValidationRuleSet: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.data.name]),
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
    },

    onValidationVersionsChange: function (grid) {
        var view = this.getValidationVersionsPreview(),
            selection = grid.view.getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.updateValidationVersionSet(selection[0]);
        }
    },

    onAddValidationVersionsChange: function (grid) {
        var view = this.getAddValidationVersionsPreview(),
            selection = grid.view.getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.ruleSetId = selection[0].get('ruleSetId');
            view.versionId = selection[0].get('id');
            view.updateValidationVersionSet(selection[0]);
        }
    },

    onAddRuleSetsChange: function (grid) {
        var me = this,
            versionsGrid;

        if (grid.getSelection().length === 1) {
            versionsGrid = me.getAddValidationVersionsPreview();
            versionsGrid.getStore().on('load', function (store, records, success) {
                var rec = store.find('status', 'CURRENT');
                if (rec == -1) {
                    if (store.getCount() > 0) {
                        if (versionsGrid.getStore().getCount() > 0) {
                            rec = 0;
                        }
                    }
                }

                if (rec >= 0) {
                    if (versionsGrid.headerCt && versionsGrid.getView()) {
                        Ext.Function.defer(function () {
                            versionsGrid.getView().select(rec);
                        }, 10);
                    }
                }

            }, versionsGrid, {
                single: true
            });
        }
    }


});
