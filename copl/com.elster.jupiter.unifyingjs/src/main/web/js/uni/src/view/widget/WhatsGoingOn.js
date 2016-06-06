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
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        if (this.type === 'device') {
            me.store.setProxy({
                type: 'rest',
                url: '/api/ddr/devices/'+ encodeURIComponent(this.mrId) +'/whatsgoingon',
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
                url: '/api/udr/usagepoints/'+ encodeURIComponent(this.mrId) +'/whatsgoingon',
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
                me.store.clearFilter();
                me.down('tabpanel').removeAll();
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
                    me.down('tabpanel').add({
                        layout: 'hbox',
                        margin: '38 0 0 0',
                        items: [
                            {
                                flex: 1
                            },
                            {
                                html: '<span style="color:#686868;font-size:20px;">' +
                                '<i class="icon-info" style="color:#686868;margin-right:15px;"></i>' +
                                    Uni.I18n.translate('whatsGoingOn.nothingToShow', 'UNI', 'No active issues, processes or service calls to show') +
                                    '</span>'
                            },
                            {
                                flex: 1
                            }
                        ]

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
                if (value.severity === undefined && value.assignee === undefined) {
                    fillColor = "#FFFFFF";
                    borderColor = "#1E7D9E";
                    textColor = "#686868";
                }
                else if (value.severity === undefined && value.assignee !== undefined) {
                    switch (value.assigneeIsCurrentUser) {
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
                } else if (value.severity !== undefined && value.assignee === undefined) {
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
                } else if (value.severity !== undefined && value.assignee !== undefined) {
                    switch (value.assigneeIsCurrentUser) {
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
        var href, html;
        switch (value.type) {
            case 'issue':
                href = this.router.getRoute('workspace/issues/view').buildUrl({issueId: value.id});
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
                break;
            case 'servicecall':
                href = this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id});
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.reference + ' (' + value.description + ')';
                break;
            case 'alarm':
                href = "#alarm";
                break;
            case 'process':
                href = this.router.getRoute('devices/device').buildUrl({mRID: this.mrId}) + '/processes?activeTab=running';
                html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
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
                result += Uni.I18n.translate('whatsGoingOn.issue', 'UNI', 'Issue');
                break;
            case 'servicecall':
                result += Uni.I18n.translate('whatsGoingOn.serviceCall', 'UNI', 'Service call');
                break;
            case 'alarm':
                result += Uni.I18n.translate('whatsGoingOn.alarm', 'UNI', 'Alarm');
                break;
            case 'process':
                result += Uni.I18n.translate('whatsGoingOn.process', 'UNI', 'Process');
                break;
        }
        result += !!value.description ? " '" + value.description + "'</span><br>" : '</span>';

        result = this.addContentToTooltip(result, value);
        result = this.addDueDateToTooltip(value, result);
        result = this.addAssigneeToTooltip(result, value);

        return Ext.String.htmlEncode(result);
    },

    addContentToTooltip: function (result, value) {
        result += !!value.description && !!value.status ? Uni.I18n.translate('whatsGoingOn.is', 'UNI', '{0} is {1}', [value.description, value.status]) + "<br>" : '';
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

    addAssigneeToTooltip: function (result, value) {
        result += !!value.assignee ? Uni.I18n.translate('whatsGoingOn.assignee', 'UNI', 'Assignee: {0}', value.assignee) + "<br>" : '';
        return result;
    }
});