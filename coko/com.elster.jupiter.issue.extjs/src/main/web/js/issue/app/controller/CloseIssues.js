Ext.define('Mtr.controller.CloseIssues', {
    extend: 'Ext.app.Controller',

    views: [
        'workspace.issues.Close'
    ],

    init: function () {
        this.control({
            'issues-close button[name=close]': {
                click: this.submitIssueClosing
            },
            'issues-close button[name=cancel]': {
                click: this.cancelIssueClosing
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

    submitIssueClosing: function (button) {
        var self = this,
            closeView = Ext.ComponentQuery.query('issues-close')[0],
            formPanel = closeView.down('form'),
            form = formPanel.getForm(),
            formValues = form.getValues(),
            url = '/api/isu/issue/close',
            preloader;
        formPanel.sendingData.status = formValues.status;
        formPanel.sendingData.comment = formValues.comment.trim();

        if (form.isValid()) {
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Closing issue",
                name: 'assign-issu-form-submit',
                target: formPanel
            });
            preloader.show();

            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: formPanel.sendingData,
                success: function (response) {
                    var result = Ext.decode(response.responseText);
                    var header = {
                        style: 'msgHeaderStyle'
                    };
                    if (Ext.isEmpty(result.failure)) {
                        Ext.History.back();
                        header.text = 'Issue closed';
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'notify',
                            msgBody: [header],
                            y: 10,
                            showTime: 5000
                        });
                    } else {
                        var msges = [];
                        var bodyItem = {};
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
                                        self.getApplication().fireEvent('closeissue')
                                    }
                                },
                                {
                                    text: 'Cancel',
                                    cls: 'isu-btn-link',
                                    // this function is necessary and MUST be empty
                                    hnd: function () {

                                    }
                                }
                            ]
                        });
                    }
                },
                callback: function () {
                    preloader.destroy();
                }
            });
        }
    },

    cancelIssueClosing: function () {
        Ext.History.back();
    }
});