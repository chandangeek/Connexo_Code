/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import javax.validation.constraints.Pattern;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.util.ArrayList;
import java.util.List;

public class FileImportScheduleInfo {

    public long id;
    public String name;
    public Boolean active;
    public Boolean deleted;
    public Boolean scheduled;
    public Boolean importerAvailable;
    @Pattern(regexp = "[a-zA-Z0-9_\\-\\s]+((/|\\\\)[a-zA-Z0-9_\\-\\s]+)*", groups = {POST.class, PUT.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    public String importDirectory;
    @Pattern(regexp = "[a-zA-Z0-9_\\-\\s]+((/|\\\\)[a-zA-Z0-9_\\-\\s]+)*", groups = {POST.class, PUT.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    public String inProcessDirectory;
    @Pattern(regexp = "[a-zA-Z0-9_\\-\\s]+((/|\\\\)[a-zA-Z0-9_\\-\\s]+)*", groups = {POST.class, PUT.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    public String successDirectory;
    @Pattern(regexp = "[a-zA-Z0-9_\\-\\s]+((/|\\\\)[a-zA-Z0-9_\\-\\s]+)*", groups = {POST.class, PUT.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    public String failureDirectory;
    public String pathMatcher;
    public String importerName;
    public Integer scanFrequency;
    public String application;
    public FileImporterInfo importerInfo;
    public PeriodicalExpressionInfo schedule;
    public List<PropertyInfo> properties = new ArrayList<>();
    public long version;

}
