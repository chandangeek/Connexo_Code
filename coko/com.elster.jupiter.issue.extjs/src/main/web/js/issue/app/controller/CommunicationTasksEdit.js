Ext.define('Isu.controller.CommunicationTasksEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.CommunicationTasks',
        'Isu.store.CommunicationTasksCategories',
        'Isu.store.CommunicationTasksActions',
        'Isu.store.TimeTypes'
    ],

    views: [
        'Isu.view.administration.communicationtasks.Edit'
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
            'communication-tasks-edit breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'communication-tasks-edit communication-tasks-categorycombo': {
                change: this.addActionCombo
            },
            'communication-tasks-edit communication-tasks-actioncombo': {
                change: this.addComandParameters
            },
            'communication-tasks-edit communication-tasks-command button[action=addCommand]': {
                click: this.addCommandToModel
            }
        });
    },

    showOverview: function (id) {
        var self = this,
            widget = Ext.widget('communication-tasks-edit');

        if (id) {
            this.operationType = 'Edit';
            this.getModel('Isu.model.CommunicationTasks').load(id, function (record) {
                self.taskModel = record;
                self.loadModelToForm();
                self.getApplication().fireEvent('changecontentevent', widget);
                self.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' communication task');
            });
        } else {
            this.operationType = 'Create';
            this.taskModel = new Isu.model.CommunicationTasks();
            this.loadModelToForm();
            this.getApplication().fireEvent('changecontentevent', widget);
            this.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' communication task');
        }
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this,
            breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Communication tasks',
                href: 'communicationtasks'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: me.operationType + ' communication task'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    loadModelToForm: function () {
        var model = this.taskModel,
            categoriesStore = this.getStore('Isu.store.CommunicationTasksCategories');

        categoriesStore.load({
            scope: this,
            callback: function () {
                if (!model.get('commands').length) {
                    this.addNewCommand();
                } else {

                }
            }
        });
    },

    addNewCommand: function () {
        var commandNames = this.getCommandNames(),
            commandFields = this.getCommandFields(),
            commandContainer;

        commandNames.add({
            html: 'New command',
            style: 'margin: 0 0 0 11px; padding: 6px 0 0 0;',
            width: 151
        });
        commandContainer = commandFields.add({
            xtype: 'communication-tasks-command'
        });

        if (this.operationType == 'Edit') {
            commandContainer.down('button[action=saveCommand]').show();
            commandContainer.down('button[action=removeCommand]').show();
            commandContainer.down('button[action=cancelEditCommand]').show();
        } else {
            commandContainer.down('button[action=addCommand]').show();
        }
    },

    addActionCombo: function (combo, newValue) {
        var commandContainer = combo.up('communication-tasks-command'),
            actionsStore = Ext.getStore('Isu.store.CommunicationTasksActions'),
            actionCombo;

        actionsStore.getProxy().setExtraParam('category', newValue);
        actionsStore.load(function () {
            actionCombo = commandContainer.add({
                xtype: 'communication-tasks-actioncombo'
            });

            combo.on('change', function () {
                actionCombo.destroy();
            }, combo, {single: true});
        });
    },

    addComandParameters: function (combo, newValue) {
        var commandContainer = combo.up('communication-tasks-command'),
            category = commandContainer.down('communication-tasks-categorycombo').getValue(),
            parametersContainer = this.chooseComandParameters(category, newValue);

        if (parametersContainer) {
            commandContainer.add(parametersContainer);
            combo.on('change', function () {
                parametersContainer.destroy();
            }, combo, {single: true});
            combo.on('destroy', function () {
                parametersContainer.destroy();
            }, combo, {single: true});
        }

        if (this.operationType == 'Create') {
            commandContainer.down('button[action=addCommand]').setDisabled(false);
        }
    },

    chooseComandParameters: function (category, action) {
        var xtype;

        switch (category) {
            case 'logbooks':
                break;
            case 'registers':
                break;
            case 'topology':
                break;
            case 'loadprofiles':
                break;
            case 'clock':
                switch (action) {
                    case '1':
                        xtype = 'communication-tasks-parameters-clock-set';
                        break;
                    case '2':
                        break;
                    case '3':
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
        var commandContainer = button.up('communication-tasks-command');


    }
});