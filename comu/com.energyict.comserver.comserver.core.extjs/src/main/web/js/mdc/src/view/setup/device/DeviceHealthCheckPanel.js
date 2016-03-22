Ext.define('Mdc.view.setup.device.DeviceHealthCheckPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceHealthCheckPanel',
    requires: [
        'Mdc.store.DeviceHealth',
        'Mdc.store.HealthCategories'
    ],
    overflowY: 'auto',
    itemId: 'deviceHealthCheckPanel',
    title: Uni.I18n.translate('deviceGeneralInformation.healthCheck', 'MDC', 'Health check'),
    ui: 'tile',
    router: null,
    layout: 'fit',
    store: 'Mdc.store.DeviceHealth',

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
        var healthTypeStore = Ext.getStore('Mdc.store.HealthCategories') || Ext.create('Mdc.store.HealthCategories');
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
        me.buildWidget();

    },

    buildWidget: function (type) {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
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
        Ext.resumeLayouts();
        me.doLayout();
    },

    addLine: function (lines, item) {
        var me = this;
        lines.push({
            xtype: 'displayfield',
            labelField: ' ',
            value: item.get('displayValue'),
            renderer: function (value) {
                var fillColor,borderColor,textColor;
                if(value.severity === undefined && value.assignee === undefined){
                    fillColor = "#FFFFFF";
                    borderColor = "#1E7D9E";
                }
                else if(value.severity === undefined && value.assignee !== undefined){
                    switch (value.assignee){
                        case 'currentuser':
                            fillColor = "#1E7D9E";
                            borderColor = "#1E7D9E";
                            textColor = "#686868";
                            break;
                        case 'unassigned':
                            fillColor = "#FFFFFF";
                            borderColor = "#1E7D9E";
                            textColor = "#686868";
                            break;
                        default:
                            fillColor = "#A0A0A0";
                            borderColor = "#A0A0A0";
                            textColor = "#A0A0A0";

                    }
                } else if (value.severity !== undefined && value.assignee === undefined){

                }
                switch (value.severity) {
                    case 'info':
                        fillColor = '#FFFFFF';
                        borderColor = '#1E7D9E';
                        break;
                    case 'warning':
                        fillColor = '#F7941E';
                        borderColor = '#A0A0A0';
                        break;
                    case 'severe':
                        fillColor = '#EB5541';
                        borderColor = '#1E7D9E';
                        break;
                }


                switch (value.type) {
                    case 'issue':
                        return me.getHtml(fillColor, borderColor, 'I', '#686868') + '<a class="a-underline">' + value.description + '</a>';
                    case 'servicecall':
                        return me.getHtml(fillColor, borderColor, 'S', '#686868') + '<a class="a-underline">' + value.description + '</a>';
                    case 'alarm':
                        return me.getHtml(fillColor, borderColor, 'A', '#686868') + '<a class="a-underline">' + value.description + '</a>';
                    case 'process':
                        return me.getHtml(fillColor, borderColor, 'P', '#686868') + '<a class="a-underline">' + value.description + '</a>';
                }
            }
        });
    },

    iconLinkRenderer: function(){

    },

    getHtml: function (fillColor, borderColor, text, textColor) {
        return '<span class="stacked-icon-container">'
            + '<i class="' + 'icon-circle2' + ' stacked-icon" style="color:' + fillColor + ';"></i>'
            + '<i class="' + 'icon-circle' + ' stacked-icon" style="color:' + borderColor + ';"></i>'
            + '<span class="stacked-text" style="color:' + textColor + '" data-qtip="probeer">' + text + '</span>'
            + '</span>';
    }
});