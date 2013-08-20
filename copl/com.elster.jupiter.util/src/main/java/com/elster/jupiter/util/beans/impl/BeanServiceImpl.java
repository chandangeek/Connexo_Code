package com.elster.jupiter.util.beans.impl;

import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.NoSuchPropertyException;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Component that implements the BeanService interface.
 */
@Component(name = "com.elster.jupiter.beans", service = { BeanService.class })
public class BeanServiceImpl implements BeanService {

    private final BeanService beanService = new DefaultBeanService();

    @Activate
    public void activate(BundleContext context) {
    }
    
    @Deactivate
    public void deactivate() {
    }

    @Override
    public Object get(Object bean, String property) throws NoSuchPropertyException {
        return beanService.get(bean, property);
    }

    @Override
    public void set(Object bean, String property, Object value) throws NoSuchPropertyException {
        beanService.set(bean, property, value);
    }
}
