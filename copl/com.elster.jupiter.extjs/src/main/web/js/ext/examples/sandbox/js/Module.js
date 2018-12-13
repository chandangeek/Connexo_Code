/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.app.Module = function(config){
    Ext.apply(this, config);
    Ext.app.Module.superclass.constructor.call(this);
    this.init();
}

Ext.extend(Ext.app.Module, Ext.util.Observable, {
    init : Ext.emptyFn
});