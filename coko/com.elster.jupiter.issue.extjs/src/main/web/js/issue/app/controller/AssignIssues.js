Ext.define('Isu.controller.AssignIssues', {
        extend: 'Ext.app.Controller',

        requires: [
            'Uni.model.BreadcrumbItem'
        ],

        stores: [
            'Isu.store.UserList',
            'Isu.store.UserRoleList',
            'Isu.store.UserGroupList'
        ],

        views: [
            'workspace.issues.Assign'
        ],

        refs: [
            {
                ref: 'itemPanel',
                selector: 'issues-item'
            }
        ],

        init: function () {
            this.control({
                'issues-assign button[name=assign]': {
                    click: this.onSubmitForm
                },
                'issues-assign breadcrumbTrail': {
                    afterrender: this.setBreadcrumb
                }
            });
            this.getApplication().on('assignissue', this.onSubmitForm)
        },

        showOverview: function (issueId) {
            var self = this,
                model = self.getModel('Issues'),
                widget;

            model.load(issueId, {
                success: function (record) {
                    widget = Ext.widget('issues-assign', {
                        record: record
                    });
                    self.getApplication().fireEvent('changecontentevent', widget);
                }
            });
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
                    text: 'Assign Issue',
                    href: 'assign'
                });
            breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);

            breadcrumbs.setBreadcrumbItem(breadcrumbParent);
        },


        onSubmitForm: function () {
            var self = this,
                assignPanel = Ext.ComponentQuery.query('issues-assign')[0],
                formPanel = assignPanel.down('issues-assign-form'),
                activeCombo = formPanel.down('combobox[allowBlank=false]'),
                form = formPanel.getForm(),
                formValues = form.getValues(),
                url = '/api/isu/issue/assign',
                sendingData = {},
                preloader;

            if (form.isValid()) {
                sendingData.issues = [
                    {
                        id: assignPanel.record.data.id,
                        version: assignPanel.record.data.version
                    }
                ];
                sendingData.assignee = {
                    id: activeCombo.findRecordByValue(activeCombo.getValue()).data.id,
                    type: activeCombo.name,
                    title: activeCombo.rawValue
                };
                sendingData.comment = formValues.comment;
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Assigning issue",
                    name: 'assign-issu-form-submit',
                    target: assignPanel
                });
                preloader.show();
                Ext.Ajax.request({
                    url: url,
                    method: 'PUT',
                    jsonData: sendingData,
                    autoAbort: true,
                    success: function (resp) {
                        var response = Ext.JSON.decode(resp.responseText),
                            successArr = response.data.success,
                            failureArr = response.data.failure,
                            activeCombo = assignPanel.down('issues-assign-form combobox[allowBlank=false]'),
                            msges = [];
                        if (failureArr.length > 0) {
                            Ext.Array.each(failureArr, function (item) {
                                Ext.Array.each(item.issues, function (issue) {
                                    var header = {},
                                        bodyItem = {};
                                    header.text = 'Failed to assign issue ' + assignPanel.record.data.reason + (assignPanel.record.data.device ? ' to ' + assignPanel.record.data.device.name + ' ' + assignPanel.record.data.device.serialNumber : '') + ' to ' + activeCombo.rawValue;
                                    header.style = 'msgHeaderStyle';
                                    msges.push(header);
                                    bodyItem.text = item.reason;
                                    bodyItem.style = 'msgItemStyle';
                                    msges.push(bodyItem);
                                })
                            });

                            if (msges.length > 0) {
                                self.getApplication().fireEvent('isushowmsg', {
                                    type: 'error',
                                    closeBtn: true,
                                    msgBody: msges,
                                    y: 10,
                                    btns: [
                                        {
                                            text: 'Retry',
                                            hnd: function () {
                                                assignPanel.enable();
                                                self.getApplication().fireEvent('assignissue')
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
                                                assignPanel.enable();
                                            }
                                        }
                                    }
                                });
                                assignPanel.disable();
                            }
                        }

                        msges = [];

                        if (successArr.length > 0) {
                            Ext.Array.each(successArr, function () {
                                var header = {};
                                header.style = 'msgHeaderStyle';
                                header.text = 'Issue assigned to ' + activeCombo.rawValue;
                                msges.push(header);
                            });

                            if (msges.length > 0) {
                                self.getApplication().fireEvent('isushowmsg', {
                                    type: 'notify',
                                    msgBody: msges,
                                    y: 10,
                                    showTime: 5000
                                });
                            }
                            window.location.href = '#/workspace/datacollection/issues';
                        }
                    },
                    callback: function () {
                        preloader.destroy();
                    }
                });
            }
        }
    }
);