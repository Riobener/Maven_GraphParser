package com.company;


import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;


public class PackageParser {
    private String groupId;
    private String artifactId;
    private String version;
    private Document html;
    private Elements elements;
    static String finalString = "digraph{\n";
    static ArrayList<String> dependenceList = new ArrayList<>();
    PackageParser(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public void downloadPackage() throws Exception {
        html = Jsoup.connect("https://mvnrepository.com/artifact/" + groupId + "/" + artifactId + "/" + version).get();
        elements = html.getElementsByAttributeValue("class", "vbtn");
        String str = elements.attr("href");
        StringBuffer buffer = new StringBuffer(str);
        buffer.delete(str.length() - 3, str.length());
        downloadFile(str, artifactId + "-" + version + ".jar");

    }

    public void showDependencies() throws Exception {
        ArrayList<DependenceInfo> info = new ArrayList<>();
        String newGroupId;
        String newArtifactId;
        String newVersion;
        boolean siteIsAccessible = true;
        try {
            html = Jsoup.connect("https://mvnrepository.com/artifact/" + groupId + "/" + artifactId + "/" + version).get();
            elements = html.getElementsByAttributeValue("class", "vbtn");
        } catch (HttpStatusException ex) {
            siteIsAccessible = false;
        }
        if (siteIsAccessible) {
            String str = elements.attr("href");
            StringBuffer buffer = new StringBuffer(str);
            try {
                buffer.delete(str.length() - 3, str.length());
            } catch (StringIndexOutOfBoundsException ex) {
                return;
            }
            Document pomText = Jsoup.connect(buffer.toString() + "pom").get();
            org.w3c.dom.Document doc = loadXMLFromString(pomText.toString());
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("dependency");
            if (nList.getLength() > 0) {
                System.out.println("Dependencies of " + groupId + "." + artifactId + "." + version);
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        System.out.println("Dependency " + (temp + 1));
                        Element eElement = (Element) nNode;
                        newGroupId = eElement
                                .getElementsByTagName("groupId")
                                .item(0)
                                .getTextContent();
                        newGroupId = newGroupId.replaceAll("\\s+", "");
                        System.out.print("groupId : " + newGroupId + "\n");
                        newArtifactId = eElement
                                .getElementsByTagName("artifactId")
                                .item(0)
                                .getTextContent();
                        newArtifactId = newArtifactId.replaceAll("\\s+", "");
                        System.out.print("artifactId : " + newArtifactId + "\n");
                        try {
                            newVersion = eElement
                                    .getElementsByTagName("version")
                                    .item(0)
                                    .getTextContent();
                            newVersion = newVersion.replaceAll("\\s+", "");
                            System.out.print("version : " + newVersion + "\n");
                            System.out.println("================================");
                            info.add(new DependenceInfo(newGroupId, newArtifactId, newVersion));

                        } catch (NullPointerException ex) {
                            System.out.println("================================");
                        }
                    }
                }

                for (DependenceInfo i : info) {
                    final char dm = (char) 34;
                    if(!dependenceList.contains(i.getArtifactId())){
                        dependenceList.add(i.getArtifactId());
                        finalString+=dm+this.artifactId+dm+" -> "+dm+i.getArtifactId()+dm+"\n";
                        new PackageParser(i.getGroupId(), i.getArtifactId(), i.getVersion()).showDependencies();
                    }else{
                        finalString+=dm+this.artifactId+dm+" -> "+dm+i.getArtifactId()+dm+"\n";
                    }
                   //this.artifactId = this.artifactId.replaceAll("[()\\s-]+", " ");
                }
            }
        }

    }

    public void downloadFile(String fromUrl, String localFileName) throws IOException {
        File localFile = new File(localFileName);
        if (localFile.exists()) {
            localFile.delete();
        }
        localFile.createNewFile();
        URL url = new URL(fromUrl);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(localFileName));
        URLConnection conn = url.openConnection();
        String encoded = Base64.getEncoder().encodeToString(("username" + ":" + "password").getBytes(StandardCharsets.UTF_8));  //Java 8
        conn.setRequestProperty("Authorization", "Basic " + encoded);
        InputStream in = conn.getInputStream();
        byte[] buffer = new byte[1024];

        int numRead;
        while ((numRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, numRead);
        }
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    private org.w3c.dom.Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

}
