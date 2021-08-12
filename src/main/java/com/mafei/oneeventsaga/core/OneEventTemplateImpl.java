package com.mafei.oneeventsaga.core;

import com.mafei.oneeventsaga.annotations.Primary1;
import com.mafei.oneeventsaga.annotations.Secondary;
import com.mafei.oneeventsaga.annotations.Start;
import com.mafei.oneeventsaga.process.ProcessStatus;
import com.mafei.oneeventsaga.process.ServiceData;
import com.mafei.oneeventsaga.process.ServiceTypes;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWord;
import de.vandermeer.asciithemes.u8.U8_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class OneEventTemplateImpl<Aggregate> {

    public static final String ANSI_RED = "\u001B[31m";

    /*    @Bean
        public OneEventTemplate<Aggregate> oneEventTemplate(ApplicationContext context) {
            return (aClass, aggregate, args) -> {
                Class<?>[] declaredClasses = aClass.getDeclaredClasses();
                Arrays.stream(declaredClasses).forEach(aClass1 -> {
                    logger.info("sub aClass {}", (Object) aClass1.getName().split("\\$"));
                    Object bean = context.getBean(aClass1);
                    Method[] methods = bean.getClass().getMethods();
                    Optional<Method> process = Arrays.stream(methods).filter(method -> method.getName().equals("process")).findFirst();

                    process.ifPresent(method -> {
                        Arrays.stream(method.getParameterTypes()).forEach(_method -> {
                            System.out.println("_method = " + _method.getName());
                        });
                        try {
                            System.out.println("OneEventTemplate > Thread name" + Thread.currentThread().getName());
                            Object invoke = method.invoke(bean, aggregate, args);
                            System.out.println("OneEventTemplateImpl.oneEventTemplate > " + invoke);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });

                });
            };
        }*/
    public static final String ANSI_GREEN = "\u001B[32m";
    Logger logger = LoggerFactory.getLogger(OneEventTemplateImpl.class);
    @Autowired
    ListableBeanFactory beanFactory;

    @Bean
    public OneEventTemplate<Aggregate> oneEventTemplate(ApplicationContext context) {
        return new OneEventTemplate<Aggregate>() {
            @Override
            public void process(Class<?> aClass, Aggregate aggregate, Object... args) {

                Map<String, ?> beansOfType = beanFactory.getBeansOfType(aClass);
                TreeMap<Double, ServiceData> serviceMap = new TreeMap<>();
                TreeMap<Double, ServiceData> prcessingServiceMap = serviceMap;

                try {

                    beansOfType.forEach((s, o) -> {
                        if (o.getClass().isAnnotationPresent(Start.class)) {
                            ServiceData startServiceData = new ServiceData();
                            startServiceData.setBeanName(s);
                            startServiceData.setServiceType(ServiceTypes.START_SERVICE);
                            startServiceData.setServiceClass(o.getClass());
                            startServiceData.setStart(o.getClass().getDeclaredAnnotation(Start.class));
                            startServiceData.setFullObject(o);
                            serviceMap.put(1.0, startServiceData);
                        } else if (o.getClass().isAnnotationPresent(Secondary.class)) {
                            ServiceData secondaryServiceData = new ServiceData();
                            secondaryServiceData.setBeanName(s);
                            secondaryServiceData.setServiceType(ServiceTypes.SECONDER_SERVICE);
                            secondaryServiceData.setServiceClass(o.getClass());
                            secondaryServiceData.setSecondary(o.getClass().getDeclaredAnnotation(Secondary.class));
                            secondaryServiceData.setFullObject(o);
                            serviceMap.put(secondaryServiceData.getSecondary().step(), secondaryServiceData);
                        }

                    });
                    serviceMap.forEach((aDouble, serviceData) -> {
                        System.out.println("serviceData = " + aDouble);
                        Method[] methods = beansOfType.get(serviceData.getBeanName()).getClass().getMethods();
                        Optional<Method> process = Arrays.stream(methods).filter(method -> method.getName().equals("process")).findFirst();
                        process.ifPresent(method -> {
                            try {
                                Object invoke = method.invoke(serviceData.getFullObject(), aggregate, args);
                                serviceData.setProcessStatus(ProcessStatus.PASSED);
                                prcessingServiceMap.replace(aDouble, serviceData);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                } finally {
                    AtomicInteger maxCount = new AtomicInteger();
                    prcessingServiceMap.forEach((aDouble, serviceData) -> {
                        StringBuilder tableRow = new StringBuilder();
                        tableRow.append(aDouble);
                        tableRow.append(serviceData.getBeanName());
                        tableRow.append(serviceData.getBeanName());
                        tableRow.append(serviceData.getProcessStatus());
                        if (tableRow.toString().length() > maxCount.intValue()) {
                            maxCount.set(tableRow.toString().length());
                        }
                    });
                    AsciiTable at = new AsciiTable();
                    at.addRule();
                    Primary1 declaredAnnotation = aClass.getDeclaredAnnotation(Primary1.class);
                    at.addRow(null, null, declaredAnnotation.name());
                    at.addRule();
                    at.addRow("Step", "Service", "Status");
                    at.addRule();
                    prcessingServiceMap.forEach((aDouble, serviceData) -> {
                        at.addRow(Double.toString(aDouble), serviceData.getBeanName(), serviceData.getProcessStatus());
                        at.addRule();
                    });
                    at.setTextAlignment(TextAlignment.LEFT);
                    at.getContext().setGrid(U8_Grids.borderLight());
                    at.getRenderer().setCWC(new CWC_LongestWord());
                    System.out.println(ANSI_GREEN + at.render());
                }

            }
        };
    }
}
