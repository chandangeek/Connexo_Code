/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycle.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycles-edit',
    xtype: 'usagepoint-life-cycles-edit',
    router: null,
    route: null,
    isEdit: false,
    requires: ['Imt.usagepointlifecycle.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'usagepoint-life-cycles-add-form',
                router: me.router,
                route: me.route,
                infoText: Uni.I18n.translate('usagePointLifeCycles.add.templateMsg', 'IMT', 'The new usage point life cycle is based on the standard template and will use the same states and transitions.'),
                btnAction: me.isEdit ? 'edit' : 'add',
                btnText: me.isEdit ? Uni.I18n.translate('general.save', 'IMT', 'Save') : Uni.I18n.translate('general.add', 'IMT', 'Add'),
                hideInfoMsg: me.isEdit ? true : false
            }
        ];
        me.callParent(arguments);
    }
});
