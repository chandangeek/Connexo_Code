Ext.define('Dal.controller.BulkChangeAlarms', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dal.store.AlarmStatuses',
        'Dal.store.AlarmAssignees',
        'Dal.store.AlarmsBuffered',
        'Dal.store.BulkChangeAlarms'
    ],

    views: [
        'Dal.view.bulk.Browse'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'alarm-bulk-browse'
        },
        {
            ref: 'bulkNavigation',
            selector: 'alarm-bulk-browse alarm-bulk-navigation'
        }
    ],
    listeners: {
        retryRequest: function (wizard, failedItems) {
            this.setFailedBulkRecordAlarms(failedItems);
            this.onWizardFinishedEvent(wizard);
        }
    },
    init: function () {
        this.control({
            'alarm-bulk-browse alarm-bulk-wizard': {
                wizardnext: this.onWizardNextEvent,
                wizardprev: this.onWizardPrevEvent,
                wizardstarted: this.onWizardStartedEvent,
                wizardfinished: this.onWizardFinishedEvent,
                wizardcancelled: this.onWizardCancelledEvent
            },
            'alarm-bulk-browse alarm-bulk-navigation': {
                movetostep: this.setActivePage
            },
            'alarm-bulk-browse alarm-bulk-wizard alarm-bulk-step2 radiogroup': {
                change: this.onStep2RadiogroupChangeEvent,
                afterrender: this.getDefaultStep2Operation
            },
            'alarm-bulk-browse alarm-bulk-wizard alarm-bulk-step3 issues-close-form radiogroup': {
                change: this.onStep3RadiogroupCloseChangeEvent,
                afterrender: this.getDefaultCloseStatus
            },
            'alarm-bulk-browse alarm-bulk-step4': {
                beforeactivate: this.beforeStep4
            },
            'alarm-bulk-browse alarm-bulk-wizard alarm-bulk-step3 issues-close-form': {
                beforerender: this.alarmClosingFormBeforeRenderEvent
            }
        });
    },

    alarmClosingFormBeforeRenderEvent: function (form) {
        var statusesContainer = form.down('[name=status]'),
            values = Ext.state.Manager.get('formCloseValues');
        Ext.Ajax.request({
            url: '/api/dal/statuses',
            method: 'GET',
            success: function (response) {
                var statuses = Ext.decode(response.responseText);
                Ext.each(statuses, function (status) {
                    if (!Ext.isEmpty(status.allowForClosing) && status.allowForClosing) {
                        statusesContainer.add({
                            boxLabel: status.name,
                            inputValue: status.id,
                            name: 'status'
                        })
                    }
                });
                if (Ext.isEmpty(values)) {
                    statusesContainer.items.items[0].setValue(true);
                } else {
                    statusesContainer.down('[inputValue=' + values.status + ']').setValue(true);
                }
            }
        });
        if (values) {
            form.down('textarea').setValue(values.comment);
        }
    },

    showOverview: function () {
        var me = this,
            alarmsStore = this.getStore('Dal.store.AlarmsBuffered'),
            alarmsStoreProxy = alarmsStore.getProxy(),
            queryStringValues = Uni.util.QueryString.getQueryStringValues(false),
            filter = [],
            widget, grid;

        alarmsStoreProxy.extraParams = {};
        if (queryStringValues.sort) {
            alarmsStoreProxy.setExtraParam('sort', queryStringValues.sort);
            delete queryStringValues.sort;
        }
        if (Ext.isDefined(queryStringValues.groupingType) && Ext.isDefined(queryStringValues.groupingValue) && Ext.isEmpty(queryStringValues[queryStringValues.groupingType])) {
            filter.push({
                property: queryStringValues.groupingType,
                value: queryStringValues.groupingValue
            });
        }
        delete queryStringValues.groupingType;
        delete queryStringValues.groupingValue;
        Ext.iterate(queryStringValues, function (name, value) {
            filter.push({
                property: name,
                value: value
            });
        });

        widget = Ext.widget('alarm-bulk-browse');
        grid = widget.down('alarm-bulk-step1').down('alarms-selection-grid');
        grid.reconfigure(alarmsStore);
        grid.filterParams = Ext.clone(filter);

        me.getApplication().fireEvent('changecontentevent', widget);
        alarmsStore.data.clear();
        alarmsStore.clearFilter(!!filter.length);
        alarmsStore.filter(filter);
        alarmsStore.on('load', function () {
            grid.onSelectDefaultGroupType();
        }, me, {single: true});
    },

    setActivePage: function (index) {
        var wizard = this.createdWizard;
        wizard.show();
        wizard.activeItemId = index - 1;
        wizard.getLayout().setActiveItem(wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
    },

    setFailedBulkRecordAlarms: function (failedAlarms) {
        var record = this.getBulkRecord(),
            previousAlarms = record.get('alarms'),
            leftAlarms = [];

        Ext.each(previousAlarms, function (alarm) {
            if (Ext.Array.contains(failedAlarms, alarm.get('id'))) {
                leftAlarms.push(alarm);
            }
        });

        record.set('alarms', leftAlarms);
        record.commit();
    },

    onWizardPrevEvent: function (wizard) {
        var index = wizard.getActiveItemId(),
            operation = this.getBulkRecord().get('operation');
            this.getBulkNavigation().movePrevStep();
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardNextEvent: function (wizard) {
        var index = wizard.getActiveItemId(),
            operation = this.getBulkRecord().get('operation');

        this.getBulkNavigation().moveNextStep();
        this.setBulkActionListActiveItem(wizard);
        var functionName = 'processNextOnStep' + index;
        this.processStep(functionName, wizard);
    },

    onWizardStartedEvent: function (wizard) {
        this.createdWizard = wizard;
        this.setBulkActionListActiveItem(wizard);
    },

    onWizardFinishedEvent: function (wizard) {
        var me = this,
            step5panel = me.getPage().down('alarm-bulk-wizard').down('alarm-bulk-step5'),
            record = me.getBulkRecord(),
            requestData = me.getRequestData(record),
            operation = record.get('operation'),
            isRetry = (operation == 'retrycomm') || (operation == 'retrycommnow') || (operation == 'retryconn'),
            requestUrl = '/api/dal/alarms/' + operation,
            warnAlarms = [],
            failedAlarms = [],
            params = [],
            allAlarms = false,
            step5panelText = '';

        this.setBulkActionListActiveItem(wizard);

        if (record.get('allAlarms')) {
            allAlarms = record.data.allAlarms;
            params = {};
            params.filter = Ext.encode(record.data.params);
        } else {
            params = [];
            allAlarms = false;
        }

        var pb = Ext.create('Ext.ProgressBar', {width: '50%'});
        Ext.suspendLayouts();
        step5panel.removeAll(true);
        switch (operation) {
            case 'assign':
                step5panelText = Uni.I18n.translate('alarms.processing.assign', 'DAL', 'Assigning {0} alarm(s). Please wait...',
                    (requestData.allAlarms ? Uni.I18n.translate('general.all', 'DAL', 'all') : requestData.alarms.length));
                break;
            case 'close':
                step5panelText = Uni.I18n.translate('alarms.processing.close', 'DAL', 'Closing {0} alarm(s). Please wait...',
                    (requestData.allAlarms ? Uni.I18n.translate('general.all', 'DAL', 'all') : requestData.alarms.length));
                break;
            default:
                requestUrl = '/api/dal/alarms/' + operation;
                step5panelText = Uni.I18n.translate('alarms.processing.default', 'DAL', 'Processing {0} alarm(s). Please wait...',
                    (requestData.allAlarms ? Uni.I18n.translate('general.all', 'DAL', 'all') : requestData.alarms.length));
                break;
        }
        step5panel.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: step5panelText
            })
        );
        Ext.resumeLayouts(true);

        Ext.Ajax.request({
            url: requestUrl,
            method: 'PUT',
            params: params,
            jsonData: requestData,
            timeout: 120000,
            success: function (response) {
                var obj = Ext.decode(response.responseText),
                    successCount = obj.success.length,
                    warnCount = 0,
                    failedCount = 0,
                    successMessage,
                    warnMessage,
                    failedMessage,
                    warnList = '',
                    failList = '';

                if (!Ext.isEmpty(obj.success)) {
                    switch (operation) {
                        case 'assign':
                            if (successCount > 0) {
                                var userId = record.get('assignee').userId;
                                var workGroupId = record.get('assignee').workGroupId;

                                if ((userId == -1) && (workGroupId == -1)) {
                                    successMessage = Uni.I18n.translatePlural('alarms.assign.success.result51', successCount, 'DAL',
                                        "There were no alarms",
                                        "Successfully unassigned one alarm",
                                        "Successfully unassigned {0} alarms");
                                } else if ((userId > -1) && (workGroupId == -1)) {
                                    successMessage = (successCount == 0) ? Uni.I18n.translate('alarms.assign.success.result520', 'DAL', "There were no alarms to assigned to {0} user", record.get('assignee').title) :
                                        (successCount == 1) ? Uni.I18n.translate('alarms.assign.success.result521', 'DAL', "Successfully assigned one alarm to {0} user", record.get('assignee').title) :
                                            Uni.I18n.translate('alarms.assign.success.result522', 'DAL', "Successfully assigned {0} alarms to {1} user", successCount, record.get('assignee').title);
                                    successMessage = '\<h3\>' + successMessage + '\</h3\>\<br\>';
                                } else if ((userId == -1) && (workGroupId > -1)) {
                                    successMessage = (successCount == 0) ? Uni.I18n.translate('alarms.assign.success.result530', 'DAL', "There were no alarms to assigned to {0} workgroup", record.get('assignee').workGroupTitle) :
                                        (successCount == 1) ? Uni.I18n.translate('alarms.assign.success.result531', 'DAL', "Successfully assigned one alarm to {0} workgroup", record.get('assignee').workGroupTitle) :
                                            Uni.I18n.translate('alarms.assign.success.result532', 'DAL', "Successfully assigned {0} alarms to {1} workgroup", successCount, record.get('assignee').workGroupTitle);
                                    successMessage = '\<h3\>' + successMessage + '\</h3\>\<br\>';
                                } else if ((userId > -1) && (workGroupId > -1)) {
                                    successMessage = (successCount == 0) ? Uni.I18n.translate('alarms.assign.success.result540', 'DAL', "There were no alarms to assigned to {0} user and {1} workgroup", [record.get('assignee').title, record.get('assignee').workGroupTitle]) :
                                        (successCount == 1) ? Uni.I18n.translate('alarms.assign.success.result541', 'DAL', "Successfully assigned one alarm to {0} user and {1} workgroup", [record.get('assignee').title, record.get('assignee').workGroupTitle]) :
                                            Uni.I18n.translate('alarms.assign.success.result542', 'DAL', "Successfully assigned {0} alarms to {1} user and {2} workgroup", [successCount, record.get('assignee').title, record.get('assignee').workGroupTitle]);
                                    successMessage = '\<h3\>' + successMessage + '\</h3\>\<br\>';
                                }
                            }
                            break;
                        case 'close':
                            if (successCount > 0) {
                                successMessage = Uni.I18n.translatePlural('alarms.close.success.result', successCount, 'DAL', '-', '<h3>Successfully closed alarm</h3><br>', '<h3>Successfully closed alarms</h3><br>');
                            }
                            break;

                    }
                }

                if (!Ext.isEmpty(obj.failure)) {
                    Ext.each(obj.failure, function (fail) {
                        switch (fail.reason) {
                            case 'Alarm doesn\'t exist':
                                warnList += '<h4>' + fail.reason + ':</h4><ul style="list-style: none; padding-left: 1em;">';
                                Ext.each(fail.issues, function (issue) {
                                    warnCount += 1;
                                    warnAlarms.push(issue.id);
                                    warnList += '<li>- <a href="javascript:void(0)">' + issue.title + '</a></li>';
                                });
                                warnList += '</ul>';
                                break;
                            default:
                                failList += '<h4>' + fail.reason + ':</h4><ul style="list-style: none; padding-left: 1em;">';
                                Ext.each(fail.issues, function (issue) {
                                    failedCount += 1;
                                    failedAlarms.push(issue.id);
                                    failList += '<li>- <a href="javascript:void(0)">' + issue.title + '</a></li>';
                                });
                                failList += '</ul>';
                        }
                    });

                    switch (operation) {
                        case 'assign':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('alarms.assign.unable.result', warnCount, 'DAL', '-','<h3>Unable to assign one alarm</h3><br>', '<h3>Unable to assign {0} alarms</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('alarms.assign.failed.result', failedCount, 'DAL', '-', '<h3>Failed to assign one alarm</h3><br>', '<h3>Failed to assign {0} alarms</h3><br>') + failList;
                            }
                            break;
                        case 'close':
                            if (warnCount > 0) {
                                warnMessage = Uni.I18n.translatePlural('alarms.close.unable.result', warnCount, 'DAL', '-','<h3>Unable to close one alarm</h3><br>', '<h3>Unable to close {0} alarms</h3><br>') + warnList;
                            }
                            if (failedCount > 0) {
                                failedMessage = Uni.I18n.translatePlural('alarms.close.failed.result', failedCount, 'DAL', '-', '<h3>Failed to close one alarm</h3><br>', '<h3>Failed to close {0} alarms</h3><br>') + failList;
                            }
                            break;
                    }
                }

                step5panel.removeAll(true);

                if (warnCount > 0) {
                    var warnMessageParams = {
                        type: 'attention',
                        msgBody: [
                            {html: warnMessage}
                        ],
                        closeBtn: false
                    };

                    var warnPanel = Ext.widget('message-panel', warnMessageParams);
                    step5panel.add(warnPanel);
                }

                if (failedCount > 0) {
                    var failedMessageParams = {
                        type: 'error',
                        msgBody: [
                            {html: failedMessage}
                        ],
                        btns: [
                            {text: "Retry", hnd: function () {
                                me.fireEvent('retryRequest', wizard, failedAlarms);
                            }},
                            {text: "Finish", hnd: function () {
                                Ext.History.back();
                            }}
                        ],
                        closeBtn: false
                    };
                    var failedPanel = Ext.widget('message-panel', failedMessageParams);
                    step5panel.add(failedPanel);
                }

                if (successCount > 0) {
                    var successMsgParams = {
                        type: 'success',
                        msgBody: [
                            {html: successMessage}
                        ],
                        closeBtn: false
                    };
                    if ((!isRetry && failedCount == 0) || (isRetry && warnCount == 0)) {
                        successMsgParams.btns = [
                            {
                                itemId: 'btn-finish',
                                text: Uni.I18n.translate('general.finish', 'DAL', 'Finish'), ui: 'action', hnd: function () {
                                step5panel.removeAll(true);
                                Ext.History.back();
                            }}
                        ];
                    }
                    var successPanel = Ext.widget('message-panel', successMsgParams);
                    step5panel.add(successPanel);
                }
                me.getBulkNavigation().moveNextStep();
            },
            failure: function (response) {
                step5panel.removeAll(true);
                Ext.History.back();
            }
        });
    },

    getRequestData: function (bulkStoreRecord) {
        var requestData = {alarms: []},
            operation = bulkStoreRecord.get('operation'),
            alarms = bulkStoreRecord.get('alarms'),
            allAlarms = bulkStoreRecord.get('allAlarms'),
            params = bulkStoreRecord.get('params');

        if (!allAlarms) {
            Ext.iterate(alarms, function (alarm) {
                requestData.alarms.push(
                    {
                        id: alarm.get('id'),
                        version: alarm.get('version')
                    }
                );
            });
            requestData.params = [];
            requestData.allAlarms = false;
        } else {
            requestData.params = params;
            requestData.allAlarms = allAlarms;
            requestData.alarms = [];
        }

        switch (operation) {
            case 'assign':
                requestData.assignee = {
                    userId: bulkStoreRecord.get('assignee').userId,
                    workGroupId: bulkStoreRecord.get('assignee').workGroupId
                };
                break;
            case 'close':
                requestData.status = bulkStoreRecord.get('status');
                break;
        }

        requestData.comment = bulkStoreRecord.get('comment');

        return requestData;
    },

    onWizardCancelledEvent: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/alarms').forward();
    },

    setBulkActionListActiveItem: function (wizard) {
        var index = wizard.getActiveItemId();
    },

    processStep: function (func, wizard) {
        if (func in this) {
            this[func](wizard);
        }
    },

    getBulkRecord: function () {
        var bulkStore = Ext.getStore('Dal.store.BulkChangeAlarms'),
            bulkRecord = bulkStore.getAt(0);

        if (!bulkRecord) {
            bulkStore.add({
                operation: 'assign'
            });
        }

        return bulkStore.getAt(0);
    },

    onStep2RadiogroupChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('operation', newValue.operation);
        record.commit();
    },

    onStep3RadiogroupCloseChangeEvent: function (radiogroup, newValue, oldValue) {
        var record = this.getBulkRecord();
        record.set('status', newValue.status);
        record.set('statusName', radiogroup.getChecked()[0].boxLabel);
        record.commit();
    },

    getDefaultStep2Operation: function () {
        var formPanel = this.getPage().down('alarm-bulk-wizard').down('alarm-bulk-step2').down('panel'),
            default_operation = formPanel.down('radiogroup').getValue().operation,
            record = this.getBulkRecord();
        record.set('operation', default_operation);
        record.commit();
    },

    getDefaultCloseStatus: function () {
        var formPanel = this.getPage().down('alarm-bulk-wizard').down('alarm-bulk-step3').down('issues-close-form'),
            default_status = formPanel.down('radiogroup').getValue().status,
            record = this.getBulkRecord();
        record.set('status', default_status);
        record.commit();
    },

    processNextOnStep1: function (wizard) {
        var record = this.getBulkRecord(),
            grid = wizard.down('alarm-bulk-step1').down('alarms-selection-grid'),
            selection = grid.getSelectionModel().getSelection();

        if (grid.isAllSelected()) {
            var allAlarms = true;
            var params = grid.filterParams;
            record.set('allAlarms', allAlarms);
            record.set('params', params);
            record.set('alarms', []);
        } else {
            record.set('alarms', selection);
            record.set('allAlarms', false);
            record.set('params', []);
        }

        record.commit();
    },

    processNextOnStep2: function (wizard) {
        var record = this.getBulkRecord(),
            step3Panel = wizard.down('alarm-bulk-step3'),
            operation = record.get('operation'),
            view,
            widget;

        switch (operation) {
            case 'assign':
                view = 'assign-issue';
                break;
            case  'close':
                view = 'issues-close-form';
                break;
        }

        widget = Ext.widget(view, {
            labelWidth: 120,
            controlsWidth: 500
        });

        if (operation == 'assign') {
            widget.down('#frm-assign-issue').setTitle('');

            widget.down('#issue-assign-action-apply').setVisible(false);
            widget.down('#issue-assign-action-cancel').setVisible(false);
            widget.down('#cbo-workgroup-issue-assignee').setValue(-1);
            widget.down('#cbo-user-issue-assignee').setValue(-1);
        }

        if (!Ext.isEmpty(widget.items.getAt(1))) {
            widget.items.getAt(1).margin = '0';
        }

        if (widget) {
            Ext.suspendLayouts();
            step3Panel.removeAll(true);
            Ext.resumeLayouts();
            step3Panel.add(widget);
        }
    },

    processNextOnStep3: function (wizard) {
        var record = this.getBulkRecord(),
            step4Panel = wizard.down('alarm-bulk-step4'),
            operation = record.get('operation'),
            formPanel = wizard.down('alarm-bulk-step3').down('form'),
            message, widget;

        switch (operation) {
            case 'assign':
                var userCombo = formPanel.down('#cbo-user-issue-assignee');
                var workGroupCombo = formPanel.down('#cbo-workgroup-issue-assignee');
                var userId = userCombo.getValue();
                var workGroupId = workGroupCombo.getValue();
                record.set('assignee', {
                    userId: userCombo.getValue(),
                    workGroupId: workGroupCombo.getValue(),
                    title: userCombo.rawValue,
                    workGroupTitle: Ext.String.htmlEncode(workGroupCombo.rawValue)
                });
                if (!record.get('allAlarms')) {
                    if ((userId == -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translatePlural('alarms.selectedAlarms.assign.msg1', record.get('alarms').length, 'DAL', '-', '<h3>Unassign one alarm?</h3><br>', '<h3>Unassign {0} alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedIssues.assign.title1', 'DAL', 'The selected alarm(s) will be unassigned');
                    } else if ((userId > -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translatePlural('alarms.selectedAlarms.assign.msg2', record.get('alarms').length, 'DAL', '-', '<h3>Assign one alarm?</h3><br>', '<h3>Assign {0} alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedIssues.assign.title2', 'DAL', 'The selected alarm(s) will be assigned to {0} user', [Ext.String.htmlEncode(userCombo.rawValue)])
                    } else if ((userId == -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translatePlural('alarms.selectedAlarms.assign.msg2', record.get('alarms').length, 'DAL', '-', '<h3>Assign one alarm?</h3><br>', '<h3>Assign {0} alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedIssues.assign.title3', 'DAL', 'The selected alarm(s) will be assigned to {0} workgroup', [Ext.String.htmlEncode(workGroupCombo.rawValue)])
                    } else if ((userId > -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translatePlural('alarms.selectedAlarms.assign.msg2', record.get('alarms').length, 'DAL', '-', '<h3>Assign one alarm?</h3><br>', '<h3>Assign {0} alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedAlarms.assign.title4', 'DAL', 'The selected alarm(s) will be assigned to {0} workgroup and {1} user', [Ext.String.htmlEncode(workGroupCombo.rawValue), Ext.String.htmlEncode(userCombo.rawValue)])
                    }
                } else {
                    if ((userId == -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translate('alarms.selectedAlarms.assign.msg3', 'DAL', '<h3>Unassign all alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedAlarms.assign.title5', 'DAL', 'All alarms will be unassigned');
                    } else if ((userId > -1) && (workGroupId == -1)) {
                        message = Uni.I18n.translate('alarms.selectedAlarms.assign.msg4', 'DAL', '<h3>Assign all alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedAlarms.assign.title6', 'DAL', 'All alarms) will be assigned to {0} user', [Ext.String.htmlEncode(userCombo.rawValue)])
                    } else if ((userId == -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translate('alarms.selectedAlarms.assign.msg4', 'DAL', '<h3>Assign all alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedAlarms.assign.title7', 'DAL', 'All alarms will be assigned to {0} workgroup', [Ext.String.htmlEncode(workGroupCombo.rawValue)])
                    } else if ((userId > -1) && (workGroupId > -1)) {
                        message = Uni.I18n.translate('alarms.selectedAlarms.assign.msg4', 'DAL', '<h3>Assign all alarms?</h3><br>')
                            + Uni.I18n.translate('alarms.selectedAlarms.assign.title8', 'DAL', 'All alarms will be assigned to {0} workgroup and {1} user', [Ext.String.htmlEncode(workGroupCombo.rawValue), Ext.String.htmlEncode(userCombo.rawValue)])
                    }
                }
                break;

            case 'close':
                if (!record.get('allAlarms')) {
                    message = Uni.I18n.translatePlural('alarms.selectedAlarms.close.withCount', record.get('alarms').length, 'DAL', '-', '<h3>Close one alarm?</h3><br>', '<h3>Close {0} alarms?</h3><br>')
                        + Uni.I18n.translate('alarms.selectedAlarms.willBeClosed','DAL', 'The selected alarm(s) will be closed with status "<b>{0}</b>"', [record.get('statusName')]);
                } else {
                    message = Uni.I18n.translate('alarms.allAlarms.willBeClosed.title', 'DAL', '<h3>Close all alarms?</h3><br>')
                        + Uni.I18n.translate('alarms.allAlarms.willBeClosed', 'DAL', 'All alarms will be closed with status "<b>{0}</b>"',[record.get('statusName')]);
                }
                break;

        }

        if (formPanel) {
            record.set('comment', formPanel.down('textarea').getValue().trim());
        }

        widget = Ext.widget('container', {
            style: 'margin: 20px 0',
            html: message
        });
        Ext.suspendLayouts();
        step4Panel.removeAll(true);
        Ext.resumeLayouts();
        step4Panel.add(widget);
    },

    beforeStep4: function () {
        return true;
    }
});