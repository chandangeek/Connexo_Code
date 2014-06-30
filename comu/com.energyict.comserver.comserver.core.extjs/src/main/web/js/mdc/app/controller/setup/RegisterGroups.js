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
        'RegisterTypes',
        'AvailableRegisterTypesForRegisterGroup'
    ],

    refs: [
        {ref: 'registerGroupSetup', selector: '#registerGroupSetup'},
        {ref: 'registerTypeGrid', selector: '#registertypegrid'},
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
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'registerGroupGridContainer', selector: '#registerGroupGridContainer'},
        {ref: 'registerGroupEmptyGrid', selector: '#registerGroupEmptyGrid'},
        {ref: 'registerTypeEmptyGrid', selector: '#registerTypeEmptyGrid'},
        {ref: 'previewMrId', selector: '#preview_mrid'},
        {ref: 'contentPanel', selector: 'viewport > #contentPanel'}
    ],

    init: function () {
        this.control({
            '#registerGroupEdit #editRegisterGroupGridField': {
                selectionchange: this.checkboxChanged
            },
            '#registerGroupEdit #editRegisterGroupGridField actioncolumn': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registerGroupSetup #registertypegrid': {
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
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    previewRegisterGroup: function (grid, record) {
        var registerGroups = this.getRegisterGroupGrid().getSelectionModel().getSelection();
        if (registerGroups.length == 1) {
            var me=this;

            this.getRegisterTypeGrid().store.getProxy().setExtraParam('registerGroup', registerGroups[0].get('id'));
            this.getRegisterTypeGrid().store.load({
                callback: function (store) {
                    if(this.totalCount > 0){
                        me.getRegisterTypeEmptyGrid().getLayout().setActiveItem('gridContainer');
                        me.getRegisterGroupPreviewTitle().update('<b>' + Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + registerGroups[0].get('name') + '</b><br>' +
                            Ext.String.format(Uni.I18n.translate('registerGroup.previewCount', 'MDC', '{0} register types'), this.totalCount));
                        me.getRegisterTypeGrid().getSelectionModel().doSelect(0);
                    }
                    else{
                        me.getRegisterTypeEmptyGrid().getLayout().setActiveItem('emptyContainer');
                        me.getRegisterGroupPreviewTitle().update('<b>' + Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + registerGroups[0].get('name') + '</b>');
                    }
                    me.getRegisterTypeEmptyGrid().setVisible(true);
                }
            });
        }
    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().setTitle(registerTypes[0].get('name'));
            this.getPreviewMrId().setValue(registerTypes[0].getReadingType().get('mrid'));
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
        var widget = Ext.widget('registerGroupEdit');
        this.backUrl = this.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();

        var me = this;
        me.mode = 'edit';

        // TODO: change this to activate infinite scrolling when JP-2844 is fixed
        Ext.ModelManager.getModel('Mdc.model.RegisterGroup').load(registerGroupId, {
            success: function (registerGroup) {
                me.getApplication().fireEvent('loadRegisterGroup', registerGroup);
                me.getStore('Mdc.store.RegisterTypes').load({
                    limit: 500,
                    callback: function (registerTypes) {
                        widget.down('form').loadRecord(registerGroup);
                        widget.down('panel').setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + registerGroup.get('name') + '\'');
                        if(this.data.items.length > 0){
                            widget.down('#registerGroupEditCard').getLayout().setActiveItem(0);
                            widget.down('#editRegisterGroupGridField').getSelectionModel().doSelect(registerGroup.registerTypes().data.items);
                            widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                            me.getRegisterEditEmptyGrid().setVisible(true);
                            me.getRegisterEditEmptyGrid().getLayout().setActiveItem('gridContainer');
                        }
                        else{
                            widget.down('#registerGroupEditCard').getLayout().setActiveItem(1);
                        }

                        widget.show();
                        widget.setLoading(false);
                    }
                });
            }
        });

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        widget.hide();
    },

    showRegisterGroupCreateView: function () {
        var widget = Ext.widget('registerGroupEdit');
        this.backUrl = this.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();

        var me = this;
        me.mode = 'create';

        // TODO: change this to activate infinite scrolling when JP-2844 is fixed
        //widget.down('#editRegisterGroupGridField').store.on('load', function () {
        me.getStore('Mdc.store.RegisterTypes').load({
            limit: 500,
            callback: function (registerTypes) {
                var registerGroup = Ext.create(Ext.ModelManager.getModel('Mdc.model.RegisterGroup'));
                widget.down('form').loadRecord(registerGroup);
                widget.down('panel').setTitle(Uni.I18n.translate('registerGroup.create', 'MDC', 'Add register group'));
                if(this.totalCount > 0){
                    widget.down('#registerGroupEditCard').getLayout().setActiveItem(0);
                    widget.down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', '{0} register types selected'), 0));
                    widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                    me.getRegisterEditEmptyGrid().setVisible(true);
                    me.getRegisterEditEmptyGrid().getLayout().setActiveItem('gridContainer');
                }
                else{
                    widget.down('#registerGroupEditCard').getLayout().setActiveItem(1);
                }

                widget.show();
                widget.setLoading(false);
            }
        });

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        widget.hide();
    },

    checkboxChanged: function (grid, selected) {
        grid.view.up('#registerGroupEdit').down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', '{0} register types selected'), selected.length));
    },

    saveRegisterGroup: function () {
        var form = this.getRegisterGroupEditForm(),
            record = form.getRecord(),
            values = form.getValues(),
            selected = form.down('#editRegisterGroupGridField').getSelectionModel().getSelection();

        record.set(values);
        record.registerTypes().removeAll();
        record.registerTypes().add(selected);

        var me=this;
        record.save({
            success: function (record) {
                var message;
                if(me.mode == 'edit'){
                    message = Uni.I18n.translatePlural('registergroup.saved', record.get('name'), 'USM', 'Register group \'{0}\' saved.');
                }
                else{
                    message = Uni.I18n.translatePlural('registergroup.added', record.get('name'), 'USM', 'Register group \'{0}\' added.');
                }
                me.getApplication().fireEvent('acknowledge', message);
                location.href = '#/administration/registergroups/';
            },
            failure: function(record,operation){
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    me.getRegisterGroupEditForm().getForm().markInvalid(json.errors);
                }
            }
        });
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
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
                    app.fireEvent('acknowledge', Uni.I18n.translatePlural('registergroup.removed', name, 'USM', 'Register group \'{0}\' removed.'));
                },
                callback: function () {
                    location.href = '#/administration/registergroups/';
                }
            });
        }
    }
});
