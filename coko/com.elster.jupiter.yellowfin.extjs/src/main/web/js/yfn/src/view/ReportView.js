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
                    collapseDirection: 'top',
                    title: 'Report Name',
                    xtype: 'panel',
                    ui: 'medium',
                    itemId:'reportFilters',
                    autoScroll:true,
                    padding:10,
                    collapsible: true,
                    titleCollapse: true,
                    maxHeight:400,
                    layout: {
                        type: 'column'//,
                       //align: 'stretch'
                    },
                    items: [
                        {
                            itemId:'reportPrompts',
                            xtype: 'fieldcontainer',
                            labelAlign: 'top',
                            labelWidth:50,
                            fieldLabel: Uni.I18n.translate('generatereport.wizard.mandatoryFilters', 'YFN', 'Mandatory filters'),
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
                            labelAlign: 'top',
                            fieldLabel: Uni.I18n.translate('generatereport.wizard.optionalFilters', 'YFN', 'In report filters'),
                            flex: 1,
                            columnWidth:1,
                            layout: 'column'
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'right',
                            items: [
                                /*{
                                    xtype: 'tbfill'
                                },
                                {
                                    xtype: 'cycle',
                                    prependText: 'Export to ',
                                    showText: true,
                                    itemId: 'export-report-btn',
                                    menu: {
                                        xtype: 'menu',
                                        items: [
                                            {
                                                xtype: 'menucheckitem',
                                                itemId: 'csv-btn',
                                                action: 'export',
                                                exportType: 'csv',
                                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'CSV')
                                            },

                                            {
                                                xtype: 'menucheckitem',
                                                itemId: 'doc-btn',
                                                action: 'export',
                                                exportType: 'doc',
                                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'Export to Word')
                                            },
                                            {
                                                xtype: 'menucheckitem',
                                                itemId: 'pdf-btn',
                                                action: 'export',
                                                exportType: 'pdf',
                                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'Export to PDF')
                                            },
                                            {
                                                xtype: 'menucheckitem',
                                                itemId: 'rtf-btn',
                                                action: 'export',
                                                exportType: 'rtf',
                                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'Export to RTF')
                                            },
                                            {
                                                xtype: 'menucheckitem',
                                                itemId: 'txt-btn',
                                                action: 'export',
                                                exportType: 'txt',
                                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'Export to TXT')
                                            },
                                            {
                                                xtype: 'menucheckitem',
                                                itemId: 'xls-btn',
                                                action: 'export',
                                                exportType: 'xls',
                                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'Export to XLS')
                                            }
                                        ]
                                    }
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'chart-btn',
                                    text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'Chart'),
                                    icon: '/apps/sky/resources/images/form/restore.png'
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'table-btn',
                                    text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'YFN', 'Table'),
                                    icon: '/apps/sky/resources/images/form/restore.png'
                                },*/
                                {
                                    xtype: 'button',
                                    itemId: 'refresh-btn',
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
                    //flex:1,
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