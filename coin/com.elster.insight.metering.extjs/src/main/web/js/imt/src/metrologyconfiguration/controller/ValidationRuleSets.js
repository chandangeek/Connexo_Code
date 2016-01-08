Ext.define('Imt.metrologyconfiguration.controller.ValidationRuleSets', {
    extend: 'Ext.app.Controller',

    requires: [
        'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
        'Cfg.store.ValidationRuleSets',
        'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
        'Cfg.store.ValidationRuleSetVersions',
        'Cfg.view.validation.RulePreview'
    ],

    views: [
        'Imt.metrologyconfiguration.view.validation.RulesOverview',
        'Imt.metrologyconfiguration.view.validation.AddRuleSets',
        'Cfg.view.validation.RulePreview'

    ],

    stores: [
        'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
        'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
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

    mcid: null,

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

    showValidationRuleSetsOverview: function (mcid) {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        model = Ext.ModelManager.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration');

        me.mcid = mcid;
        me.getStore('Imt.metrologyconfiguration.store.LinkedValidationRulesSet').getProxy().setUrl(mcid);
        var widget = Ext.widget('validation-rules-overview', {
        	router: router,
        	mcid: mcid
        });

        model.getProxy().setExtraParam('mcid', mcid);
        model.load(mcid, {
            success: function (metrologyConfiguration) {
                widget.metrologyConfiguration = metrologyConfiguration;
                me.getApplication().fireEvent('loadMetrologyConfiguration', metrologyConfiguration);
                widget.down('#stepsMenu #metrology-configuration-overview-link').setText(metrologyConfiguration.get('name'));
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    showAddValidationRuleSets: function (mcid) {
        var me = this,
        	router = me.getController('Uni.controller.history.Router'),
        	model = Ext.ModelManager.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            linkableValidationRuleSetsStore = me.getStore('Imt.metrologyconfiguration.store.LinkableValidationRulesSet'),
            ruleSetsStore;

        me.mcid = mcid;

        linkableValidationRuleSetsStore.getProxy().setUrl(mcid);
        linkableValidationRuleSetsStore.load();
        var widget = Ext.widget('validation-add-rulesets', {
        	router: router,
            mcid: mcid,
        });
//        if (me.getAddValidationRuleSetsGrid()) {
//            me.getAddValidationRuleSetsGrid().getStore().removeAll();
//        }
     
        model.getProxy().setExtraParam('mcid', mcid);
        model.load(mcid, {
            success: function (metrologyConfiguration) {
                me.getApplication().fireEvent('loadMetrologyConfiguration', metrologyConfiguration);
                widget.down('#stepsMenu #metrology-configuration-overview-link').setText(metrologyConfiguration.get('name'));
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    onValidationRuleSetsSelectionChange: function (selectionModel, selectedRecords) {
        var view = this.getValidationRuleSetsOverview();
        this.onAddRuleSetsChange(selectionModel, selectedRecords);
        if (selectedRecords.length > 0) {
            view.updateValidationRuleSet(selectedRecords[0]);
        }
    },

    onAddValidationRuleSetsSelectionChange: function (selectionModel, selectedRecords) {
        var view = this.getAddValidationRuleSets();
        if (selectedRecords.length > 0) {
            view.updateValidationRuleSet(selectedRecords[0]);
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
            url = '/api/ucr/metrologyconfigurations/' + me.mcid + '/assignedvalidationrulesets',
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
                location.href = '#/administration/metrologyconfiguration/' + me.mcid + '/associatedvalidationrulesets';

                var message = Uni.I18n.translatePlural(
                    'validation.ruleSetAdded',
                    selection.length,
                    'IMT',
                    'Successfully added {0} validation rule sets.',
                    'Successfully added {0} validation rule set.',
                    'Successfully added {0} validation rule sets.'
                );

                me.getApplication().fireEvent('acknowledge', message);
            },
            failure: function (response) {
                if (response.status === 400) {
                    var result = Ext.decode(response.responseText, true),
                        title = Uni.I18n.translate('general.failedToAdd', 'IMT', 'Failed to add'),
                        message = Uni.I18n.translate('validation.failedToAddRuleSets', 'IMT',
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
            title: Uni.I18n.translate('general.removeConfirmation', 'IMT', 'Remove \'{0}\'?', [record.data.name]),
            msg: Uni.I18n.translate('validation.metrologyconfig.remove.confirmation.msg', 'IMT',
                'This validation rule set type will no longer be available on the metrology configuration.'),
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
                url: '/api/ucr/metrologyconfigurations/' + scope.mcid
                + '/assignedvalidationrulesets/' + cfg.config.record.getId(),
                jsonData: Ext.merge(cfg.config.record.getData(), {parent: scope.getValidationRuleSetsOverview().metrologyConfiguration.getData()}),
                method: 'DELETE',
                success: function () {
                    scope.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.remove.success', 'IMT', 'Successfully removed.'));
                    store.load();
                },
                failure: function (response) {
                    if (response.status === 400) {
                        var record = cfg.config.record,
                            result = Ext.decode(response.responseText, true),
                            title = Uni.I18n.translate('general.failedToRemove', 'IMT', 'Failed to remove {0}', [record.data.name]),
                            message = Uni.I18n.translate('general.serverError', 'IMT', 'Server error');
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

    onAddRuleSetsChange: function (selectionModel, selectedRecords) {
        var me = this,
            versionsGrid;

        if (selectedRecords.length === 1) {
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
