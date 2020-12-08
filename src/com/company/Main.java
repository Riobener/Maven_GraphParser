package com.company;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.Scanner;

import static com.company.PackageParser.finalString;

/*
USE THIS FOR TEST!!!!

    SMALL PACKAGE
    -------------
    groupId: org.springframework.boot
    artifactId: spring-boot-actuator
    version: 2.3.4.RELEASE

    LARGE PACKAGE
    -------------
    groupId: org.jsoup
    artifactId: jsoup
    version: 1.13.1

*/
public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        String groupId;
        String artifactId;
        String version;
        System.out.print("Введите groupId: ");
        groupId = scan.nextLine();
        System.out.print("Введите artifactId : ");
        artifactId = scan.nextLine();
        System.out.print("Введите version: ");
        version = scan.nextLine();
        PackageParser parser = new PackageParser(groupId, artifactId, version);
        parser.downloadPackage();
        parser.showDependencies();
        finalString += "\n}";
        try {
            FileWriter myWriter = new FileWriter("color.txt");
            myWriter.write(finalString + "");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try (InputStream dot = new FileInputStream("color.txt")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("FIN.png"));
        }
        System.out.println("DONE!!!! You can check the result in file FIN.png");

    }
}
