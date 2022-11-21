Ext.define('Uni.view.toolbar.CommunicationCustomExporterButton', {
    extend: 'Ext.ux.exporter.ExporterButton',
    xtype: 'commcustomexporterbutton',
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
        me.widget - this
        me.callParent(arguments);
    },
    onAfterRender: function () {
        var me = this;
        me.component = me.up('grid');
        var componentStore = me.component.getStore();
        me.exportStore = Ext.create(componentStore.storeId);
        me.exportStore.proxy.url = componentStore.proxy.url;
        me.exportStore.proxy.timeout = 100000000;
        me.exportStore.remoteFilter = componentStore.remoteFilter;
        me.exportStore.remoteSort = componentStore.remoteSort;
        me.exportStore.proxy.extraParams = componentStore.proxy.extraParams;
        if (componentStore.lastOptions) {
            me.paramsOfLastLoading = componentStore.lastOptions.params;
        }
        me.temporaryGrid = me.createTemporaryGrid();
    },
    createTemporaryGrid: function () {
        var me = this;
        var config = me.component.initialConfig;
        config.store = me.exportStore;
        return Ext.widget(me.component.xtype, config);

    },
    onClick: function () {

        var me = this;
        if (me.disabled) {
            return;
        }
        if (me.up("pagingtoolbartop").needLazyExportInit) {

            me.temporaryGrid = me.createTemporaryGrid();

            for (var i = 0; i < me.component.columns.length; i++) {
                var column = me.component.columns[i];
                me.temporaryGrid.columns[i].setVisible(!column.hidden);
                me.temporaryGrid.columns[i].setText(column.text);
            }
        }

        me.component.getStore().load({
            callback: function (records, op, success) {
                if (records.length > 0 && this.filterParams.filter !== '[]') {
                    var customExporterWindow = Ext.create('Uni.util.customexport.CustomExporterWindow', {
                        grid: me.temporaryGrid,
                        gridStore: me.component.getStore(),
                        paramsOfLastLoading: me.paramsOfLastLoading,
                        title: Uni.I18n.translate('general.exportTable', 'UNI', 'Export table')
                    });
                } else {
                    var box = Ext.create('Ext.window.MessageBox', {
                        buttons: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.close', 'UNI', 'Close'),
                                action: 'close',
                                name: 'close',
                                ui: 'remove',
                                handler: function () {
                                    box.close();
                                }
                            }
                        ]
                    });
                    box.show({
                        title: Uni.I18n.translate('widget.dataCommunication.noFilters', 'UNI', 'No filters applied'),
                        msg: Uni.I18n.translate('communication.export.forward.failure', 'UNI', 'Failed to \'export\' data due to missing filter criteria. Please apply filter criteria then proceed.'),
                        modal: false,
                        ui: 'message-error',
                        icon: 'icon-warning2',
                        style: 'font-size: 34px;'
                    });
                }
            }
        });
    }
});