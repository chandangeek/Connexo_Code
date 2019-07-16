/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.ProcessPreviewForm', {
    extend: 'Ext.form.Panel',
    xtype: 'process-preview-form',

    border: false,
    itemId: 'processPreviewForm',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 150
    },
    router: null,

    initComponent: function () {
           var me = this;

           me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'column',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        fieldLabel: "Column 1",
                        itemId: 'all-process-detail-column-1',

                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150
                        },

                        items: [
                        {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.device', 'IMT', 'Device'),
                                itemId: 'deviceName',
                                hidden: true,
                            },
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.alarm', 'IMT', 'Alarm'),
                                itemId: 'alarmName',
                                hidden: true,
                            },
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.issue', 'IMT', 'Issue'),
                                itemId: 'issueName',
                                hidden: true,
                            },
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.usagepoint', 'IMT', 'UsagePoint'),
                                itemId: 'usagePointName',
                                hidden: true,
                                renderer: function (value) {
                                    var result = '';
                                    if (value && Imt.privileges.UsagePoint.canView()){
                                        var url = me.router.getRoute('usagepoints/view').buildUrl({usagePointId: value});
                                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                    } else if (value){
                                        result = value;
                                    }

                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.device', 'IMT', 'Device'),
                                itemId: 'deviceName',
                                hidden: true,
                                renderer: function (value) {
                                    var result = '';
                                    if (value && Imt.privileges.UsagePoint.canView()){
                                        var url = me.router.getRoute('usagepoints/device').buildUrl({deviceId: value});
                                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                    } else if (value){
                                        result = value;
                                    }

                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.alarm', 'IMT', 'Alarm'),
                                itemId: 'alarmName',
                                hidden: true,
                                renderer: function (value) {
                                    var result = '';
                                    if(value){
                                        result = value.get('objectName');
                                    }

                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.issue', 'IMT', 'Issue'),
                                itemId: 'issueName',
                                hidden: true,
                                renderer: function (value) {
                                    var result = '';
                                    if (value && Isu.privileges.Issue.canViewAdminDevice()) {
                                        var url = me.router.getRoute('workspace/issues/view').buildUrl({issueId: value.get('value')}, {issueType: value.get('issueType')});
                                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.get('objectName')) + '</a>';
                                    } else if (value){
                                        result = value.get('objectName');
                                    }

                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'startDateDisplay',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.startdate', 'IMT', 'Start date'),
                                itemId: 'startDateDisplay'
                            },
                            {
                                xtype: 'displayfield',
                                name: 'endDateDisplay',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.enddate', 'IMT', 'End date'),
                                itemId: 'endDateDisplay'
                            },
                            {
                                xtype: 'displayfield',
                                name: 'deviceForAlarm',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.device', 'IMT', 'Device'),
                                itemId: 'deviceForAlarm',
                                hidden: true,
                                renderer: function (value) {
                                    var result = '';
                                    if (value && value != "-" && Imt.privileges.UsagePoint.canView()) {
                                        var url = me.router.getRoute('usagepoints/device').buildUrl({deviceId: value});
                                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                    } else if (value){
                                        result = value;
                                    }
                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'deviceForIssue',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.device', 'IMT', 'Device'),
                                itemId: 'deviceForIssue',
                                hidden: true,
                                renderer: function (value) {
                                    var result = '';
                                    if (value && value != "-" && Imt.privileges.UsagePoint.canView()) {
                                        var url = me.router.getRoute('usagepoints/device').buildUrl({deviceId: value});
                                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                    } else if (value){
                                        result = value;
                                    }

                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.usertask', 'IMT', 'User task'),
                                name: 'openTasks',
                                itemId: 'preview-running-process-open-tasks-all-processes',
                                renderer: function (value, field) {
                                    return value;
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        fieldLabel: "Column 2",
                        itemId: 'all-process-detail-column-2',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'statusDisplay',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.status', 'IMT', 'Status'),
                                itemId: 'statusDisplay',
                            },
                            {
                                xtype: 'displayfield',
                                name: 'startedBy',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.startedby', 'IMT', 'Started by'),
                                itemId: 'startedBy'
                            },
                            {
                                xtype: 'displayfield',
                                name: 'version',
                                fieldLabel: Uni.I18n.translate('imt.processpreviewform.version', 'IMT', 'Version'),
                                itemId: 'version'
                            }
                        ]
                    }
                ]
            }
        ];
       me.callParent(arguments);
    }
});