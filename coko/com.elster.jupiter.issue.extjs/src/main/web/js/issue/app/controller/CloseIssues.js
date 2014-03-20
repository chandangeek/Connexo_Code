Ext.define('Isu.controller.CloseIssues', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'workspace.issues.Close'
    ],

    init: function () {
        this.control({
            'issues-close issues-close-form': {
                beforerender: this.issueClosingFormBeforeRenderEvent
            },
            'issues-close button[name=close]': {
                click: this.submitIssueClosing
            },
            'issues-close breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            }
        });
        this.getApplication().on('closeissue', this.submitIssueClosing)
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Workspace',
                href: '#/workspace'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issues',
                href: 'issues'
            }),
            breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Close Issue',
                href: 'closeissue'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    issueClosingFormBeforeRenderEvent: function (form) {
        var statusesContainer = form.down('[name=status]');
        Ext.Ajax.request({
            url: '/api/isu/statuses',
            method: 'GET',
            success: function (response) {
                var statuses = Ext.decode(response.responseText).data;
                Ext.each(statuses, function (status) {
                    if (!Ext.isEmpty(status.allowForClosing) && status.allowForClosing) {
                        statusesContainer.add({
                            boxLabel: status.name,
                            inputValue: status.name,
                            name: 'status'
                        })
                    }
                });
                statusesContainer.items.items[0].setValue(true);
            }
        });
    },

    submitIssueClosing: function () {
        var self = this,
            closeView = Ext.ComponentQuery.query('issues-close')[0],
            formPanel = closeView.down('issues-close-form'),
            form = formPanel.getForm(),
            formValues = form.getValues(),
            url = '/api/isu/issue/close',
            sendingData = {},
            preloader;

        if (form.isValid()) {
            sendingData.issues = [
                {
                    id: closeView.record.data.id,
                    version: closeView.record.data.version
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
                    var result = Ext.decode(response.responseText);
                    var header = {
                        style: 'msgHeaderStyle'
                    };
                    if (Ext.isEmpty(result.failure)) {
                        window.location.href = '#/workspace/datacollection/issues';
                        header.text = 'Issue closed';
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'notify',
                            msgBody: [header],
                            y: 10,
                            showTime: 5000
                        });
                    } else {
                        var msges = [],
                            bodyItem = {};
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
                                    // this function is necessary and MUST be empty
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
                callback: function () {
                    preloader.destroy();
                }
            });
        }
    }
});