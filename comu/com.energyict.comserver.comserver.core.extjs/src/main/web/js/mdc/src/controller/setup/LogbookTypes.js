Ext.define('Mdc.controller.setup.LogbookTypes', {
    extend: 'Ext.app.Controller',
    stores: [
        'LogbookTypes'
    ],
    views: [
        'setup.logbooktype.LogbookTypeSetup',
        'setup.logbooktype.LogbookTypeGrid',
        'setup.logbooktype.LogbookTypePreview',
        'setup.logbooktype.LogbookTypeActionMenu',
        'setup.logbooktype.LogbookTypeEdit'
    ],
    refs: [
        {ref: 'logbookTypeSetup', selector: '#logbookTypeSetup'},
        {ref: 'logbookTypeGrid', selector: '#logbookTypeGrid'},
        {ref: 'logbookTypePreviewForm', selector: '#logbookTypePreviewForm'},
        {ref: 'logbookTypePreview', selector: '#logbookTypeSetup #logbookTypePreview'},
        {ref: 'logbookTypeEditView', selector: '#logbookTypeEdit'},
        {ref: 'logbookTypeEditForm', selector: '#logbookTypeEditForm'},
        {ref: 'logbookTypeEditForm', selector: '#logbookTypeEditForm'}
    ],


    init: function () {
        this.getLogbookTypesStore().on('load', this.onLogbookTypesStoreLoad, this);

        this.control({
            '#logbookTypeSetup #logbookTypeGrid': {
                selectionchange: this.previewLogbookType
            },
            '#logbookTypeGrid actioncolumn': {
                editLogbookType: this.editLogbookTypeHistory,
                removeLogbookType: this.deleteLogbookType
            },
            '#logbookTypeSetup button[action = createLogbookType]': {
                click: this.createLogbookTypeHistory
            },
            '#logbookTypePreview menuitem[action=editLogbookType]': {
                click: this.editLogbookTypeHistoryFromPreview
            },
            '#logbookTypePreview menuitem[action=removeLogbookType]': {
                click: this.deleteLogbookTypeFromPreview
            },
            '#createEditButton[action=createLogbookType]': {
                click: this.createLogbookType
            },
            '#createEditButton[action=editLogbookType]': {
                click: this.editLogbookType
            }

        });
    },

    onLogbookTypesStoreLoad: function () {
        if (this.getLogbookTypesStore().data.items.length > 0) {
            var setupWidget = this.getLogbookTypeSetup(),
                gridWidget = this.getLogbookTypeGrid();
            if (!Ext.isEmpty(setupWidget) && !Ext.isEmpty(gridWidget)) {
                setupWidget.show();
                gridWidget.getSelectionModel().doSelect(0);
            }

        }
    },

    previewLogbookType: function (grid, record) {
        var logbookTypes = this.getLogbookTypeGrid().getSelectionModel().getSelection();
        if (logbookTypes.length == 1) {
            this.getLogbookTypePreviewForm().loadRecord(logbookTypes[0]);
            this.getLogbookTypePreview().setTitle(Ext.String.htmlEncode(logbookTypes[0].get('name')));
        }
    },

    createLogbookTypeHistory: function () {
        location.href = '#/administration/logbooktypes/add';
    },

    editLogbookTypeHistory: function (item) {
        location.href = '#/administration/logbooktypes/' + encodeURIComponent(item.get('id')) + '/edit';
    },

    editLogbookTypeHistoryFromPreview: function () {
        location.href = '#/administration/logbooktypes/' + encodeURIComponent(this.getLogbookTypeGrid().getSelectionModel().getSelection()[0].get('id')) + '/edit';
    },

    deleteLogbookType: function (logbookTypeToDelete) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('logbookType.deleteLogbookType', 'MDC', 'The logbook type will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?",[logbookTypeToDelete.get('name')]),
            config: {
                logbookTypeToDelete: logbookTypeToDelete,
                me: me.getApplication()
            },
            fn: me.deleteLogbookTypeInDatabase
        });
    },

    deleteLogbookTypeFromPreview: function (logbookTypeToDelete) {
        this.deleteLogbookType(this.getLogbookTypeGrid().getSelectionModel().getSelection()[0]);
    },

    deleteLogbookTypeInDatabase: function (btn, text, opt) {
        if (btn === 'confirm') {
            var logbookTypeToDelete = opt.config.logbookTypeToDelete;
            var me = opt.config.me;
            var message = Uni.I18n.translate('logbookType.acknowlegment.removed', 'MDC', 'Logbook type removed');
            logbookTypeToDelete.destroy({
                success: function () {
                    me.fireEvent('acknowledge',message );
                    location.href = '#/administration/logbooktypes/';
                }
            });
        }
    },

    showLogbookTypeEditView: function (logbookType) {
        var me = this;

        var widget = Ext.widget('logbookTypeEdit', {
            edit: true,
            returnLink: me.getController('Uni.controller.history.Router').getRoute('administration/logbooktypes').buildUrl()
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.LogbookType').load(logbookType, {
            success: function (logbookType) {
                me.getApplication().fireEvent('loadLogbookType', logbookType);

                widget.down('form').loadRecord(logbookType);
                widget.down('#logbookTypeEditCreateTitle').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'",[logbookType.get('name')]));

                if (logbookType.get('isInUse') === true) {
                    widget.down('obis-field').disable();
                    widget.down('#logbookTypeEditCreateInformation').update(Uni.I18n.translate('logbooktype.warningLinkedTodeviceType', 'MDC', 'The logbook type has been added to a device type.  Only the name is editable.'));
                    widget.down('#logbookTypeEditCreateInformation').show();
                }
                widget.setLoading(false);
            }
        })
    },

    showLogbookTypes: function () {
        var widget = Ext.widget('logbookTypeSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showLogbookTypeCreateView: function () {
        var widget = Ext.widget('logbookTypeEdit', {
            edit: false,
            returnLink: '#/administration/logbooktypes/'
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.down('#logbookTypeEditCreateTitle').setTitle(Uni.I18n.translate('logbookType.createLogbookType', 'MDC', 'Add logbook type'));
    },

    createLogbookType: function () {
        var me = this,
            record = Ext.create(Mdc.model.LogbookType),
            form = me.getLogbookTypeEditForm(),
            values = form.getValues(),
            baseForm = form.getForm(),
            formErrorsPanel = me.getLogbookTypeEditForm().down('uni-form-error-message[name=errors]');

        baseForm.clearInvalid();
        formErrorsPanel.hide();
        if (record) {
            record.set(values);
            record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('logbooktype.acknowlegment.added', 'MDC', 'Logbook type added') );
                    location.href = '#/administration/logbooktypes/';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);

                    if (json && json.errors) {
                        baseForm.markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            });
        }

    },

    editLogbookType: function () {
        var record = this.getLogbookTypeEditForm().getRecord(),
            values = this.getLogbookTypeEditForm().getValues(),
            me = this;
        var formErrorsPanel = me.getLogbookTypeEditForm().down('uni-form-error-message[name=errors]');
        formErrorsPanel.hide();
        if (record) {
            record.set(values);
            record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('logbookType.acknowlegment.saved', 'MDC', 'Logbook type saved') );
                    location.href = '#/administration/logbooktypes/';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getLogbookTypeEditForm().getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            });
        }
    }
});
