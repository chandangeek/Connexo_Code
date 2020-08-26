Ext.define('Uni.view.toolbar.CustomExporterButton', {
    extend: 'Ext.ux.exporter.ExporterButton',
    xtype: 'customexporterbutton',
    ui: 'icon',
    iconCls: 'icon-file-download',
    text: '',
    requires: [
        'Uni.util.customexport.CustomExporterWindow',
        'Uni.util.customexport.CustomExportTypeStore'
    ],
    initComponent: function () {
        var me = this;
        me.on({
            afterrender: {
                fn: me.onAfterRender,
                scope: me,
                single: true
            }
        });
        me.callParent(arguments);
    },
    onAfterRender: function(){
        var me = this;
        me.component = me.up('grid');
        var componentStore = me.component.getStore();
        me.exportStore = Ext.create(componentStore.storeId);
        me.exportStore.proxy.url = componentStore.proxy.url;
        me.exportStore.proxy.timeout = 100000000;
        me.exportStore.remoteFilter = componentStore.remoteFilter;
        me.exportStore.remoteSort = componentStore.remoteSort;
        me.exportStore.proxy.extraParams = componentStore.proxy.extraParams;
        if (componentStore.lastOptions){
            me.paramsOfLastLoading = componentStore.lastOptions.params;
        }
        me.temporaryGrid = me.createTemporaryGrid();
    },
    createTemporaryGrid: function(){
        var me = this;
        var config = me.component.initialConfig;
        config.store = me.exportStore;
        return Ext.widget(me.component.xtype, config);

    },
    onClick: function(){

        var me = this;

        var customExporterWindow = Ext.create('Uni.util.customexport.CustomExporterWindow', {
              grid: me.temporaryGrid,
              gridStore: me.component.getStore(),
              paramsOfLastLoading: me.paramsOfLastLoading,
              title: Uni.I18n.translate('general.exportTable', 'UNI', 'Export table')
        });

    }
});
