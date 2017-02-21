/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.controller.CreationRuleActionEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dal.store.CreationRuleActions',
        'Dal.store.CreationRuleActionPhases',
        'Dal.store.Clipboard',
        'Dal.store.Users'
    ],

    views: [
        'Dal.view.creationrules.EditAction'
    ],

    refs: [
        {
            ref: 'actionForm',
            selector: 'alarms-creation-rules-edit-action form'
        }
    ],

    models: [
        'Dal.model.CreationRuleAction',
        'Dal.model.CreationRule'
    ],

    init: function () {
        this.control({
            'alarms-creation-rules-edit-action button[action=saveRuleAction]': {
                click: this.saveAction
            }
        });
    },

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = me.getStore('Dal.store.Clipboard'),
            widget = Ext.widget('alarms-creation-rules-edit-action', {
                isEdit: false,
                router: router,
                returnLink: router.getRoute(router.currentRoute.replace('/addaction', '')).buildUrl()
            }),
            dependenciesCounter = 1,
            dependenciesOnLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    if (widget.rendered) {
                        widget.down('form').loadRecord(Ext.create('Dal.model.CreationRuleAction'));
                        me.getStore('Dal.store.CreationRuleActionPhases').load(function (records) {
                            if (widget.rendered) {
                                widget.down('alarms-creation-rules-edit-action-form').addPhases(records);
                                widget.setLoading(false);
                            }
                        });
                    }
                }
            };

        Ext.util.History.on('change', this.checkRoute, me);

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        if (!clipboard.get('alarmsCreationRuleState')) {
            if (id) {
                me.getModel('Dal.model.CreationRule').load(id, {
                    success: function (record) {
                        clipboard.set('alarmsCreationRuleState', record);
                        me.getApplication().fireEvent('alarmCreationRuleEdit', record);
                        dependenciesOnLoad();
                    }
                });
            } else {
                var rule = Ext.create('Dal.model.CreationRule');
                clipboard.set('alarmsCreationRuleState', rule);
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
                'administration/alarmcreationrules/add',
                'administration/alarmcreationrules/edit'
            ];

        Ext.util.History.un('change', me.checkRoute, me);

        if (!Ext.Array.findBy(allowableRoutes, function (item) {return item === currentRoute})) {
            me.getStore('Dal.store.Clipboard').clear('alarmsCreationRuleState');
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
        record.save({
            callback: function (validatedRecord, operation, success) {
                var json;

                form.setLoading(false);
                if (success) {
                    me.getStore('Dal.store.Clipboard').get('alarmsCreationRuleState').actions().add(record);
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