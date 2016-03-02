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
                var icon, iconColor;
                switch (value.severity) {
                    case 'info':
                        icon = 'icon-circle';
                        iconColor = '#686868';
                        break;
                    case 'warning':
                        icon = 'icon-circle2';
                        iconColor = '#F7941E';
                        break;
                    case 'severe':
                        icon = 'icon-circle2';
                        iconColor = '#EB5541';
                        break;
                }
                switch (value.type) {
                    case 'issue':
                        return me.getHtml(icon, iconColor, 'I', '#686868') + '<a class="a-underline">' + value.name + '</a>';
                    case 'servicecall':
                        return me.getHtml(icon, iconColor, 'S', '#686868') + '<a class="a-underline">' + value.name + '</a>';
                    case 'alarm':
                        return me.getHtml(icon, iconColor, 'A', '#686868') + '<a class="a-underline">' + value.name + '</a>';
                    case 'process':
                        return me.getHtml(icon, iconColor, 'P', '#686868') + '<a class="a-underline">' + value.name + '</a>';
                }
            }
        });
    },

    getHtml: function (icon, iconColor, text, textColor) {
        return '<span class="stacked-icon-container">'
            + '<i class="' + icon + ' stacked-icon" style="color:' + iconColor + ';"></i>'
            + '<span class="stacked-text" style="color:' + textColor + '">' + text + '</span>'
            + '</span>';
    }
});