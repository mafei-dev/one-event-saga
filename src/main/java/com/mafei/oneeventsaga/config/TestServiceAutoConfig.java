package com.mafei.oneeventsaga.config;

import com.mafei.oneeventsaga.annotations.OneEventListener;
import com.mafei.oneeventsaga.annotations.Primary1;
import com.mafei.oneeventsaga.annotations.Secondary;
import com.mafei.oneeventsaga.annotations.Start;
import com.mafei.oneeventsaga.utils.ServiceData;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWord;
import de.vandermeer.asciithemes.u8.U8_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mafei.oneeventsaga.utils.Resources.ANSI_CYAN;
import static com.mafei.oneeventsaga.utils.Resources.ANSI_RESET;

@Configuration
@Component
@EnableConfigurationProperties(OneEventProperties.class)
public class TestServiceAutoConfig {

    private final OneEventProperties oneEventProperties;
    @Autowired
    ListableBeanFactory beanFactory;

    public TestServiceAutoConfig(OneEventProperties oneEventProperties) {
        this.oneEventProperties = oneEventProperties;
    }

    @PostConstruct
    public void doValidateAndGenerate() {
        System.out.println("oneEventProperties.getComponentScan() = " + oneEventProperties.getComponentScan());


        ValidateResponse validate = validate();
        if (!validate.pageList.isEmpty()) {
            createFile();
            validate.getProcess().forEach((name, doubleServiceDataTreeMap) -> {
                try {
                    generateHtml(doubleServiceDataTreeMap, name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            generatePages(validate.getPageList());
        } else {
            System.err.println("There was no one-event implementation.");
        }


        printConsoleTable(validate.getProcess());
        printConsoleSummary(validate.getPageList());


    }

    private void printConsoleSummary(Set<Class<?>> pageList) {
        String title = "One-Event configured service summery.";
        String baseClass = "Base Class";
        String processName = "Process Name";
        String description = "Description";
        String version = "Process Version";
        String basePackage = "Base Class Package";
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow(null, null, null, null, title);
        at.addRule();
        at.addRow(baseClass, processName, description, version, basePackage);
        at.addRule();

        AtomicInteger maxCount = new AtomicInteger();
        if (title.length() > maxCount.get()) {
            maxCount.set(title.length());
        }
        String classNames = (baseClass + processName + description + version + basePackage);
        if (classNames.length() > maxCount.get()) {
            maxCount.set(classNames.length());
        }


        /*pageList.forEach(aClass -> {
            Primary1 rootAnnotation = aClass.getDeclaredAnnotation(Primary1.class);
            StringBuilder row = new StringBuilder();
            row.append(aClass.getSimpleName());
            row.append(rootAnnotation.name());
            row.append(rootAnnotation.description());
            row.append(rootAnnotation.version().equals("") ? "-" : rootAnnotation.version());
            row.append(aClass.getPackage().getName());
            if (row.toString().length() > maxCount.get()) {
                maxCount.set(row.length());
            }
            System.out.println("row = " + row.toString().length());

        });*/
        pageList.forEach(aClass -> {

            Primary1 rootAnnotation = aClass.getDeclaredAnnotation(Primary1.class);
            at.addRow(
                    aClass.getSimpleName(),
                    rootAnnotation.name(),
                    rootAnnotation.description(),
                    rootAnnotation.version().equals("") ? "-" : rootAnnotation.version(),
                    aClass.getPackage().getName()
            );
            at.addRule();
        });
        at.setTextAlignment(TextAlignment.LEFT);
        at.getContext().setGrid(U8_Grids.borderLight());
        at.getRenderer().setCWC(new CWC_LongestWord());
        System.out.println("maxCount = " + maxCount.get());
        System.out.println(ANSI_CYAN + at.render(maxCount.get() + 10) + ANSI_RESET);
    }

    private void printConsoleTable(Map<String, TreeMap<Double, ServiceData>> process) {


        process.forEach((s, doubleServiceDataTreeMap) -> {
            AtomicInteger maxCount = new AtomicInteger();
            doubleServiceDataTreeMap.forEach((aDouble, serviceData) -> {
                if ((serviceData.getName()).length() > maxCount.intValue()) {
                    maxCount.set(serviceData.getName().length());
                }
            });
            AsciiTable at = new AsciiTable();
            at.addRule();
            at.addRow(s);
            at.addRule();
            AtomicInteger count = new AtomicInteger(0);
            doubleServiceDataTreeMap.forEach((aDouble, serviceData) -> {
                if (count.intValue() == 0) {
                    at.addRow("*");
                    at.addRow(("[" + serviceData.getStep() + "]"));
                    at.addRow((serviceData.getName()));
                } else {
                    at.addRow(("[" + serviceData.getStep() + "]"));
                    at.addRow((serviceData.getName()));
                }
                if (doubleServiceDataTreeMap.size() != count.intValue()) {
                    at.addRow("↓");
                }

                count.getAndIncrement();
            });
            at.addRule();
            at.setTextAlignment(TextAlignment.CENTER);
            at.getContext().setGrid(U8_Grids.borderDoubleLight());
            System.out.println(ANSI_CYAN + at.render(maxCount.intValue() + 10) + ANSI_RESET);
        });
    }

    private void createFile() {
        File file1 = new File("one-event");
        file1.mkdir();
    }

    private ValidateResponse validate() {
        Set<Class<?>> pageList = new HashSet<>();
        Reflections reflectionsPrimary = new Reflections(oneEventProperties.getComponentScan(),
                new TypeAnnotationsScanner(), new SubTypesScanner());
        Map<String, TreeMap<Double, ServiceData>> dataMap = new HashMap<>();
        Set<Class<?>> typesAnnotatedWith = reflectionsPrimary.getTypesAnnotatedWith(Primary1.class, true);
        typesAnnotatedWith.forEach(aClass -> {
            System.out.println("aClass = " + aClass);
            Set<Class<?>> subTypesOf = reflectionsPrimary.getSubTypesOf((Class<Object>) aClass);
            TreeMap<Double, ServiceData> process = new TreeMap<>();
            subTypesOf.forEach(aClass1 -> {

                if (aClass1.isAnnotationPresent(Start.class)) {
                    ServiceData root = new ServiceData();
                    root.setName(aClass1.getSimpleName());
                    Start rootAnnotation = aClass1.getDeclaredAnnotation(Start.class);
                    root.setVersion(rootAnnotation.version());
                    root.setStep(1.0);
                    root.setDescription(rootAnnotation.description());
                    process.put(1.0, root);
                    System.out.println("start service - " + aClass.getName());
                }

                if (aClass1.isAnnotationPresent(Secondary.class)) {
                    Secondary subServiceAnnotation = aClass1.getDeclaredAnnotation(Secondary.class);

                    if (process.containsKey(subServiceAnnotation.step())) {
                        throw new RuntimeException(subServiceAnnotation.step() + " step is already exist.");
                    } else {
                        ServiceData subService = new ServiceData();
                        subService.setStep(subServiceAnnotation.step());
                        subService.setVersion(subServiceAnnotation.version());
                        subService.setDescription(subServiceAnnotation.description());
                        subService.setName(aClass1.getSimpleName());
                        process.put(subServiceAnnotation.step(), subService);
                    }
                    System.out.println("Secondary service" + aClass.getName());
                }

                if (aClass1.isAnnotationPresent(OneEventListener.class)) {
                    System.out.println("OneEventListener service" + aClass.getName());
                }

                System.out.println("subTypesOf = " + aClass1);
            });
            dataMap.put(aClass.getSimpleName(), process);
            pageList.add(aClass);
            System.out.println("----------------");
        });

        /*typesAnnotatedWith.forEach(cClass -> {
            TreeMap<Double, ServiceData> process = new TreeMap<>();
            ServiceData root = new ServiceData();
            root.setName(cClass.getSimpleName());
            Primary rootAnnotation = cClass.getDeclaredAnnotation(Primary.class);
            root.setVersion(rootAnnotation.version());
            root.setStep(1.0);
            root.setDescription(rootAnnotation.description());
            process.put(1.0, root);

            Class<?>[] declaredClasses = cClass.getDeclaredClasses();

            Arrays.stream(declaredClasses).forEach(innerClass -> {
                Secondary subServiceAnnotation = innerClass.getDeclaredAnnotation(Secondary.class);

                if (process.containsKey(subServiceAnnotation.step())) {
                    throw new RuntimeException(subServiceAnnotation.step() + " step is already exist.");
                } else {
                    ServiceData subService = new ServiceData();
                    subService.setStep(subServiceAnnotation.step());
                    subService.setVersion(subServiceAnnotation.version());
                    subService.setDescription(subServiceAnnotation.description());
                    subService.setName(innerClass.getSimpleName());
                    process.put(subServiceAnnotation.step(), subService);
                }
            });
            dataMap.put(cClass.getSimpleName(), process);
            pageList.add(cClass.getSimpleName());
        });*/
        ValidateResponse response = new ValidateResponse();
        response.setProcess(dataMap);
        response.setPageList(pageList);
        return response;
    }

    private void generateHtml(TreeMap<Double, ServiceData> process, String name) throws IOException {


        File file = new File("src//main//resources//templates//index.html");
        Document document = Jsoup.parse(file, null);
        Element title = document.select("#topic-title").first();
        title.text(name);

        Element ele = document.getElementsByTag("tbody").last();

        AtomicInteger count = new AtomicInteger();
        process.forEach((aDouble, serviceData) -> {

            SortedMap<Double, ServiceData> doubles = process.headMap(aDouble);
            StringBuilder serviceRow = new StringBuilder();
            if (doubles.isEmpty()) {
                serviceRow.append("<td> <div class='tt' data-tt-id='service-1.0' data-tt-parent=''>")
                        .append(serviceData.getName())
                        .append(" - ")
                        .append(serviceData.getStep())
                        .append("</div> </td>");
            } else {
                if (count.intValue() != 0) {

                    serviceRow.append("<td>");
                    serviceRow.append("<div class='tt' data-tt-id='").append("service-").append(aDouble).append("' data-tt-parent='service-").append(doubles.lastKey()).append("'>")
                            .append(serviceData.getName())
                            .append(" - ")
                            .append(serviceData.getStep())
                            .append("</div>");
                    serviceRow.append("</td>");
                }

            }
            String row = "<tr> " + serviceRow + " </tr>";
            ele.append(row);
            count.getAndIncrement();
        });

        document.html();
        BufferedWriter bw = new BufferedWriter(new FileWriter("one-event//" + name + ".one-event.html"));
        bw.write(document.toString());
        bw.close();

    }

    private void generatePages(Set<Class<?>> pageList) {
        try {

            Document document = Jsoup.parseBodyFragment("<ul id='page-list' class='list-unstyled components mb-5'></ul>");

            Element pageListTag = document.select("#page-list").first();
            StringBuilder pageItem = new StringBuilder();
            pageList.forEach(element -> {
                pageItem.append("<li><a href='");
                pageItem.append(element.getSimpleName()).append(".one-event.html");
                pageItem.append("'>");
                pageItem.append(element.getSimpleName());
                pageItem.append("</a>");
                pageItem.append("</li>");

            });
            pageListTag.append(String.valueOf(pageItem));
            BufferedWriter bw = new BufferedWriter(new FileWriter("one-event//page-list.html"));

            bw.write(document.body().html());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        {//copy static folder
            File fromDir = new File("src//main//resources//static");
            File toDir = new File("one-event//static");
            try {
                FileUtils.copyDirectory(fromDir, toDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ValidateResponse {
        private Map<String, TreeMap<Double, ServiceData>> process;
        private Set<Class<?>> pageList;


        public Map<String, TreeMap<Double, ServiceData>> getProcess() {
            return process;
        }

        public void setProcess(Map<String, TreeMap<Double, ServiceData>> process) {
            this.process = process;
        }

        public Set<Class<?>> getPageList() {
            return this.pageList;
        }

        public void setPageList(Set<Class<?>> pageList) {
            this.pageList = pageList;
        }


    }

}



