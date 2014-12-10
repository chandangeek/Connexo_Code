Ext.define('Yfn.view.ReportView', {
    extend: 'Ext.container.Container',
    alias: 'widget.report-view',
    itemId: 'reportView',
    overflowY: 'hidden',
    layout: 'border',
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
                    height: 270,
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
                            fieldLabel: Uni.I18n.translate('generatereport.reportPromptsTitle', 'YFN', 'Prompts'),
                            hidden:true,
                            flex: 1,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            }
                        },
                        {
                            itemId:'reportFilters',
                            xtype: 'fieldcontainer',
                            hidden:true,
                            labelAlign: 'left',
                            fieldLabel: Uni.I18n.translate('generatereport.reportFiltersTitle', 'YFN', 'Filters'),
                            flex: 2,
                            layout: 'column'
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
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
                    border:1,
                    style: {
                        borderColor: 'lightgray',
                        borderStyle: 'solid'
                    },
                    layout: 'fit'
                }
            ]
        });

        me.callParent(arguments);
    }
});