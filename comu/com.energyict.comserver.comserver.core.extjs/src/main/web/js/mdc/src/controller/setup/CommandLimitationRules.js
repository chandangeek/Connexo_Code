Ext.define('Mdc.controller.setup.CommandLimitationRules', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.commandrules.CommandLimitationRulesOverview',
        'Mdc.view.setup.commandrules.CommandRuleOverview',
        'Mdc.view.setup.commandrules.CommandRulePendingChangesOverview',
        'Mdc.view.setup.commandrules.CommandRuleAddEdit',
        'Mdc.view.setup.commandrules.AddCommandsToRuleView'
    ],
    models: [
        'Mdc.model.CommandLimitRule',
        'Mdc.model.CommandLimitationRule',
        'Mdc.model.Command'
    ],

    stores: [
        'Mdc.store.Clipboard',
        'Mdc.store.Commands',
        'Mdc.store.CommandsForRule'
    ],

    refs: [
        {ref: 'commandRulePreview', selector: 'commandRulesOverview #mdc-command-rule-preview'},
        {ref: 'commandRulePreviewForm', selector: 'commandRulesOverview #mdc-command-rule-preview-form'},
        {ref: 'commandRulePreviewMenu', selector: 'commandRulesOverview #mdc-command-rule-preview commandRuleActionMenu'},
        {ref: 'ruleEdit', selector: 'commandRuleAddEdit' },
        {ref: 'dayLimitNumberField', selector: 'commandRuleAddEdit #mdc-command-rule-addEdit-dayLimit-number' },
        {ref: 'weekLimitNumberField', selector: 'commandRuleAddEdit #mdc-command-rule-addEdit-weekLimit-number' },
        {ref: 'monthLimitNumberField', selector: 'commandRuleAddEdit #mdc-command-rule-addEdit-monthLimit-number' },
        {ref: 'addCommandsButton', selector: 'AddCommandsToRuleView #mdc-command-rule-add-commands-addButton' },
        {ref: 'addCommandsToRuleView', selector: 'AddCommandsToRuleView' }
    ],

    CLIPBOARD_KEY: 'addCommandLimitationRule',
    goToRulesOverview: false,
    commandRuleBeingEdited: null,
    commandsArray: null,
    ruleModel: null,
    commandsForRuleStore: null,

    init: function () {
        var me = this;
        me.control({
            'commandRulesOverview commandRulesGrid': {
                select: me.loadCommandRuleDetail
            },
            'commandRuleActionMenu' : {
                click: me.onMenuClicked
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
                click: me.forwardToPreviousPage
            },
            '#mdc-command-rule-add-commands-addButton': {
                click: me.onAddChosenCommands
            },
            'commandRuleAddEdit': {
                render: me.populateStore
            }
        });
    },

    showRulesView: function () {
        var widget = Ext.widget('commandRulesOverview', {
            router: this.getController('Uni.controller.history.Router')
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        this.goToRulesOverview = false;
    },

    loadCommandRuleDetail: function(rowmodel, record, index) {
        var me = this,
            form = me.getCommandRulePreviewForm();
        form.setLoading();
        me.getCommandRulePreview().setTitle( Ext.String.htmlEncode(record.get('name')) );
        form.loadRecord(record);
        form.setLoading(false);
        me.getCommandRulePreviewMenu().record = record;
    },

    showCommandRuleOverview: function(commandRuleId) {
        var me = this,
            rulesModel = me.getModel('Mdc.model.CommandLimitRule');

        rulesModel.load(commandRuleId, {
            success: function (commandRule) {
                var widget = Ext.widget('commandRuleOverview', {
                    router: me.getController('Uni.controller.history.Router'),
                    commandRuleRecord: commandRule
                });
                me.getApplication().fireEvent('loadCommandRule', commandRule);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.goToRulesOverview = true;
                me.commandRuleBeingEdited = commandRule;
            }
        });
    },

    showCommandRulePendingChanges: function() {
        var widget = Ext.widget('commandRulesPendingChangesOverview', {
            router: this.getController('Uni.controller.history.Router')
        });
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onMenuClicked: function(menu, menuItem) {
        switch(menuItem.action) {
            case 'toggleCommandRuleActivation':
                break;
            case 'editCommandRule':
                break;
            case 'removeCommandRule':
                break;
        }
    },

    showAddCommandRule: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('commandRuleAddEdit'),
            ruleId = router.arguments['ruleId'];

        me.getApplication().fireEvent('changecontentevent', widget);
        //me.getTaskEdit().down('toolbar').getComponent('createEditTask').setText(
        //    Ext.isEmpty(taskId)
        //        ? Uni.I18n.translate('general.add', 'MDC', 'Add')
        //        : Uni.I18n.translate('general.save', 'MDC', 'Save')
        //);
        //me.getTaskEdit().down('toolbar').getComponent('createEditTask').action = Ext.isEmpty(taskId) ? 'add' : 'save';
        me.getRuleEdit().getCenterContainer().down().setTitle(
            Ext.isEmpty(ruleId)
                ? Uni.I18n.translate('commandRules.create', 'MDC', 'Add command limitation rule')
                : Uni.I18n.translate('commandRules.edit', 'MDC', 'Edit command limitation rule')
        );
        if (ruleId) {
        //    me.loadModelToEditForm(taskId, widget);
        } else {
            me.commandRuleBeingEdited = null;
            me.goToRulesOverview = true;
        }

        if (me.getStore('Mdc.store.Clipboard').get(me.CLIPBOARD_KEY)) {
            me.setFormValues(widget);
        }
    },

    onCancelAddEdit: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        window.location.href = me.goToRulesOverview && me.commandRuleBeingEdited
            ? router.getRoute('administration/commandrules/view').buildUrl({ ruleId: me.commandRuleBeingEdited.get('id') })
            : router.getRoute('administration/commandrules').buildUrl();
        me.commandRuleBeingEdited = null;
        me.getStore('Mdc.store.Clipboard').clear(me.CLIPBOARD_KEY);
    },

    onAddEdit: function(button) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            page = me.getRuleEdit(),
            form = page.down('#mdc-command-rule-addEdit-rule-form'),
            formValues = form.getValues(),
            formErrorsPanel = form.down('#mdc-command-rule-addEdit-error');

        if (form.isValid() /*&& !emptyCommands*/) {
            var record = me.ruleModel || Ext.create('Mdc.model.CommandLimitationRule'),
                commandsStore = page.down('#mdc-command-rule-addEdit-commands-grid').getStore(),
                arrayCommands = [];

            record.beginEdit();
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            record.set('name', formValues.name);
            if (!formValues.noDayLimit && !Ext.isEmpty(formValues.dayLimit)) {
                record.set('dayLimit', formValues.dayLimit);
            }
            if (!formValues.noWeekLimit && !Ext.isEmpty(formValues.weekLimit)) {
                record.set('weekLimit', formValues.weekLimit);
            }
            if (!formValues.noMonthLimit && !Ext.isEmpty(formValues.monthLimit)) {
                record.set('monthLimit', formValues.monthLimit);
            }
            commandsStore.each(function (record) {
                arrayCommands.push(record.getData());
            });
            record.set('commands', arrayCommands);
            record.endEdit();
            mainView.setLoading();
            record.save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/commandrules').buildUrl(),
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('administration/commandrules').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('commandLimitationRule.add.success', 'MDC', 'Command limitation rule added.'));
                    me.getStore('Mdc.store.Clipboard').clear(me.CLIPBOARD_KEY);
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        Ext.suspendLayouts();
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                        Ext.resumeLayouts(true);
                    }
                },
                callback: function () {
                    mainView.setLoading(false);
                }
            });
        }
    },

    onDayLimitChange: function(radioGroup, newValue, oldValue) {
        this.getDayLimitNumberField().setDisabled( newValue.dayLimit );
    },

    onWeekLimitChange: function(radioGroup, newValue, oldValue) {
        this.getWeekLimitNumberField().setDisabled( newValue.weekLimit );
    },

    onMonthLimitChange: function(radioGroup, newValue, oldValue) {
        this.getMonthLimitNumberField().setDisabled( newValue.monthLimit );
    },

    onAddCommandsButtonClicked: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            page = me.getRuleEdit(),
            commandsStore = page.down('#mdc-command-rule-addEdit-commands-grid').getStore(),
            addCommandsRoute = router.currentRoute + '/commands';

        // Prepare the already assigned commands (for method showAddCommandsPage())
        me.commandsArray = [];
        commandsStore.each(function (record) {
            me.commandsArray.push(record.getData());
        });

        me.saveFormValues();
        router.getRoute(addCommandsRoute).forward();
    },

    saveFormValues: function () {
        var me = this,
            page = me.getRuleEdit(),
            form = page.down('#mdc-command-rule-addEdit-rule-form'),
            formValues = form.getValues(),
            commandsStore = page.down('#mdc-command-rule-addEdit-commands-grid').getStore(),
            arrayCommands = [];

        commandsStore.each(function(record) {
            arrayCommands.push(record.getData());
        });

        formValues.commands = arrayCommands;
        me.getStore('Mdc.store.Clipboard').set(me.CLIPBOARD_KEY, formValues);
    },

    setFormValues: function(widget) {
        var me = this,
            storedObject = me.getStore('Mdc.store.Clipboard').get(me.CLIPBOARD_KEY),
            commandsArray = storedObject.commands,
            commandsForRuleGrid = widget.down('#mdc-command-rule-addEdit-commands-grid'),
            emptyCommandsLabel = widget.down('#mdc-command-rule-addEdit-noCommands-label'),
            gridStore = commandsForRuleGrid.getStore();

        widget.down('#mdc-command-rule-addEdit-name-field').setValue(storedObject.name);
        if (storedObject.noDayLimit || Ext.isEmpty(storedObject.dayLimit)) {
            widget.down('#mdc-command-rule-addEdit-dayLimit-radioBtn-none').setValue(true);
        } else {
            widget.down('#mdc-command-rule-addEdit-dayLimit-radioBtn-value').setValue(true);
        }
        widget.down('#mdc-command-rule-addEdit-dayLimit-number').setValue( storedObject.noDayLimit || Ext.isEmpty(storedObject.dayLimit)
            ? 1
            : Number(storedObject.dayLimit) );
        if (storedObject.noWeekLimit || Ext.isEmpty(storedObject.weekLimit)) {
            widget.down('#mdc-command-rule-addEdit-weekLimit-radioBtn-none').setValue(true);
        } else {
            widget.down('#mdc-command-rule-addEdit-weekLimit-radioBtn-value').setValue(true);
        }
        widget.down('#mdc-command-rule-addEdit-weekLimit-number').setValue( storedObject.noWeekLimit || Ext.isEmpty(storedObject.weekLimit)
            ? 1
            : Number(storedObject.weekLimit) );
        if (storedObject.noMonthLimit || Ext.isEmpty(storedObject.monthLimit)) {
            widget.down('#mdc-command-rule-addEdit-monthLimit-radioBtn-none').setValue(true);
        } else {
            widget.down('#mdc-command-rule-addEdit-monthLimit-radioBtn-value').setValue(true);
        }
        widget.down('#mdc-command-rule-addEdit-monthLimit-number').setValue( storedObject.noMonthLimit || Ext.isEmpty(storedObject.monthLimit)
            ? 1
            : Number(storedObject.monthLimit) );

        Ext.suspendLayouts();
        gridStore.removeAll();

        if (me.commandsArray) {
            Ext.each(me.commandsArray, function(command) {
                gridStore.add(command);
            });
        } else if (!Ext.isEmpty(commandsArray)) {
            Ext.each(commandsArray, function(command) {
                gridStore.add(command);
            });
        }

        if (gridStore.count() > 0) {
            emptyCommandsLabel.hide();
            commandsForRuleGrid.show();
        }
        Ext.resumeLayouts(true);
    },

    showAddCommandsPage: function() {
        var me = this,
            commandsStore = me.getStore('Mdc.store.Commands');

        if (!me.commandsArray) {
            me.forwardToPreviousPage();
            return;
        }

        me.getApplication().fireEvent('changecontentevent', Ext.widget('AddCommandsToRuleView', {
            defaultFilters: {
                selectedcommands: _.map(me.commandsArray, function (command) {
                    return command.commandName;
                })
            }
        }));

        commandsStore.on('beforeload', function () {
            me.getAddCommandsButton().setDisabled(true);
        }, {single: true});
        commandsStore.load();
    },

    forwardToPreviousPage: function() {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();
        router.getRoute(splittedPath.join('/')).forward();
    },

    onAddChosenCommands: function() {
        var me = this,
            widget = this.getAddCommandsToRuleView(),
            grid = widget.down('#mdc-command-rule-add-commands-grid'),
            selection = grid.getSelectedItems();

        if (selection.length > 0) {
            Ext.each(selection, function(record) {
                me.commandsArray.push(record);
            });
        }
        me.forwardToPreviousPage();
    },

    populateStore: function() {
        if (Ext.isEmpty(this.commandsForRuleStore)) {
            this.commandsForRuleStore = Ext.getStore('Mdc.store.CommandsForRule');
        }
    }

});