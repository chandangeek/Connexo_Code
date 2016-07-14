Ext.define('Imt.usagepointmanagement.view.widget.DataCompletion', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.data-completion-widget',
    requires: [
        'Imt.usagepointmanagement.store.DataCompletion',
        'Imt.usagepointmanagement.view.widget.OutputKpi',
        'Ext.util.Bindable'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    overflowY: 'auto',
    title: Uni.I18n.translate('deviceGeneralInformation.whatsGoingOn', 'UNI', 'What\'s going on'),
    ui: 'tile',
    router: null,
    layout: 'fit',
    store: 'Imt.usagepointmanagement.store.DataCompletion',

    initComponent: function () {
        var me = this,
            store;

        me.items = {
            xtype: 'tabpanel',
            layout: 'fit',
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

        // purpose and period stores
        //var healthTypeStore = Ext.getStore('Uni.store.HealthCategories') || Ext.create('Uni.store.HealthCategories');
        //me.tools = [
        //    {
        //        xtype: 'toolbar',
        //        itemId: 'comboTool',
        //        margin: '0 20 0 0',
        //        layout: 'fit',
        //        items: [
        //            {
        //                xtype: 'combobox',
        //                value: 'all',
        //                store: healthTypeStore,
        //                displayField: 'displayValue',
        //                valueField: 'type',
        //                listeners: {
        //                    change: function (combo, newvalue) {
        //                        me.buildWidget(newvalue);
        //                    }
        //                }
        //            }
        //        ]
        //
        //    }
        //];

        me.callParent(arguments);
        me.bindStore(me.store || 'ext-empty-store', true);
        store = me.getStore();
        store.getProxy().extraParams = {
            usagePointMRID: me.usagePoint.get('mRID'),
            purposeId: 1, //todo: change it after integration
            periodId: 1 //todo: change it after integration
        };

        store.load();
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeLoad: function () {
        this.setLoading(true);
    },

    onLoad: function () {
        this.reconfigure();
        this.setLoading(false);
    },

    reconfigure: function() {
        var me = this,
            store = me.getStore(),
            tab = {
                layout: 'column',
                columnWidth: 0.33,
                items: []
            };

        store.each(function (item, index, total) {
            var widget = me.addWidget(item);
            tab.items.push(widget);
        });

        me.down('tabpanel').add({
            layout: 'fit',
            iconCls: 'icon-circle',
            //iconCls: index === 0 ? 'icon-circle' : 'icon-circle2',
            items: [tab]
        });
    },


    addWidget: function (output) {
        var me = this;

        return {
            xtype: 'output-kpi-widget',
            output: output,
            router: me.router
        };
    }

    //buildWidget: function (type) {
    //    me.store.load({
    //        callback: function(){
    //            me.store.clearFilter();
    //            me.down('tabpanel').removeAll();
    //            if (type !== 'all' && type !== '' && type !== undefined) {
    //                me.store.filter([
    //                    {
    //                        filterFn: function (item) {
    //                            return item.get('type') === type;
    //                        }
    //                    }
    //                ]);
    //            }
    //            var tabContents = [];
    //            var lines = [];
    //            Ext.suspendLayouts();
    //            me.store.each(function (item, index, total) {
    //                if (index !== 0 && (index + 1) % 10 === 0) {
    //                    me.addWidget(lines, item);
    //                    tabContents.push(lines);
    //                    lines = [];
    //                } else {
    //                    me.addWidget(lines, item);
    //                }
    //            });
    //            if (lines.length !== 0) {
    //                tabContents.push(lines);
    //            }
    //            if (tabContents.length===0){
    //                me.down('tabpanel').add({
    //                    layout: 'hbox',
    //                    margin: '38 0 0 0',
    //                    tabConfig: {
    //                        ui: 'default',
    //                        cls: 'x-tab-default-disabled'
    //                    },
    //                    items: [
    //                        {
    //                            flex: 1
    //                        },
    //                        {
    //                            html: '<span style="color:#686868;font-size:20px;">' +
    //                            '<i class="icon-info" style="color:#686868;margin-right:15px;"></i>' +
    //                            Uni.I18n.translate('whatsGoingOn.nothingToShow', 'UNI', 'No active issues, processes or service calls to show') +
    //                            '</span>'
    //                        },
    //                        {
    //                            flex: 1
    //                        }
    //                    ]
    //
    //                });
    //            } else {
    //                if(tabContents.length===1){
    //                    me.down('tabpanel').add({
    //                        tabConfig: {
    //                            ui: 'default',
    //                            cls: 'x-tab-default-disabled'
    //                        },
    //                        layout: 'column',
    //
    //                        items: [{
    //                            columnWidth: 0.50,
    //                            items: tabContents[0].splice(0, 5)
    //                        }, {
    //                            columnWidth: 0.50,
    //                            items: tabContents[0]
    //                        }]
    //                    });
    //                } else {
    //                    Ext.each(tabContents, function (tabContent, index) {
    //                        me.down('tabpanel').add({
    //                            layout: 'column',
    //                            iconCls: index === 0 ? 'icon-circle' : 'icon-circle2',
    //                            items: [{
    //                                columnWidth: 0.50,
    //                                items: tabContent.splice(0, 5)
    //                            }, {
    //                                columnWidth: 0.50,
    //                                items: tabContent
    //                            }]
    //                        });
    //                    });
    //                }
    //            }
    //            Ext.resumeLayouts();
    //            me.doLayout();
    //        }
    //    });
    //},

    //getHtml: function (fillColor, borderColor, iconText, iconTextColor, textColor, value) {
    //    var html = '<span style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">'
    //        +'<span class="stacked-icon-container" style="cursor: pointer;">'
    //        + '<i class="' + 'icon-circle2' + ' stacked-icon" style="color:' + fillColor + ';"></i>'
    //        + '<i class="' + 'icon-circle' + ' stacked-icon" style="color:' + borderColor + ';"></i>'
    //        + '<span class="stacked-text" style="color:' + iconTextColor + '"'
    //        + 'data-qtip="' + this.createToolTip(value, borderColor) + '"'
    //        + '>'
    //        + iconText + '</span>'
    //        + '</span>';
    //    html += this.createLink(textColor, value);
    //    html += '<span>';
    //    return html;
    //},
    //
    //createLink: function (textColor, value) {
    //    var href, html;
    //
    //    switch (value.type) {
    //        case 'issue':
    //            href = this.router.getRoute('workspace/issues/view').buildUrl({issueId: value.id}, {issueType: value.issueType});
    //            html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
    //            break;
    //        case 'servicecall':
    //            href = this.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id});
    //            html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.reference + ' (' + value.description + ')';
    //            break;
    //        case 'alarm':
    //            href = "#alarm";
    //            break;
    //        case 'process':
    //            href = this.router.getRoute('devices/device').buildUrl({mRID: this.mrId}) + '/processes?activeTab=running';
    //            html = '<a class="a-underline" style="color:' + textColor + ';" href="' + href + '">' + value.description;
    //            break;
    //    }
    //    html += !!value.dueDate ? ' ' + Uni.I18n.translate('whatsGoingOn.due', 'UNI', '(due {0})', Uni.DateTime.formatDateShort(new Date(value.dueDate))) : '';
    //    html += '</a>';
    //    return html;
    //},
    //
    //createToolTip: function (value, color) {
    //    var result = '<span style="color:' + color + ';font-size:16px;line-height:24px;">';
    //    switch (value.type) {
    //        case 'issue':
    //            result += Uni.I18n.translate('whatsGoingOn.issue', 'UNI', 'Issue');
    //            break;
    //        case 'servicecall':
    //            result += Uni.I18n.translate('whatsGoingOn.serviceCall', 'UNI', 'Service call');
    //            break;
    //        case 'alarm':
    //            result += Uni.I18n.translate('whatsGoingOn.alarm', 'UNI', 'Alarm');
    //            break;
    //        case 'process':
    //            result += Uni.I18n.translate('whatsGoingOn.process', 'UNI', 'Process');
    //            break;
    //    }
    //    result += !!value.description ? " '" + value.description + "'</span><br>" : '</span>';
    //
    //    result = this.addContentToTooltip(result, value);
    //    result = this.addDueDateToTooltip(value, result);
    //    result = this.addAssigneeToTooltip(result, value);
    //
    //    return Ext.String.htmlEncode(result);
    //},
    //
    //addContentToTooltip: function (result, value) {
    //    result += !!value.description && !!value.status ? Uni.I18n.translate('whatsGoingOn.is', 'UNI', '{0} is {1}', [value.description, value.status]) + "<br>" : '';
    //    return result;
    //},
    //
    //addDueDateToTooltip: function (value, result) {
    //    if (!!value.severity && !!value.dueDate) {
    //        if (value.severity === 'HIGH') {
    //            result += '<br><span style="color: #EB5642">' + Uni.I18n.translate('whatsGoingOn.dueDate', 'UNI', 'Due date: {0}', Uni.DateTime.formatDateTimeLong(new Date(value.dueDate))) + '<span class="icon-warning" style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px"></span></span><br><br>';
    //        } else {
    //            result += '<br>' + Uni.I18n.translate('whatsGoingOn.dueDate', 'UNI', 'Due date: {0}', Uni.DateTime.formatDateTimeLong(new Date(value.dueDate))) + "<br><br>";
    //        }
    //
    //    }
    //    return result;
    //},
    //
    //addAssigneeToTooltip: function (result, value) {
    //    result += !!value.assignee ? Uni.I18n.translate('whatsGoingOn.assignee', 'UNI', 'Assignee: {0}', value.assignee) + "<br>" : '';
    //    return result;
    //}
});