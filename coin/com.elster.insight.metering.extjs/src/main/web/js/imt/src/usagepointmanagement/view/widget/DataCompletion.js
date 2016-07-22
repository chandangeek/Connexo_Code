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
    overflowY: 'auto',
    title: Uni.I18n.translate('deviceGeneralInformation.whatsGoingOn', 'UNI', 'What\'s going on'),
    ui: 'tile',
    router: null,
    layout: 'fit',
    store: 'Imt.usagepointmanagement.store.DataCompletion',

    config: {
        purpose: null,
        period: null
    },

    initComponent: function () {
        var me = this,
            store,
            purposesStore = Ext.getStore('Imt.usagepointmanagement.store.Purposes'),
            periodsStore = Ext.getStore('Imt.usagepointmanagement.store.Periods') || Ext.create('Imt.usagepointmanagement.store.Periods'),
            defaultPurposeId = purposesStore.first().getId();
        
        me.setPurpose(purposesStore.first());        
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
        me.callParent(arguments);
        periodsStore.getProxy().extraParams = {
            mRID: me.usagePoint.get('mRID'),
            purposeId: defaultPurposeId
        };
        periodsStore.load(function () {
            me.tools = [
                {
                    xtype: 'toolbar',
                    itemId: 'comboTool',
                    margin: '0 20 0 0',
                    layout: 'fit',
                    items: [
                        {
                            xtype: 'combobox',
                            fieldLabel: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                            itemId: 'purposes-combo',
                            value: 'id',
                            store: purposesStore,
                            displayField: 'name',
                            listeners: {
                                change: function (combo, newvalue) {
                                    // me.buildWidget(newvalue);
                                }
                            }
                        },
                        {
                            xtype: 'combobox',
                            fieldLabel: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                            itemId: 'periods-combo',
                            value: 'id',
                            store: periodsStore,
                            displayField: 'name',
                            listeners: {
                                change: function (combo, newvalue) {
                                    // me.buildWidget(newvalue);
                                }
                            }
                        }
                    ]
                }
            ];

            me.bindStore(me.store || 'ext-empty-store', true);
            store = me.getStore();
            store.getProxy().extraParams = {
                usagePointMRID: me.usagePoint.get('mRID'),
                purposeId: defaultPurposeId,
                periodId: periodsStore.first().getId()
            };

            store.load();
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

    reconfigure: function() {
        var me = this,
            store = me.getStore(),
            tabs = [];

        store.each(function (item, index, total) {
            var mod = Math.floor(index / 3);
            if (!tabs[mod]) {
                tabs[mod] = me.addTab(mod)
            }

            tabs[mod].items.push(me.addWidget(item));
        });

        me.down('tabpanel').add(tabs);
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
            output: output,
            purpose: me.getPurpose(),
            router: me.router
        };
    }
});