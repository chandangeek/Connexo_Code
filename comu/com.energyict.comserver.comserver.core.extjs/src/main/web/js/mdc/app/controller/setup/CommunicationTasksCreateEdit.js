Ext.define('Mdc.controller.setup.CommunicationTasksCreateEdit', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

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
        'Mdc.view.setup.comtasks.Edit',
        'Mdc.view.setup.comtasks.Command',
        'Mdc.view.setup.comtasks.parameters.Logbooks',
        'Mdc.view.setup.comtasks.parameters.Profiles',
        'Mdc.view.setup.comtasks.parameters.Registers'
    ],

    refs: [
        {
            ref: 'taskEdit',
            selector: 'communication-tasks-edit'
        },
        {
            ref: 'commandNames',
            selector: 'communication-tasks-edit [name=commandnames]'
        },
        {
            ref: 'commandFields',
            selector: 'communication-tasks-edit [name=commandfields]'
        }
    ],

    init: function () {
        this.control({
            'communication-tasks-edit communication-tasks-categorycombo': {
                change: this.addActionCombo
            },
            'communication-tasks-edit communication-tasks-actioncombo': {
                change: this.addCommandParameters,
                afterrender: this.disableBtn
            },
            'communication-tasks-edit communication-tasks-command button[action=addCommand]': {
                click: this.addCommandToModel
            },
            'communication-tasks-edit communication-tasks-command button[action=saveCommand]': {
                click: this.addCommandToModel
            },
            'communication-tasks-edit communication-tasks-command button[action=cancelEditCommand]': {
                click: this.cancelEdit
            },
            'communication-tasks-edit communication-tasks-command button[action=removeCommand]': {
                click: this.removeEdit
            },
            'communication-tasks-edit #createEditTask': {
                click: this.createEdit
            },
            'communication-tasks-profilescombo': {
                afterrender: this.setTooltip
            }
        });
        this.commands = [];
    },

    showCommunicationTasksCreateEdit: function (id) {
        var self = this,
            widget = Ext.widget('communication-tasks-edit');
        if (id) {
            self.taskEditId = id;
            this.operationType = 'Edit';
            this.getApplication().fireEvent('changecontentevent', widget);
            this.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' communication task');
            this.getTaskEdit().down('toolbar').getComponent('createEditTask').setText('Save');
            this.getTaskEdit().down('toolbar').getComponent('createEditTask').enable();
            self.commands = [];
            this.loadModelToEditForm(id);
        } else {
            this.operationType = 'Create';
            this.getApplication().fireEvent('changecontentevent', widget);
            this.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' communication task');
            this.getTaskEdit().down('toolbar').getComponent('createEditTask').setText('Create');
            this.loadModelToCreateForm();
            self.commands = [];
        }
    },

    disableBtn: function () {
        var btn = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-command button[action=addCommand]')[0];
        btn.setDisabled(true);
    },

    removeEdit: function () {
        var categoryCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-categorycombo')[0],
            actionBtn = Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0],
            btns = Ext.ComponentQuery.query('communication-tasks-edit tag-button');
        Ext.Array.each(btns, function (btn) {
            if (btn.category === categoryCombo.value) {
                btn.fireEvent('closeclick', btn);
                btn.destroy();
            }
        });
        if (!Ext.isEmpty(this.commands)) {
            actionBtn.setDisabled(false);
        }
        if (this.getTaskEdit().down('#addAnotherButton') === null) {
            this.addAnotherButton();
        }
        if (Ext.isEmpty(this.commands) && this.getTaskEdit().down('#addAnotherButton') !== null) {
            this.getTaskEdit().down('#addAnotherButton').destroy();
        }
    },

    cancelEdit: function () {
        var commandView = Ext.ComponentQuery.query('communication-tasks-command')[0],
            actionBtn = Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0];
        commandView.destroy();
        actionBtn.setDisabled(false);
    },

    showErrorsPanel: function () {
        var formErrorsPanel = this.getTaskEdit().down('#errors');
        formErrorsPanel.hide();
        formErrorsPanel.removeAll();
        formErrorsPanel.add({
            html: 'There are errors on this page that require your attention.'
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
                    msg: "Creating communication task",
                    target: editView
                });
                preloader.show();
                self.createTask(preloader, sendingData, btn);
            } else if (btn.text === 'Save') {
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Editing communication task",
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
                Ext.create('widget.uxNotification', {
                    html: 'Successfully created',
                    ui: 'notification-success'
                }).show();
                self.commands = [];
            },
            failure: function (response) {
                var result = Ext.decode(response.responseText, true);
                if (result !== null) {
                    if (result.errors[0].id === 'protocolTasks') {
                        var msgWindow = Ext.widget('messagebox', {
                            itemId: 'msgWindow',
                            buttons: [
                                {
                                    text: 'Cancel',
                                    action: 'cancel',
                                    ui: 'link',
                                    handler: function (me) {
                                        me.up('#msgWindow').close();
                                        window.location = '#/administration/communicationtasks';
                                    }
                                }
                            ]
                        });
                        msgWindow.show({
                            ui: 'notification-error',
                            title: 'Error during creation',
                            msg: 'Requires at least one command',
                            icon: Ext.MessageBox.ERROR
                        });
                    } else {
                        self.showNameNotUniqueError();
                        self.showErrorsPanel();
                    }
                } else {
                    self.showDatabaseError(btn);
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
                Ext.create('widget.uxNotification', {
                    html: 'Successfully edited',
                    ui: 'notification-success'
                }).show();
                self.commands = [];
            },
            failure: function (response) {
                var result = Ext.decode(response.responseText, true);
                if (result !== null) {
                    if (result.errors[0].id === 'name') {
                        self.showNameNotUniqueError();
                        self.showErrorsPanel();
                    }
                } else {
                    self.showDatabaseError(btn);
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    showNameNotUniqueError: function () {
        var self = this,
            editView = self.getTaskEdit(),
            nameField = editView.down('form').down('[name=name]'),
            nameText = 'The name is not unique';
        nameField.markInvalid(nameText);
    },

    trimFields: function () {
        var nameField = this.getTaskEdit().down('form').down('[name=name]'),
            nameValue = Ext.util.Format.trim(nameField.value);
        nameField.setValue(nameValue);
    },

    showDatabaseError: function (btn) {
        var msgWindow = Ext.widget('messagebox', {
            itemId: 'msgWindowDb',
            buttons: [
                {
                    text: 'Cancel',
                    action: 'cancel',
                    ui: 'link',
                    handler: function (me) {
                        me.up('#msgWindowDb').close();
                        window.location = '#/administration/communicationtasks';
                    }
                }
            ]
        });
        if (btn.text === 'Create') {
            msgWindow.show({
                ui: 'notification-error',
                title: 'Error during creating',
                msg: 'The communication task could not be created because of an error in the database.',
                icon: Ext.MessageBox.ERROR
            });
        } else {
            msgWindow.show({
                ui: 'notification-error',
                title: 'Error during editing',
                msg: 'The communication task could not be edited because of an error in the database.',
                icon: Ext.MessageBox.ERROR
            });
        }
    },

    setTooltip: function () {
        var iconIntervals = Ext.ComponentQuery.query('#radioIntervals')[0].getEl().down('img'),
            textIntervals = 'If the clock difference between the clock in the meter and the clock of the communication server is equal to or bigger than the minimum clock difference, the intervals will be marked as bad time',
            iconEvents = Ext.ComponentQuery.query('#radioEvents')[0].getEl().down('img'),
            textEvents = 'When data with a status flag comes in, meter events will be created',
            iconFail = Ext.ComponentQuery.query('#radioFail')[0].getEl().down('img'),
            textFail = "A profile configuration defines how a load profile of that configuration looks like. When the profile configuration doesn't match the load profile, a failure occurs.";
        iconIntervals.tooltip = Ext.create('Ext.tip.ToolTip', {
            target: iconIntervals,
            html: textIntervals
        });
        iconEvents.tooltip = Ext.create('Ext.tip.ToolTip', {
            target: iconEvents,
            html: textEvents
        });
        iconFail.tooltip = Ext.create('Ext.tip.ToolTip', {
            target: iconFail,
            html: textFail
        });
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
            commandView = Ext.ComponentQuery.query('communication-tasks-command')[0],
            commandContainer,
            categoryCombo,
            actionCombo;
        if (commandView) {
            commandView.destroy();
        }
        commandContainer = commandFields.add({
            xtype: 'communication-tasks-command'
        });
        commandContainer.add({
            xtype: 'communication-tasks-actioncombo'
        });
        categoryCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-categorycombo')[0];
        actionCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-actioncombo')[0];
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
        var parametersContainer = commandContainer.down('communication-tasks-actioncombo').nextNode();
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
                html: 'New command',
                style: 'margin: 0 0 0 0px; padding: 8px 0 0 0;',
                itemId: 'newCommand',
                width: 151
            });
        }
        commandContainer = commandFields.add({
            xtype: 'communication-tasks-command'
        });
        commandContainer.down('button[action=addCommand]').show();
    },

    addActionCombo: function (combo, newValue) {
        var commandContainer = combo.up('communication-tasks-command'),
            actionsStore = Ext.getStore('Mdc.store.CommunicationTasksActions'),
            actionCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-actioncombo')[0];
        if (combo.lastActiveError !== '') {
            combo.clearInvalid();
        }
        if (!actionCombo) {
            actionsStore.getProxy().setExtraParam('category', newValue);
            actionsStore.load(function () {
                actionCombo = commandContainer.add({
                    xtype: 'communication-tasks-actioncombo'
                });
                combo.on('change', function () {
                    actionCombo.destroy();
                }, combo, {single: true});
            });
        }
    },

    addCommandParameters: function (combo, newValue) {
        var self = this,
            commandContainer = combo.up('communication-tasks-command'),
            category = commandContainer.down('communication-tasks-categorycombo').getValue(),
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
                            msg: "Loading load profile types",
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
            commandContainer = button.up('communication-tasks-command'),
            commandFields = self.getCommandFields(),
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            category = commandContainer.down('communication-tasks-categorycombo').value,
            action = commandContainer.down('communication-tasks-actioncombo').value,
            newCommand = Ext.ComponentQuery.query('#newCommand')[0],
            btns = Ext.ComponentQuery.query('communication-tasks-edit tag-button'),
            parametersContainer = commandContainer.down('communication-tasks-actioncombo').nextNode(),
            btnAction = button.action,
            numItem,
            couldAdd = true,
            protocol = {};
        Ext.Array.each(btns, function (btn) {
            if (button.action === 'addCommand' && btn.category === category) {
                commandContainer.down('communication-tasks-categorycombo').markInvalid('The communication task already has this category');
                button.disable();
            }
        });
        if (commandContainer.down('communication-tasks-categorycombo').lastActiveError === '') {
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
                        parametersContainer.markInvalid('This is a required field');
                        button.disable();
                        couldAdd = false;
                    }
                    self.fillLogbooks(protocol, parametersContainer);
                    break;
                case 'communication-tasks-registerscombo':
                    if (parametersContainer.value.length < 1) {
                        parametersContainer.markInvalid('This is a required field');
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
                    Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0].setDisabled(false);
                } else {
                    Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0].setDisabled(false);
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
            text: command.action + ' ' + command.category,
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
                Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0].setDisabled(true);
            },
            listeners: {
                closeclick: function (me) {
                    var commandView = Ext.ComponentQuery.query('communication-tasks-command')[0],
                        numItem;
                    if (commandView) {
                        if (me.category === commandView.down('communication-tasks-categorycombo').value) {
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
                    Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0].setDisabled(false);
                    if (self.commands.length < 1) {
                        var addAnotherBtn = Ext.ComponentQuery.query('communication-tasks-edit #addAnotherButton')[0];
                        if (addAnotherBtn) {
                            addAnotherBtn.destroy();
                        }
                        if (!commandView || Ext.isEmpty(commandView.items.items)) {
                            self.loadModelToCreateForm();
                        }
                        Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0].setDisabled(true);
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
            text: '+ Add another',
            margin: '5 0 5 0',
            handler: function () {
                this.destroy();
                var categoriesStore = self.getStore('Mdc.store.CommunicationTasksCategories'),
                    commandView = Ext.ComponentQuery.query('communication-tasks-command')[0];
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
