Ext.define('Isu.controller.AssignIssues', {
        extend: 'Ext.app.Controller',

        requires: [
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
                    widget = Ext.widget('issues-assign');
                    widget.getCenterContainer().down('issues-assign-form').loadRecord(record);
                    self.getApplication().fireEvent('changecontentevent', widget);
                }
            });
        },

        onSubmitForm: function () {
            var self = this,
                assignPanel = Ext.ComponentQuery.query('issues-assign')[0],
                formPanel = assignPanel.down('issues-assign-form'),
                activeCombo = formPanel.down('combobox[allowBlank=false]'),
                form = formPanel.getForm(),
                record = formPanel.getRecord(),
                formValues = form.getValues(),
                url = '/api/isu/issue/assign',
                sendingData = {},
                preloader;


            if (form.isValid()) {
                sendingData.issues = [
                    {
                        id: record.getId(),
                        version: record.get('version')
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
                                    header.text = 'Failed to assign issue ' + record.data.reason_name + (record.data.device_name ? ' to ' + record.data.device_name + ' ' + record.raw.device.serialNumber : '') + ' to ' + activeCombo.rawValue;
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
                                self.getApplication().fireEvent('acknowledge', 'Issue assigned to ' + activeCombo.rawValue);
                            });
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