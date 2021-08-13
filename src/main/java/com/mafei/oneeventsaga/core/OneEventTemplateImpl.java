package com.mafei.oneeventsaga.core;

import com.mafei.oneeventsaga.annotations.OneEventListener;
import com.mafei.oneeventsaga.annotations.Primary1;
import com.mafei.oneeventsaga.annotations.Secondary;
import com.mafei.oneeventsaga.annotations.Start;
import com.mafei.oneeventsaga.process.*;
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
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
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
                TreeMap<Integer, ServiceListenerData> serviceListeners = new TreeMap<>();


                try {
                    for (Map.Entry<String, ?> entry : beansOfType.entrySet()) {

                        if (entry.getValue().getClass().isAnnotationPresent(Start.class)) {
                            ServiceData startServiceData = new ServiceData();
                            startServiceData.setBeanName(entry.getKey());
                            startServiceData.setServiceType(ServiceTypes.START_SERVICE);
                            startServiceData.setServiceClass(entry.getValue().getClass());
                            startServiceData.setStart(entry.getValue().getClass().getDeclaredAnnotation(Start.class));
                            startServiceData.setFullObject(entry.getValue());
                            // TODO: 8/13/2021 check and throw the exception if duplicated
                            serviceMap.putIfAbsent(1.0, startServiceData);
                        } else if (entry.getValue().getClass().isAnnotationPresent(Secondary.class)) {
                            ServiceData secondaryServiceData = new ServiceData();
                            secondaryServiceData.setBeanName(entry.getKey());
                            secondaryServiceData.setServiceType(ServiceTypes.SECONDER_SERVICE);
                            secondaryServiceData.setServiceClass(entry.getValue().getClass());
                            secondaryServiceData.setSecondary(entry.getValue().getClass().getDeclaredAnnotation(Secondary.class));
                            secondaryServiceData.setFullObject(entry.getValue());
                            // TODO: 8/13/2021 check and throw the exception if duplicated
                            serviceMap.putIfAbsent(secondaryServiceData.getSecondary().step(), secondaryServiceData);
                        } else if (entry.getValue().getClass().isAnnotationPresent(OneEventListener.class)) {
                            OneEventListener declaredAnnotation = entry.getValue().getClass().getDeclaredAnnotation(OneEventListener.class);
                            ServiceListenerData serviceListenerData = new ServiceListenerData();
                            serviceListenerData.setFullObject(entry.getValue());
                            serviceListenerData.setBeanName(entry.getKey());
                            serviceListenerData.setListenerClass(entry.getValue().getClass());
                            serviceListenerData.setOneEventListener(declaredAnnotation);
                            if (serviceListeners.containsKey(declaredAnnotation.order())) {
                                throw new Exception("Order [" + declaredAnnotation.order() + "] has been duplicated.");
                            } else {
                                serviceListeners.put(declaredAnnotation.order(), serviceListenerData);
                            }

                        }
                    }


                    serviceMap.forEach((s, o) -> {
                        System.out.println("service = " + s);
                    });


                    //send start event
                    serviceListeners.forEach((s, o) -> {
                        System.out.println("listener = " + s);
                        Method[] methods = beansOfType.get(o.getBeanName()).getClass().getMethods();
                        Optional<Method> onStart = Arrays.stream(methods).filter(method -> method.getName().equals("onStart")).findFirst();

                        onStart.ifPresent(method -> {
                            if (o.getOneEventListener().mode().equals(RunningMode.Async)) {
                                Thread thread = new Thread(() -> {
                                    try {
                                        method.invoke(o.getFullObject(), aggregate, args);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                });
                                thread.setName("one_event-" + aClass.getSimpleName() + "-on_start");
                                thread.start();
                            } else {
                                try {
                                    method.invoke(o.getFullObject(), aggregate, args);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    });

                    serviceMap.forEach((aDouble, serviceData) -> {
//                        System.out.println("serviceData = " + aDouble);
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
                    System.out.println(ANSI_CYAN + at.render() + ANSI_RESET);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }
}
