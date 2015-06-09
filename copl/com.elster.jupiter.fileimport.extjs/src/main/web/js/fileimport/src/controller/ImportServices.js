Ext.define('Fim.controller.ImportServices', {
    extend: 'Ext.app.Controller',
    requires: [
        'Fim.privileges.DataImport'
    ],
    views: [
        'Fim.view.importservices.Setup',
        'Fim.view.importservices.AddImportService',
        'Fim.view.importservices.Details',
        'Fim.view.importservices.Menu'
    ],
    stores: [
        'Fim.store.ImportServices',
        'Fim.store.FileImporters'
    ],
    models: [
        'Fim.model.ImportService',
        'Fim.model.FileImporter'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'fim-import-services-setup'
        },
        {
            ref: 'addPage',
            selector: 'fim-add-import-service'
        },
        {
            ref: 'importServicesGrid',
            selector: '#grd-import-services'
        },
        {
            ref: 'importServicePreviewContainerPanel',
            selector: '#pnl-import-service-preview'
        },
        {
            ref: 'importServiceOverview',
            selector: '#frm-import-service-details'
        }
    ],
    applicationName: null,

    init: function () {
        this.control({
            'fim-import-services-setup fim-import-services-grid': {
                select: this.showPreview
            },
            'fim-import-service-action-menu': {
                click: this.chooseImportServiceAction,
                show: this.onShowImportServiceMenu
            },
            'fim-add-import-service #btn-add': {
                click: this.addImportService
            },
            'fim-add-import-service #cbo-file-importer': {
                select: this.updateProperties
            },
            'fim-add-import-service': {
                displayinfo: this.displayInfo
            },
            'fim-import-service-preview-form': {
                displayinfo: this.displayInfo
            }
        });

        applicationName = typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : '';
    },

    showImportServices: function () {
        var me = this,
            view = Ext.widget('fim-import-services-setup', {
                router: me.getController('Uni.controller.history.Router')
            });
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showImportService: function (importServiceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            importServiceModel = me.getModel('Fim.model.ImportService'),
            view = Ext.widget('fin-details-import-service', {
                router: router,
                importServiceId: importServiceId
            }),
            actionsMenu = view.down('fim-import-service-action-menu');

        importServiceModel.load(importServiceId, {
            success: function (record) {
                var detailsForm = view.down('fim-import-service-preview-form'),
                    propertyForm = detailsForm.down('property-form');

                me.getApplication().fireEvent('changecontentevent', view);

                if (actionsMenu) {
                    actionsMenu.record = record;
                    actionsMenu.down('#view-import-service').hide();
                }
                view.down('#import-service-view-menu #import-services-view-link').setText(record.get('name'));
                me.getApplication().fireEvent('importserviceload', record);

                detailsForm.loadRecord(record);
                if (record.properties() && record.properties().count()) {
                    propertyForm.loadRecord(record);
                }
            }
        });
    },


    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('fim-import-service-preview'),
            previewForm = page.down('fim-import-service-preview-form'),
            propertyForm = previewForm.down('property-form');

        Ext.suspendLayouts();

        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('fim-import-service-action-menu').record = record;
        Ext.resumeLayouts();
    },

    chooseImportServiceAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getImportServicesGrid().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'viewImportService':
                location.href = '#/administration/importservices/' + record.get('id');
                break;
            case 'viewImportServiceHistory':
                location.href = '#/administration/importservices/' + record.get('id') + '/history';
                break;
            case 'activateimportservice':
                me.deactivate(record);
                break;
            case 'deactivateimportservice':
                me.deactivate(record);
                break;
            case 'editImportService':
                location.href = '#/administration/importservices/' + record.get('id') + '/edit';
                break;
            case 'removeImportService':
                me.remove(record);
                break;
        }
    },

    deactivate: function (record) {
        var me = this,
            isActive = record.get('active');

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: isActive ? Uni.I18n.translate('importService.deactivate.button', 'FIM', 'Deactivate') :
                Uni.I18n.translate('importService.activate.button', 'FIM', 'Activate')
        }).show({
            title: isActive ? Ext.String.format(Uni.I18n.translate('importService.deactivate.title', 'FIM', 'Deactivate \'{0}?\''), record.get('name')) :
                Ext.String.format(Uni.I18n.translate('importService.activate.title', 'FIM', 'Activate \'{0}?\''), record.get('name')),
            msg: isActive ? Uni.I18n.translate('importService.deactivate.message', 'FIM', 'Files in the import folder will no longer be imported.') :
                Uni.I18n.translate('importService.activate.message', 'FIM', 'Files will be imported as soon as they are placed in the import folder.'),
            fn: function (state) {
                if (state === 'confirm') {
                    me.deactivateImportService(record);
                }
            }
        });

    },

    deactivateImportService: function (record) {
        var me = this,
            importServicesGrid = me.getImportServicesGrid(),
            importServicePreviewContainerPanel = me.getImportServicePreviewContainerPanel(),
            importServiceOverview = me.getImportServiceOverview(),
            view = importServicesGrid || importServicePreviewContainerPanel || importServiceOverview,
            isActive = record.get('active');

        record.beginEdit();
        record.set('active', !isActive);
        record.endEdit(true);

        view.setLoading();
        record.save({
            params: {
                id: record.get('id')
            },
            success: function (rec, operation) {

                // the non-persisted attribute values are reset to their default values and their are not re-evaluated
                rec.set('statusDisplay', rec.get('active') ? Uni.I18n.translate('general.active', 'FIM', 'Active') : Uni.I18n.translate('general.inactive', 'FIM', 'Inactive'));

                if (importServicePreviewContainerPanel) {
                    importServicePreviewContainerPanel.down('#pnl-import-service-preview-form').loadRecord(rec);
                }

                if (importServiceOverview) {
                    importServiceOverview.loadRecord(rec);
                }

                me.getApplication().fireEvent('acknowledge', rec.get('active') ? Uni.I18n.translate('importService.activate.confirmation', 'FIM', 'Import service activated') :
                    Uni.I18n.translate('importService.deactivate.confirmation', 'FIM', 'Import service deactivated'));
            },
            callback: function () {
                view.setLoading(false);

            }
        });
    },

    remove: function (record) {
        var me = this;

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('importService.remove.button', 'FIM', 'Remove')
        }).show({
            title: Ext.String.format(Uni.I18n.translate('importService.remove.title', 'FIM', 'Remove \'{0}\'?'), record.get('name')),
            msg: Uni.I18n.translate('importService.remove.message', 'FIM', 'This importer service will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeImportService(record);
                }
            }
        });
    },

    removeImportService: function (record) {
        var me = this,
            importServicesGrid = me.getImportServicesGrid(),
            importServicePreviewContainerPanel = me.getImportServicePreviewContainerPanel(),
            importServiceOverview = me.getImportServiceOverview(),
            view = importServicesGrid || importServicePreviewContainerPanel || importServiceOverview;

        view.setLoading();

        record.destroy({
            success: function () {
                if (me.getPage()) {
                    importServicesGrid.down('pagingtoolbartop').totalCount = 0;
                    importServicesGrid.down('pagingtoolbarbottom').resetPaging();
                    importServicesGrid.getStore().getProxy().setExtraParam('application', me.applicationName);
                    importServicesGrid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/importservices').forward();
                }
                view.setLoading(false);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('importService.remove.confirmation', 'FIM', 'Import service removed'));

            },
            failure: function (object, operation) {
                view.setLoading(false);
            }
        });
    },

    onShowImportServiceMenu: function (menu) {
        var activate = menu.down('#activate-import-service'),
            deactivate = menu.down('#deactivate-import-service');
        activate && activate.setVisible(!menu.record.get('active'));
        deactivate && deactivate.setVisible(menu.record.get('active'));
    },

    showEditImportService: function (importServiceId) {
        var me = this,
            importServicesGrid = me.getImportServicesGrid(),
            importServicePreviewContainerPanel = me.getImportServicePreviewContainerPanel(),
            router = me.getController('Uni.controller.history.Router'),
            addImportServiceView, addImportServiceForm, returnLink;

        returnLink = importServicesGrid ? router.getRoute('administration/importservices').buildUrl() : router.getRoute('administration/importservices/importservice').buildUrl({importServiceId: importServiceId});
        addImportServiceView = Ext.create('Fim.view.importservices.AddImportService', {
            edit: true,
            returnLink: returnLink
        });

        var fileImporterCombo = addImportServiceView.down('#cbo-file-importer');
        fileImporterCombo.store.getProxy().setExtraParam('application', me.applicationName);
        fileImporterCombo.store.load({
            callback: function (records, operation, success) {
                if (fileImporterCombo.store.getCount() == 0) {
                    fileImporterCombo.allowBlank = true;
                    //fileImporterCombo.hide();
                    addImportServiceView.down('#no-file-importer').show();
                }

                var importServicesStore = Ext.create('Fim.store.ImportServices');
                importServicesStore.getProxy().setExtraParam('application', me.applicationName);
                importServicesStore.load({
                    callback: function (records, operation, success) {
                        var importServiceRecord = importServicesStore.getById(parseInt(importServiceId));
                        addImportServiceView.importServiceRecord = importServiceRecord;
                        me.getApplication().fireEvent('importserviceload', importServiceRecord);

                        addImportServiceForm = addImportServiceView.down('#frm-add-import-service');
                        addImportServiceForm.setTitle(Ext.String.format(Uni.I18n.translate('importService.edit', 'FIM', 'Edit \'{0}\''), importServiceRecord.get('name')));

                        //fileImporterCombo.setValue(fileImporterCombo.store.getById(importServiceRecord.get('importerName')));
                        //fileImporterCombo.setValue(importServiceRecord.get('importerName'));
                        addImportServiceForm.loadRecord(importServiceRecord);

                        if (importServiceRecord.properties() && importServiceRecord.properties().count()) {
                            addImportServiceForm.down('grouped-property-form').addEditPage = true;
                            addImportServiceForm.down('grouped-property-form').loadRecord(importServiceRecord);
                        }

                        me.getApplication().fireEvent('changecontentevent', addImportServiceView);
                    }
                });
            }
        });
    },

    addImportService: function (button) {
        var me = this,
            addPage = me.getAddPage(),
            importServiceRecord = addPage.importServiceRecord || Ext.create('Fim.model.ImportService'),
            addImportServiceForm = addPage.down('#frm-add-import-service'),
            formErrorsPanel = addImportServiceForm.down('#form-errors'),
            propertyForm = addImportServiceForm.down('grouped-property-form');

        propertyForm.updateRecord();
        if (!addImportServiceForm.isValid()) {
            formErrorsPanel.show();
        } else {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }
        }


        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }

        addImportServiceForm.updateRecord(importServiceRecord);
        importServiceRecord.beginEdit();
        if (propertyForm.getRecord()) {
            importServiceRecord.propertiesStore = propertyForm.getRecord().properties();
        }

        importServiceRecord.set('importerInfo', {
            name: addImportServiceForm.down('#cbo-file-importer').getValue()
        });
        importServiceRecord.set('destinationName', 'FileImport1');
        importServiceRecord.endEdit();

        importServiceRecord.save({
            success: function () {
                //if (button.action === 'edit') {
                me.getController('Uni.controller.history.Router').getRoute('administration/importservices/importservice').forward({importServiceId: importServiceRecord.getId()});
                //} else {
                //	me.getController('Uni.controller.history.Router').getRoute('administration/importservices').forward();
                //}
                if (button.action === 'edit') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('importService.successMsg.saved', 'FIM', 'Import service saved'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('importService.successMsg.added', 'FIM', 'Import service added'));
                }
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    addImportServiceForm.getForm().markInvalid(json.errors);
                }
                formErrorsPanel.show();
            }
        })

    },

    showAddImportService: function () {
        var me = this,
            importServicesGrid = me.getImportServicesGrid(),
            importServicePreviewContainerPanel = me.getImportServicePreviewContainerPanel(),
        //importServiceOverview = me.getImportServiceOverview(),
            router = me.getController('Uni.controller.history.Router'),
            addImportServiceView, addImportServiceForm, returnLink;

        returnLink = router.getRoute('administration/importservices').buildUrl();
        addImportServiceView = Ext.create('Fim.view.importservices.AddImportService', {
            edit: false,
            returnLink: returnLink
        });

        var fileImporterCombo = addImportServiceView.down('#cbo-file-importer');
        fileImporterCombo.store.getProxy().setExtraParam('application', me.applicationName);
        fileImporterCombo.store.load({
            callback: function (records, operation, success) {
                if (fileImporterCombo.store.getCount() == 0) {
                    fileImporterCombo.allowBlank = true;
                    //fileImporterCombo.hide();
                    addImportServiceView.down('#no-file-importer').show();
                }

                addImportServiceForm = addImportServiceView.down('#frm-add-import-service');
                addImportServiceForm.down('#num-folder-scan-frequency').setValue(1);
                addImportServiceForm.setTitle(Uni.I18n.translate('importService.add', 'FIM', 'Add import service'));

                me.getApplication().fireEvent('changecontentevent', addImportServiceView);
            }
        });
    },

    updateProperties: function (control, records) {
        var me = this,
            addImportServiceView = me.getAddPage(),
            record = records[0],
            propertyForm = addImportServiceView.down('grouped-property-form');

        if (record && record.properties() && record.properties().count()) {
            propertyForm.addEditPage = true;
            propertyForm.loadRecord(record);
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
    },

    displayInfo: function (panel) {

        infoDialog = Ext.create('widget.window', {
            title: Uni.I18n.translate('importService.filePatternInfo', 'FIM', 'File pattern info'),
            closable: true,
            overflowY: 'auto',
            modal: true,
            width: 600,
            minWidth: 350,
            height: 350,
            layout: {
                type: 'border',
                padding: 5
            },
            items: [
                {
                    xtype: 'container',
                    html: Uni.I18n.translate('importService.filePatternInfo.title', 'FIM', 'Pattern to filter which files will be importer, based on file name and/or extension.') + '<br><br>' +
                    Uni.I18n.translate('importService.filePatternInfo.example', 'FIM', 'Here are some examples of pattern syntax:') + '<br><ul>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex1', 'FIM', '*.html – Matches all strings that end in .html') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex2', 'FIM', '??? – Matches all strings with exactly three letters or digits') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex3', 'FIM', '*[0-9]* – Matches all strings containing a numeric value') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex4', 'FIM', '*.{htm,html,pdf} – Matches any string ending with .htm, .html or .pdf') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex5', 'FIM', 'a?*.java – Matches any string beginning with a, followed by at least one letter or digit, and ending with .java') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex6', 'FIM', '{foo*,*[0-9]*} – Matches any string beginning with foo or any string containing a numeric value') + '</li>' + '<br></ul>'
                }
            ]
        });

        infoDialog.show();
    }


});