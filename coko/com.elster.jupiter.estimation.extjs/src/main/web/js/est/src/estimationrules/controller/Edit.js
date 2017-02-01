/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.controller.Edit', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Uni.util.History'
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
        },
        {
            ref: 'editFormErrorMessage',
            selector: 'estimation-rule-edit estimation-rule-edit-form #form-errors'
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
                    ? router.queryParams.previousRoute
                    : router.getRoute('administration/estimationrulesets/estimationruleset/rules').buildUrl()
            }),
            waitBeforeLoadingTheRule = 2,
            // the following function loads the estimation rule into the form the 2nd time this method is called
            checkFlagAndEventuallyLoadTheRuleIntoTheForm = function () {
                waitBeforeLoadingTheRule--;
                if (!waitBeforeLoadingTheRule) {
                    if (rule) {
                        widget.down('estimation-rule-edit-form') && widget.down('estimation-rule-edit-form').loadRecord(rule);
                    }
                    widget.setLoading(false);
                }
            },
            ruleModel = me.getModel('Est.estimationrules.model.Rule'),
            rule;

        if (router.queryParams.previousRoute) {
            setTimeout(function () { // make redirect after executing this method
                Uni.util.History.setParsePath(false);
                Uni.util.History.suspendEventsForNextCall();
                window.location.replace(router.getRoute().buildUrl(router.arguments, null));
            }, 0);
        }

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        ruleModel.getProxy().setUrl(ruleSetId);
        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: function (record) {
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
            }
        });
        if (savedState) { // Coming back after having been to the "Add reading types" page
            rule = savedState;
            me.showReadingTypesGrid(widget, rule.readingTypesStore.data.length > 0)
            clipboard.clear('estimationRule');
            checkFlagAndEventuallyLoadTheRuleIntoTheForm();
        } else if (!ruleId) { // Adding a new estimation rule
            rule = Ext.create('Est.estimationrules.model.Rule');
            me.showReadingTypesGrid(widget, false);
            checkFlagAndEventuallyLoadTheRuleIntoTheForm();
        } else { // Editing an existing estimation rule
            ruleModel.load(ruleId, {
                success: function (record) {
                    rule = record;
                    me.showReadingTypesGrid(widget, true);
                    me.getApplication().fireEvent('loadEstimationRule', record);
                    if (widget.rendered) {
                        checkFlagAndEventuallyLoadTheRuleIntoTheForm();
                    }
                },
                failure: function () {
                    widget.setLoading(false);
                }
            });
        }
        me.getStore('Est.estimationrules.store.Estimators').load(checkFlagAndEventuallyLoadTheRuleIntoTheForm);

    },

    showReadingTypesGrid: function(widget, show) {
        if (show) {
            widget.down('#noReadingTypesForEstimationRuleLabel').hide();
            widget.down('#reading-types-grid').show();
        } else {
            widget.down('#noReadingTypesForEstimationRuleLabel').show();
            widget.down('#reading-types-grid').hide();
        }
    },

    saveRule: function () {
        var me = this,
            page = me.getPage(),
            form = me.getEditForm(),
            errorMsg = me.getEditFormErrorMessage();

        form.updateValid();
        if (form.isValid()) {
            form.updateRecord();
            page.setLoading(true);
            form.getRecord().save({
                backUrl: page.returnLink,
                callback: function (record, operation, success) {
                    var responseText = Ext.decode(operation.response.responseText, true);

                    page.setLoading(false);
                    if (success) {
                        me.getApplication().fireEvent('acknowledge', operation.action === 'create'
                            ? Uni.I18n.translate('estimationrules.addRuleSuccess', 'EST', 'Estimation rule successfully added')
                            : Uni.I18n.translate('estimationrules.saveRuleSuccess', 'EST', 'Estimation rule successfully saved'));
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
        } else {
            errorMsg.setText(errorMsg.defaultText);
            errorMsg.show();
        }
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