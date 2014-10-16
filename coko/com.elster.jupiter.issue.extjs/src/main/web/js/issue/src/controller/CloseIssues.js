Ext.define('Isu.controller.CloseIssues', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'workspace.issues.Close'
    ],

    init: function () {
        this.control({
            'issues-close issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            },
            'bulk-browse bulk-wizard bulk-step3 issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            },
            'issues-close button[name=close]': {
                click: this.submitIssueClosing
            },
            'message-window': {
                remove: this.enableButtons
            }
        });
        this.getApplication().on('closeissue', this.submitIssueClosing)
    },

    showOverview: function (issueId) {
        var self = this,
            model = self.getModel('Issues'),
            widget;

        model.load(issueId, {
            success: function (record) {
                widget = Ext.widget('issues-close', {
                    record: record
                });
                self.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    enableButtons: function () {
        var  formButtons = Ext.ComponentQuery.query('issues-close button');

        Ext.each(formButtons, function (button) {
            button.enable();
        });

    },

    issueClosingFormBeforeRenderEvent: function (form) {
        var statusesContainer = form.down('[name=status]'),
            values = Ext.state.Manager.get('formCloseValues');
        Ext.Ajax.request({
            url: '/api/isu/statuses',
            method: 'GET',
            success: function (response) {
                var statuses = Ext.decode(response.responseText).data;
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

    submitIssueClosing: function () {
        var self = this,
            closeView = Ext.ComponentQuery.query('issues-close')[0],
            record = closeView.record,
            formPanel = closeView.down('issues-close-form'),
            form = formPanel.getForm(),
            formValues = form.getValues(),
            url = '/api/idc/issue/close',
            sendingData = {},
            preloader;

        if (form.isValid()) {
            sendingData.issues = [
                {
                    id: record.data.id,
                    version: record.data.version
                }
            ];
            sendingData.status = formValues.status;
            sendingData.comment = formValues.comment.trim();
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Closing issue",
                name: 'close-issue-form-submit',
                target: closeView
            });
            preloader.show();

            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: sendingData,
                success: function (response) {
                    var result = Ext.decode(response.responseText).data;
                    var header = {
                        style: 'msgHeaderStyle'
                    };
                    if (Ext.isEmpty(result.failure)) {
                        window.location.href = '#/workspace/datacollection/issues';
                        self.getApplication().fireEvent('acknowledge', 'Issue closed');
                    } else {
                        var msges = [],
                            bodyItem = {},
                            formButtons = Ext.ComponentQuery.query('issues-close button');

                        Ext.each(formButtons, function (button) {
                            button.disable();
                        });

                        header.text = 'Failed to close issue ' + formPanel.recordTitle;
                        msges.push(header);
                        bodyItem.text = result.failure[0].reason;
                        bodyItem.style = 'msgItemStyle';
                        msges.push(bodyItem);
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'error',
                            msgBody: msges,
                            y: 10,
                            closeBtn: true,
                            btns: [
                                {
                                    text: 'Retry',
                                    hnd: function () {
                                        formPanel.enable();
                                        self.getApplication().fireEvent('closeissue');
                                    }
                                },
                                {
                                    text: 'Cancel',
                                    cls: 'isu-btn-link',
                                    hrefTarget: '',
                                    href: '#/workspace/datacollection/issues',
                                    hnd: function () {
                                    }
                                }
                            ],
                            listeners: {
                                close: {
                                    fn: function () {
                                        formPanel.enable();
                                    }
                                }
                            }
                        });
                        formPanel.disable();
                    }
                },
                failure: function (response) {
                    var result;
                    if (response != null) {
                        result = Ext.decode(response.responseText, true);
                    }
                    if (result !== null) {
                        Ext.widget('messagebox', {
                            buttons: [
                                {
                                    text: 'Close',
                                    action: 'cancel',
                                    handler: function(btn){
                                        btn.up('messagebox').hide()
                                    }
                                }
                            ]
                        }).show({
                            ui: 'notification-error',
                            title: result.error,
                            msg: result.message,
                            icon: Ext.MessageBox.ERROR
                        })

                    } else {
                        Ext.widget('messagebox', {
                            buttons: [
                                {
                                    text: 'Close',
                                    action: 'cancel',
                                    handler: function(btn){
                                        btn.up('messagebox').hide()
                                    }
                                }
                            ]
                        }).show({
                            ui: 'notification-error',
                            title: 'Failed to close issue: ' + record.data.title,
                            msg: 'Issue already closed',
                            icon: Ext.MessageBox.ERROR
                        })
                    }
                },

                callback: function () {
                    preloader.destroy();
                }
            });
        }
    }
});