/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.controller.DataValidationKpi', {
    extend: 'Ext.app.Controller',
    stores: [
        'Cfg.store.DataValidationKpis',
        'Cfg.store.DataValidationKpiFrequency',
        'Cfg.store.DataValidationGroups'
    ],
    models: [
        'Cfg.model.DataValidationKpi'
    ],

    views: [
        'Cfg.view.datavalidationkpis.Setup',
        'Cfg.view.datavalidationkpis.Add'
    ],
    refs: [
        {ref: 'dataValidationKpisSetup', selector: 'cfg-data-validation-kpi-setup'},
        {ref: 'dataValidationKpisGrid', selector: '#data-validation-kpis-grid'},
        {ref: 'dataValidationKpisPreviewContainer', selector: '#preview-data-validation-kpis'},
        {ref: 'dataValidationKpisPreviewForm', selector: '#data-validation-kpis-details-form'},
        {ref: 'dataValidationKpiEditForm', selector: '#frm-data-validation-kpi-add'}
    ],

    init: function () {
        this.control({
            '#data-validation-kpis-preview-action-menu': {
                click: this.chooseAction
            },
            'cfg-data-validation-kpi-setup #data-validation-kpis-grid': {
                select: this.showKpiPreview
            },
            'cfg-data-validation-kpis-grid actioncolumn': {
                remove: this.removeDataValidationKPI
            },
            'cfg-data-validation-kpi-setup button[action=addDataValidationKpi]': {
                click: this.addDataValidationKpi
            },
            'cfg-data-validation-kpi-add button[action=cancel-add-button]': {
                click: this.cancelDataValidationKpi
            },
            'cfg-data-validation-kpi-add #create-add-button': {
                click: this.saveDataValidationKPI
            }
        });
    },

    showDataValidationKPIs: function () {
        var me = this,
            view = Ext.widget('cfg-data-validation-kpi-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showKpiPreview: function (selectionModel, record) {
        var preview = this.getDataValidationKpisPreviewContainer();

        Ext.suspendLayouts();
        if (preview.down('cfg-data-validation-kpis-action-menu')) {
            preview.down('cfg-data-validation-kpis-action-menu').record = record;
        }
        preview.setTitle(Ext.String.htmlEncode(record.get('deviceGroup').name));
        this.getDataValidationKpisPreviewForm().loadRecord(record);
        Ext.resumeLayouts(true);
    },

    removeDataValidationKPI: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removex.kpi', 'CFG', "Remove '{0}'?", [record.get('deviceGroup').name]),
            msg: Uni.I18n.translate('dataqualitykpis.deleteConfirmation.msg', 'CFG', 'This data quality KPI will no longer be available in the system. Already calculated data will not be removed.'),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeKpi(record);
                        break;
                }
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'remove':
                me.removeDataValidationKPI(menu.record);
        }
    },

    removeKpi: function (record) {
        var me = this,
            page = me.getDataValidationKpisSetup(),
            grid = me.getDataValidationKpisGrid(),
            gridToolbarTop = grid.down('pagingtoolbartop');

        page.setLoading(Uni.I18n.translate('general.removing', 'CFG', 'Removing...'));
        record.destroy({
            success: function () {
                gridToolbarTop.isFullTotalCount = false;
                gridToolbarTop.totalCount = -1;
                grid.down('pagingtoolbarbottom').totalCount--;
                grid.getStore().loadPage(1);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataqualitykpis.kpiRemoved', 'CFG', 'Data quality KPI scheduled for removal'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    addDataValidationKpi: function () {
        var me = this;

        me.getController('Uni.controller.history.Router').getRoute('administration/datavalidationkpis/add').forward();
    },

    cancelDataValidationKpi: function () {
        var me = this;

        me.getController('Uni.controller.history.Router').getRoute('administration/datavalidationkpis').forward();
    },

    showAddDataValidationKpi: function () {
        var me = this,
            widget = Ext.widget('cfg-data-validation-kpi-add'),
            deviceGroupStore = widget.down('combobox[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Cfg.model.DataValidationKpi'),
            form = widget.down('#frm-data-validation-kpi-add'),
            deviceGroupCombo = widget.down('#cmb-device-group'),
            deviceGroupDisplayField = widget.down('#device-group-field'),
            createEditBtn = widget.down('#create-add-button');

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        deviceGroupStore.load({
            callback: function () {
                if (deviceGroupStore.getCount() > 0) {
                    form.loadRecord(Ext.create(kpiModel));
                } else {
                    Ext.suspendLayouts();
                    deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('datavalidationkpis.noDeviceGroup', 'CFG', 'No device group available.') + '</span>');
                    deviceGroupDisplayField.show();
                    deviceGroupCombo.hide();
                    createEditBtn.disable();
                    Ext.resumeLayouts(true);
                }
                widget.down('#frm-data-validation-kpi-add').setTitle(Uni.I18n.translate('dataqualitykpis.add', 'CFG', 'Add data quality KPI'));
                widget.setLoading(false);
            }
        });
    },

    saveDataValidationKPI: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getDataValidationKpiEditForm(),
            record = editForm.getRecord(),
            frequency = editForm.down('[name=frequency]').getValue(),
            backUrl = router.getRoute('administration/datavalidationkpis').buildUrl(),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            };

        me.showErrorPanel(false);
        editForm.setLoading();
        record.beginEdit();
        if (!record.getId()) {
            record.set('deviceGroup', deviceGroup);
        } else {
            record.set('deviceGroup', {
                id: record.get('deviceGroup').id,
                name: record.get('deviceGroup').name
            });
        }
        if (frequency) {
            record.set('frequency', me.getStore('Cfg.store.DataValidationKpiFrequency').getById(frequency).get('value'));
        }
        record.endEdit();
        record.save({
            backUrl: backUrl,
            success: function (record, operation) {

                window.location.href = backUrl;
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataqualitykpis.added', 'CFG', 'Data quality KPI added'));
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    me.showErrorPanel(true);
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function (error) {

                                if (error.id === 'endDeviceGroup') {
                                    editForm.down('[name=deviceGroup]').markInvalid(error.msg);
                                }
                            });
                            editForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            },
            callback: function () {
                editForm.setLoading(false);
            }
        });
    },

    showErrorPanel: function (value) {
        var editForm = this.getDataValidationKpiEditForm(),
            errorPanel = editForm.down('#form-errors');

        editForm.getForm().clearInvalid();
        errorPanel.setVisible(value);
    }
});
