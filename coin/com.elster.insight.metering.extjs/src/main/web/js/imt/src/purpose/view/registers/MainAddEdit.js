Ext.define('Imt.purpose.view.registers.MainAddEdit', {
    extend: 'Uni.view.container.ContentContainer',

    requires: [
        'Uni.view.menu.SideMenu'
    ],

    edit: false,
    registerType: null,

    isEdit: function () {
        return this.edit
    },

    setValues: function (record) {
        var me = this;
    },

    setEdit: function (edit, returnLink) {
        var me = this,
            menuHref;
        me.edit = edit;
        if (me.isEdit()) {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'IMT', 'Save'));
            me.down('#addEditButton').action = 'editRegisterDataAction';
            me.down('#registerDataEditForm').setTitle(Uni.I18n.translate('usagepoint.registerData.editReading', 'IMT', 'Edit reading'));
            me.down('#editReading').setText(Uni.I18n.translate('usagepoint.registerData.editReading', 'IMT', 'Edit reading'));
            me.menuHref = me.router.getRoute('usagepoints/view/purpose/output/editregisterdata').buildUrl();
        } else {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'IMT', 'Add'));
            me.down('#addEditButton').action = 'addRegisterDataAction';
            me.down('#registerDataEditForm').setTitle(Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'));
            me.down('#editReading').setText(Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'));
            me.menuHref = me.router.getRoute('usagepoints/view/purpose/output/addregisterdata').buildUrl();
        }
        me.down('#cancelLink').href = returnLink;
    },

    isValid: function () {
        return this.down('#registerDataEditForm').isValid();
    },

    showErrors: function (errors) {
        var me = this,
            formErrorsPlaceHolder = me.down('#registerDataEditFormErrors');
        if(errors){
            me.down('#registerDataEditForm').getForm().markInvalid(errors);
        }
        formErrorsPlaceHolder.hide();
        Ext.suspendLayouts();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('general.formErrors', 'IMT', 'There are errors on this page that require your attention.')
        });
        Ext.resumeLayouts();
        formErrorsPlaceHolder.show();
    },

    hideErrors: function(){
        var me = this,
            formErrorsPlaceHolder = me.down('#registerDataEditFormErrors');
        Ext.suspendLayouts();
        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        Ext.resumeLayouts();
    },

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'uni-view-menu-side',
                        title: Uni.I18n.translate('general.registers', 'IMT', 'Registers'),
                        itemId: 'stepsMenu',
                        menuItems: [
                            {
                                itemId: 'editReading',
                                href: me.menuHref
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

