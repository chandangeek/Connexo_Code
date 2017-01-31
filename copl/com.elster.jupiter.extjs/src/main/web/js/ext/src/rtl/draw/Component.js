/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.draw.Component', {
    override: 'Ext.draw.Component',
    
    initSurfaceCfg: function(cfg) {
        if (this.getHierarchyState().rtl) {
            cfg.isRtl = true;
        }
    }    
});
