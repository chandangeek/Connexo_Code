package com.elster.jupiter.properties;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ListValueTest {

    private static TestBean bean1 = new TestBean("1", "bean1");
    private static TestBean bean2 = new TestBean("2", "bean2");
    @Test
    public void testListValueEmpty() {
        ListValue<TestBean> listValue = new ListValue<>();
        assertThat(listValue.getValue()).isEqualTo(null);
        assertThat(listValue.hasSingleValue()).isFalse();
    }

    @Test
    public void testListValueWithSingleValue() {
        ListValue<TestBean> listValue = new ListValue<>(bean1);
        assertThat(listValue.getValue()).isEqualTo(bean1);
        assertThat(listValue.getIds()).containsExactly("1");
        assertThat(listValue.hasSingleValue()).isTrue();
    }

    @Test
    public void testListValueWithMultipleValues() {
        ListValue<TestBean> listValue = new ListValue<>();
        listValue.addValue(bean1);
        listValue.addValue(new ListValue<TestBean>(bean2));

        assertThat(listValue.getValues()).containsExactly(bean1, bean2);
        assertThat(listValue.getIds()).containsExactly("1", "2");
        assertThat(listValue.hasSingleValue()).isFalse();
    }

    @Test
    public void testListValueFactory() {
        ListValueFactory<TestBean> factory = new ListValueFactory<>(new TestBeanFinder());
        
        assertThat(factory.getValueType().isAssignableFrom(ListValue.class)).isTrue();
        assertThat(factory.getDatabaseTypeName()).isEqualTo("varchar2(4000)");
        assertThat(factory.getJdbcType()).isEqualTo(java.sql.Types.VARCHAR);
    }
    
    @Test
    public void testListValueFactoryValueFromString() throws Exception {
        ListValueFactory<TestBean> factory = new ListValueFactory<>(new TestBeanFinder());
        
        ListValue<TestBean> testBean = factory.fromStringValue(null);
        assertThat(testBean.getValue()).isNull();
        
        testBean = factory.fromStringValue("1,2");
        assertThat(testBean.getValues().get(0).getId()).isEqualTo("1");
        assertThat(testBean.getValues().get(0).getName()).isEqualTo("bean1");
        assertThat(testBean.getValues().get(1).getId()).isEqualTo("2");
        assertThat(testBean.getValues().get(1).getName()).isEqualTo("bean2");
        
        testBean = factory.valueFromDatabase("3");
        assertThat(testBean.getValue()).isNull();
    }
    
    @Test
    public void testListValueFactoryValueToString() {
        ListValueFactory<TestBean> factory = new ListValueFactory<>(new TestBeanFinder());
        ListValue<TestBean> value = new ListValue<>();
        
        assertThat(factory.toStringValue(value)).isEqualTo("");
        
        value.addValue(bean1);
        assertThat(factory.toStringValue(value)).isEqualTo("1");
        
        value.addValue(bean2);
        assertThat(factory.toStringValue(value)).isEqualTo("1,2");
        
        assertThat(factory.valueToDatabase(value)).isEqualTo("1,2");
    }
    
    @Test
    public void testEqualsAndHashCode() {
        ListValue<TestBean> listValue1 = new ListValue<>(bean1);
        ListValue<TestBean> listValue2 = new ListValue<>(bean1);
        ListValue<TestBean> listValue3 = new ListValue<>(bean2);
        
        assertThat(listValue1.equals("")).isFalse();
        assertThat(listValue1).isEqualTo(listValue1);
        assertThat(listValue1.hashCode() == listValue2.hashCode()).isTrue();
        assertThat(listValue1).isEqualTo(listValue2);
        assertThat(listValue1).isNotEqualTo(listValue3);
        assertThat(listValue1.hashCode() != listValue3.hashCode()).isTrue();
        
        
        listValue1.addValue(bean2);
        assertThat(listValue1).isNotEqualTo(listValue2);
        assertThat(listValue1.hashCode() != listValue2.hashCode()).isTrue();
        
        listValue2.addValue(bean2);
        assertThat(listValue1).isEqualTo(listValue2);
        assertThat(listValue1.hashCode() == listValue2.hashCode()).isTrue();
    }
    
    @Test
    public void testEqualsAndHashCodeNullable() {
        ListValue<TestBean> listValue1 = new ListValue<>(null);
        ListValue<TestBean> listValue2 = new ListValue<>(null);
        ListValue<TestBean> listValue3 = new ListValue<>(bean1);
        ListValue<TestBean> listValue4 = new ListValue<>(new TestBean(null, "name"));
        
        assertThat(listValue1).isEqualTo(listValue2);
        assertThat(listValue1).isNotEqualTo(new ListValue<TestBean>());
        assertThat(new ListValue<TestBean>()).isNotEqualTo(listValue1);
        
        assertThat(listValue3).isNotEqualTo(listValue4);
        assertThat(listValue4).isNotEqualTo(listValue3);
        
        assertThat(listValue1.hashCode() == listValue2.hashCode()).isTrue();
        assertThat(listValue1.hashCode() != new ListValue<HasIdAndName>(bean1).hashCode()).isTrue();
        
        assertThat(listValue3.hashCode() != listValue4.hashCode()).isTrue();
    }

    private static class TestBean extends HasIdAndName {
        
        private Object id;
        private String name;
        
        public TestBean(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        @Override
        public Object getId() {
            return id;
        }
        
        @Override
        public String getName() {
            return name;
        }
    }

    private static class TestBeanFinder implements CanFindByStringKey<TestBean> {

        @Override
        public Optional<TestBean> find(String key) {
            switch (key) {
            case "1":
                return Optional.of(bean1);
            case "2":
                return Optional.of(bean2);
            default:
                break;
            }
            return Optional.empty();
        }

        @Override
        public Class<TestBean> valueDomain() {
            return TestBean.class;
        }
    }

}
