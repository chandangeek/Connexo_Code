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
    layout: 'fit',
    overflowY: 'auto',
    store: 'Imt.usagepointmanagement.store.DataCompletion',

    config: {
        purpose: null,
        period: null
    },

    listeners: {
        resize: function (panel, width, height, oldWidth) {
            if (!oldWidth || width === oldWidth) {
                return;
            }

            var outputKpiWidgets = Ext.ComponentQuery.query('data-completion-widget output-kpi-widget'),
                visibleWidgets = 0;

            Ext.Array.each(outputKpiWidgets, function (widget) {
                if (!widget.isHidden()) {
                    visibleWidgets++;
                }
            });

            if (visibleWidgets > 1) {
                this.reconfigure();
            }
        }
    },

    initComponent: function () {
        var me = this,
            store,
            purposesStore = Ext.getStore('Imt.usagepointmanagement.store.Purposes'),
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
                        value: defaultPurposeId,
                        margin: '0 10 0 0',
                        listeners: {
                            change: function (combo, newvalue) {
                                me.setPurpose(purposesStore.getById(newvalue));
                                me.loadPeriodsStore({
                                    mRID: me.usagePoint.get('mRID'),
                                    purposeId: newvalue
                                });
                            }
                        }
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                        itemId: 'periods-combo',
                        valueField: 'id',
                        store: 'Imt.usagepointmanagement.store.Periods',
                        displayField: 'name',
                        queryMode: 'local',
                        editable: false,
                        labelWidth: 60,
                        width: 250,
                        labelPad: 2,
                        forceSelection: true,
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
                    }
                ]
            }
        ];
        me.callParent(arguments);

        me.loadPeriodsStore({
            mRID: me.usagePoint.get('mRID'),
            purposeId: defaultPurposeId
        });

        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    loadPeriodsStore: function (params) {
        var me = this,
            periodsStore = Ext.getStore('Imt.usagepointmanagement.store.Periods');

        periodsStore.getProxy().extraParams = params;
        periodsStore.load(function () {
            if (me.rendered) {
                me.down('#periods-combo').setValue(periodsStore.first().getId());
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

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    reconfigure: function() {
        var me = this;

        Ext.suspendLayouts();
        me.removeAll(true);
        me.addWidgetsOnTab();
        Ext.resumeLayouts(true);
    },

    addTab: function(mod) {
        return {
            layout: {
                type: 'hbox',
                align: 'stretchmax'
            },
            iconCls: mod === 0 ? 'icon-circle' : 'icon-circle2',
            items: []
        };
    },

    addWidget: function (output) {
        var me = this;

        return {
            xtype: 'output-kpi-widget',
            minWidth: 400,
            flex: 1,
            output: output,
            purpose: me.getPurpose(),
            router: me.router
        };
    },

    addTabPanel: function () {
        return {
            xtype: 'tabpanel',
            tabPosition: 'bottom',
            tabBar: {
                layout: {pack: 'center'}
            },
            defaults: {
                listeners: {
                    activate: function (tab) {
                        tab.setIconCls('icon-circle');
                    },
                    deactivate: function (tab) {
                        tab.setIconCls('icon-circle2');
                    }
                }
            }
        }
    },

    addWidgetsOnTab: function () {
        var me = this,
            tabs = [],
            maxWidth = me.getContentTarget().getWidth(),
            width = 0,
            tab = me.addTab(0)
        ;

        tabs.push(tab);
        me.getStore().each(function (item) {
            var widget = Ext.createWidget(me.addWidget(item));
            width += widget.minWidth;
            if (width > maxWidth) {
                tab = me.addTab(1);
                width = widget.width;
                tabs.push(tab);
                tab.items.push(widget);
            }else {
                tab.items.push(widget);
            }
        });

        if (tabs.length > 1) {
            me.add(Ext.apply(me.addTabPanel(), {items: tabs}))
        } else {
            me.add(tabs.pop());
        }
    }
});