Ext.define('Imt.usagepointmanagement.view.widget.DataCompletion', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.data-completion-widget',
    requires: [
        'Imt.usagepointmanagement.store.DataCompletion',
        'Imt.usagepointmanagement.view.widget.OutputKpi',
        'Ext.util.Bindable',
        'Imt.usagepointmanagement.store.Purposes',
        'Imt.usagepointmanagement.store.Periods'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    ui: 'tile',
    router: null,
    layout: 'hbox',
    overflowY: 'auto',
    store: 'Imt.usagepointmanagement.store.DataCompletion',

    config: {
        purpose: null,
        period: null
    },

    listeners: {
        resize: function (panel, width) {
            var outputKpiWidgets = Ext.ComponentQuery.query('data-completion-widget output-kpi-widget'),
                visibleWidgets = 0;

            Ext.Array.each(outputKpiWidgets, function (widget) {
                if (!widget.isHidden()) {
                    visibleWidgets++;
                }
            });

            if (visibleWidgets > 1) {
                if (width < 1200 ) {
                    this.reconfigure(2, this.getStore().getCount() == 3);
                } else {
                    this.reconfigure();
                }
            }
        }
    },

    initComponent: function () {
        var me = this,
            store,
            purposesStore = Ext.getStore('Imt.usagepointmanagement.store.Purposes'),
            periodsStore = Ext.getStore('Imt.usagepointmanagement.store.Periods') || Ext.create('Imt.usagepointmanagement.store.Periods'),
            defaultPurposeId = purposesStore.first().getId();

        me.bindStore(me.store || 'ext-empty-store', true);
        store = me.getStore();
        me.setPurpose(purposesStore.first());        
        me.items = me.addTabPanel();
        me.tools = [
            {
                xtype: 'toolbar',
                itemId: 'comboTool',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                        itemId: 'purposes-combo',
                        valueField: 'id',
                        store: purposesStore,
                        displayField: 'name',
                        queryMode: 'local',
                        editable: false,
                        labelWidth: 60,
                        width: 250,
                        labelPad: 2,
                        forceSelection: true,
                        value: me.getPurpose().getId(),
                        margin: '0 10 0 0',
                        listeners: {
                            change: function (combo, newvalue) {
                                me.setPurpose(purposesStore.getById(newvalue));
                                store.getProxy().extraParams = {
                                    usagePointMRID: me.usagePoint.get('mRID'),
                                    purposeId: newvalue,
                                    periodId: me.down('#periods-combo').getValue()
                                };
                                store.load();
                            }
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
        periodsStore.getProxy().extraParams = {
            mRID: me.usagePoint.get('mRID'),
            purposeId: defaultPurposeId
        };
        periodsStore.load(function () {
            if (me.down('#comboTool')) {
                me.down('#comboTool').add({
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                    itemId: 'periods-combo',
                    valueField: 'id',
                    store: periodsStore,
                    displayField: 'name',
                    queryMode: 'local',
                    editable: false,
                    labelWidth: 60,
                    width: 250,
                    labelPad: 2,
                    forceSelection: true,
                    value: periodsStore.first().getId(),
                    listeners: {
                        change: function (combo, newvalue) {
                            store.getProxy().extraParams = {
                                usagePointMRID: me.usagePoint.get('mRID'),
                                purposeId: me.down('#purposes-combo').getValue(),
                                periodId: newvalue
                            };
                            store.load();
                        }
                    }
                });
                store.getProxy().extraParams = {
                    usagePointMRID: me.usagePoint.get('mRID'),
                    purposeId: defaultPurposeId,
                    periodId: periodsStore.first().getId()
                };
                store.load();
            }
        });
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

    reconfigure: function(count, withTabPanel) {
        var me = this,
            store = me.getStore(),
            numberOfVisibleItems = count || 3;

        Ext.suspendLayouts();
        if (store.getCount() > 3) {
            if (me.down('tabpanel')) {
                me.down('tabpanel').removeAll(true);
            }
            me.addWidgetsOnTab(numberOfVisibleItems);
        } else if (withTabPanel) {
            me.removeAll(true);
            me.add(me.addTabPanel());
            me.addWidgetsOnTab(numberOfVisibleItems);
        } else {
            me.removeAll(true);
            store.each(function (item, index, total) {
                me.add(me.addWidget(item));
            });
        }
        Ext.resumeLayouts(true);
    },

    addTab: function(mod) {
        return {
            layout: 'column',
            columnWidth: 0.3,
            iconCls: mod === 0 ? 'icon-circle' : 'icon-circle2',
            items: []
        };
    },

    addWidget: function (output) {
        var me = this;

        return {
            xtype: 'output-kpi-widget',
            width: 400,
            output: output,
            purpose: me.getPurpose(),
            router: me.router
        };
    },

    addTabPanel: function () {
        return {
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
        }
    },

    addWidgetsOnTab: function (numberOfVisibleItems) {
        var me = this,
            tabs = [];

        me.getStore().each(function (item, index, total) {
            var mod = Math.floor(index / numberOfVisibleItems);
            if (!tabs[mod]) {
                tabs[mod] = me.addTab(mod)
            }

            tabs[mod].items.push(me.addWidget(item));
        });

        if (me.down('tabpanel')) {
            me.down('tabpanel').add(tabs);
        }
    }
});