/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.widget.WhatsGoingOn', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.whatsgoingon',
    requires: [
        'Uni.store.HealthCategories',
        'Uni.store.WhatsGoingOn'
    ],
    overflowY: 'auto',
    itemId: 'whatsgoingon',
    title: Uni.I18n.translate('deviceGeneralInformation.whatsGoingOn', 'UNI', 'What\'s going on'),
    ui: 'tile',
    router: null,
    layout: 'fit',


    initComponent: function () {
        var me = this;
        me.pageToLoad=1;
        me.mustLoadData = true;
        me.items = {
            xtype: 'tabpanel',
            tabPosition: 'bottom',
            tabBar: {
                layout: {pack: 'center'}
            },
            defaults: {
                listeners: {
                    beforerender: function(tab){
                        var tabPanel = me.down('tabpanel');
                        var nrOfTabs = tabPanel.items.length;
                        var nrOfItemsOnTab = tab.items.items[0].items.length + tab.items.items[1].items.length;
                        if(me.mustLoadData && nrOfTabs > 1 && tab === tabPanel.items.items[nrOfTabs-1] && nrOfItemsOnTab < 10){
                            me.pageToLoad+=1;
                            me.buildWidget();
                        }
                        me.mustLoadData = true;
                    },
                    activate: function (tab, eOpts) {
                        tab.setIconCls('icon-circle');
                    },
                    deactivate: function (tab, eOpts) {
                        tab.setIconCls('icon-circle2');
                    }
                }
            }
        };
        me.store = 'Uni.store.WhatsGoingOn';

        var healthTypeStore = Ext.create('Uni.store.HealthCategories', {
            filters : function(item){
                if(item.data.type == 'issue'){
                    return me.type == 'device'&& Isu.privileges.Issue.canViewAdminDevice();
                }
                if(item.data.type == 'alarm'){
                    return me.type == 'device'&& Dal.privileges.Alarm.canViewAdmimAlarm();
                }
                if(item.data.type == 'process'){
                    return Bpm.privileges.BpmManagement.canViewProcesses();
                }

                if(item.data.type == 'servicecall') {
                    return Scs.privileges.ServiceCall.canView();
                }
                return true;
            }
        });

        if(healthTypeStore.data.items.length <= 1){
            me.setVisible(false);
            me.callParent(arguments);
            return;
        }

        me.tools = [
            {
                xtype: 'toolbar',
                itemId: 'comboTool',
                margin: '0 20 0 0',
                layout: 'fit',
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'uni-whatsgoingon-combo',
                        hidden: healthTypeStore.data.items.length <= 2,
                        value: 'all',
                        store: healthTypeStore,
                        displayField: 'displayValue',
                        cls: 'uni-cb-item',
                        valueField: 'type',
                        listeners: {
                            change: function (combo, newvalue) {
                                me.mustLoadData = false;
                                me.down('tabpanel').removeAll();
                                me.pageToLoad=1;
                                me.buildWidget(newvalue);
                            }
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
        if(this.autoBuild){
            me.buildWidget();
        }
    },

    buildWidget: function (type) {
        var me = this;
        me.setLoading(true);
        var clearTabPannel = false;
        if (Ext.isEmpty(type) && !Ext.isEmpty(me.down('#uni-whatsgoingon-combo'))) {
            type = me.down('#uni-whatsgoingon-combo').getValue();
        }else{
            clearTabPannel = true;
        }
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        if (this.type === 'device') {
            me.store.setProxy({
                type: 'rest',
                url: '/api/ddr/devices/'+ encodeURIComponent(this.deviceId) +'/whatsgoingon',
                timeout: 120000,
                startParam: 'start',
                limitParam: 'limit',
                reader: {
                    type: 'json',
                    root: 'goingsOn'
                }
            });
        } else if (this.type === 'usagepoint') {
            me.store.setProxy({
                type: 'rest',
                url: '/api/udr/usagepoints/'+ encodeURIComponent(this.usagePointId) +'/whatsgoingon',
                timeout: 120000,
                startParam: 'start',
                limitParam: 'limit',
                reader: {
                    type: 'json',
                    root: 'goingsOn'
                }
            });
        }
        me.store.load({
            page:me.pageToLoad,
            limit:10,
            callback: function(){
                me.setLoading(false);
                me.store.clearFilter();
                if (me.down('tabpanel')) {
                    //me.down('tabpanel').removeAll();
                } else {
                    return;
                }
                if (type !== 'all' && type !== '' && type !== undefined) {
                    me.store.filter([
                        {
                            filterFn: function (item) {
                                return item.get('type') === type;
                            }
                        }
                    ]);
                }
                var tabContents = [];
                var tabPanel = me.down('tabpanel');
                var nrOfTabs = tabPanel.items.length;
                var itemsPerTab = 10;
                var lines = [];
                var emptyText;
                Ext.suspendLayouts();

                me.store.each(function (item, index, total) {
                     var size = itemsPerTab;
                     if(tabContents.length === 0 && nrOfTabs > 1){
                        var nrOfItemsOnLastTab = tabPanel.items.items[nrOfTabs-1].items.items[0].items.length + tabPanel.items.items[nrOfTabs-1].items.items[1].items.length;
                        size = itemsPerTab - nrOfItemsOnLastTab;
                     }
                     me.addLine(lines, item);
                     if(lines.length === size){
                        tabContents.push(lines);
                        lines = [];
                     }

                });
                if (lines.length !== 0) {
                    tabContents.push(lines);
                }
                if (tabContents.length===0 && nrOfTabs === 0){
                    switch (type) {
                        case 'issue':
                            emptyText = Uni.I18n.translate('whatsGoingOn.nothingToShowIssues', 'UNI', 'No active issues to show');
                            break;
                        case 'servicecall':
                            emptyText = Uni.I18n.translate('whatsGoingOn.nothingToShowServiceCalls', 'UNI', 'No active service calls to show');
                            break;
                        case 'alarm':
                            emptyText = Uni.I18n.translate('whatsGoingOn.nothingToShowAlarms', 'UNI', 'No active alarms to show');
                            break;
                        case 'process':
                            emptyText = Uni.I18n.translate('whatsGoingOn.nothingToShowProcesses', 'UNI', 'No active processes to show');
                            break;
                        default:
                            if(me.type == 'device') {
                                emptyText = Uni.I18n.translate('whatsGoingOn.nothingToShow', 'UNI', 'No active alarms, issues, processes or service calls to show');
                            }else{
                                emptyText = Uni.I18n.translate('whatsGoingOn.nothingToShowUP', 'UNI', 'No active processes to show');
                            }
                            break;

                    }
                    me.down('tabpanel').add({
                        layout: 'hbox',
                        margin: '38 0 0 0',
                        tabConfig: {
                            ui: 'default',
                            cls: 'x-tab-default-disabled'
                        },
                        items: [
                            {
                                flex: 1
                            },
                            {
                                html: '<span style="color:#686868;font-size:20px;">' +
                                '<i class="icon-info" style="color:#686868;margin-right:15px;"></i>' +
                                    emptyText +
                                    '</span>'
                            },
                            {
                                flex: 1
                            }
                        ]

                    });
                } else if(tabContents.length > 0){
                    me.mustLoadData = true;
                    var nrOfItemsOnLastTab = tabContents[tabContents.length-1].length;
                    if(nrOfItemsOnLastTab === 10){
                        tabContents.push([]);
                    }
                    if(nrOfTabs === 0 &&tabContents.length===1){
                        me.down('tabpanel').add({
                            tabConfig: {
                                ui: 'default',
                                cls: 'x-tab-default-disabled'
                            },
                            layout: 'column',

                            items: [{
                                columnWidth: 0.50,
                                items: tabContents[0].splice(0, 5)
                            }, {
                                columnWidth: 0.50,
                                items: tabContents[0]
                            }]
                        });
                    } else {
                        Ext.each(tabContents, function (tabContent, index) {
                            var tabPanel = me.down('tabpanel');
                            var nrOfTabs = tabPanel.items.length;
                            var tabElements = [];
                            var appended = false;
                            if(nrOfTabs > 1 &&  (tabPanel.items.items[nrOfTabs-1].items.items[0].items.items.length + tabPanel.items.items[nrOfTabs-1].items.items[1].items.items.length) < 10){
                                Ext.each(tabPanel.items.items[nrOfTabs-1].items.items, function(column){
                                    var itms = [];
                                    Ext.each(column.items.items, function(itm){
                                        me.addLine(itms, me.prepareItem(itm));
                                    })
                                    tabElements = tabElements.concat(itms);
                                });
                                tabPanel.remove(tabPanel.items.items[nrOfTabs-1]);
                                appended = true;
                                if(tabElements.length === 0 && tabContent.length > 0){
                                 me.mustLoadData = false;
                                }
                            }
                            tabElements = tabElements.concat(tabContent);
                            tabPanel.add({
                                layout: 'column',
                                iconCls: (index === 0 && nrOfTabs === 0)? 'icon-circle' : 'icon-circle2',
                                items: [{
                                    columnWidth: 0.50,
                                    items: tabElements.splice(0, 5)
                                }, {
                                    columnWidth: 0.50,
                                    items: tabElements
                                }]
                            });
                            if(appended){
                                tabPanel = me.down('tabpanel');
                                nrOfTabs = tabPanel.items.length;
                                tabPanel.setActiveTab(nrOfTabs-1);
                            }
                        });

                    }
                }else{
                    var tabPanel = me.down('tabpanel');
                    var nrOfTabs = tabPanel.items.length;
                    tabPanel.remove(tabPanel.items.items[nrOfTabs-1]);
                    nrOfTabs = tabPanel.items.length;
                    tabPanel.setActiveTab(nrOfTabs-1);
                }
                Ext.resumeLayouts();
                me.doLayout();
            }
        });
    },
    prepareItem: function(item){
        var result = {
            data:{},
            status:'',
            get: function(value){
               return this.data[value];
            }
        };
        result.data.displayValue = item.value;
        result.data.status = item.value.status;
        return result;
    },

    addLine: function (lines, item) {
        var me = this;
        lines.push({
            xtype: 'displayfield',
            labelField: ' ',
            value: item.get('displayValue'),
            renderer: function (value) {
                var fillColor, borderColor, textColor;
                value.status = item.get('status');
                if (value.severity === undefined && value.userAssignee === undefined) {
                    fillColor = "#FFFFFF";
                    borderColor = "#1E7D9E";
                    textColor = "#686868";
                }
                else if (value.severity === undefined && value.userAssignee !== undefined) {
                    switch (value.userAssigneeIsCurrentUser) {
                        case true:
                            fillColor = "#1E7D9E";
                            borderColor = "#1E7D9E";
                            textColor = "#686868";
                            break;
                        case false:
                            fillColor = "#A0A0A0";
                            borderColor = "#A0A0A0";
                            textColor = "#A0A0A0";
                            break;
                    }
                } else if (value.severity !== undefined && value.userAssignee === undefined) {
                    switch (value.severity) {
                        case 'HIGH':
                            fillColor = '#FFFFFF';
                            borderColor = '#EB5642';
                            textColor = '#EB5642';
                            break;
                        case 'WARNING':
                            fillColor = '#FFFFFF';
                            borderColor = '#FB9F76';
                            textColor = '#FB9F76';
                            break;
                    }
                } else if (value.severity !== undefined && value.userAssignee !== undefined) {
                    switch (value.userAssigneeIsCurrentUser) {
                        case true:
                            switch (value.severity) {
                                case 'HIGH':
                                    fillColor = '#EB5642';
                                    borderColor = '#EB5642';
                                    textColor = '#EB5642';
                                    break;
                                case 'WARNING':
                                    fillColor = '#FB9F76';
                                    borderColor = '#FB9F76';
                                    textColor = '#FB9F76';
                                    break;
                            }
                            break;
                        case false:
                            switch (value.severity) {
                                case 'HIGH':
                                    fillColor = '#A0A0A0';
                                    borderColor = '#EB5642';
                                    textColor = '#EB5642';
                                    break;
                                case 'WARNING':
                                    fillColor = '#A0A0A0';
                                    borderColor = '#FB9F76';
                                    textColor = '#FB9F76';
                                    break;
                            }
                            break;
                    }
                }
                return me.getHtml(fillColor, borderColor, value.type.charAt(0).toUpperCase(), '#686868', textColor, value);
            }
        });
    },

    getHtml: function (fillColor, borderColor, iconText, iconTextColor, textColor, value) {
        var html = '<span style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">'
            +'<span class="stacked-icon-container" style="cursor: pointer;">'
            + '<i class="' + 'icon-circle2' + ' stacked-icon" style="color:' + fillColor + ';"></i>'
            + '<i class="' + 'icon-circle' + ' stacked-icon" style="color:' + borderColor + ';"></i>'
            + '<span class="stacked-text" style="color:' + iconTextColor + '"'
            + 'data-qtip="' + this.createToolTip(value, borderColor) + '"'
            + '>'
            + iconText + '</span>'
            + '</span>';
        html += this.createLink(textColor, value);
        html += '<span>';
        return html;
    },

    getTaskInfoLink : function(textColor, userTaskInfo){
        var taskName = userTaskInfo.name.length > 0 ? userTaskInfo.name :
            Uni.I18n.translate('bpm.process.noTaskName', 'UNI', 'No task name');

        var href = this.router.getRoute('workspace/tasks/task').buildUrl({taskId: userTaskInfo.id});
        var html = '<a class="a-underline" title="'+Ext.String.htmlEncode(taskName)+'" style="text-overflow: ellipsis; color:' + textColor + ';" href="' + href + '">'
                + Ext.util.Format.ellipsis(Ext.String.htmlEncode(taskName), 30, true)
                + '</a>';

        return html;

    },
    createLink: function (textColor, value) {
        var me = this, href, html;

        switch (value.type) {
            case 'issue':
                href = this.router.getRoute('workspace/issues/view').buildUrl({issueId: value.id.replace(/\D/g,'')}, {issueType: value.issueType, meter: this.deviceId});
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
                break;
            case 'servicecall':
                href = this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id});
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.reference + ' (' + value.description + ')';
                break;
            case 'alarm':
                href = this.router.getRoute('workspace/alarms/view').buildUrl({alarmId: value.id.replace(/\D/g,'')}, {meter: this.deviceId});
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
                break;
            case 'process':
                if(me.type == 'usagepoint'){
                    href = this.router.getRoute('usagepoints/view').buildUrl({usagePointId: this.usagePointId}) + '/processes?activeTab=running';
                    html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
                } else  if (me.type == 'device'){
                    href = this.router.getRoute('devices/device').buildUrl({deviceId: this.deviceId}) + '/processes?activeTab=running';
                    html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
                }
                break;
        }
        html += !!value.dueDate ? ' ' + Uni.I18n.translate('whatsGoingOn.due', 'UNI', '(due {0})', Uni.DateTime.formatDateShort(new Date(value.dueDate))) : '';
        html += '</a>';

        if(value.type === 'process' && value.userTaskInfo){
            html += ' - ' + me.getTaskInfoLink(textColor, value.userTaskInfo);
        }

        return html;
    },

    createToolTip: function (value, color) {
        var result = '<span style="color:' + color + ';font-size:16px;line-height:24px;">';
        switch (value.type) {
            case 'issue':
                result += Uni.I18n.translate('whatsGoingOn.issue', 'UNI', 'Issue') + ' ID: ';
                break;
            case 'servicecall':
                result += Uni.I18n.translate('whatsGoingOn.serviceCall', 'UNI', 'Service call') + ' ID: ';
                break;
            case 'alarm':
                result += Uni.I18n.translate('whatsGoingOn.alarm', 'UNI', 'Alarm') + ' ID: ';
                break;
            case 'process':
                result += Uni.I18n.translate('whatsGoingOn.process', 'UNI', 'Process') + ' ID: ';
                break;
        }
        result += !!value.id ? value.id + "</span><br>" : '';

        result = this.addContentToTooltip(result, value);
        result = this.addIssueTypeToTooltip(result, value);
        result = this.addReasonToTooltip(result, value);
        result = this.addDueDateToTooltip(value, result);
        result = this.addUserAssigneeToTooltip(result, value);
        result = this.addWorkGroupAssigneeToTooltip(result, value);

        return Ext.String.htmlEncode(result);
    },

    addContentToTooltip: function (result, value) {
        result += !!value.status ? Uni.I18n.translate('whatsGoingOn.status', 'UNI', 'Status: {0}', value.status) + "<br>" : '';
        return result;
    },

    addIssueTypeToTooltip: function (result, value) {
        result += !!value.issueType ? Uni.I18n.translate('whatsGoingOn.issueType', 'UNI', 'Issue type: {0}', value.issueType)+ "<br>" : '<br>';
        return result;
    },


    addReasonToTooltip: function (result, value) {
        result += !!value.reason ? Uni.I18n.translate('whatsGoingOn.reason', 'UNI', 'Reason: {0}', value.reason)+ "<br>" : '<br>';
        return result;
    },

    addDueDateToTooltip: function (value, result) {
        if (!!value.severity && !!value.dueDate) {
            if (value.severity === 'HIGH') {
                result += '<br><span style="color: #EB5642">' + Uni.I18n.translate('whatsGoingOn.dueDate', 'UNI', 'Due date: {0}', Uni.DateTime.formatDateTimeLong(new Date(value.dueDate))) + '<span class="icon-warning" style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px"></span></span><br>';
            } else {
                result += '<br>' + Uni.I18n.translate('whatsGoingOn.dueDate', 'UNI', 'Due date: {0}', Uni.DateTime.formatDateTimeLong(new Date(value.dueDate))) + "<br>";
            }

        }
        return result;
    },

    addUserAssigneeToTooltip: function (result, value) {
        result += !!value.userAssignee ? Uni.I18n.translate('whatsGoingOn.userAssignee', 'UNI', 'User: {0}', value.userAssignee): '';
        result += value.userAssigneeIsCurrentUser ? Uni.I18n.translate('whatsGoingOn.currentUser', 'UNI', ' (Current user)') + "<br>"  : '<br>';

        return result;
    },

    addWorkGroupAssigneeToTooltip: function (result, value) {
        result += !!value.workGroupAssignee ? Uni.I18n.translate('whatsGoingOn.workGroupAssignee', 'UNI', 'Workgroup: {0}', value.workGroupAssignee): '';
        result += value.isMyWorkGroup ? Uni.I18n.translate('whatsGoingOn.myWorkGroup', 'UNI', ' (My workgroup)') + "<br>"  : '<br>';

        return result;
    }

});