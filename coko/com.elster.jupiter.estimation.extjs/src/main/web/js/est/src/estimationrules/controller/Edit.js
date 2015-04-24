Ext.define('Est.estimationrules.controller.Edit', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Est.estimationrules.view.Edit'
    ],

    stores: [
        'Est.estimationrules.store.Estimators',
        'Est.main.store.Clipboard'
    ],

    models: [
        'Est.estimationrulesets.model.EstimationRuleSet',
        'Est.estimationrules.model.Rule',
        'Est.main.model.ReadingType'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'estimation-rule-edit'
        },
        {
            ref: 'editForm',
            selector: 'estimation-rule-edit estimation-rule-edit-form'
        }
    ],

    init: function () {
        this.control({
            'estimation-rule-edit [action=editRule]': {
                click: this.saveRule
            },
            'estimation-rule-edit [action=addReadingTypes]': {
                click: this.addReadingTypes
            }
        });
    },

    showOverview: function (ruleSetId, ruleId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = me.getStore('Est.main.store.Clipboard'),
            savedState = clipboard.get('estimationRule'),
            widget = Ext.widget('estimation-rule-edit', {
                edit: !!ruleId,
                returnLink: router.queryParams.previousRoute
                    ? router.getRoute(router.queryParams.previousRoute).buildUrl()
                    : router.getRoute('administration/estimationrulesets/estimationruleset/rules').buildUrl()
            }),
            dependencies = 2,
            checkDependencies = function () {
                dependencies--;
                if (!dependencies) {
                    if (rule) {
                        widget.down('estimation-rule-edit-form').loadRecord(rule);
                    }
                    widget.setLoading(false);
                }
            },
            ruleModel = me.getModel('Est.estimationrules.model.Rule'),
            rule;

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        ruleModel.getProxy().setUrl(ruleSetId);
        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: function (record) {
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
            }
        });
        if (savedState) {
            rule = savedState;
            clipboard.clear('estimationRule');
            checkDependencies();
        } else if (!ruleId) {
            rule = Ext.create('Est.estimationrules.model.Rule');
            checkDependencies();
        } else {
            ruleModel.load(ruleId, {
                success: function (record) {
                    rule = record;
                    me.getApplication().fireEvent('loadEstimationRule', record);
                    if (widget.rendered) {
                        checkDependencies();
                    }
                },
                failure: function () {
                    widget.setLoading(false);
                }
            });
        }
        me.getStore('Est.estimationrules.store.Estimators').load(checkDependencies);

    },

    saveRule: function () {
        var me = this,
            page = me.getPage(),
            form = me.getEditForm();

        form.updateValid();
        form.updateRecord();
        page.setLoading(true);
        form.getRecord().save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                page.setLoading(false);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', operation.action === 'create'
                        ? Uni.I18n.translate('estimationrules.addRuleSuccess', 'EST', 'Estimation rule added')
                        : Uni.I18n.translate('estimationrules.saveRuleSuccess', 'EST', 'Estimation rule saved'));
                    if (page.rendered) {
                        window.location.href = page.returnLink;
                    }
                } else {
                    if (page.rendered && responseText && responseText.errors) {
                        form.updateValid(responseText.errors);
                    }
                }
            }
        });
    },

    addReadingTypes: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            form = me.getEditForm();

        form.updateRecord();
        me.getStore('Est.main.store.Clipboard').set('estimationRule', form.getRecord());
        router.getRoute(router.currentRoute + '/addreadingtypes').forward(router.arguments, router.queryParams);
    }
});