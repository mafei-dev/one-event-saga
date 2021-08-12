package com.mafei.oneeventsaga.config;

import com.mafei.oneeventsaga.annotations.Primary;
import com.mafei.oneeventsaga.annotations.Secondary;
import com.mafei.oneeventsaga.utils.ServiceData;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.u8.U8_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
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

@Configuration
@Component
@EnableConfigurationProperties(OneEventProperties.class)
public class TestServiceAutoConfig {

    private final OneEventProperties oneEventProperties;

    public TestServiceAutoConfig(OneEventProperties oneEventProperties) {
        this.oneEventProperties = oneEventProperties;
    }


    @PostConstruct
    public void doValidateAndGenerate() {
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
            at.addRow("OneEvent");
            at.addRule();
            AtomicInteger count = new AtomicInteger(0);
            doubleServiceDataTreeMap.forEach((aDouble, serviceData) -> {
                if (count.intValue() == 0) {
                    at.addRow(("["+serviceData.getStep()+"]"));
                    at.addRow((serviceData.getName()));
                    at.addRow("*");
                } else {
                    at.addRow(("["+serviceData.getStep()+"]"));
                    at.addRow((serviceData.getName()));
                }
                if (doubleServiceDataTreeMap.size() != count.intValue()) {
                    at.addRow("↓");
                }

                count.getAndIncrement();
            });
            at.addRule();
            at.setTextAlignment(TextAlignment.CENTER);
            at.getContext().setGrid(U8_Grids.borderDouble());
            System.out.println(at.render(maxCount.intValue() + 10));
        });
    }

    private void createFile() {
        File file1 = new File("one-event");
        file1.mkdir();
    }

    private ValidateResponse validate() {
        Set<String> pageList = new HashSet<>();
        Reflections reflectionsPrimary = new Reflections(oneEventProperties.getComponentScan(),
                new TypeAnnotationsScanner(), new SubTypesScanner());
        Map<String, TreeMap<Double, ServiceData>> dataMap = new HashMap<>();
        Set<Class<?>> typesAnnotatedWith = reflectionsPrimary.getTypesAnnotatedWith(Primary.class);
        typesAnnotatedWith.forEach(cClass -> {
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
        });
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

    private void generatePages(Set<String> pageList) {
        try {

            Document document = Jsoup.parseBodyFragment("<ul id='page-list' class='list-unstyled components mb-5'></ul>");

            Element pageListTag = document.select("#page-list").first();
            StringBuilder pageItem = new StringBuilder();
            pageList.forEach(element -> {
                pageItem.append("<li><a href='");
                pageItem.append(element).append(".one-event.html");
                pageItem.append("'>");
                pageItem.append(element);
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
        private Set<String> pageList;


        public Set<String> getPageList() {
            return pageList;
        }

        public void setPageList(Set<String> pageList) {
            this.pageList = pageList;
        }

        public Map<String, TreeMap<Double, ServiceData>> getProcess() {
            return process;
        }

        public void setProcess(Map<String, TreeMap<Double, ServiceData>> process) {
            this.process = process;
        }
    }

}



