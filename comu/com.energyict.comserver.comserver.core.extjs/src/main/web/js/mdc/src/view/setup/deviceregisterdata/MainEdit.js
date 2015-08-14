Ext.define('Mdc.view.setup.deviceregisterdata.MainEdit', {
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
        var me = this;
        me.edit = edit;
        if (me.isEdit()) {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            me.down('#addEditButton').action = 'editRegisterDataAction';
            me.down('#registerDataEditForm').setTitle(Uni.I18n.translate('device.registerData.editReading', 'MDC', 'Edit reading'));
        } else {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            me.down('#addEditButton').action = 'addRegisterDataAction';
            me.down('#registerDataEditForm').setTitle(Uni.I18n.translate('general.addReading', 'MDC', 'Add reading'));
        }
        me.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this,
            mRID = me.mRID,
            registerId = me.registerId;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'uni-view-menu-side',
                        title: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                        itemId: 'stepsMenu',
                        menuItems: [
                            {
                                itemId: 'editReading',
                                href:  me.router.getRoute('devices/device/registers/registerdata/create').buildUrl({mRID: encodeURIComponent(mRID), registerId: registerId})
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

