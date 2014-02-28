Ext.define('Mtr.view.usagepoint.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.usagePointBrowse',

    requires: [
        'Mtr.widget.Breadcrumbs',
        'Mtr.widget.GoogleMaps'
    ],

    cls: 'browse-page',
    layout: 'border',

    usagePointId: null,

    initComponent: function () {
        var me = this;

        this.items = [
            {
//            xtype: 'navigationBreadcrumbs',
                xtype: 'breadcrumbs',
                itemId: 'breadcrumbs',
                region: 'north'/*,*/
//            layout: 'hbox'
            },
//        {
//            xtype: 'navigationSubmenu',
//            region: 'west'
//        },
            {
                xtype: 'container',
                region: 'center',
                itemId: 'contentPanel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                overflowY: 'auto',

                items: [
                    {
                        xtype: 'panel',
                        title: 'General information',
                        itemId: 'generalInfoWrapper',
                        cls: 'info-panel',
                        tools: [
                            {
                                type: 'edit',
                                tooltip: 'Edit general information',
                                tooltipType: 'title',
                                handler: function (event, toolEl, panelHeader) {
                                    me.fireEvent('editgeneralinfo', me.usagePointId);
                                }
                            }
                        ],
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        defaults: {
                            flex: 1
                        },
                        items: [
                            {
                                xtype: 'container',
                                itemId: 'generalInfo',
                                cls: 'info-props',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    layout: {
                                        type: 'hbox',
                                        align: 'stretch'
                                    },
                                    defaults: {
                                        flex: 1
                                    }
                                }
                            },
                            {
                                xtype: 'container',
                                itemId: 'generalMap',
                                layout: 'fit'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        cls: 'info-panel',
                        defaults: {
                            flex: 1
                        },
                        items: [
                            {
                                xtype: 'panel',
                                itemId: 'techInfo',
                                cls: 'info-panel-col-left',
                                bodyCls: 'info-props',
                                tools: [
                                    {
                                        type: 'edit',
                                        tooltip: 'Edit technical information',
                                        tooltipType: 'title',
                                        handler: function (event, toolEl, panelHeader) {
                                            me.fireEvent('edittechinfo', me.usagePointId);
                                        }
                                    }
                                ],
                                title: 'Technical information'
                            },
                            {
                                xtype: 'panel',
                                itemId: 'otherInfo',
                                cls: 'info-panel-col-right',
                                bodyCls: 'info-props',
                                tools: [
                                    {
                                        type: 'edit',
                                        tooltip: 'Edit other information',
                                        tooltipType: 'title',
                                        handler: function (event, toolEl, panelHeader) {
                                            me.fireEvent('editotherinfo', me.usagePointId);
                                        }
                                    }
                                ],
                                title: 'Other information'
                            }
                        ]
                    },

                    {
                        xtype: 'panel',
                        itemId: 'chart-2',
                        cls: 'info-panel',
                        title: 'Interval readings',
                        layout: 'fit',
                        margins: '0 0 5 0',
                        items: [
                            {
                                xtype: 'highchart',
                                itemId: 'linechart',
                                height: 300,
                                initAnimAfterLoad: false,
                                xField: 'timeStamp',

                                series: [
                                    {
                                        dataIndex: 'values',
                                        type: 'line',
                                        name: 'Values'
                                    }
                                ],

                                chartConfig: {
                                    chart: {
                                        zoomType: 'x'
                                    },

                                    title: {
                                        text: 'Interval readings'
                                    },

                                    xAxis: [
                                        {
                                            type: 'datetime',
                                            title: {
                                                text: 'Time',
                                                margin: 30
                                            },
                                            labels: {
                                                rotation: 270,
                                                y: 35,
                                                formatter: function () {
                                                    var dt = Ext.Date.parse(parseInt(this.value) / 1000, "U");
                                                    if (dt) {
                                                        return Ext.Date.format(dt, "H:i");
                                                    }
                                                    return this.value;
                                                }
                                            }
                                        }
                                    ],
                                    legend: {
                                        layout: 'vertical',
                                        align: 'right',
                                        verticalAlign: 'top',
                                        x: -10,
                                        y: 100,
                                        borderWidth: 0
                                    },
                                    credits: {
                                        enabled: false
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    setBreadcrumbItems: function (items) {
        var breadcrumbs = this.down('#breadcrumbs');
        breadcrumbs.removeAll();
        breadcrumbs.add(items);
    },

    setGeneralInfo: function (id, properties) {
        this.setInfoFromProperties('#generalInfo', id, properties);
    },

    setTechInfo: function (id, properties) {
        this.setInfoFromProperties('#techInfo', id, properties);
    },

    setOtherInfo: function (id, properties) {
        this.setInfoFromProperties('#otherInfo', id, properties);
    },

    setInfoFromProperties: function (widgetId, usagePointId, properties) {
        this.usagePointId = usagePointId;

        var widget = this.down(widgetId);
        widget.removeAll();

        for (var key in properties) {
            var value = properties[key];

            if (typeof value == 'boolean') {
                value = value.toString();
                value = value.charAt(0).toUpperCase() + value.slice(1);
            } else if (typeof value == 'object') {
                value = value.toString();
            }

            if (key != undefined) {
                var keyValueWidget = this.createInfoKeyValueContainer(key, value);
                widget.add(keyValueWidget);
            }
        }
    },

    createInfoKeyValueContainer: function (key, value) {
        var rowWidget = Ext.widget('container', {
            cls: 'info-prop-row',
            layout: {
                type: 'hbox',
                align: 'stretch'
            }
        });

        var keyWidget = Ext.widget('component', {
            cls: 'info-prop-key',
            html: key,
            flex: 1
        });
        rowWidget.add(keyWidget);

        // TODO Use a matching component for each value type (e.g. checkbox for boolean).
        var valueWidget = Ext.widget('component', {
            cls: 'info-prop-value',
            html: value,
            flex: 1
        });
        rowWidget.add(valueWidget);

        return rowWidget;
    },

    setGeneralMapLocation: function (name, address) {
        var mapWrapper = this.down('#generalMap');
        mapWrapper.removeAll();

        if (address != null) {
            var styledWrapper = Ext.widget('container', {
                cls: 'info-map',
                layout: 'fit'
            });

            var map = Ext.widget('googlemaps', {
                center: {
                    geoCodeAddr: address,
                    marker: {
                        title: name
                    }
                }
            });

            styledWrapper.add(map);
            mapWrapper.add(styledWrapper);
        }
    }
});