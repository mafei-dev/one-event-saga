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

import static com.mafei.oneeventsaga.utils.Resources.ANSI_CYAN;
import static com.mafei.oneeventsaga.utils.Resources.ANSI_RESET;

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

    Logger logger = LoggerFactory.getLogger(OneEventTemplateImpl.class);
    @Autowired
    ListableBeanFactory beanFactory;

    @Bean
    public OneEventTemplate<Aggregate> oneEventTemplate(ApplicationContext context) {
        return new OneEventTemplate<Aggregate>() {
            @Override
            public OneEventResponse process(Class<?> aClass, Aggregate aggregate, Object... args) {
                OneEventResponse response = new OneEventResponse();
                TreeMap<Double, ServiceData> completedService = new TreeMap<>();

                Map<String, ?> beansOfType = beanFactory.getBeansOfType(aClass);
                TreeMap<Double, IServiceData> serviceMap = new TreeMap<>();
                TreeMap<Double, IServiceData> prcessingServiceMap = serviceMap;
                TreeMap<Integer, ServiceListenerData> serviceListeners = new TreeMap<>();


                try {
                    for (Map.Entry<String, ?> entry : beansOfType.entrySet()) {

                        if (entry.getValue().getClass().isAnnotationPresent(Start.class)) {
                            IServiceData startIServiceData = new IServiceData();
                            startIServiceData.setBeanName(entry.getKey());
                            startIServiceData.setServiceType(ServiceTypes.START_SERVICE);
                            startIServiceData.setServiceClass(entry.getValue().getClass());
                            startIServiceData.setStart(entry.getValue().getClass().getDeclaredAnnotation(Start.class));
                            startIServiceData.setFullObject(entry.getValue());
                            // TODO: 8/13/2021 check and throw the exception if duplicated
                            serviceMap.putIfAbsent(1.0, startIServiceData);
                        } else if (entry.getValue().getClass().isAnnotationPresent(Secondary.class)) {
                            IServiceData secondaryIServiceData = new IServiceData();
                            secondaryIServiceData.setBeanName(entry.getKey());
                            secondaryIServiceData.setServiceType(ServiceTypes.SECONDER_SERVICE);
                            secondaryIServiceData.setServiceClass(entry.getValue().getClass());
                            secondaryIServiceData.setSecondary(entry.getValue().getClass().getDeclaredAnnotation(Secondary.class));
                            secondaryIServiceData.setFullObject(entry.getValue());
                            // TODO: 8/13/2021 check and throw the exception if duplicated
                            serviceMap.putIfAbsent(secondaryIServiceData.getSecondary().step(), secondaryIServiceData);
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

                    serviceMap.forEach((aDouble, IServiceData) -> {
//                        System.out.println("serviceData = " + aDouble);
                        Method[] methods = beansOfType.get(IServiceData.getBeanName()).getClass().getMethods();
                        Optional<Method> process = Arrays.stream(methods).filter(method -> method.getName().equals("process")).findFirst();
                        process.ifPresent(method -> {
                            try {
                                Object invoke = method.invoke(IServiceData.getFullObject(), aggregate, args);
                                IServiceData.setProcessStatus(ProcessStatus.PASSED);
                                prcessingServiceMap.replace(aDouble, IServiceData);
                                ServiceData serviceData = new ServiceData();
                                serviceData.setArgs(args);
                                serviceData.setBeanName(IServiceData.getBeanName());
                                serviceData.setProcessStatus(ProcessStatus.PASSED);
                                serviceData.setServiceClass(IServiceData.getServiceClass());
                                if (IServiceData.getServiceType().equals(ServiceTypes.START_SERVICE)) {

                                    ServiceMetaData metadata = new ServiceMetaData();
                                    metadata.setStep(aDouble);
                                    metadata.setDescription(IServiceData.getStart().description());
                                    metadata.setVersion(IServiceData.getStart().version());
                                    metadata.setServiceType(ServiceTypes.START_SERVICE);
                                    serviceData.setMetadata(metadata);
                                } else if (IServiceData.getServiceType().equals(ServiceTypes.SECONDER_SERVICE)) {
                                    ServiceMetaData metadata = new ServiceMetaData();
                                    metadata.setStep(aDouble);
                                    metadata.setDescription(IServiceData.getSecondary().description());
                                    metadata.setVersion(IServiceData.getSecondary().version());
                                    metadata.setServiceType(ServiceTypes.SECONDER_SERVICE);
                                    serviceData.setMetadata(metadata);
                                }
                                completedService.put(aDouble, serviceData);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    });

                    AtomicInteger maxCount = new AtomicInteger();
                    prcessingServiceMap.forEach((aDouble, IServiceData) -> {
                        StringBuilder tableRow = new StringBuilder();
                        tableRow.append(aDouble);
                        tableRow.append(IServiceData.getBeanName());
                        tableRow.append(IServiceData.getBeanName());
                        tableRow.append(IServiceData.getProcessStatus());
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
                    prcessingServiceMap.forEach((aDouble, IServiceData) -> {
                        at.addRow(Double.toString(aDouble), IServiceData.getBeanName(), IServiceData.getProcessStatus());
                        at.addRule();
                    });
                    at.setTextAlignment(TextAlignment.LEFT);
                    at.getContext().setGrid(U8_Grids.borderLight());
                    at.getRenderer().setCWC(new CWC_LongestWord());
                    System.out.println(ANSI_CYAN + at.render() + ANSI_RESET);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.setCompletedService(completedService);
                return response;
            }
        };
    }
}
