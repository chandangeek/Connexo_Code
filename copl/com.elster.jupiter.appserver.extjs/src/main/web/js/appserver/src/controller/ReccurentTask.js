/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.ReccurentTask', {
    extend: 'Apr.controller.TaskManagementGeneralTask',


    init: function () {
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.reccurentTask', 'APR', 'Reccurent Task'),
            controller: this
        });
    },

    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Apr.privileges.AppServer.reccurentTaskAdmin);
    },

    canRun: function () {
        return Uni.Auth.checkPrivileges(Apr.privileges.AppServer.reccurentTaskAdmin);
    },

    getType: function () {
        return 'AddCertReqDataTopic';
    },

    runTaskManagement: function (taskManagement, operationStartFunc, operationCompletedFunc, controller) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('reccurentTask.general.run', 'APR', 'Run'),
                confirmation: function () {
                    me.submitRunTask(taskManagement, operationStartFunc, operationCompletedFunc, controller, this);
                }
            });

        confirmationWindow.insert(1,
            {
                xtype: 'panel',
                itemId: 'date-errors',
                hidden: true,
                cls: 'confirmation-window',
                html: 'sssss'
            }
        );

        confirmationWindow.show({
            msg: Uni.I18n.translate('recurrentTask.runMsg', 'APR', 'This recurrent task will be queued to run at the earliest possible time.'),
            title: Uni.I18n.translate('recurrentTask.runTask', 'APR', "Run recurrent task '{0}'?", [taskManagement.get('name')])
        });
    },

    submitRunTask: function (taskManagement, operationStartFunc, operationCompletedFunc, controller, confWindow) {
        var me = this;

        operationStartFunc.call(controller);

                    Ext.Ajax.request({
                        url: '/api/tsk/task/runaddcertreqdatatask',
                        method: 'PUT',
                        isNotEdit: true,
                        success: function () {
                            confWindow.destroy();
                            operationCompletedFunc.call(controller, true);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.run', 'APR', 'Data validation task run queued'));
                        },
                        failure: function (response) {
                            operationCompletedFunc.call(controller, false);
                            if (response.status === 400) {
                                var res = Ext.JSON.decode(response.responseText);
                                confWindow.update(res.errors[0].msg);
                                confWindow.setVisible(true);
                            }
                            else {
                                confWindow.destroy();
                            }
                        }
                    });


    }

});