Ext.define('Mdc.controller.setup.Comtasks', {
    extend: 'Ext.app.Controller',
    stores: [
        'Mdc.store.CommunicationTasks',
        'Mdc.store.CommunicationTasksCategories',
        'Mdc.store.CommunicationTasksActions',
        'Mdc.store.TimeUnits',
        'Mdc.store.Logbook',
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.RegisterGroups'
    ],
    views: [
        'Mdc.view.setup.comtasks.ComtaskSetup',
        'Mdc.view.setup.comtasks.ComtaskGrid',
        'Mdc.view.setup.comtasks.ComtaskPreview',
        'Mdc.view.setup.comtasks.ComtaskCreateEditForm',
        'Mdc.view.setup.comtasks.ComtaskCommand',
        'Mdc.view.setup.comtasks.parameters.Logbooks',
        'Mdc.view.setup.comtasks.parameters.Profiles',
        'Mdc.view.setup.comtasks.parameters.Registers'
    ],
    refs: [
        { ref: 'tasksView', selector: 'comtaskSetup' },
        { ref: 'itemPanel', selector: 'comtaskPreview' },
        { ref: 'tasksGrid', selector: 'comtaskGrid' },
        { ref: 'nameField', selector: '#comtaskName' },
        { ref: 'commandsField', selector: '#comtaskCommands' },
        { ref: 'gridPagingToolbarTop', selector: 'comtaskGrid pagingtoolbartop' },
        { ref: 'taskEdit', selector: 'comtaskCreateEdit' },
        { ref: 'commandNames', selector: 'comtaskCreateEdit [name=commandnames]' },
        { ref: 'commandFields', selector: 'comtaskCreateEdit [name=commandfields]' }
    ],
    init: function () {
        this.control({
            'comtaskSetup comtaskGrid': {
                select: this.showTaskDetails
            },
            'comtaskSetup comtaskGrid uni-actioncolumn': {
                menuclick: this.chooseCommunicationTasksAction
            },
            'comtaskActionMenu': {
                click: this.chooseCommunicationTasksAction
            },
            'comtaskCreateEdit comtaskCommandCategoryCombo': {
                change: this.addActionCombo
            },
            'comtaskCreateEdit comtaskCommandCategoryActionCombo': {
                change: this.addCommandParameters,
                afterrender: this.disableBtn
            },
            'comtaskCreateEdit comtaskCommand button[action=addCommand]': {
                click: this.addCommandToModel
            },
            'comtaskCreateEdit comtaskCommand button[action=saveCommand]': {
                click: this.addCommandToModel
            },
            'comtaskCreateEdit comtaskCommand button[action=cancelEditCommand]': {
                click: this.cancelEdit
            },
            'comtaskCreateEdit comtaskCommand button[action=removeCommand]': {
                click: this.removeEdit
            },
            'comtaskCreateEdit #createEditTask': {
                click: this.createEdit
            },
            'communication-tasks-profilescombo': {
                afterrender: this.setTooltip
            }
        });
        this.store = this.getStore('Mdc.store.CommunicationTasks');
        this.commands = [];
    },

    showCommunicationTasksView: function () {
        var widget = Ext.widget('comtaskSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    chooseCommunicationTasksAction: function (menu, item) {
        switch (item.action) {
            case 'edit':
                window.location.href = '#/administration/communicationtasks/' + this.taskId;
                break;
            case 'delete':
                this.deleteTask(this.taskId);
                break;
        }
    },

    showTaskDetails: function (grid, record) {
        var itemPanel = this.getItemPanel(),
            nameField = this.getNameField(),
            commandsField = this.getCommandsField();

        if (this.displayedItemId != record.id) {
            grid.view.clearHighlight();
        }

        this.displayedItemId = record.id;
        this.taskId = record.data.id;

        Ext.Ajax.request({
            url: '/api/cts/comtasks/' + record.data.id,
            success: function (response) {
                var rec = Ext.decode(response.responseText),
                    str = '';
                itemPanel.setTitle(rec.name);
                nameField.setValue(rec.name);
                Ext.Array.each(rec.commands, function (command) {
                    str += command.action.charAt(0).toUpperCase() + command.action.slice(1) + ' ' + command.category.charAt(0).toUpperCase() + command.category.slice(1) + '<br/>';
                });
                commandsField.setValue(str);
            }
        });
    },

    deleteTask: function (id) {
        var self = this,
            tasksView = self.getTasksView(),
            grid = tasksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: self.getTasksView()
            });

        Ext.create('Uni.view.window.Confirmation').show({
            title: Ext.String.format(Uni.I18n.translate('comtask.remove.confirmation.title', 'MDC', 'Remove {0}?'), record.data.name),
            msg: Uni.I18n.translate('comtask.remove.confirmation.msg', 'MDC', 'This communication task will no longer be available'),
            fn: function (state) {
                if (state === 'confirm') {
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/cts/comtasks/' + id,
                        method: 'DELETE',
                        success: function () {
                            this.getApplication().fireEvent('acknowledge', 'Successfully removed');
                            self.store.loadPage(1, {
                                callback: function () {
                                    grid.getSelectionModel().select(0);
                                }
                            });
                        },
                        failure: function (response) {
                            if (response.status == 400) {
                                var result = Ext.decode(response.responseText, true),
                                    title = Ext.String.format(Uni.I18n.translate('comtask.remove.failed', 'MDC', 'Failed to remove {0}'), record.data.name),
                                    message = Uni.I18n.translate('general.server.error', 'MDC', 'Server error');
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
            }
        });
    },

    showCommunicationTasksCreateEdit: function (id) {
        this.operationType = Ext.isEmpty(id) ? Uni.I18n.translate('general.create', 'MDC', 'Create') : Uni.I18n.translate('general.edit', 'MDC', 'Edit');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('comtaskCreateEdit'));
        this.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' ' + Uni.I18n.translate('comtask.comtask', 'MDC', 'communication task'));
        this.getTaskEdit().down('toolbar').getComponent('createEditTask').setText(
            Ext.isEmpty(id) ? Uni.I18n.translate('general.create', 'MDC', 'Create') : Uni.I18n.translate('general.save', 'MDC', 'Save')
        );
        if (id) {
            this.taskEditId = id;
            this.getTaskEdit().down('toolbar').getComponent('createEditTask').enable();
            this.commands = [];
            this.loadModelToEditForm(id);
        } else {
            this.loadModelToCreateForm();
            this.commands = [];
        }
    },

    disableBtn: function () {
        var btn = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommand button[action=addCommand]')[0];
        btn.setDisabled(true);
    },

    removeEdit: function () {
        var categoryCombo = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommandCategoryCombo')[0],
            actionBtn = Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0],
            btns = Ext.ComponentQuery.query('comtaskCreateEdit tag-button');
        Ext.Array.each(btns, function (btn) {
            if (btn.category === categoryCombo.value) {
                btn.fireEvent('closeclick', btn);
                btn.destroy();
            }
        });
        if (!Ext.isEmpty(this.commands)) {
            actionBtn.setDisabled(false);
        }
    },

    cancelEdit: function () {
        var commandView = Ext.ComponentQuery.query('comtaskCommand')[0],
            actionBtn = Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0];
        commandView.destroy();
        actionBtn.setDisabled(false);
    },

    showErrorsPanel: function () {
        var formErrorsPanel = this.getTaskEdit().down('#errors');
        formErrorsPanel.hide();
        formErrorsPanel.removeAll();
        formErrorsPanel.add({
            html: Uni.I18n.translate('general.form.errors', 'MDC', 'There are errors on this page that require your attention')
        });
        formErrorsPanel.show();
    },

    createEdit: function (btn) {
        var self = this,
            sendingData = {},
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            preloader,
            formErrorsPanel = self.getTaskEdit().down('#errors'),
            nameField = editView.down('form').down('textfield[name=name]');
        self.trimFields();
        sendingData.name = nameField.getValue();
        sendingData.commands = self.commands;
        if (form.isValid()) {
            formErrorsPanel.hide();
            if (btn.text === 'Create') {
                preloader = Ext.create('Ext.LoadMask', {
                    msg: Uni.I18n.translate('comtask.creating', 'MDC', 'Creating communication task'),
                    target: editView
                });
                preloader.show();
                self.createTask(preloader, sendingData, btn);
            } else if (btn.text === 'Save') {
                preloader = Ext.create('Ext.LoadMask', {
                    msg: Uni.I18n.translate('comtask.updating', 'MDC', 'Updating communication task'),
                    target: editView
                });
                preloader.show();
                self.editTask(preloader, sendingData, btn);
            }
        } else {
            self.showErrorsPanel();
        }
    },

    createTask: function (preloader, sendingData, btn) {
        var self = this;
        Ext.Ajax.request({
            url: '/api/cts/comtasks',
            method: 'POST',
            jsonData: sendingData,
            success: function () {
                window.location.href = '#/administration/communicationtasks';
                this.getApplication().fireEvent('acknowledge', 'Successfully created');
                self.commands = [];
            },
            failure: function (response) {
                if (response.status == 400) {
                    var result = Ext.decode(response.responseText, true),
                        title = Uni.I18n.translate('general.during.creation', 'MDC', 'Error during creation'),
                        message = Uni.I18n.translate('general.server.error', 'MDC', 'Server error');
                    if (!Ext.isEmpty(response.statusText)) {
                        message = response.statusText;
                    }
                    if (result) {
                        if (result.errors[0].id === 'name') {
                            var nameField = self.getTaskEdit().down('form').down('[name=name]');
                            nameField.markInvalid(Uni.I18n.translate('comtask.name.not.unique', 'MDC', 'The name is not unique'));
                            self.showErrorsPanel();
                            return;
                        }
                        if (result.message) {
                            message = result.message;
                        } else if (result.error) {
                            message = result.error;
                        } else if (result.errors[0].id === 'protocolTasks') {
                            message = Uni.I18n.translate('comtask.requires.at.least.one.command', 'MDC', 'Requires at least one command');
                        }
                    }
                    self.getApplication().getController('Uni.controller.Error').showError(title, message);
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    editTask: function (preloader, sendingData, btn) {
        var self = this;
        Ext.Ajax.request({
            url: '/api/cts/comtasks/' + self.taskEditId,
            method: 'PUT',
            jsonData: sendingData,
            success: function () {
                window.location.href = '#/administration/communicationtasks';
                self.getApplication().fireEvent('acknowledge', 'Successfully updated');
                self.commands = [];
            },
            failure: function (response) {
                if (response.status == 400) {
                    var result = Ext.decode(response.responseText, true),
                        title = Uni.I18n.translate('general.during.editing', 'MDC', 'Error during editing'),
                        message = Uni.I18n.translate('general.server.error', 'MDC', 'Server error');
                    if (!Ext.isEmpty(response.statusText)) {
                        message = response.statusText;
                    }
                    if (result) {
                        if (result.errors[0].id === 'name') {
                            var nameField = self.getTaskEdit().down('form').down('[name=name]');
                            nameField.markInvalid(Uni.I18n.translate('comtask.name.not.unique', 'MDC', 'The name is not unique'));
                            self.showErrorsPanel();
                            return;
                        }
                        if (result.message) {
                            message = result.message;
                        } else if (result.error) {
                            message = result.error;
                        } else if (result.errors[0].id === 'protocolTasks') {
                            message = Uni.I18n.translate('comtask.requires.at.least.one.command', 'MDC', 'Requires at least one command');
                        }
                    }
                    self.getApplication().getController('Uni.controller.Error').showError(title, message);
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    trimFields: function () {
        var nameField = this.getTaskEdit().down('form').down('[name=name]'),
            nameValue = Ext.util.Format.trim(nameField.value);
        nameField.setValue(nameValue);
    },

    setTooltip: function () {
        var iconIntervals = Ext.ComponentQuery.query('#radioIntervals')[0].getEl().down('img'),
            textIntervals = Uni.I18n.translate('comtask.tooltip.textIntervals', 'MDC', 'If the clock difference between the clock in the meter and the clock of the communication server is equal to or bigger than the minimum clock difference, the intervals will be marked as bad time'),
            iconEvents = Ext.ComponentQuery.query('#radioEvents')[0].getEl().down('img'),
            textEvents = Uni.I18n.translate('comtask.tooltip.textEvents', 'MDC', 'When data with a status flag comes in, meter events will be created'),
            iconFail = Ext.ComponentQuery.query('#radioFail')[0].getEl().down('img'),
            textFail = Uni.I18n.translate('comtask.tooltip.textFail', 'MDC', 'A profile configuration defines how a load profile of that configuration looks like. When the profile configuration doesn\'t match the load profile, a failure occurs');

        iconIntervals.tooltip = Ext.create('Ext.tip.ToolTip', { target: iconIntervals, html: textIntervals });
        iconEvents.tooltip = Ext.create('Ext.tip.ToolTip', { target: iconEvents, html: textEvents });
        iconFail.tooltip = Ext.create('Ext.tip.ToolTip', { target: iconFail, html: textFail });
    },

    loadModelToCreateForm: function () {
        var self = this,
            categoriesStore = this.getStore('Mdc.store.CommunicationTasksCategories');
        categoriesStore.load({
            scope: this,
            callback: function () {
                self.addNewCommand();
            }
        });
    },

    loadModelToEditForm: function (id) {
        var self = this,
            nameField = self.getTaskEdit().down('textfield'),
            categoriesStore = self.getStore('Mdc.store.CommunicationTasksCategories');
        categoriesStore.load({
            scope: this,
            callback: function () {
                Ext.Ajax.request({
                    url: '/api/cts/comtasks/' + id,
                    success: function (response) {
                        var rec = Ext.decode(response.responseText);
                        self.getApplication().fireEvent('loadCommunicationTask', rec);
                        nameField.setValue(rec.name);
                        Ext.Array.each(rec.commands, function (command) {
                            self.addTagButton(command);
                        });
                        self.commands = rec.commands;
                        self.addAnotherButton();
                    }
                });
            }
        });
    },

    showCommandsAndActions: function (command) {
        var self = this,
            commandFields = self.getCommandFields(),
            commandView = Ext.ComponentQuery.query('comtaskCommand')[0],
            commandContainer,
            categoryCombo,
            actionCombo;
        if (commandView) {
            commandView.destroy();
        }
        commandContainer = commandFields.add({
            xtype: 'comtaskCommand'
        });
        commandContainer.add({
            xtype: 'comtaskCommandCategoryActionCombo'
        });
        categoryCombo = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommandCategoryCombo')[0];
        actionCombo = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommandCategoryActionCombo')[0];
        categoryCombo.setValue(command.category);
        actionCombo.setValue(command.action);
        self.setValuesToForm(command, commandContainer);
        categoryCombo.setDisabled(true);
        actionCombo.setDisabled(true);
        commandContainer.down('button[action=saveCommand]').show();
        commandContainer.down('button[action=removeCommand]').show();
        commandContainer.down('button[action=cancelEditCommand]').show();
    },

    setValuesToForm: function (command, commandContainer) {
        var parametersContainer = commandContainer.down('comtaskCommandCategoryActionCombo').nextNode();
        switch (command.category) {
            case 'logbooks':
                var logValues = [];
                Ext.Array.each(command.parameters[0].value, function (item) {
                    logValues.push(item.value);
                });
                parametersContainer.setValue(logValues);
                break;
            case 'registers':
                var regValues = [];
                Ext.Array.each(command.parameters[0].value, function (item) {
                    regValues.push(item.value);
                });
                parametersContainer.setValue(regValues);
                break;
            case 'loadprofiles':
                var proValues = [];
                Ext.Array.each(command.parameters[0].value, function (item) {
                    proValues.push(item.value);
                });
                parametersContainer.down('#checkProfileTypes').setValue(proValues);
                parametersContainer.down('#radioIntervals').setValue({
                    intervals: command.parameters[2].value.toString()
                });
                parametersContainer.down('#radioEvents').setValue({
                    events: command.parameters[3].value.toString()
                });
                parametersContainer.down('#radioFail').setValue({
                    fail: command.parameters[1].value.toString()
                });
                parametersContainer.down('#disContTime').setValue(command.parameters[4].value.name);
                parametersContainer.down('#disContNum').setValue(command.parameters[4].value.value);
                break;
            case 'clock':
                switch (command.action) {
                    case 'set':
                        parametersContainer.down('#setMinTime').setValue(command.parameters[0].value.name);
                        parametersContainer.down('#setMinNum').setValue(command.parameters[0].value.value);
                        parametersContainer.down('#setMaxTime').setValue(command.parameters[1].value.name);
                        parametersContainer.down('#setMaxNum').setValue(command.parameters[1].value.value);
                        break;
                    case 'synchronize':
                        parametersContainer.down('#syncMinTime').setValue(command.parameters[0].value.name);
                        parametersContainer.down('#syncMinNum').setValue(command.parameters[0].value.value);
                        parametersContainer.down('#syncMaxTime').setValue(command.parameters[1].value.name);
                        parametersContainer.down('#syncMaxNum').setValue(command.parameters[1].value.value);
                        parametersContainer.down('#syncMaxTimeShift').setValue(command.parameters[2].value.name);
                        parametersContainer.down('#syncMaxNumShift').setValue(command.parameters[2].value.value);
                        break;
                }
                break;
        }
    },

    addNewCommand: function () {
        var commandNames = this.getCommandNames(),
            commandFields = this.getCommandFields(),
            newCommand = Ext.ComponentQuery.query('#newCommand')[0],
            commandContainer;
        if (newCommand && newCommand.isHidden()) {
            newCommand.show();
        } else {
            commandNames.add({
                html: Uni.I18n.translate('comtask.new.command', 'MDC', 'New command'),
                style: 'margin: 0 0 0 0px; padding: 8px 0 0 0;',
                itemId: 'newCommand',
                width: 151
            });
        }
        commandContainer = commandFields.add({
            xtype: 'comtaskCommand'
        });
        commandContainer.down('button[action=addCommand]').show();
    },

    addActionCombo: function (combo, newValue) {
        var commandContainer = combo.up('comtaskCommand'),
            actionsStore = Ext.getStore('Mdc.store.CommunicationTasksActions'),
            actionCombo = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommandCategoryActionCombo')[0];
        if (combo.lastActiveError !== '') {
            combo.clearInvalid();
        }
        if (!actionCombo) {
            actionsStore.getProxy().setExtraParam('category', newValue);
            actionsStore.load(function (records) {
                actionCombo = commandContainer.add({
                    xtype: 'comtaskCommandCategoryActionCombo'
                });
                combo.on('change', function () {
                    actionCombo.destroy();
                }, combo, {single: true});
                if (records.length === 1) {
                    actionCombo.setValue(records[0]);
                }
            });
        }
    },

    addCommandParameters: function (combo, newValue) {
        var self = this,
            commandContainer = combo.up('comtaskCommand'),
            category = commandContainer.down('comtaskCommandCategoryCombo').getValue(),
            valuesArr = [],
            parametersContainer = this.chooseCommandParameters(category, newValue);
        if (parametersContainer) {
            var parametersComponent = commandContainer.add(parametersContainer);
            if (!commandContainer.down('button[action=addCommand]').isHidden()) {
                switch (parametersContainer.xtype) {
                    case 'communication-tasks-logbookscombo':
                        parametersComponent.getStore().load({
                            callback: function (records) {
                                Ext.Array.each(records, function (rec) {
                                    valuesArr.push(rec.data.id);
                                });
                                parametersComponent.setValue(valuesArr);
                            }
                        });
                        break;
                    case 'communication-tasks-registerscombo':
                        parametersComponent.getStore().load({
                            callback: function (records) {
                                Ext.Array.each(records, function (rec) {
                                    valuesArr.push(rec.data.id);
                                });
                                parametersComponent.setValue(valuesArr);
                            }
                        });
                        break;
                    case 'communication-tasks-profilescombo':
                        var preloader = Ext.create('Ext.LoadMask', {
                            msg: Uni.I18n.translate('comtask.loading.load.profile.types', 'MDC', 'Loading load profile types'),
                            target: self.getTaskEdit()
                        });
                        preloader.show();
                        parametersComponent.down('#checkProfileTypes').getStore().load({
                            callback: function (records) {
                                Ext.Array.each(records, function (rec) {
                                    valuesArr.push(rec.data.id);
                                });
                                parametersComponent.down('#checkProfileTypes').setValue(valuesArr);
                                preloader.destroy();
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
            combo.on('change', function () {
                parametersContainer.destroy();
            }, combo, {single: true});
            combo.on('destroy', function () {
                parametersContainer.destroy();
            }, combo, {single: true});
        }
        if (!commandContainer.down('button[action=addCommand]').isHidden()) {
            commandContainer.down('button[action=addCommand]').setDisabled(false);
        }
    },

    chooseCommandParameters: function (category, action) {
        var xtype,
            self = this,
            logbooksStore = self.getStore('Mdc.store.Logbook'),
            profilesStore = self.getStore('Mdc.store.LoadProfileTypes'),
            registersStore = self.getStore('Mdc.store.RegisterGroups');
        switch (category) {
            case 'logbooks':
                logbooksStore.load();
                xtype = 'communication-tasks-logbookscombo';
                break;
            case 'registers':
                registersStore.load();
                xtype = 'communication-tasks-registerscombo';
                break;
            case 'topology':
                break;
            case 'loadprofiles':
                profilesStore.load();
                xtype = 'communication-tasks-profilescombo';
                break;
            case 'clock':
                switch (action) {
                    case 'set':
                        xtype = 'communication-tasks-parameters-clock-set';
                        break;
                    case 'synchronize':
                        xtype = 'communication-tasks-parameters-clock-synchronize';
                        break;
                }
                break;
        }
        if (xtype) {
            return Ext.widget(xtype);
        }
        return null;
    },

    addCommandToModel: function (button) {
        var self = this,
            commandContainer = button.up('comtaskCommand'),
            commandFields = self.getCommandFields(),
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            category = commandContainer.down('comtaskCommandCategoryCombo').value,
            action = commandContainer.down('comtaskCommandCategoryActionCombo').value,
            newCommand = Ext.ComponentQuery.query('#newCommand')[0],
            btns = Ext.ComponentQuery.query('comtaskCreateEdit tag-button'),
            parametersContainer = commandContainer.down('comtaskCommandCategoryActionCombo').nextNode(),
            btnAction = button.action,
            numItem,
            couldAdd = true,
            protocol = {};
        Ext.Array.each(btns, function (btn) {
            if (button.action === 'addCommand' && btn.category === category) {
                commandContainer.down('comtaskCommandCategoryCombo').markInvalid(
                    Uni.I18n.translate('comtask.already.has.this.category', 'MDC', 'The communication task already has this category')
                );
                button.disable();
            }
        });
        if (commandContainer.down('comtaskCommandCategoryCombo').lastActiveError === '') {
            protocol.category = category;
            protocol.action = action;
            protocol.parameters = [];
            Ext.Array.each(self.commands, function (item) {
                if (item.category === protocol.category) {
                    protocol.id = item.id;
                    numItem = self.commands.indexOf(item);
                }
            });
            if (numItem) {
                self.commands.splice(numItem, 1);
            }
            switch (parametersContainer.xtype) {
                case 'communication-tasks-logbookscombo':
                    if (parametersContainer.value.length < 1) {
                        parametersContainer.markInvalid(
                            Uni.I18n.translate('general.required.field', 'MDC', 'This is a required field')
                        );
                        button.disable();
                        couldAdd = false;
                    }
                    self.fillLogbooks(protocol, parametersContainer);
                    break;
                case 'communication-tasks-registerscombo':
                    if (parametersContainer.value.length < 1) {
                        parametersContainer.markInvalid(
                            Uni.I18n.translate('general.required.field', 'MDC', 'This is a required field')
                        );
                        button.disable();
                        couldAdd = false;
                    }
                    self.fillRegisters(protocol, parametersContainer);
                    break;
                case 'communication-tasks-profilescombo':
                    if (parametersContainer.down('#checkProfileTypes').value.length < 1) {
                        parametersContainer.down('#checkProfileTypes').markInvalid('This is a required field');
                        button.disable();
                        couldAdd = false;
                    }
                    self.fillProfiles(protocol, parametersContainer);
                    break;
                case 'communication-tasks-parameters-clock-set':
                    self.fillClockSet(protocol, parametersContainer);
                    break;
                case 'communication-tasks-parameters-clock-synchronize':
                    self.fillClockSync(protocol, parametersContainer);
                    break;
                default:
                    self.commands.push(protocol);
                    break;
            }
            if (couldAdd) {
                if (btnAction === 'addCommand') {
                    newCommand.destroy();
                    self.addTagButton(protocol);
                    if (!(self.commands.length === 5)) {
                        self.addAnotherButton();
                    }
                    Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0].setDisabled(false);
                } else {
                    Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0].setDisabled(false);
                    self.updateTagButton(protocol);
                }
                commandFields.removeAll();
            }
        }
    },

    fillLogbooks: function (protocol, parametersContainer) {
        var self = this,
            logbooks = {};
        logbooks.name = "logbooktypeids";
        logbooks.value = [];
        Ext.Array.each(parametersContainer.value, function (item) {
            var logbook = {};
            logbook.name = "logbooktypeid";
            logbook.value = item;
            logbooks.value.push(logbook);
        });
        protocol.parameters.push(logbooks);
        self.commands.push(protocol);
    },

    fillRegisters: function (protocol, parametersContainer) {
        var self = this,
            registers = {};
        registers.name = "registergroupids";
        registers.value = [];
        Ext.Array.each(parametersContainer.value, function (item) {
            var register = {};
            register.name = "registergroupid";
            register.value = item;
            registers.value.push(register);
        });
        protocol.parameters.push(registers);
        self.commands.push(protocol);
    },

    fillProfiles: function (protocol, parametersContainer) {
        var self = this,
            profiles = {},
            intervals = {},
            events = {},
            fail = {},
            intervalsBoolean = false,
            eventsBoolean = false,
            failBoolean = false;
        profiles.name = "loadprofiletypeids";
        profiles.value = [];
        Ext.Array.each(parametersContainer.down('#checkProfileTypes').value, function (item) {
            var profile = {};
            profile.name = "loadprofiletypeid";
            profile.value = item;
            profiles.value.push(profile);
        });
        protocol.parameters.push(profiles);
        fail.name = "failifconfigurationmismatch";
        if (parametersContainer.down('#radioFail').getValue().fail === 'true') {
            failBoolean = true;
        }
        fail.value = failBoolean;
        protocol.parameters.push(fail);
        intervals.name = "markintervalsasbadtime";
        if (parametersContainer.down('#radioIntervals').getValue().intervals === 'true') {
            intervalsBoolean = true;
        }
        intervals.value = intervalsBoolean;
        protocol.parameters.push(intervals);
        events.name = "createmetereventsfromflags";
        if (parametersContainer.down('#radioEvents').getValue().events === 'true') {
            eventsBoolean = true;
        }
        events.value = eventsBoolean;
        protocol.parameters.push(events);
        var minTime = {},
            minTimeValue = {};
        minTime.name = "minclockdiffbeforebadtime";
        minTimeValue.name = parametersContainer.down('#disContTime').value;
        minTimeValue.value = parseInt(parametersContainer.down('#disContNum').getValue());
        minTime.value = minTimeValue;
        protocol.parameters.push(minTime);
        self.commands.push(protocol);
    },

    fillClockSet: function (protocol, parametersContainer) {
        var self = this,
            setMinTime = {},
            setMaxTime = {},
            setMinTimeValue = {},
            setMaxTimeValue = {};
        setMinTime.name = "minimumclockdifference";
        setMaxTime.name = "maximumclockdifference";
        setMinTimeValue.name = parametersContainer.down('#setMinTime').value;
        setMinTimeValue.value = parseInt(parametersContainer.down('#setMinNum').getValue());
        setMaxTimeValue.name = parametersContainer.down('#setMaxTime').value;
        setMaxTimeValue.value = parseInt(parametersContainer.down('#setMaxNum').getValue());
        setMinTime.value = setMinTimeValue;
        setMaxTime.value = setMaxTimeValue;
        protocol.parameters.push(setMinTime);
        protocol.parameters.push(setMaxTime);
        self.commands.push(protocol);
    },

    fillClockSync: function (protocol, parametersContainer) {
        var self = this,
            syncMinTime = {},
            syncMaxTime = {},
            syncMinTimeValue = {},
            syncMaxTimeValue = {},
            syncMaxTimeShift = {},
            syncMaxTimeShiftValue = {};
        syncMinTime.name = "minimumclockdifference";
        syncMaxTime.name = "maximumclockdifference";
        syncMaxTimeShift.name = "maximumclockshift";
        syncMinTimeValue.name = parametersContainer.down('#syncMinTime').value;
        syncMinTimeValue.value = parseInt(parametersContainer.down('#syncMinNum').getValue());
        syncMaxTimeValue.name = parametersContainer.down('#syncMaxTime').value;
        syncMaxTimeValue.value = parseInt(parametersContainer.down('#syncMaxNum').getValue());
        syncMaxTimeShiftValue.name = parametersContainer.down('#syncMaxTimeShift').value;
        syncMaxTimeShiftValue.value = parseInt(parametersContainer.down('#syncMaxNumShift').getValue());
        syncMinTime.value = syncMinTimeValue;
        syncMaxTime.value = syncMaxTimeValue;
        syncMaxTimeShift.value = syncMaxTimeShiftValue;
        protocol.parameters.push(syncMinTime);
        protocol.parameters.push(syncMaxTime);
        protocol.parameters.push(syncMaxTimeShift);
        self.commands.push(protocol);
    },

    updateTagButton: function (command) {
        var self = this;
        self.getCommandNames().down('#tagBtn' + command.category).handler = function () {
            var newCommand = Ext.ComponentQuery.query('#newCommand')[0];
            if (newCommand) {
                newCommand.hide();
                self.addAnotherButton();
            }
            self.showCommandsAndActions(command);
        };
    },

    addTagButton: function (command) {
        var self = this;
        self.getCommandNames().add({
            xtype: 'tag-button',
            itemId: 'tagBtn' + command.category,
            text: command.action.charAt(0).toUpperCase() + command.action.slice(1) + ' ' + command.category.charAt(0).toUpperCase() + command.category.slice(1),
            margin: '5 0 5 0',
            width: 150,
            category: command.category,
            handler: function () {
                var newCommand = Ext.ComponentQuery.query('#newCommand')[0];
                if (newCommand) {
                    newCommand.hide();
                    self.addAnotherButton();
                }
                self.showCommandsAndActions(command);
                Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0].setDisabled(true);
            },
            listeners: {
                closeclick: function (me) {
                    var commandView = Ext.ComponentQuery.query('comtaskCommand')[0],
                        numItem;
                    if (commandView) {
                        if (me.category === commandView.down('comtaskCommandCategoryCombo').value) {
                            commandView.destroy();
                        }
                    }
                    Ext.Array.each(self.commands, function (item) {
                        if (item.category === me.category) {
                            numItem = self.commands.indexOf(item);
                        }
                    });
                    self.commands.splice(numItem, 1);
                    numItem = null;
                    Ext.Array.each(self.commands, function (item) {
                        if (item.category === me.category) {
                            numItem = self.commands.indexOf(item);
                        }
                    });
                    if (numItem !== null) {
                        self.commands.splice(numItem, 1);
                    }
                    Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0].setDisabled(false);
                    if (self.commands.length < 1) {
                        var addAnotherBtn = Ext.ComponentQuery.query('comtaskCreateEdit #addAnotherButton')[0];
                        if (addAnotherBtn) {
                            addAnotherBtn.destroy();
                        }
                        if (!commandView || Ext.isEmpty(commandView.items.items)) {
                            self.loadModelToCreateForm();
                        }
                        Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0].setDisabled(true);
                    }
                    if (self.getTaskEdit().down('#addAnotherButton') === null) {
                        self.addAnotherButton();
                    }
                    if (Ext.isEmpty(self.commands) && self.getTaskEdit().down('#addAnotherButton') !== null) {
                        self.getTaskEdit().down('#addAnotherButton').destroy();
                    }
                }
            }
        });
    },

    addAnotherButton: function () {
        var self = this;
        self.getCommandNames().add({
            xtype: 'button',
            itemId: 'addAnotherButton',
            text: Uni.I18n.translate('comtask.add.another', 'MDC', '+ Add another'),
            margin: '5 0 5 0',
            handler: function () {
                this.destroy();
                var categoriesStore = self.getStore('Mdc.store.CommunicationTasksCategories'),
                    commandView = Ext.ComponentQuery.query('comtaskCommand')[0];
                if (commandView) {
                    commandView.destroy();
                }
                categoriesStore.load({
                    scope: this,
                    callback: function () {
                        self.addNewCommand();
                    }
                });
            }
        });
    }
});
