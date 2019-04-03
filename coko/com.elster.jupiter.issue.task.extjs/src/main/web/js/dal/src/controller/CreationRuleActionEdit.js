/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.CreationRuleActionEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Itk.store.CreationRuleActions',
        'Itk.store.CreationRuleActionPhases',
        'Itk.store.Clipboard',
        'Itk.store.Users'
    ],

    views: [
        'Itk.view.creationrules.EditAction'
    ],

    refs: [
        {
            ref: 'actionForm',
            selector: 'issues-creation-rules-edit-action form'
        }
    ],

    models: [
        'Itk.model.CreationRuleAction',
        'Itk.model.CreationRule'
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
            clipboard = me.getStore('Itk.store.Clipboard'),
            widget = Ext.widget('issues-creation-rules-edit-action', {
                isEdit: false,
                router: router,
                returnLink: router.getRoute(router.currentRoute.replace('/addaction', '')).buildUrl()
            }),
            dependenciesCounter = 1,
            dependenciesOnLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    if (widget.rendered) {
                        widget.down('form').loadRecord(Ext.create('Itk.model.CreationRuleAction'));
                        me.getStore('Itk.store.CreationRuleActionPhases').load(function (records) {
                            if (widget.rendered) {
                                widget.down('issues-creation-rules-edit-action-form').addPhases(records);
                                widget.setLoading(false);
                            }
                        });
                    }
                }
            };

        Ext.util.History.on('change', this.checkRoute, me);

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        if (!clipboard.get('issuesCreationRuleState')) {
            if (id) {
                me.getModel('Itk.model.CreationRule').load(id, {
                    success: function (record) {
                        clipboard.set('issuesCreationRuleState', record);
                        me.getApplication().fireEvent('issueCreationRuleEdit', record);
                        dependenciesOnLoad();
                    }
                });
            } else {
                var rule = Ext.create('Itk.model.CreationRule');
                clipboard.set('issuesCreationRuleState', rule);
                dependenciesOnLoad();
            }
        } else {
            dependenciesOnLoad();
        }
    },

    checkRoute: function (token) {
        var me = this,
            currentRoute = me.getController('Uni.controller.history.Router').currentRoute,
            allowableRoutes = [
                'administration/issuecreationrules/add',
                'administration/issuecreationrules/edit'
            ];

        Ext.util.History.un('change', me.checkRoute, me);

        if (!Ext.Array.findBy(allowableRoutes, function (item) {return item === currentRoute})) {
            me.getStore('Itk.store.Clipboard').clear('issuesCreationRuleState');
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
        record.getProxy().extraParams = ({reason_name: me.getStore('Itk.store.Clipboard').get('issuesCreationRuleState').get('reason_id')});
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
                    me.getStore('Itk.store.Clipboard').get('issuesCreationRuleState').actions().add(record);
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