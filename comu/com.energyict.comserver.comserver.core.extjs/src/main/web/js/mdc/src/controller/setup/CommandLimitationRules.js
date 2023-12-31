/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.CommandLimitationRules', {
    extend: 'Ext.app.Controller',

    views: [
        'Uni.view.window.Confirmation',
        'Mdc.view.setup.commandrules.CommandLimitationRulesOverview',
        'Mdc.view.setup.commandrules.CommandRuleOverview',
        'Mdc.view.setup.commandrules.CommandRulePendingChangesOverview',
        'Mdc.view.setup.commandrules.CommandRuleAddEdit',
        'Mdc.view.setup.commandrules.AddCommandsToRuleView'
    ],
    models: [
        'Mdc.model.CommandLimitRule',
        'Mdc.model.Command'
    ],

    stores: [
        'Mdc.store.Clipboard',
        'Mdc.store.Commands',
        'Mdc.store.CommandsForRule',
        'Mdc.store.SelectedCommands',
        'Mdc.store.CommandLimitationRules'
    ],

    refs: [
        {ref: 'commandRulePreview', selector: 'commandRulesOverview #mdc-command-rule-preview'},
        {ref: 'commandRulePreviewForm', selector: 'commandRulesOverview #mdc-command-rule-preview-form'},
        {ref: 'commandRulePreviewMenu', selector: 'commandRulesOverview #mdc-command-rule-preview commandRuleActionMenu'},
        {ref: 'ruleEdit', selector: 'commandRuleAddEdit'},
        {ref: 'dayLimitNumberField', selector: 'commandRuleAddEdit #mdc-command-rule-addEdit-dayLimit-number'},
        {ref: 'weekLimitNumberField', selector: 'commandRuleAddEdit #mdc-command-rule-addEdit-weekLimit-number'},
        {ref: 'monthLimitNumberField', selector: 'commandRuleAddEdit #mdc-command-rule-addEdit-monthLimit-number'},
        {ref: 'addCommandsButton', selector: 'AddCommandsToRuleView #mdc-command-rule-add-commands-addButton'},
        {ref: 'addCommandsToRuleView', selector: 'AddCommandsToRuleView'},
        {ref: 'commandRulesPendingChangesOverview', selector: 'commandRulesPendingChangesOverview'},
        {ref: 'commandRuleOverview', selector: 'commandRuleOverview'},
        {ref: 'commandRulesOverview', selector: 'commandRulesOverview'}
    ],

    CLIPBOARD_KEY: 'addCommandLimitationRule',
    goToRuleOverview: false,
    commandRuleBeingEdited: null,
    commandsForRuleStore: null, // Store containing the commands to show on the Add/Edit page
    selectedCommandsStore: null, // Store containing the commands selected on the "Add commands page"
    comingFromAddingCommands: false,
    router: null,

    init: function () {
        var me = this;
        me.router = this.getController('Uni.controller.history.Router');
        me.control({
            'commandRulesOverview commandRulesGrid': {
                select: me.loadCommandRuleDetail
            },
            'commandRuleActionMenu': {
                click: me.onRuleActionMenuClicked
            },
            '#mdc-command-rule-addEdit-cancel-link': {
                click: me.onCancelAddEdit
            },
            '#mdc-command-rule-addEdit-add-button': {
                click: me.onAddEdit
            },
            '#mdc-command-rule-addEdit-dayLimit-radioGroup': {
                change: me.onDayLimitChange
            },
            '#mdc-command-rule-addEdit-weekLimit-radioGroup': {
                change: me.onWeekLimitChange
            },
            '#mdc-command-rule-addEdit-monthLimit-radioGroup': {
                change: me.onMonthLimitChange
            },
            'commandRuleAddEdit #mdc-command-rule-addEdit-addCommands-button': {
                click: me.onAddCommandsButtonClicked
            },
            '#mdc-command-rule-add-commands-cancelLink': {
                click: me.onCancelAddingCommands
            },
            '#mdc-command-rule-add-commands-addButton': {
                click: me.onAddChosenCommands
            },
            'commandRulesPendingChangesOverview #uni-pendingChangesPnl-approve': {
                click: me.onApprovePendingChanges
            },
            'commandRulesPendingChangesOverview #uni-pendingChangesPnl-reject': {
                click: me.onRejectPendingChanges
            }
        });
    },

    getCommandsForRuleStore: function () {
        if (Ext.isEmpty(this.commandsForRuleStore)) {
            this.commandsForRuleStore = Ext.getStore('Mdc.store.CommandsForRule');
        }
        return this.commandsForRuleStore;
    },

    getSelectedCommandsStore: function () {
        if (Ext.isEmpty(this.selectedCommandsStore)) {
            this.selectedCommandsStore = Ext.getStore('Mdc.store.SelectedCommands');
        }
        return this.selectedCommandsStore;
    },

    showRulesView: function () {
        var widget = Ext.widget('commandRulesOverview', {
            router: this.router
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        this.goToRuleOverview = false;
    },

    loadCommandRuleDetail: function (rowmodel, record, index) {
        var me = this,
            form = me.getCommandRulePreviewForm();
        form.setLoading();
        me.getCommandRulePreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        form.loadRecord(record);
        form.setLoading(false);
        me.getCommandRulePreviewMenu().record = record;
    },

    showCommandRuleOverview: function (commandRuleId) {
        var me = this,
            rulesModel = me.getModel('Mdc.model.CommandLimitRule');

        rulesModel.load(commandRuleId, {
            success: function (commandRule) {
                var widget = Ext.widget('commandRuleOverview', {
                    router: me.router,
                    commandRuleRecord: commandRule
                });
                me.getApplication().fireEvent('loadCommandRule', commandRule);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.goToRuleOverview = true;
                me.commandRuleBeingEdited = commandRule;
            }
        });
    },

    showCommandRulePendingChanges: function (commandRuleId) {
        var me = this,
            rulesModel = me.getModel('Mdc.model.CommandLimitRule');

        rulesModel.load(commandRuleId, {
            success: function (commandRule) {
                var widget = Ext.widget('commandRulesPendingChangesOverview', {
                    router: me.router,
                    commandRuleRecord: commandRule
                });
                me.getApplication().fireEvent('loadCommandRule', commandRule);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.goToRuleOverview = true;
                me.commandRuleBeingEdited = commandRule;
            }
        });
    },

    onRuleActionMenuClicked: function (menu, menuItem) {
        var me = this,
            record = menu.record;
        switch (menuItem.action) {
            case menu.ACTION_TOGGLE_ACTIVATION:
                if (record.get('active') === false) {
                    me.activateRule(record);
                } else {
                    me.deactivateRule(record);
                }
                break;
            case menu.ACTION_EDIT_RULE:
                me.router.getRoute('administration/commandrules/view/edit').forward({ruleId: record.get('id')});
                break;
            case menu.ACTION_VIEW_PENDING_CHANGES:
                me.router.getRoute('administration/commandrules/view/changes').forward({ruleId: record.get('id')});
                break;
            case menu.ACTION_REMOVE_RULE:
                me.removeRule(record);
                break;
        }
    },

    showAddEditCommandRule: function () {
        var me = this,
            widget = Ext.widget('commandRuleAddEdit'),
            ruleId = me.router.arguments['ruleId'];

        me.getApplication().fireEvent('changecontentevent', widget);
        me.getRuleEdit().down('#mdc-command-rule-addEdit-add-button').setText(
            Ext.isEmpty(ruleId)
                ? Uni.I18n.translate('general.add', 'MDC', 'Add')
                : Uni.I18n.translate('general.save', 'MDC', 'Save')
        );
        me.getRuleEdit().getCenterContainer().down().setTitle(
            Ext.isEmpty(ruleId)
                ? Uni.I18n.translate('commandLimitationRule.add', 'MDC', 'Add command limitation rule')
                : Uni.I18n.translate('commandLimitationRule.edit', 'MDC', 'Edit command limitation rule')
        );
        if (Ext.isEmpty(ruleId)) { // Adding a new rule
            me.commandRuleBeingEdited = null;
            me.goToRulesOverview = true;
            if (me.comingFromAddingCommands) {
                me.comingFromAddingCommands = false; // and don't touch the (previously updated) commandsForRuleStore
            } else {
                me.getCommandsForRuleStore().removeAll();
            }
            if (me.clipBoardHasData()) {
                me.setFormValues(widget); // Set back previously filled in name or limits
            }
        } else { // Editing a rule
            if (me.clipBoardHasData()) {
                me.setFormValues(widget);
            } else {
                me.loadModelToEditForm(ruleId, widget);
            }
        }
    },

    loadModelToEditForm: function (ruleId, widget) {
        var me = this,
            editView = me.getRuleEdit(),
            rulesModel = me.getModel('Mdc.model.CommandLimitRule'),
            infoPanel = editView.down('#mdc-command-rule-addEdit-infoPanel');

        widget.setLoading(true);
        rulesModel.load(ruleId, {
            success: function (commandRule) {
                me.commandRuleBeingEdited = commandRule;
                if (commandRule.get('active')) {
                    if (!Ext.isEmpty(commandRule.getDualControl())) { // Pending changes
                        editView.down('#mdc-command-rule-addEdit-infoMsg').setText(
                            Uni.I18n.translate('commandLimitationRule.pendingChanges.editingWillCancel', 'MDC',
                                'There are already pending changes on this command limitation rule. After saving, the current pending changes will be canceled.')
                            + '</br>' +
                            Uni.I18n.translate('commandLimitationRule.editingRequiresApproval', 'MDC',
                                'Editing the attributes below requires approval before taking effect.')
                        );
                    } else {
                        editView.down('#mdc-command-rule-addEdit-infoMsg').setText(
                            Uni.I18n.translate('commandLimitationRule.editingRequiresApproval', 'MDC',
                                'Editing the attributes below requires approval before taking effect.')
                        );
                    }
                    infoPanel.show();
                } else {
                    infoPanel.hide();
                }
                editView.getCenterContainer().down().setTitle(
                    Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", commandRule.get('name'))
                );
                me.getApplication().fireEvent('loadCommandRule', commandRule);

                if (me.comingFromAddingCommands) {
                    me.comingFromAddingCommands = false;
                } else {
                    me.getCommandsForRuleStore().suspendEvents();
                    me.getCommandsForRuleStore().removeAll();
                    commandRule.commands().each(function (command) {
                        me.getCommandsForRuleStore().add(command);
                    });
                    me.getCommandsForRuleStore().resumeEvents();
                    editView.updateCommandsGrid();
                }
                editView.loadCommandRule(commandRule);
                widget.setLoading(false);
            }
        });
    },

    onCancelAddEdit: function () {
        var me = this;
        window.location.href = me.goToRuleOverview && me.commandRuleBeingEdited
            ? me.router.getRoute('administration/commandrules/view').buildUrl({ruleId: me.commandRuleBeingEdited.get('id')})
            : me.router.getRoute('administration/commandrules').buildUrl();

        me.getCommandsForRuleStore().removeAll();
        if (me.commandRuleBeingEdited) {
            me.commandRuleBeingEdited.commands().each(function (command) {
                me.getCommandsForRuleStore().add(command);
            });
        }
        me.commandRuleBeingEdited = null;
        me.clearClipBoard();
        me.comingFromAddingCommands = false;
    },

    onAddEdit: function (button) {
        var me = this,
            page = me.getRuleEdit(),
            form = page.down('#mdc-command-rule-addEdit-rule-form'),
            formValues = form.getValues(),
            formErrorsPanel = form.down('#mdc-command-rule-addEdit-error'),
            dayLimitContainer = form.down('#mdc-command-rule-addEdit-dayLimit-radioGroupContainer'),
            weekLimitContainer = form.down('#mdc-command-rule-addEdit-weekLimit-radioGroupContainer'),
            monthLimitContainer = form.down('#mdc-command-rule-addEdit-monthLimit-radioGroupContainer'),
            commandsContainer = form.down('#mdc-command-rule-addEdit-commands-fieldContainer');

        if (form.isValid()) {
            var record = Ext.isEmpty(me.commandRuleBeingEdited) ? Ext.create('Mdc.model.CommandLimitRule') : me.commandRuleBeingEdited;
            formErrorsPanel.hide();
            form.getForm().clearInvalid();
            dayLimitContainer.unsetActiveError();
            weekLimitContainer.unsetActiveError();
            monthLimitContainer.unsetActiveError();
            commandsContainer.unsetActiveError();
            page.setLoading(true);
            record.beginEdit();
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }
            record.set('name', formValues.name);
            if (!formValues.noDayLimit && !Ext.isEmpty(formValues.dayLimit)) {
                record.set('dayLimit', formValues.dayLimit);
            } else {
                record.set('dayLimit', 0);
            }
            if (!formValues.noWeekLimit && !Ext.isEmpty(formValues.weekLimit)) {
                record.set('weekLimit', formValues.weekLimit);
            } else {
                record.set('weekLimit', 0);
            }
            if (!formValues.noMonthLimit && !Ext.isEmpty(formValues.monthLimit)) {
                record.set('monthLimit', formValues.monthLimit);
            } else {
                record.set('monthLimit', 0);
            }
            if (!Ext.isEmpty(me.commandRuleBeingEdited)) {
                record.commands().removeAll();
            }
            record.commands().add(me.getCommandsForRuleStore().data.items);
            record.endEdit();
            record.save({
                success: function () {
                    if (me.goToRuleOverview && me.commandRuleBeingEdited) {
                        me.router.getRoute('administration/commandrules/view').forward({ruleId: me.commandRuleBeingEdited.get('id')});
                    } else {
                        me.router.getRoute('administration/commandrules').forward();
                    }
                    me.getApplication().fireEvent('acknowledge',
                        me.commandRuleBeingEdited
                            ? (me.commandRuleBeingEdited.get('active')
                                ? Uni.I18n.translate('commandLimitationRule.active.save.success', 'MDC', 'Command limitation rule changes waiting for approval')
                                : Uni.I18n.translate('commandLimitationRule.inactive.save.success', 'MDC', 'Command limitation rule saved'))
                            : Uni.I18n.translate('commandLimitationRule.add.success', 'MDC', 'Command limitation rule added')
                    );
                    me.clearClipBoard();
                    me.comingFromAddingCommands = false;
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        var errorsToShow = [];
                        Ext.each(json.errors, function (item) {
                            switch (item.id) {
                                case 'dayLimit':
                                    dayLimitContainer.setActiveError(item.msg);
                                    break;
                                case 'weekLimit':
                                    weekLimitContainer.setActiveError(item.msg);
                                    break;
                                case 'monthLimit':
                                    monthLimitContainer.setActiveError(item.msg);
                                    break;
                                case 'commands':
                                    commandsContainer.setActiveError(item.msg);
                                    break;
                                default:
                                    errorsToShow.push(item);
                                    break;
                            }
                        });
                        Ext.suspendLayouts();
                        form.getForm().markInvalid(errorsToShow);
                        formErrorsPanel.show();
                        Ext.resumeLayouts(true);
                    }
                },
                callback: function () {
                    page.setLoading(false);
                }
            });
        } else {
            page.setLoading(false);
            formErrorsPanel.show();
        }
    },

    onDayLimitChange: function (radioGroup, newValue, oldValue) {
        this.getDayLimitNumberField().setDisabled(newValue.noDayLimit);
    },

    onWeekLimitChange: function (radioGroup, newValue, oldValue) {
        this.getWeekLimitNumberField().setDisabled(newValue.noWeekLimit);
    },

    onMonthLimitChange: function (radioGroup, newValue, oldValue) {
        this.getMonthLimitNumberField().setDisabled(newValue.noMonthLimit);
    },

    onAddCommandsButtonClicked: function () {
        var me = this;
        me.saveFormValues();
        if (Ext.isEmpty(me.commandRuleBeingEdited)) { // busy adding a new rule
            me.router.getRoute('administration/commandrules/add/commands').forward();
        } else { // busy editing a rule
            me.router.getRoute('administration/commandrules/view/edit/commands').forward({ruleId: me.commandRuleBeingEdited.get('id')});
        }
    },

    saveFormValues: function () {
        var me = this,
            widget = me.getRuleEdit(),
            form = widget.down('#mdc-command-rule-addEdit-rule-form'),
            formValues = form.getValues();
        me.addToClipBoard(formValues);
    },

    setFormValues: function (widget) {
        var me = this,
            storedObject = me.getFromClipBoard(),
            commandsForRuleGrid = widget.down('#mdc-command-rule-addEdit-commands-grid'),
            emptyCommandsLabel = widget.down('#mdc-command-rule-addEdit-noCommands-label');

        widget.down('#mdc-command-rule-addEdit-name-field').setValue(storedObject.name);
        if (storedObject.noDayLimit || Ext.isEmpty(storedObject.dayLimit)) {
            widget.down('#mdc-command-rule-addEdit-dayLimit-radioBtn-none').setValue(true);
        } else {
            widget.down('#mdc-command-rule-addEdit-dayLimit-radioBtn-value').setValue(true);
        }
        widget.down('#mdc-command-rule-addEdit-dayLimit-number').setValue(storedObject.noDayLimit || Ext.isEmpty(storedObject.dayLimit)
            ? 1
            : Number(storedObject.dayLimit));
        if (storedObject.noWeekLimit || Ext.isEmpty(storedObject.weekLimit)) {
            widget.down('#mdc-command-rule-addEdit-weekLimit-radioBtn-none').setValue(true);
        } else {
            widget.down('#mdc-command-rule-addEdit-weekLimit-radioBtn-value').setValue(true);
        }
        widget.down('#mdc-command-rule-addEdit-weekLimit-number').setValue(storedObject.noWeekLimit || Ext.isEmpty(storedObject.weekLimit)
            ? 1
            : Number(storedObject.weekLimit));
        if (storedObject.noMonthLimit || Ext.isEmpty(storedObject.monthLimit)) {
            widget.down('#mdc-command-rule-addEdit-monthLimit-radioBtn-none').setValue(true);
        } else {
            widget.down('#mdc-command-rule-addEdit-monthLimit-radioBtn-value').setValue(true);
        }
        widget.down('#mdc-command-rule-addEdit-monthLimit-number').setValue(storedObject.noMonthLimit || Ext.isEmpty(storedObject.monthLimit)
            ? 1
            : Number(storedObject.monthLimit));

        Ext.suspendLayouts();
        emptyCommandsLabel.setVisible(me.getCommandsForRuleStore().count() === 0);
        commandsForRuleGrid.setVisible(me.getCommandsForRuleStore().count() > 0);
        Ext.resumeLayouts(true);
    },

    showAddCommandsPage: function () {
        var me = this,
            commandsStore = me.getStore('Mdc.store.Commands');

        commandsStore.filter([
            {
                filterFn: function (command) {
                    return me.getCommandsForRuleStore().findExact('commandName', command.get('commandName')) === -1;
                }
            }
        ]);
        me.getApplication().fireEvent('changecontentevent', Ext.widget('AddCommandsToRuleView'));

        commandsStore.on('beforeload', function () {
            me.getAddCommandsButton().setDisabled(true);
        }, {single: true});
        commandsStore.load();
    },

    forwardToPreviousPage: function () {
        var splittedPath = this.router.currentRoute.split('/');

        splittedPath.pop();
        this.router.getRoute(splittedPath.join('/')).forward();
    },

    onAddChosenCommands: function () {
        var me = this;

        me.getSelectedCommandsStore().each(function (command) {
            me.getCommandsForRuleStore().add(command);
        });
        me.getSelectedCommandsStore().removeAll();
        me.comingFromAddingCommands = true;
        me.forwardToPreviousPage();
    },

    onCancelAddingCommands: function () {
        var me = this;
        me.getSelectedCommandsStore().removeAll();
        me.comingFromAddingCommands = true;
        me.forwardToPreviousPage();
    },

    activateRule: function (rule) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                green: true,
                confirmText: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                confirmation: function () {
                    me.doActivateRule(rule, this);
                }
            });

        confirmationWindow.show({
            msg: Uni.I18n.translate('commandLimitationRule.activate.msg', 'MDC', 'Activating a command limitation rule requires approval before taking effect.'),
            title: Uni.I18n.translate('general.activateX', 'MDC', "Activate '{0}'?", rule.get('name'))
        });
    },

    doActivateRule: function (rule, confirmationWindow) {
        var me = this, view;
        view = me.getCommandRulesOverview() ? me.getCommandRulesOverview() : me.getCommandRuleOverview();

        confirmationWindow.destroy();
        view.setLoading(true);
        rule.beginEdit();
        rule.set('active', true);
        rule.endEdit();
        rule.save({
            success: function () {
                if (me.goToRuleOverview && me.commandRuleBeingEdited) {
                    me.router.getRoute('administration/commandrules/view').forward({ruleId: me.commandRuleBeingEdited.get('id')});
                } else {
                    me.router.getRoute('administration/commandrules').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('commandLimitationRule.activate.success', 'MDC', 'Command limitation rule activation waiting for approval'));
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                }
                view.setLoading(false);
            }
        });

    },

    deactivateRule: function (rule) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
                confirmation: function () {
                    me.doDeactivateRule(rule, this);
                }
            });
        confirmationWindow.show({
            msg: Ext.isEmpty(rule.getDualControl())
                ? Uni.I18n.translate('commandLimitationRule.deactivate.msg', 'MDC', 'Deactivating a command limitation rule requires approval before taking effect.')
                : Uni.I18n.translate('commandLimitationRule.pendingChanges.deactivate.msg', 'MDC',
                    'There are already pending changes on this command limitation rule. After deactivation, the current pending changes will be canceled. Deactivating a command limitation rule requires approval before taking effect.'),
            title: Uni.I18n.translate('general.deactivateX', 'MDC', "Deactivate '{0}'?", rule.get('name'))
        });
    },

    doDeactivateRule: function (rule, confirmationWindow) {
        var me = this, view;
        view = me.getCommandRulesOverview() ? me.getCommandRulesOverview() : me.getCommandRuleOverview();

        confirmationWindow.destroy();
        view.setLoading(true);
        rule.beginEdit();
        rule.set('active', false);
        rule.endEdit();
        rule.save({
            success: function () {
                if (me.goToRuleOverview && me.commandRuleBeingEdited) {
                    me.router.getRoute('administration/commandrules/view').forward({ruleId: me.commandRuleBeingEdited.get('id')});
                } else {
                    me.router.getRoute('administration/commandrules').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('commandLimitationRule.deactivate.success', 'MDC', 'Command limitation rule deactivation waiting for approval'));
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                }
                view.setLoading(false);
            }
        });

    },

    removeRule: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                confirmation: function () {
                    me.doRemoveRule(record, this);
                }
            });
        confirmationWindow.show({
            msg: record.get('active')
                ? Uni.I18n.translate('commandLimitationRule.removeActive.msg', 'MDC',
                    'The creation of commands will no longer be limited by this command limitation rule. Removing an active command limitation rule requires approval before taking effect.')
                : Uni.I18n.translate('commandLimitationRule.removeInactive.msg', 'MDC', 'This command limitation rule will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", record.get('name'))
        });
    },

    doRemoveRule: function (record, confirmationWindow) {
        var me = this,
            activeRule = record.get('active'),
            acknowledgeMessage = activeRule
                ? Uni.I18n.translate('commandLimitationRule.active.remove.success', 'MDC', 'Command limitation rule removal waiting for approval')
                : Uni.I18n.translate('commandLimitationRule.inactive.remove.success', 'MDC', 'Command limitation rule removed');

        record.destroy({
            success: function () {
                if (activeRule && me.goToRuleOverview && me.commandRuleBeingEdited) {
                    me.router.getRoute('administration/commandrules/view').forward({ruleId: record.get('id')});
                } else {
                    me.router.getRoute('administration/commandrules').forward();
                }
                me.getApplication().fireEvent('acknowledge', acknowledgeMessage);
                confirmationWindow.destroy();
            }
        });
    },

    onApprovePendingChanges: function () {
        this.performApproveOrReject('accept');
    },

    onRejectPendingChanges: function () {
        this.performApproveOrReject('reject');
    },

    performApproveOrReject: function (action) {
        var me = this,
            commandRuleId = me.commandRuleBeingEdited.get('id'),
            data = me.commandRuleBeingEdited.getData(),
            view = me.getCommandRulesPendingChangesOverview();

        data.currentCounts = [];
        view.setLoading(true);
        Ext.Ajax.request({
            url: '/api/crr/commandrules/' + commandRuleId + '/' + action,
            method: 'POST',
            jsonData: data,
            success: function (response) {
                var jsonResponse = Ext.decode(response.responseText, true);
                if (action === 'reject') { // This was a reject
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('commandLimitationRule.reject.success', 'MDC', 'Pending changes rejected'));
                    me.router.getRoute('administration/commandrules/view').forward({ruleId: commandRuleId}); // navigate to the rule's detail page
                } else { // This was an approve
                    if (Ext.isEmpty(jsonResponse.dualControl)) { // No more pending changes
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('commandLimitationRule.approve.success', 'MDC', 'Pending changes approved'));
                        if (me.commandRuleBeingEdited.getDualControl().get('pendingChangesType') === 'REMOVAL') { // Removal completely approved, so
                            me.router.getRoute('administration/commandrules').forward(); // navigate to the rules overview
                        } else {
                            me.router.getRoute('administration/commandrules/view').forward({ruleId: commandRuleId}); // navigate to the rule's detail page
                        }
                    } else { // Still others to accept, so refresh the current page
                        me.router.getRoute(me.router.currentRoute).forward();
                    }
                }
            }
        });
    },

    addToClipBoard: function (itemToStore) {
        var clipBoard = this.getClipBoard();
        if (clipBoard) {
            clipBoard.set(this.CLIPBOARD_KEY, itemToStore);
        }
    },

    getFromClipBoard: function () {
        var clipBoard = this.getClipBoard();
        if (clipBoard) {
            return clipBoard.get(this.CLIPBOARD_KEY);
        }
        return undefined;
    },

    clipBoardHasData: function () {
        return !Ext.isEmpty(this.getFromClipBoard());
    },

    clearClipBoard: function () {
        if (this.clipBoardHasData()) {
            this.getClipBoard().clear(this.CLIPBOARD_KEY);
        }
    },

    getClipBoard: function () {
        return this.getStore('Mdc.store.Clipboard') || Ext.create('Mdc.store.Clipboard');
    }

});