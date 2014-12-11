Ext.define('Yfn.view.ReportView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report-view',
    itemId: 'reportView',
    overflowY: 'hidden',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    padding:10,
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'panel',
                    ui: 'medium',
                    itemId:'reportFilters',
                    region: 'north',
                    split: true,
                    //height: 200,
                    autoscroll:true,
                    padding:10,
                    collapsible: true,
                    titleCollapse: true,
                    collapseDirection: 'top',
                    title: 'Report Name',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            itemId:'reportPrompts',
                            xtype: 'fieldcontainer',
                            labelAlign: 'left',
                            labelWidth:50,
                            fieldLabel: Uni.I18n.translate('generatereport.reportPromptsTitle', 'YFN', 'Prompts'),
                            hidden:true,
                            labelStyle: 'color:#cccccc',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            }
                        },
                        {
                            itemId:'reportFilters',
                            xtype: 'fieldcontainer',
                            hidden:true,
                            labelWidth:50,
                            margin:'0 0 0 30', //trbl
                            labelStyle: 'color:#cccccc',
                            labelAlign: 'left',
                            fieldLabel: Uni.I18n.translate('generatereport.reportFiltersTitle', 'YFN', 'Filters'),
                            flex: 1,
                            layout: 'column'
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            flex: 1,
                            dock: 'right',
                            items: [
                                {
                                    xtype: 'button',
                                    itemId: 'refresh-btn',
                                    style: {
                                        'background-color': '#71adc7'
                                    },
                                    text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                                    icon: '/apps/sky/resources/images/form/restore.png'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'container',
                    itemId:'reportContent',
                    region: 'center',
                    split: true,
                    flex:1,
                    border:1,
                    style: {
                        borderColor: 'lightgray',
                        borderStyle: 'solid'
                    },
                    layout: 'fit',
                    dockedItems: [
                        {
                            xtype: 'container',
                            flex: 1,
                            dock: 'bottom',
                            items: [
                                {
                                    xtype: 'button',
                                    itemId: 'refresh-btn',
                                    style: {
                                        'background-color': '#71adc7'
                                    },
                                    text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                                    icon: '/apps/sky/resources/images/form/restore.png'
                                }
                            ]
                        }
                    ]
                }
            ]
        });

        me.callParent(arguments);
    }
});