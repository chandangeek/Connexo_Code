Ext.define('Mdc.controller.setup.DataCollectionKpi', {
    extend: 'Ext.app.Controller',
    stores: [
        'Mdc.store.DataCollectionKpis',
        'Mdc.store.DataCollectionKpiFrequency',
        'Mdc.store.MeterExportGroups',
        'Mdc.store.DataCollectionKpiRange'
    ],
    models: [
        'Mdc.model.DataCollectionKpi'
    ],

    views: [
        'Mdc.view.setup.datacollectionkpis.Setup',
        'Mdc.view.setup.datacollectionkpis.Edit'
    ],
    refs: [
        {ref: 'dataCollectionKpisPreviewForm', selector: '#dataCollectionKpisDetailsForm'},
        {ref: 'dataCollectionKpisGrid', selector: '#datacollectionkpisgrid'},
        {ref: 'dataCollectionKpiEditForm', selector: '#dataCollectionKpiEditForm'},
        {ref: 'dataCollectionKpisPreviewContainer', selector: '#datacollectionkpispreview'},
        {ref: 'kpiErrorContainer', selector: '#kpiErrorContainer'},
        {ref: 'dataCollectionKpisSetup', selector: 'dataCollectionKpiSetup'}
    ],


    init: function () {
        this.control({
            '#dataCollectionKpiSetup #datacollectionkpisgrid': {
                select: this.showKpiPreview
            },
            '#dataCollectionKpiSetup button[action=addDataCollectionKpi]': {
                click: this.moveToCreatePage
            },
            'dataCollectionKpisActionMenu': {
                click: this.chooseAction
            },
            '#dataCollectionKpiEdit button[action=cancelAction]': {
                click: this.moveToViewPage
            },
            '#dataCollectionKpiEdit #createEditButton': {
                click: this.saveModel
            },
            'dataCollectionKpiEdit #cmb-frequency': {
                change: this.onFrequencyChange
            }
        });
    },

    moveToViewPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/datacollectionkpis').forward();
    },

    moveToCreatePage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/datacollectionkpis/add').forward();
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        switch (item.action) {
            case 'edit':
                router.getRoute('administration/datacollectionkpis/edit').forward({id: menu.record.get('id')});
                break;
            case 'remove':
                Ext.create('Uni.view.window.Confirmation').show({
                    title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' \'' + menu.record.get('deviceGroup').name + '\'?',
                    msg: Uni.I18n.translate('datacollectionkpis.deleteConfirmation.msg', 'MDC', 'This data collection KPI will no longer be available on connections &#38; communications overview.'),
                    fn: function (state) {
                        switch (state) {
                            case 'confirm':
                                me.removeKpi(menu.record);
                                break;
                        }
                    }
                });
        }
    },

    showKpiPreview: function (selectionModel, record) {
        var preview = this.getDataCollectionKpisPreviewContainer();

        Ext.suspendLayouts();
        preview.down('dataCollectionKpisActionMenu').record = record;
        preview.setTitle(Ext.String.htmlEncode(record.get('deviceGroup').name));
        this.getDataCollectionKpisPreviewForm().loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showErrorPanel: function (value) {
        var editForm = this.getDataCollectionKpiEditForm(),
            errorPanel = editForm.down('#form-errors');

        editForm.getForm().clearInvalid();
        errorPanel.setVisible(value);
    },

    saveModel: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getDataCollectionKpiEditForm(),
            record = editForm.getRecord(),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            displayRange = editForm.down('[name=displayRange]').getValue(),
            connectionTarget = editForm.down('#connectionKpiField').getValue(),
            communicationTarget = editForm.down('#communicationKpiField').getValue(),
            kpiMessageContainer = me.getKpiErrorContainer();

        kpiMessageContainer.hide();

        me.showErrorPanel(false);
        editForm.setLoading();
        record.beginEdit();
        if (!record.getId()) {
            record.set('deviceGroup', deviceGroup);
        } else {
            record.set('deviceGroup', {
                id: record.get('deviceGroup').id
            });
        }
        if (frequency) {
            record.set('frequency', me.getStore('Mdc.store.DataCollectionKpiFrequency').getById(frequency).get('value'));
        }
        if (displayRange) {
            record.set('displayRange', me.getStore('Mdc.store.DataCollectionKpiRange').getById(displayRange).get('value'));
        }
        record.set('communicationTarget', communicationTarget);
        record.set('connectionTarget', connectionTarget);
        record.endEdit();
        record.save({
            success: function (record, operation) {
                var successMessage = '';

                switch (operation.action) {
                    case 'update':
                        successMessage = Uni.I18n.translate('datacollectionkpis.saved', 'MDC', 'Data collection KPI saved');
                        break;
                    case 'create':
                        successMessage = Uni.I18n.translate('datacollectionkpis.added', 'MDC', 'Data collection KPI added');
                        break;
                }

                router.getRoute('administration/datacollectionkpis').forward();
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    me.showErrorPanel(true);
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function (error) {
                                if (error.id === 'communicationKpi') {
                                    kpiMessageContainer.update(error.msg);
                                    kpiMessageContainer.show();
                                }
                                if (error.id === 'endDeviceGroup') {
                                    deviceGroupCombo.markInvalid(error.msg);
                                }
                            });
                            editForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            },
            callback: function() {
                editForm.setLoading(false);
            }
        });
    },


    showDataCollectionKpiView: function () {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('dataCollectionKpiSetup'));
    },

    showDataCollectionKpiEditView: function (id) {
        var me = this,
            widget = Ext.widget('dataCollectionKpiEdit'),
            deviceGroupStore = widget.down('combobox[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.model.DataCollectionKpi'),
            form = widget.down('#dataCollectionKpiEditForm'),
            deviceGroupCombo = widget.down('#cmb-device-group'),
            deviceGroupDisplayField = widget.down('#devicegroupDisplayField'),
            createBtn = widget.down('#createEditButton');

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        deviceGroupStore.load({
            callback: function () {
                if (!Ext.isEmpty(id)) {
                    widget.down('#dataCollectionKpiEditForm').setTitle(Uni.I18n.translate('datacollectionkpis.editDataCollectionKpi', 'MDC', 'Edit data collection KPI'));
                    kpiModel.load(id, {
                        success: function (kpiRecord) {
                            var editTitle = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + Ext.String.htmlEncode(kpiRecord.get('deviceGroup').name) + "'";

                            Ext.suspendLayouts();
                            form.loadRecord(kpiRecord);
                            form.down('[name=deviceGroup]').disable();
                            form.down('[name=frequency]').disable();
                            widget.down('#dataCollectionKpiEditForm').setTitle(editTitle);
                            me.getApplication().fireEvent('loadDataCollectionKpi', editTitle);
                            createBtn.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                            createBtn.action = 'save';
                            Ext.resumeLayouts(true);

                            widget.setLoading(false);
                        }
                    });
                } else {
                    if (deviceGroupStore.getCount() > 0) {
                        form.loadRecord(Ext.create(kpiModel));
                    } else {
                        Ext.suspendLayouts();
                        deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('datacollectionkpis.noDeviceGroup', 'MDC', 'No device group defined yet.') +  '</span>' );
                        deviceGroupDisplayField.show();
                        deviceGroupCombo.hide();
                        createBtn.disable();
                        Ext.resumeLayouts(true);
                    }
                    widget.down('#dataCollectionKpiEditForm').setTitle(Uni.I18n.translate('datacollectionkpis.addDataCollectionKpi', 'MDC', 'Add data collection KPI'));
                    widget.setLoading(false);
                }
            }
        });
    },

    onFrequencyChange: function (combo, newValue) {
        var me = this,
            displayRangeCombo = me.getDataCollectionKpiEditForm().down('#cmb-display-range');

        Ext.suspendLayouts();
        if (newValue) {
            me.getStore('Mdc.store.DataCollectionKpiRange').filterByFrequency(newValue);
            displayRangeCombo.enable();
        } else {
            displayRangeCombo.disable();
        }
        displayRangeCombo.reset();
        Ext.resumeLayouts(true);
    },

    removeKpi: function (record) {
        var me = this,
            page = me.getDataCollectionKpisSetup(),
            grid = me.getDataCollectionKpisGrid(),
            gridToolbarTop = grid.down('pagingtoolbartop');

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            success: function () {
                gridToolbarTop.isFullTotalCount = false;
                gridToolbarTop.totalCount = -1;
                grid.down('pagingtoolbarbottom').totalCount--;
                grid.getStore().loadPage(1);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('datacollectionkpis.kpiRemoved', 'MDC', 'Data collection KPI removed'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});
