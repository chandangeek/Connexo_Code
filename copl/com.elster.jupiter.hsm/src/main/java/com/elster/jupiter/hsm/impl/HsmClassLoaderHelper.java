package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.internal.spring.JssEmbeddedRuntimeConfig;

import java.net.URLClassLoader;

public class HsmClassLoaderHelper {

    public static void setClassLoader(){
        ClassLoader hsmClassLoader = JssEmbeddedRuntimeConfig.class.getClassLoader();
        URLClassLoader appClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new URLClassLoader(appClassLoader.getURLs(), hsmClassLoader);
        Thread.currentThread().setContextClassLoader(loader);
    }

}
