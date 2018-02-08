package com.company;

import java.io.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {

    // First, get all repositories.
    // Then, get and harvest and store all .sol files in root of those repos.
    // Then, get links to all other directories
    // Then, get and harvest and store all .sol files in those directories.


    public static void main(String[] args) throws IOException, InterruptedException {
        String url = "https://github.com/search?utf8=%E2%9C%93&q=contract+extension%3Asol+created%3A%3E2015&type=Repositories&ref=advsearch&l=&l=";

        ArrayList<String> allUrls = new ArrayList<String>();

        while (!url.contains("&p=10&") ) {
            Document doc = Jsoup.connect(url).get();
            ArrayList<String> theseUrls = getLinks(doc);
            allUrls.addAll(theseUrls);
            Elements nextPage = doc.select(".current+ a");
            url = nextPage.select("a[href]").attr("abs:href").toString();
            TimeUnit.SECONDS.sleep(10);
        }

        for (String url2 : allUrls) {
            harvestSolFilesFromRepo(url2);
        }

    }

    private static void harvestSolFilesFromRepo(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select(".js-navigation-open");

        for (Element link : links) {
            String linkUrl = link.attr("abs:href");
            print(linkUrl);

            if (linkUrl.toString().endsWith(".sol")) {
                Document doc2 = Jsoup.connect(linkUrl).get();

                String rawLink = doc2.getElementById("raw-url").attr("abs:href").toString();

                String actualRawLink = rawLink.substring(19);
                String fileName = actualRawLink.replace("/","-");
                print("link at 50 = " + actualRawLink);
                actualRawLink = "https://raw.githubusercontent.com/" + actualRawLink;
                print("link at 52 = " + actualRawLink);
                actualRawLink = actualRawLink.replace("blob/","");
                actualRawLink = actualRawLink.replace("raw/","");
                print("link at 54 = " + actualRawLink);

                Document doc3 = Jsoup.connect(actualRawLink).get();

                String body = doc3.body().text();

                // Print and save .sol file!
                BufferedWriter writer = new BufferedWriter(new FileWriter("Users/t/Desktop/solidity" + fileName));
                writer.write(body);

                writer.close();

            }
//            else if {
//                // If a directory search it please ...
//            }
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }

    private static ArrayList<String> getLinks(Document doc) {
        Elements links = doc.select(".v-align-middle , em"); // direct a after h3

        links = links.select("a[href]");

        ArrayList<String> urls= new ArrayList<String>();

        print("\nLinks: (%d)", links.size());
        for (Element link : links) {
            urls.add(link.attr("abs:href"));
            print(link.attr("abs:href"));
        }

        return urls;
    }

}
