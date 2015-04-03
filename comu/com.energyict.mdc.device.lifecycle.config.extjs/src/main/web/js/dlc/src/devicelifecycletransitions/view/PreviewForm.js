Ext.define('Dlc.devicelifecycletransitions.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycle-transitions-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                name: 'name'
            },
            {
                fieldLabel: Uni.I18n.translate('general.from', 'DLC', 'From'),
                name: 'fromState_name'
            },
            {
                fieldLabel: Uni.I18n.translate('general.to', 'DLC', 'To'),
                name: 'toState_name'
            },
            {
                fieldLabel: Uni.I18n.translate('general.triggeredBy', 'DLC', 'Triggered by'),
                name: 'triggeredBy_name'
            },
            {
                fieldLabel: Uni.I18n.translate('general.privileges', 'DLC', 'Privileges'),
                name: 'privileges',
                renderer: function (privileges) {
                    var str = '';
                    if (privileges) {
                        Ext.Array.each(privileges, function (privilege) {
                            if (privilege.privilege === privileges[privileges.length - 1].privilege) {
                                str += privilege.name;
                            } else {
                                str += privilege.name + ' - ';
                            }
                        });
                    }
                    return str;
                }
            }
        ];

        me.callParent(arguments);
    }
});
