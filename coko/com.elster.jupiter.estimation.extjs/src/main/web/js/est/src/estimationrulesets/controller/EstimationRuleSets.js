Ext.define('Est.estimationrulesets.controller.EstimationRuleSets', {
    extend: 'Ext.app.Controller',

    views: [
        'Est.estimationrulesets.view.RuleSetsSetup',
        'Est.estimationrulesets.view.RuleSetDetails',
        'Est.estimationrulesets.view.RuleSetEdit'
    ],

    requires: [
        'Est.estimationrulesets.model.EstimationRuleSet',
        'Uni.view.window.Confirmation'
    ],

    stores: [
        'Est.estimationrulesets.store.EstimationRuleSetsStore',
        'Est.estimationrules.store.Rules'
    ],

    refs: [
        {ref: 'ruleSetEditForm', selector: '#rule-set-edit #rule-set-edit-form'},
        {ref: 'ruleSetGrid', selector: '#rule-sets-setup #rule-sets-grid'},
        {ref: 'ruleSetRulesGrid', selector: '#rule-sets-setup #rule-sets-rule-grid'},
        {ref: 'ruleSetRulePreview', selector: '#rule-sets-setup #rule-sets-rule-preview'}

    ],

    preventSelection: false,

    init: function () {
        this.control({
            '#rule-set-action-menu': {
                click: this.chooseAction
            },
            '#rule-set-edit-form #save-button': {
                click: this.saveRuleSet
            },
            '#rule-set-edit-form #cancel-button': {
                click: this.navigatePrevious
            },
            '#add-estimation-rule-set-button': {
                click: this.navigateAdd
            },
            '#rule-sets-setup #rule-sets-grid': {
                select: this.selectRuleSet
            },
            '#rule-sets-setup #rule-sets-rule-grid': {
                select: this.selectRule
            },
            '#rule-set-rule-action-menu': {
                click: this.chooseRuleAction
            },
            '#rule-sets-add-rule-button': {
                click: this.navigateAddRule
            }
        })
    },

    chooseAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'edit' :
                me.navigateEdit(menu.record);
                break;
            case 'remove' :
                me.removeAction(menu.record);
                break;
        }
    },

    chooseRuleAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'edit' :
                me.navigateEditRule(menu.record);
                break;
            case 'remove' :
                me.removeRuleAction(menu.record);
                break;
            case 'toggleActivation' :
                me.toggleActivation(menu.record);
                break;
        }
    },

    refreshRuleSetRecord: function (ruleSetId) {
        var me = this,
            rulesetGrid = me.getRuleSetGrid(),
            ruleSetStore = rulesetGrid.getStore();
        rulesetGrid.setLoading(true);
        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            callback: function (record, operation, success) {
                if (success) {
                    var ruleSet = ruleSetStore.getById(ruleSetId);
                    ruleSet.beginEdit();
                    ruleSet.set(record.getData());
                    ruleSet.endEdit();
                    ruleSetStore.commitChanges();
                }
                rulesetGrid.setLoading(false);
            }
        });
    },


    toggleActivation: function (record) {
        var me = this,
            isActive = record.get('active'),
            ruleSetId = record.get('ruleSet').id;

        record.set('active', !isActive);
        record.getProxy().setUrl(ruleSetId);
        record.save({
            callback: function (record, operation, success) {
                if (success) {
                    record.commit();
                    me.refreshRuleSetRecord(record.get('ruleSet').id);
                    me.getApplication().fireEvent('acknowledge', isActive
                        ? Uni.I18n.translate('estimationrules.deactivateRuleSuccess', 'EST', 'Estimation rule deactivated')
                        : Uni.I18n.translate('estimationrules.activateRuleSuccess', 'EST', 'Estimation rule activated'));
                } else {
                    record.set('active', isActive);
                }
            }
        });
    },

    selectRuleSet: function (grid, record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            rulesStore = me.getStore('Est.estimationrules.store.Rules');
        me.getRuleSetRulesGrid().setTitle(record.get('name'));
        rulesStore.getProxy().setUrl(record.getId());
        rulesStore.load();
        router.arguments.ruleSetId = record.getId();
    },

    selectRule: function (grid, record) {
        var me =this,
            previewForm = me.getRuleSetRulePreview();
        previewForm.updateForm(record);
        previewForm.down('menu').record = record;
    },

    showEstimationRuleSets: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            rulesStore = me.getStore('Est.estimationrules.store.Rules'),
            widget = Ext.widget('rule-sets-setup', {router: router, rulesStore: rulesStore});
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showEstimationRuleSetAdd: function () {
        var me = this,
            widget = Ext.widget('rule-set-edit'),
            model = new Est.estimationrulesets.model.EstimationRuleSet;
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.down('#rule-set-edit-form').setTitle(Uni.I18n.translate('estimationrulesets.add.title', 'EST', 'Add estimation rule set'));
        widget.down('#rule-set-edit-form').loadRecord(model)
    },

    showEstimationRuleSetEdit: function (id) {
        var me = this,
            widget = Ext.widget('rule-set-edit'),
            model = me.getModel('Est.estimationrulesets.model.EstimationRuleSet');
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        model.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
                widget.down('#rule-set-edit-form').loadRecord(record);
                widget.down('#rule-set-edit-form').setTitle(Uni.I18n.translate('estimationrulesets.edit.title', 'EST', 'Edit \'{name}\'').replace('{name}', record.get('name')));
                widget.down('#rule-set-edit-form #save-button').setText(Uni.I18n.translate('general.save', 'EST', 'Save'));
                widget.setLoading(false);
            },
            failure: function () {
                widget.setLoading(false)
            }
        });
    },

    showEstimationRuleSetDetails: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('rule-set-details', {router: router}),
            model = me.getModel('Est.estimationrulesets.model.EstimationRuleSet');
        widget.setLoading(true);
        model.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
                if (widget.rendered) {
                    Ext.suspendLayouts();
                    widget.down('#rule-set-form').loadRecord(record);
                    widget.down('estimation-rule-set-side-menu #estimation-rule-set-link').setText(record.get('name'));
                    var actionBtn = widget.down('#action-button');
                    if (actionBtn) {
                        actionBtn.menu.record = record;
                    }
                    Ext.resumeLayouts(true);
                }
                widget.setLoading(false);
            }
        });
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showErrorPanel: function (value) {
        var form = this.getRuleSetEditForm(),
            errorPanel = form.down('#form-errors');
        errorPanel.setVisible(value);
    },

    saveRuleSet: function (btn) {
        var me = this,
            form = btn.up('#rule-set-edit-form'),
            action;
        form.updateRecord();
        var record = form.getRecord();
        record.getId() ? action = 'update' : action = 'create';
        record.save({
            action: action,
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    me.showErrorPanel(true);
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            form.getForm().markInvalid(json.errors);
                        }
                    }
                }
            },
            success: function (record) {
                me.navigatePrevious();
                var name = record.get('name'),
                    msg = action == 'create' ?
                        Uni.I18n.translate('estimationrulesets.add.successMsg', 'EST', 'Estimation rule set added') :
                        Uni.I18n.translate('estimationrulesets.edit.successMsg', 'EST', 'Estimation rule set edited');
                me.getApplication().fireEvent('acknowledge', msg);
            }
        })
    },

    removeAction: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('estimationrulesets.remove.message', 'EST', 'This estimation rele set and estimation rules will no longer available'),
            title: Uni.I18n.translate('estimationrulesets.remove.title', 'EST', 'Remove ') + ' \'' + record.get('name') + '\'?',
            config: {
                record: record,
                me: me
            },
            fn: function (action, un, initConfig) {
                if (action == 'confirm') {
                    me.makeRemove(initConfig.config.record)
                }
            }
        });
    },

    removeRuleAction: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.remove', 'EST', 'Remove') + ' \'' + record.get('name') + '\'?',
            msg: Uni.I18n.translate('estimationrules.deleteConfirmation.msg', 'EST', 'This estimation rule will no longer be available.'),
            config: {
                record: record,
                me: me
            },
            fn: function (action, un, initConfig) {
                if (action == 'confirm') {
                    me.makeRemoveRule(initConfig.config.record)
                }
            }
        });
    },

    makeRemove: function (record) {
        var me = this,
            name = record.get('name');
        record.destroy({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationrulesets.remove.successMsg', 'EST', 'Estimation rule set removed'));
                me.navigatePrevious()
            }
        });
    },

    makeRemoveRule: function (record) {
        var me = this,
            ruleGrid = me.getRuleSetRulesGrid(),
            rulesetId = record.get('ruleSet').id;
        record.getProxy().setUrl(rulesetId);
        ruleGrid.setLoading(true);
        record.destroy({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationrules.estimationRuleRemoved', 'EST', 'Estimation rule removed'));
                me.refreshRuleSetRecord(rulesetId);
                ruleGrid.getStore().load({
                    callback: function () {
                        ruleGrid.setLoading(false);
                    }
                })
            },
            failure: function () {
                ruleGrid.setLoading(false);
            }
        });
    },

    navigatePrevious: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            previousRoute = router.getQueryStringValues().previousRoute;

        if (previousRoute) {
            location.href = previousRoute;
        } else {
            router.getRoute('administration/estimationrulesets').forward()
        }
    },

    navigateEditRule: function (record) {
        var me = this,
            ruleId = record.getId(),
            ruleSetId = record.get('ruleSet').id,
            router = me.getController('Uni.controller.history.Router'),
            previousRoute = router.getRoute().buildUrl();

        router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule/edit').forward({ruleSetId: ruleSetId, ruleId: ruleId}, {previousRoute: previousRoute})
    },

    navigateEdit: function (record) {
        var me = this,
            id = record.getId(),
            router = me.getController('Uni.controller.history.Router'),
            previousRoute = router.getRoute().buildUrl();

        router.getRoute('administration/estimationrulesets/estimationruleset/edit').forward({ruleSetId: id}, {previousRoute: previousRoute})
    },

    navigateAddRule: function () {
        var me = this,
            ruleSetGrid = me.getRuleSetGrid(),
            ruleSetId = ruleSetGrid.getSelectionModel().getSelection()[0].getId(),
            router = me.getController('Uni.controller.history.Router'),
            previousRoute = router.getRoute().buildUrl();
        router.getRoute('administration/estimationrulesets/estimationruleset/rules/add').forward({ruleSetId: ruleSetId}, {previousRoute: previousRoute})
    },

    navigateAdd: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            previousRoute = router.getRoute().buildUrl();
        router.getRoute('administration/estimationrulesets/addruleset').forward({}, {previousRoute: previousRoute})
    }

});

