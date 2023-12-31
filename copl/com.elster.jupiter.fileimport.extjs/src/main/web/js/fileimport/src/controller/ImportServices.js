/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.controller.ImportServices', {
    extend: 'Ext.app.Controller',
    requires: [
        'Fim.privileges.DataImport',
        'Ldr.store.LogLevels'
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
        'Fim.model.ImportServiceDetails',
        'Fim.model.FileImporter',
        'Ldr.model.LogLevel'
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
            ref: 'detailsImportService',
            selector: 'fin-details-import-service'
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

    triggeredFromGrid: false,

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
            importServiceModelDetails = me.getModel('Fim.model.ImportServiceDetails'),
            view = Ext.widget('fin-details-import-service', {
                router: router,
                importServiceId: importServiceId
            }),
            actionsMenu = view.down('fim-import-service-action-menu');

        importServiceModelDetails.load(importServiceId, {
            success: function (recordDetails) {
                var detailsForm = view.down('fim-import-service-preview-form'),
                    propertyForm = detailsForm.down('property-form');

                me.getApplication().fireEvent('changecontentevent', view);

                if (!recordDetails.get('deleted')) {
                    importServiceModel.load(importServiceId, {
                        success: function (record) {
                            if (actionsMenu) {
                                actionsMenu.record = record;
                            }
                        }
                    });
                }
                else {
                    actionsMenu.record = recordDetails;
                }

                view.down('#import-service-view-menu').setHeader(recordDetails.get('name'));
                new Ext.ToolTip({
                    target: view.down('#dsf-status-display').getEl(),
                    dismissDelay: 5000,
                    html: recordDetails.get('statusTooltip')
                });

                me.getApplication().fireEvent('importserviceload', recordDetails.get('name'));

                detailsForm.loadRecord(recordDetails);
                if (recordDetails.get('importerAvailable') && recordDetails.properties() && recordDetails.properties().count()) {
                    propertyForm.loadRecord(recordDetails);
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
        new Ext.ToolTip({
            target: previewForm.down('#dsf-status-display').getEl(),
            dismissDelay: 5000,
            html: record.get('statusTooltip')
        });
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
                me.triggeredFromGrid = !Ext.isEmpty(me.getImportServicesGrid());
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
            confirmText: isActive ? Uni.I18n.translate('general.deactivate', 'FIM', 'Deactivate') :
                Uni.I18n.translate('general.activate', 'FIM', 'Activate')
        }).show({
            title: isActive ? Ext.String.format(Uni.I18n.translate('importService.deactivate.title', 'FIM', 'Deactivate \'{0}\'?'), record.get('name')) :
                Ext.String.format(Uni.I18n.translate('importService.activate.title', 'FIM', 'Activate \'{0}\'?'), record.get('name')),
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
                var statusDisplay = record.get('deleted') ? Uni.I18n.translate('general.removed', 'FIM', 'Removed') :
                    !record.get('importerAvailable') ? Uni.I18n.translate('general.notAvailable', 'FIM', 'Not available') :
                        !record.get('active') ? Uni.I18n.translate('general.inactive', 'FIM', 'Inactive') :
                            !record.get('scheduled') ? Uni.I18n.translate('general.notScheduled', 'FIM', 'Not scheduled') :
                                Uni.I18n.translate('general.active', 'FIM', 'Active');
                rec.set('statusDisplay', statusDisplay);

                var statusTooltip = record.get('deleted') ? Uni.I18n.translate('importService.status.removed', 'FIM', 'This import service has been removed.') :
                        !record.get('importerAvailable') ? Uni.I18n.translate('importService.status.notAvailable', 'FIM', "This import service's configured file importer is not available and it will not be executed.") :
                            !record.get('active') ? Uni.I18n.translate('importService.status.inactive', 'FIM', 'This import service is inactive and it will not be executed.') :
                                !record.get('scheduled') ? Uni.I18n.translate('importService.status.notScheduled', 'FIM', 'This import service has not been configured on any application server and it will not be executed.') :
                                    Uni.I18n.translate('importService.status.active', 'FIM', 'This import service is active and it will be executed by an application server.');

                rec.set('statusTooltip', statusTooltip);
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

        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'FIM', 'Remove')
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
        var me = this,
            activate = menu.down('#activate-import-service'),
            deactivate = menu.down('#deactivate-import-service'),
            edit = menu.down('#edit-import-service'),
            remove = menu.down('#remove-import-service'),
            history = menu.down('#view-history-import-service'),
            active = menu.record.get('active');


        if (menu.record.get('deleted')) {
            edit && edit.setVisible(false);
            remove && remove.setVisible(false);
            activate && activate.setVisible(false);
            deactivate && deactivate.setVisible(false);
        }
        else if (!menu.record.get('importerAvailable')) {
            edit && edit.setVisible(false);
            activate && activate.setVisible(false);
            deactivate && deactivate.setVisible(false);
        }
        else {
            edit && edit.setVisible(true);
            remove && remove.setVisible(true);
            history && history.setVisible(!me.getDetailsImportService());
            activate && activate.setVisible(!active);
            deactivate && deactivate.setVisible(active);
        }
        menu.fireEvent('refreshMenuSeparators', menu);
    },

    showEditImportService: function (importServiceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            addImportServiceView, addImportServiceForm, returnLink;

        returnLink = me.triggeredFromGrid ? router.getRoute('administration/importservices').buildUrl() : router.getRoute('administration/importservices/importservice').buildUrl({importServiceId: importServiceId});
        addImportServiceView = Ext.create('Fim.view.importservices.AddImportService', {
            edit: true,
            returnLink: returnLink
        });

        var fileImporterCombo = addImportServiceView.down('#cbo-file-importer');
        fileImporterCombo.store.load({
            callback: function (records, operation, success) {
                if (fileImporterCombo.store.getCount() == 0) {
                    fileImporterCombo.allowBlank = true;
                    addImportServiceView.down('#no-file-importer').show();
                }

                var importService = me.getModel('Fim.model.ImportService');
                importService.load(importServiceId, {
                    success: function (importServiceRecord) {
                        addImportServiceView.importServiceRecord = importServiceRecord;
                        me.getApplication().fireEvent('importserviceload', importServiceRecord.get('name'));

                        addImportServiceForm = addImportServiceView.down('#frm-add-import-service');
                        addImportServiceForm.setTitle(Ext.String.format(Uni.I18n.translate('importService.edit', 'FIM', 'Edit \'{0}\''), importServiceRecord.get('name')));

                        addImportServiceForm.loadRecord(importServiceRecord);

                        if (importServiceRecord.properties() && importServiceRecord.properties().count()) {
                            addImportServiceForm.down('grouped-property-form').addEditPage = true;
                            addImportServiceForm.down('grouped-property-form').loadRecord(importServiceRecord);
                        }
                        me.getApplication().fireEvent('changecontentevent', addImportServiceView);
                        addImportServiceForm.doLayout();

                    }
                });
            }
        });
    },

    addImportService: function (button) {
        var me = this,
            addPage = me.getAddPage(),
            router = me.getController('Uni.controller.history.Router'),
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

            addImportServiceForm.updateRecord(importServiceRecord);
            importServiceRecord.beginEdit();
            if (propertyForm.getRecord()) {
                importServiceRecord.propertiesStore = propertyForm.getRecord().properties();
            }

            importServiceRecord.set('importerInfo', {
                name: addImportServiceForm.down('#cbo-file-importer').getValue()
            });

            importServiceRecord.set('logLevel', addImportServiceForm.down('#fim-data-logLevels').getValue());

            importServiceRecord.endEdit();

            importServiceRecord.save({
                backUrl: me.getAddPage().returnLink,
                success: function () {
                    location.href = me.getAddPage().returnLink
                        ? me.getAddPage().returnLink
                        : router.getRoute('administration/importservices/importservice').buildUrl({importServiceId: importServiceRecord.getId()});

                    if (button.action === 'edit') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('importService.successMsg.saved', 'FIM', 'Import service saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('importService.successMsg.added', 'FIM', 'Import service added'));
                    }
                },
                failure: function (record, operation) {
                    if (operation.response.status == 400) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            addImportServiceForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            })
        }
    },

    showAddImportService: function () {
        var me = this,
            addImportServiceView, addImportServiceForm, returnLink;

        addImportServiceView = Ext.create('Fim.view.importservices.AddImportService', {
            edit: false,
            returnLink: me.getController('Uni.controller.history.Router').getRoute('administration/importservices').buildUrl()
        });

        var logLevelCombo = addImportServiceView.down('#fim-data-logLevels');
        logLevelCombo.store.load();

        var fileImporterCombo = addImportServiceView.down('#cbo-file-importer');
        fileImporterCombo.store.load({
            callback: function (records, operation, success) {
                if (fileImporterCombo.store.getCount() == 0) {
                    fileImporterCombo.allowBlank = true;
                    //fileImporterCombo.hide();
                    addImportServiceView.down('#no-file-importer').show();
                }

                addImportServiceForm = addImportServiceView.down('#frm-add-import-service');
                addImportServiceForm.down('#num-folder-scan-frequency').setValue(1);
                addImportServiceForm.setTitle(Uni.I18n.translate('general.addImportService', 'FIM', 'Add import service'));

                me.getApplication().fireEvent('changecontentevent', addImportServiceView);
            }
        });
    },

    updateProperties: function (control, records) {
        var me = this,
            addImportServiceView = me.getAddPage(),
            record = records[0] ? records[0] : records,
            propertyForm = addImportServiceView.down('grouped-property-form');

        if (record && record.properties() && record.properties().count()) {
            propertyForm.addEditPage = true;
            propertyForm.loadRecord(record);
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
        propertyForm.up('#frm-add-import-service').doLayout();
    },

    displayInfo: function (panel) {

        var infoDialog = Ext.create('widget.window', {
            title: Uni.I18n.translate('importService.filePatternInfo', 'FIM', 'File pattern info'),
            closable: true,
            overflowY: 'auto',
            modal: true,
            width: 600,
            minWidth: 350,
            height: 365,
            layout: {
                type: 'border',
                padding: 5
            },
            items: [
                {
                    xtype: 'container',
                    html: Uni.I18n.translate('importService.filePatternInfo.title', 'FIM', 'Pattern to filter which files will be imported, based on file name and/or extension.') + '<br><br>' +
                    Uni.I18n.translate('importService.filePatternInfo.example', 'FIM', 'Here are some examples of pattern syntax:') + '<br><ul>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex1', 'FIM', '*.csv - Matches all strings that end in .csv') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex2', 'FIM', '??? - Matches all strings with exactly three letters or digits') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex3', 'FIM', '*[0-9]* - Matches all strings containing a numeric value') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex4', 'FIM', '*.{txt,csv,xlsx} - Matches any string ending with .txt, .csv or.xlsx') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex5', 'FIM', 'a?*.csv - Matches any string beginning with a, followed by at least one letter or digit, and ending with .csv') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex6', 'FIM', '{foo*,*[0-9]*} - Matches any string beginning with foo or any string containing a numeric value') + '</li>' + '<br></ul>'
                }
            ]
        });

        infoDialog.show();
    }


});