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
        me.items = {
            xtype: 'tabpanel',
            tabPosition: 'bottom',
            tabBar: {
                layout: {pack: 'center'}
            },
            defaults: {
                listeners: {
                    activate: function (tab, eOpts) {
                        tab.setIconCls('icon-circle');
                    },
                    deactivate: function (tab, eOpts) {
                        tab.setIconCls('icon-circle2');
                    }
                }
            }
        };
        this.store = 'Uni.store.WhatsGoingOn';


        var healthTypeStore = Ext.getStore('Uni.store.HealthCategories') || Ext.create('Uni.store.HealthCategories');
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
                        value: 'all',
                        store: healthTypeStore,
                        displayField: 'displayValue',
                        valueField: 'type',
                        listeners: {
                            change: function (combo, newvalue) {
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
        if (Ext.isEmpty(type) && !Ext.isEmpty(me.down('#uni-whatsgoingon-combo'))) {
            type = me.down('#uni-whatsgoingon-combo').getValue();
        }
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        if (this.type === 'device') {
            me.store.setProxy({
                type: 'rest',
                url: '/api/ddr/devices/'+ encodeURIComponent(this.deviceId) +'/whatsgoingon',
                startParam: null,
                limitParam: null,
                reader: {
                    type: 'json',
                    root: 'goingsOn'
                }
            });
        } else if (this.type === 'usagepoint') {
            me.store.setProxy({
                type: 'rest',
                url: '/api/udr/usagepoints/'+ encodeURIComponent(this.usagePointId) +'/whatsgoingon',
                startParam: null,
                limitParam: null,
                reader: {
                    type: 'json',
                    root: 'goingsOn'
                }
            });
        }
        me.store.load({
            callback: function(){
                me.setLoading(false);
                me.store.clearFilter();
                if (me.down('tabpanel')) {
                    me.down('tabpanel').removeAll();
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
                var lines = [];
                var emptyText;
                Ext.suspendLayouts();
                me.store.each(function (item, index, total) {
                    if (index !== 0 && (index + 1) % 10 === 0) {
                        me.addLine(lines, item);
                        tabContents.push(lines);
                        lines = [];
                    } else {
                        me.addLine(lines, item);
                    }
                });
                if (lines.length !== 0) {
                    tabContents.push(lines);
                }
                if (tabContents.length===0){
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
                            emptyText = Uni.I18n.translate('whatsGoingOn.nothingToShow', 'UNI', 'No active alarms, issues, processes or service calls to show');
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
                } else {
                    if(tabContents.length===1){
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
                            me.down('tabpanel').add({
                                layout: 'column',
                                iconCls: index === 0 ? 'icon-circle' : 'icon-circle2',
                                items: [{
                                    columnWidth: 0.50,
                                    items: tabContent.splice(0, 5)
                                }, {
                                    columnWidth: 0.50,
                                    items: tabContent
                                }]
                            });
                        });
                    }
                }
                Ext.resumeLayouts();
                me.doLayout();
            }
        });

    },

    addLine: function (lines, item) {
        var me = this;
        lines.push({
            xtype: 'displayfield',
            labelField: ' ',
            value: item.get('displayValue'),
            renderer: function (value) {
                var fillColor, borderColor, textColor;
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

    createLink: function (textColor, value) {
        var me = this, href, html;

        switch (value.type) {
            case 'issue':
                href = this.router.getRoute('workspace/issues/view').buildUrl({issueId: value.id}, {issueType: value.issueType});
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
                break;
            case 'servicecall':
                href = this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id});
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.reference + ' (' + value.description + ')';
                break;
            case 'alarm':
                href = this.router.getRoute('workspace/alarms/view').buildUrl({alarmId: value.id});
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
        result = this.addDueDateToTooltip(value, result);
        result = this.addUserAssigneeToTooltip(result, value);
        result = this.addWorkGroupAssigneeToTooltip(result, value);

        return Ext.String.htmlEncode(result);
    },

    addContentToTooltip: function (result, value) {
        result += !!value.status ? Uni.I18n.translate('whatsGoingOn.status', 'UNI', 'Status: {0}', value.status) + "<br>" : '';
        return result;
    },

    addDueDateToTooltip: function (value, result) {
        if (!!value.severity && !!value.dueDate) {
            if (value.severity === 'HIGH') {
                result += '<br><span style="color: #EB5642">' + Uni.I18n.translate('whatsGoingOn.dueDate', 'UNI', 'Due date: {0}', Uni.DateTime.formatDateTimeLong(new Date(value.dueDate))) + '<span class="icon-warning" style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px"></span></span><br><br>';
            } else {
                result += '<br>' + Uni.I18n.translate('whatsGoingOn.dueDate', 'UNI', 'Due date: {0}', Uni.DateTime.formatDateTimeLong(new Date(value.dueDate))) + "<br><br>";
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