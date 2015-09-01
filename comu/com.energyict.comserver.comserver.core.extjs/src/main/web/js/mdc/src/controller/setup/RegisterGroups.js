Ext.define('Mdc.controller.setup.RegisterGroups', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification'
    ],

    views: [
        'setup.registergroup.RegisterGroupSetup',
        'setup.registergroup.RegisterGroupGrid',
        'setup.registergroup.RegisterGroupPreview',
        'setup.registergroup.RegisterGroupEdit'
    ],

    stores: [
        'RegisterGroups',
        'InfiniteRegisterTypes',
        'AvailableRegisterTypesForRegisterGroup'
    ],

    refs: [
        {ref: 'registerGroupSetup', selector: '#registerGroupSetup'},
        {ref: 'registerTypeGrid', selector: '#register-groups-register-types-grid'},
        {ref: 'registerGroupGrid', selector: '#registerGroupGrid'},
        {ref: 'registerGroupPreviewForm', selector: '#registerGroupPreviewForm'},
        {ref: 'registerGroupPreview', selector: '#registerGroupPreview'},
        {ref: 'registerGroupPreviewTitle', selector: '#registerGroupPreviewTitle'},
        {ref: 'registerGroupEditView', selector: '#registerGroupEdit'},
        {ref: 'registerGroupEditForm', selector: '#registerGroupEditForm'},
        {ref: 'registerEditEmptyGrid', selector: '#registerEditEmptyGrid'},
        {ref: 'registerGroupPreviewDetails', selector: '#registerGroupPreviewDetails'},
        {ref: 'registerTypePreviewForm', selector: '#registerTypePreviewForm'},
        {ref: 'registerTypePreview', selector: '#registerGroupSetup #registerTypePreview'},
        {ref: 'registerGroupGridContainer', selector: '#registerGroupGridContainer'},
        {ref: 'registerGroupEmptyGrid', selector: '#registerGroupEmptyGrid'},
        {ref: 'registerTypeEmptyGrid', selector: '#registerTypeEmptyGrid'},
        {ref: 'contentPanel', selector: 'viewport > #contentPanel'}
    ],

    init: function () {
        this.control({
            '#registerGroupEdit #editRegisterGroupGridField': {
                selectionchange: this.checkboxChanged
            },
            '#registerGroupSetup #register-groups-register-types-grid': {
                selectionchange: this.previewRegisterType
            },
            '#registerGroupSetup #registerGroupGrid': {
                selectionchange: this.previewRegisterGroup
            },
            '#registerGroupSetup #registerGroupGrid actioncolumn': {
                editRegisterGroup: this.editRegisterGroupHistory,
                removeRegisterGroup: this.removeRegisterGroup
            },
            '#registerGroupSetup button[action = createRegisterGroup]': {
                click: this.createRegisterGroupHistory
            },
            '#registerGroupSetup #registerGroupPreview menuitem[action=editRegisterGroup]': {
                click: this.editRegisterGroupHistoryFromPreview
            },
            '#registerGroupSetup #registerGroupPreview menuitem[action=removeRegisterGroup]': {
                click: this.removeRegisterGroupFromPreview
            },
            '#registerGroupEdit button[action = createRegisterType]': {
                click: this.createRegisterTypeHistory
            },
            '#registerGroupEdit button[action=save]': {
                click: this.saveRegisterGroup
            },
            '#registerGroupEdit button[action=cancel]': {
                click: this.back
            },
            '#registerGroupPreview #editEmptyPreviewButton[action=editRegisterGroup]': {
                click: this.editRegisterGroupHistoryFromPreview
            }

        });
    },

    backUrl: null,

    back: function () {
        location.href = this.backUrl;
    },

    showRegisterGroups: function (grid, record) {
        var widget = Ext.widget('registerGroupSetup');
        var me = this;
        me.getRegisterTypeEmptyGrid().setVisible(false);
        me.getRegisterTypePreview().setVisible(false);
        me.getRegisterGroupPreview().setVisible(false);
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    previewRegisterGroup: function (grid, record) {
        var registerGroups = this.getRegisterGroupGrid().getSelectionModel().getSelection();
        if (registerGroups.length == 1) {
            var me = this;
            me.getRegisterTypeGrid().getStore().getProxy().setExtraParam('registerGroup', registerGroups[0].get('id'));
            me.getRegisterGroupPreview().setVisible(true);
            me.getRegisterTypeEmptyGrid().setVisible(true);
            me.getRegisterTypeGrid().getStore().load({
                callback: function (records) {
                    if (records.length > 0) {
                        me.getRegisterGroupPreview().setTitle(Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + Ext.String.htmlEncode(registerGroups[0].get('name')));
                        me.getRegisterTypeGrid().getSelectionModel().doSelect(0);
                    } else {
                        me.getRegisterGroupPreview().setTitle(Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + Ext.String.htmlEncode(registerGroups[0].get('name')));
                    }
                    me.getRegisterTypeEmptyGrid().setVisible(true);
                    me.getRegisterTypePreview().setVisible(true);
                }
            });
        }
    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().setTitle(Ext.String.htmlEncode(registerTypes[0].get('name')));
        }
    },

    createRegisterGroupHistory: function () {
        location.href = '#/administration/registergroups/add';
    },

    editRegisterGroupHistory: function (item) {
        location.href = '#/administration/registergroups/' + item.get('id') + '/edit';
    },

    editRegisterGroupHistoryFromPreview: function () {
        location.href = '#/administration/registergroups/' + this.getRegisterGroupGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    createRegisterTypeHistory: function () {
        location.href = '#/administration/registertypes/add';
    },

    showRegisterGroupEditView: function (registerGroupId) {
        var me = this,
            store = me.getStore('Mdc.store.InfiniteRegisterTypes'),
            widget,
            grid;

        Ext.ModelManager.getModel('Mdc.model.RegisterGroup').load(registerGroupId, {
            success: function (registerGroup) {
                store.load({
                    callback: function (registerTypes) {
                        widget = Ext.widget('registerGroupEdit', {edit: true});
                        me.backUrl = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();
                        me.mode = 'edit';
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.setLoading(true);
                        me.getApplication().fireEvent('loadRegisterGroup', registerGroup);
                        widget.down('form').loadRecord(registerGroup);
                        widget.down('panel').setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + Ext.String.htmlEncode(registerGroup.get('name')) + '\'');
                        grid = widget.down('#editRegisterGroupGridField');
                        if (this.data.first.value.length > 0) {
                            grid.reconfigure(store);
                            grid.getSelectionModel().suspendChanges();
                            grid.getSelectionModel().select(registerGroup.registerTypes().data.items, false, true);
                            me.checkboxChanged(grid, grid.getSelectionModel().getSelection());
                            grid.getSelectionModel().resumeChanges();
                        }
                        widget.setLoading(false);
                    }
                });
            }
        });
    },

    showRegisterGroupCreateView: function () {
        var me = this,
            store = me.getStore('Mdc.store.InfiniteRegisterTypes'),
            widget,
            registerGroup;

        store.load({
            callback: function (registerTypes) {
                widget = Ext.widget('registerGroupEdit', {edit: false});
                me.backUrl = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();
                me.mode = 'create';
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);
                widget.down('panel').setTitle(Uni.I18n.translate('registerGroup.add', 'MDC', 'Add register group'));
                registerGroup = Ext.create(Ext.ModelManager.getModel('Mdc.model.RegisterGroup'));
                widget.down('form').loadRecord(registerGroup);
                if (this.totalCount > 0) {
                    widget.down('#editRegisterGroupSelectedField').setValue(
                        Uni.I18n.translatePlural('general.nrOfRegisterTypes.selected', 0, 'MDC',
                            'No register types selected', '{0} register type selected', '{0} register types selected')
                    );
                    widget.down('#editRegisterGroupGridField').reconfigure(store);
                }
                widget.setLoading(false);
            }
        });
    },

    checkboxChanged: function (grid, selected) {
        grid.view.up('#registerGroupEdit').down('#editRegisterGroupSelectedField').setValue(
            Uni.I18n.translatePlural('general.nrOfRegisterTypes.selected', selected.length, 'MDC',
                'No register types selected', '{0} register type selected', '{0} register types selected')
        );
    },

    saveRegisterGroup: function () {
        var me = this,
            form = me.getRegisterGroupEditForm(),
            record = form.getRecord(),
            values = form.getValues(),
            baseForm = form.getForm(),
            selected = form.down('#editRegisterGroupGridField').getSelectionModel().getSelection();

        record.set(values);
        record.registerTypes().removeAll();
        record.registerTypes().add(selected);

        baseForm.clearInvalid();
        me.hideErrorPanel();
        record.save({
            success: function (record) {
                var message;

                if (me.mode == 'edit') {
                    message = Uni.I18n.translate('registergroup.saved', 'MDC', 'Register group {0} saved.', [record.get('name')]);
                }
                else {
                    message = Uni.I18n.translate('registergroup.added', 'MDC', 'Register group {0} added.', [record.get('name')]);
                }
                me.getApplication().fireEvent('acknowledge', message);
                location.href = '#/administration/registergroups/';
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);

                if (json && json.errors) {
                    baseForm.markInvalid(json.errors);
                }
                me.showErrorPanel();
            }
        });
    },

    removeRegisterGroup: function (itemToRemove) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('registerGroup.deleteRegisterGroup', 'MDC', 'The register group will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' ' + itemToRemove.get('name') + '?',
            config: {
                registerGroupToDelete: itemToRemove
            },
            fn: me.removeRegisterGroupInDatabase,
            app: me.getApplication()
        });
    },

    removeRegisterGroupFromPreview: function () {
        this.removeRegisterGroup(this.getRegisterGroupGrid().getSelectionModel().getSelection()[0]);
    },

    removeRegisterGroupInDatabase: function (btn, text, opt) {
        var me = this;
        if (btn === 'confirm') {
            var registerTypeToDelete = opt.config.registerGroupToDelete,
                name = registerTypeToDelete.get('name'),
                app = opt.app;
            registerTypeToDelete.destroy({
                success: function () {
                    app.fireEvent('acknowledge', Uni.I18n.translate('registergroup.removed', 'MDC', 'Register group {0} removed.', [name]));
                },
                callback: function () {
                    location.href = '#/administration/registergroups/';
                }
            });
        }
    },

    showErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getRegisterGroupEditForm().down('#registerGroupEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('general.formErrors', 'MDC', 'There are errors on this page that require your attention.')
        });
        formErrorsPlaceHolder.show();
    },

    hideErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getRegisterGroupEditForm().down('#registerGroupEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
    }

});
