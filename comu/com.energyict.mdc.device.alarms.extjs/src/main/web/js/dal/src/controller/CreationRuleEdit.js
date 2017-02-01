Ext.define('Dal.controller.CreationRuleEdit', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dal.store.CreationRules',
        'Dal.store.CreationRuleTemplates',
        'Dal.store.DueinTypes',
        'Dal.store.Clipboard',
        'Dal.store.CreationRuleActionPhases',
        'Dal.store.AlarmReasons'
    ],
    views: [
        'Dal.view.creationrules.Edit'
    ],

    models: [
        'Dal.model.CreationRuleAction',
        'Dal.model.CreationRule'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'alarms-creation-rules-edit'
        },
        {
            ref: 'ruleForm',
            selector: 'alarms-creation-rules-edit form'
        },
        {
            ref: 'actionsGrid',
            selector: 'alarms-creation-rules-edit alarms-creation-rules-actions-list'
        }
    ],

    init: function () {
        this.control({
            'alarms-creation-rules-edit button[action=save]': {
                click: this.ruleSave
            },
            'alarms-creation-rules-edit button[action=addAction]': {
                click: this.addAction
            }
        });
    },

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            clipboard = this.getStore('Dal.store.Clipboard'),
            savedData = clipboard.get('alarmsCreationRuleState'),
            widget = Ext.widget('alarms-creation-rules-edit', {
                router: router,
                isEdit: !!id
            }),
            issueTypesStore = me.getStore('Isu.store.IssueTypes'),
            dependencesCounter = 2,
            dependenciesOnLoad = function () {
                dependencesCounter--;
                if (!dependencesCounter) {
                    if (widget.rendered) {
                        widget.setLoading(false);
                        widget.down('form').loadRecord(rule);
                    }
                }
            },
            rule;

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        if (savedData) {
            rule = savedData;
            clipboard.clear('alarmsCreationRuleState');
            me.loadDependencies(dependenciesOnLoad);
        } else {
            if (id) {
                dependencesCounter++;
                me.getModel('Dal.model.CreationRule').load(id, {
                    success: function (record) {
                        rule = record;
                        me.getApplication().fireEvent('alarmCreationRuleEdit', rule);
                        dependenciesOnLoad();
                        me.loadDependencies(record, dependenciesOnLoad);
                    }
                });
            } else {
                rule = Ext.create('Dal.model.CreationRule');
            }
        }

        me.loadDependencies(dependenciesOnLoad);
    },

    loadDependencies: function (callback) {
        var me = this,
            templatesStore = me.getStore('Dal.store.CreationRuleTemplates'),
            alarmReasonsStore = me.getStore('Dal.store.AlarmReasons');

        templatesStore.getProxy().setExtraParam('issueType', 'datacollection'); //FixMe remove query parameter issueType;
        templatesStore.load(callback);
        alarmReasonsStore.load(callback);
    },

    ruleSave: function () {
        var me = this,
            form = me.getRuleForm(),
            basicForm = form.getForm(),
            formErrorsPanel = me.getRuleForm().down('[name=form-errors]'),
            page = me.getPage();

        basicForm.clearInvalid();
        formErrorsPanel.hide();
        page.setLoading();
        form.updateRecord();
        form.getRecord().save({
            backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/alarmcreationrules').buildUrl(),
            callback: function (record, operation, success) {
                var messageText,
                    json;

                page.setLoading(false);
                if (success) {
                    switch (operation.action) {
                        case 'create':
                            messageText = Uni.I18n.translate('administration.alarmCreationRules.createSuccess.msg', 'DAL', 'Alarm creation rule added');
                            break;
                        case 'update':
                            messageText = Uni.I18n.translate('administration.alarmCreationRules.updateSuccess.msg', 'DAL', 'Alarm creation rule updated');
                            break;
                    }
                    me.getApplication().fireEvent('acknowledge', messageText);
                    me.getController('Uni.controller.history.Router').getRoute('administration/alarmcreationrules').forward();
                } else {
                    json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        basicForm.markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            }
        });
    },

    addAction: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            form = me.getRuleForm();

        form.updateRecord();
        me.getStore('Dal.store.Clipboard').set('alarmsCreationRuleState', form.getRecord());

        router.getRoute(router.currentRoute + '/addaction').forward();
    }
});