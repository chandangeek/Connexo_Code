/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.CreationRuleActionEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.CreationRuleActions',
        'Isu.store.CreationRuleActionPhases',
        'Isu.store.Clipboard',
        'Isu.store.Users',
        'Isu.store.IssueTypes',
        'Isu.store.CreationRuleIssueTypes'
    ],

    views: [
        'Isu.view.creationrules.EditAction'
    ],

    refs: [
        {
            ref: 'actionForm',
            selector: 'issues-creation-rules-edit-action form'
        }
    ],

    models: [
        'Isu.model.CreationRuleAction',
        'Isu.model.CreationRule'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit-action button[action=saveRuleAction]': {
                click: this.saveAction
            }
        });
    },

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = me.getStore('Isu.store.Clipboard'),
            record = clipboard.get('creationRuleActionState');

        var widget = Ext.widget('issues-creation-rules-edit-action', {
                isEdit: clipboard.get('creationRuleActionEdit') || false,
                router: router,
                returnLink: router.getRoute(router.currentRoute.replace('/addaction', '')).buildUrl()
            }),
            dependenciesCounter = 1,
            dependenciesOnLoad = function () {
                if (dependenciesCounter) {
                    dependenciesCounter--;
                    if (widget.rendered) {
                        if (!record) {
                            widget.down('form').loadRecord(Ext.create('Isu.model.CreationRuleAction'));
                            me.getStore('Isu.store.CreationRuleActionPhases').load(function (records) {
                                if (widget.rendered) {
                                    widget.down('issues-creation-rules-edit-action-form').addPhases(records);
                                    widget.setLoading(false);
                                }
                            });
                        } else {
                            me.getStore('Isu.store.CreationRuleActionPhases').load(function (records) {
                                if (widget.rendered) {
                                    widget.down('issues-creation-rules-edit-action-form').addPhases(records);
                                    widget.down('form').loadRecord(record);
                                    widget.setLoading(false);
                                }

                            });
                        }
                    }
                }
            };
        Ext.util.History.on('change', this.checkRoute, me);
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        if (!clipboard.get('issuesCreationRuleState')) {
            if (id) {
                me.getModel('Isu.model.CreationRule').load(id, {
                    success: function (record) {
                        clipboard.set('issuesCreationRuleState', record);
                        me.getApplication().fireEvent('issueCreationRuleEdit', record);
                        dependenciesOnLoad();
                    }
                });
            } else {
                me.getStore('Isu.store.CreationRuleIssueTypes').load(function (records) {
                    var rule = Ext.create('Isu.model.CreationRule');

                    rule.setIssueType(records[0]);
                    clipboard.set('issuesCreationRuleState', rule);
                    dependenciesOnLoad();
                });
            }
        } else {
            dependenciesOnLoad();
        }
    },

    checkRoute: function (token) {
        var me = this,
            currentRoute = me.getController('Uni.controller.history.Router').currentRoute,
            allowableRoutes = [
                'administration/creationrules/add',
                'administration/creationrules/edit'
            ];

        Ext.util.History.un('change', me.checkRoute, me);

        if (!Ext.Array.findBy(allowableRoutes, function (item) {
            return (currentRoute === item || currentRoute.startsWith(item + '/'))
        })) {
            me.getStore('Isu.store.Clipboard').clear('issuesCreationRuleState');
        }
    },

    saveAction: function () {
        var me = this,
            form = me.getActionForm(),
            baseForm = form.getForm(),
            formErrorsPanel = form.down('uni-form-error-message'),
            record;

        baseForm.clearInvalid();
        formErrorsPanel.hide();
        form.setLoading();
        form.updateRecord();
        record = form.getRecord();
        record.getProxy().url = '/api/isu/creationrules/validateaction';
        var clipboard = Ext.getStore('Isu.store.Clipboard');
        var issuesCreationRuleState = clipboard && clipboard.get('issuesCreationRuleState');
        var reason_id = issuesCreationRuleState && issuesCreationRuleState.get('reason_id');
        record.getProxy().extraParams = reason_id ? {"reason_id": reason_id} : {};
        record.save({
            callback: function (validatedRecord, operation, success) {
                var json;

                form.setLoading(false);
                if (success) {
                    // set description value
                    var json = Ext.decode(operation.response.responseText, true);
                    record.beginEdit();
                    record.set('description', json.description);
                    record.endEdit();
                    me.getStore('Isu.store.Clipboard').get('issuesCreationRuleState').actions().add(record);
                    window.location.href = form.returnLink;
                } else {
                    json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        baseForm.markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            }
        });
    }
});