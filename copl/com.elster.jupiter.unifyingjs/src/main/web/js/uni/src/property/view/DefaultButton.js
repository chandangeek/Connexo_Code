/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.DefaultButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-default-button',
    tabIndex: -1,
    border: 0,
    iconCls: 'icon-rotate-ccw3',
    iconAlign: 'center',
    height: 28,
    width: 28,
    action: 'delete',
    margin: '0 0 0 5',
    hidden: true
});