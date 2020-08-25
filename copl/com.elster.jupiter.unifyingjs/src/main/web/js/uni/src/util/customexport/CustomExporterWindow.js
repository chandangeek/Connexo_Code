/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.customexport.CustomExporterWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.custom-exporter-window',
    itemId: 'exportTypeWindow',
    closable: false,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    floating: true,
    button: null,
    startRow: 0,
    exportedRowsCount: 10,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.customexport.CustomExportTypeStore'
    ],
    initComponent: function () {

        var me = this;
        var queryString = Uni.util.QueryString.getQueryStringValues(false);

        var startRow = queryString && queryString.limit && parseInt(queryString.start);
        var rowsCount = queryString && queryString.limit && parseInt(queryString.limit);

        me.startRow = startRow || 0;
        me.exportedRowsCount = rowsCount || 10;

        me.items = {
            xtype: 'form',
            border: false,
            itemId: 'eventTypeForm',
            width: 400,
            layout: {
                type: 'vbox'
            },
            defaults: {
                labelWidth: 180
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    itemId: 'form-errors',
                    margin: '10 0 10 0',
                    hidden: true
                },
                {
                    xtype: 'container',
                    width: '100%',
                    margin: '20 50 10 10',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                    {
                        xtype: 'combobox',
                        itemId: 'exporttype-customrows-type-combo',
                        store: Ext.create('Uni.util.customexport.CustomExportTypeStore'),
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        forceSelection: true,
                        value: 'EXPORT_CUSTOM_NUMBER',
                        width: 130,
                        listeners: {
                            change: function(field, val) {
                                var form = field.up('form');
                                form.down('#exporttype-customrows-end-row').setVisible(val === "EXPORT_CUSTOM_NUMBER");
                                form.down('#exporttype-allexport-type-help').setVisible(val !== "EXPORT_CUSTOM_NUMBER");
                            }
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('general.export.allExportTooltip', 'UNI', "The limit is 100 000 rows"),
                        text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                        disabled: true,
                        ui: 'blank',
                        hidden: true,
                        margin: '8 0 0 8',
                        itemId: 'exporttype-allexport-type-help',
                        shadow: false,
                        width: 16
                    }]
                },
                {
                    xtype: 'container',
                    width: '100%',
                    margin: '6 10 5 70',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    items: [
                    {
                        xtype: 'numberfield',
                        itemId: 'exporttype-customrows-start-row',
                        width: 250,
                        required: true,
                        fieldLabel: Uni.I18n.translate('export.exportType.startRow', 'UNI', 'Start from row'),
                        value: me.startRow,
                        hideTrigger: true,
                        keyNavEnabled: false,
                        mouseWheelEnabled: false,
                        allowBlank: false,
                        listeners : {
                            change: function(field, value){
                                if (value > 99999){
                                    field.setValue(99999);
                                }else if (value < 0){
                                    field.setValue(0);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('general.export.startRowTooltip', 'UNI', "Export will be done starting from the row following the selected number. Choose '0' to start from the first row on the top of the first page"),
                        text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                        disabled: true, // to avoid a hand cursor
                        ui: 'blank',
                        itemId: 'exporttype-customrows-start-row-help',
                        shadow: false,
                        margin: '6 3 0 6',
                        width: 16
                    }]
                },
				{
                    xtype: 'numberfield',
                    itemId: 'exporttype-customrows-end-row',
                    required: true,
                    width: 330,
                    style: {
                        'margin-top': '5px',
                        'margin-left': '-10px'
                    },
                    fieldLabel: Uni.I18n.translate('export.exportType.rowsCount', 'UNI', 'Number of exported rows'),
                    value: me.exportedRowsCount,
                    hideTrigger: true,
                    keyNavEnabled: false,
                    mouseWheelEnabled: false,
                    allowBlank: false,
                    listeners : {
                        change: function(field, value){
                            if (value > 100000){
                                field.setValue(100000);
                            }else if (value < 1){
                                field.setValue(1);
                            }
                        }
                    }
                }
             ]
        },

        me.bbar = [
            {
                xtype: 'container',
                width: 100
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.customExport','UNI','Export'),
                ui: 'action',
                itemId: 'export-table-button',
                listeners: {
                    click: this.exportTable
                },
                scope: me
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.cancel','UNI','Cancel'),
                action: 'cancel',
                ui: 'link',
                listeners: {
                    click: {
                        fn: function () {
                            this.up('#exportTypeWindow').destroy();
                        }
                    }
                }
            }
        ],
        me.callParent(arguments);

    },
    saveDataToFile: function(res){
        var title = "export";
        var filename = title + "_" + Ext.Date.format(new Date(), "Y-m-d h:i:s") + "." + res.ext;
        var blob = new Blob([res.data], {  type: res.mimeType + ";charset=" + res.charset + ","});
        var downloadUrl = window.URL.createObjectURL(blob);
        var a = document.createElement("a");
        a.href = downloadUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        URL.revokeObjectURL(downloadUrl);
    },

    getAdditionalParamsFromQueryString : function(){

         var queryString = Uni.util.QueryString.getQueryStringValues(false);
         var params = {};

         if (queryString.sort) {
             params.sort = queryString.sort;
         }

         /*var result = [];
         for (var dataIndex in queryString) {
                if (dataIndex === 'sort' || dataIndex === 'start' || dataIndex === 'limit'){
                    continue;
                }
                var value = queryString[dataIndex];

                if (queryString.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                    if(!Ext.isArray(value)){
                        value = [value];
                    }
                    var filter = {
                        property: dataIndex,
                        value: isNaN(value) ? value : parseInt(value)
                    };

                    result.push(filter);
                }
            }
            if (result.length > 0) {
                params.filter = Ext.encode(result);
            }*/

            return params;
    },
    exportTable : function(){

         var me = this;
         var exportWindow = this.up('#exportTypeWindow');
         var title = "export";
         var startRow = exportWindow.down('#exporttype-customrows-start-row').getValue();
         var rowsCount = exportWindow.down('#exporttype-customrows-type-combo').getValue() === 'EXPORT_CUSTOM_NUMBER' ? exportWindow.down('#exporttype-customrows-end-row').getValue() : 100000;
         var form = exportWindow.down('form');
         var errMsgPanel = form.down('#form-errors');
         var exportGrid = exportWindow.grid;

         errMsgPanel.hide();
         if (!form.isValid()) {
              errMsgPanel.show();
              return;
         }
         var progressbar = form.add({
                xtype: 'progressbar',
                itemId: 'snooze-progressbar',
                margin: '5 0 15 0'
         });
         progressbar.wait({
            duration: 10000,
            interval: 100
         });

         var startTime = Uni.DateTime.formatDateTimeLong(new Date());

         var waitMessage = form.add({
                xtype: 'component',
                itemId: 'exporttype-waitmessage',
                html: '<span>' + Uni.I18n.translate('general.startedOn', 'UNI', 'Started on') + ':' + startTime + '</spzn><br>' +
                      '<span>' + Uni.I18n.translate('export.exportType.keepPopup.', 'UNI', 'Keep this pop-up window open while export is ongoing') + '</spzn>',
                margin: '5 0 15 0'
         });
         exportWindow.down('#export-table-button').setDisabled(true);
         form.setDisabled(true);
         var options = {
            start : startRow,
            limit : rowsCount,
            params : {},
            callback : function(records, operation, success){
                if (success === true){
                    exportWindow.saveDataToFile(Ext.ux.exporter.Exporter.exportAny(exportGrid, "csv", {title: title}));
                    //exportWindow.getController('Uni.controller.history.Router').getApplication().fireEvent('acknowledge', Uni.I18n.translate('appServers.configureImportServicesSuccess', 'APR', 'Import services saved'));
                    exportWindow.destroy();
                }else{
                    exportWindow.down('#export-table-button').setDisabled(false);
                    form.setDisabled(false);
                    form.remove(progressbar);
                    form.remove(waitMessage);
                    errMsgPanel.setText(Uni.I18n.translate('export.exportType.exportFailed', 'UNI', 'Data export was failed'));
                    errMsgPanel.show();

                }
            }
         }


         for (var param in exportWindow.paramsOfLastLoading){
            if (!exportGrid.store.proxy.extraParams[param]){
                options.params[param] =  exportWindow.paramsOfLastLoading[param];
            }
         }

         Ext.apply(options.params, exportWindow.gridStore.filterParams);

         var sortParams = exportWindow.getAdditionalParamsFromQueryString();
         Ext.apply(options.params, sortParams);

         exportGrid.store.load(options)
     }

});

