package com.no7sag.core;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Functions {

    Document document;
    Logger log = Logger.getLogger(Functions.class.getName());

    final static String SERVER_STATUS = "#content > div:nth-child(2) > div.well.well3 > div > div.span6 > div > div.page-header > div > span.value > span.serverstatustext.online > span";
    final static String MAP_NAME = "inlineblocktop map";  // Class
    final static String NO_PLAYERS = "#tabs-inner-serverplayers > table > tbody > tr > td:nth-child(2)";
    final static String PLAYER_COUNT = "playersonline";  // Class
    final static String PLAYER_LIST_A = "#tabs-inner-serverplayers > div > div > div:nth-child(2) > div > table > tbody";  // Without spectators
    final static String PLAYER_LIST_B = "#tabs-inner-serverplayers > div:nth-child(1) > div:nth-child(2) > div:nth-child(2) > div > table > tbody";  // With spectators

    public static String playerList;

    public String updateStatus() throws IOException {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String configPath = rootPath + "config.properties";

        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));

        String url = "https://www.gs4u.net/en/s/" + properties.getProperty("server_id") + ".html";

        try {
            document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!isOnline()) {
            log.info(">>>>> UPDATING STATUS: SERVER IS OFFLINE!");
            playerList = "Server is offline.";
            return "Server is offline";
        }

        Element mapNameElem = document.getElementsByClass(MAP_NAME).first();

        if (isEmpty()) {
            log.info(">>>>> UPDATING STATUS: SERVER IS EMPTY!");
            playerList = "No players connected.";
            return "0 players | " + mapNameElem.text();
        }

        String status = countPlayers() + " players | " + mapNameElem.text().split(" ")[1];
        log.info(">>>>> UPDATING STATUS: " + status);

        updatePlayerList();

        return status;

    }

    @NotNull
    private Boolean isOnline() {

        Element serverStatusElem = document.selectFirst(SERVER_STATUS);
        return serverStatusElem != null && serverStatusElem.text().equals("Online");

    }

    @NotNull
    private Boolean isEmpty() {

        Element noPlayersElem = document.selectFirst(NO_PLAYERS);
        return noPlayersElem != null;

    }

    @NotNull
    private String countPlayers() {

        Element playerCountElem = document.getElementsByClass(PLAYER_COUNT).first();
        String[] playerCountSplit = playerCountElem.text().split(" ");
        return playerCountSplit[0] + "/" + playerCountSplit[2];

    }

    private void updatePlayerList() {

        Element playerListElem;

        // Are there spectators? (just checking, we won't list them)
        if (document.selectFirst(PLAYER_LIST_B) != null)
            playerListElem = document.selectFirst(PLAYER_LIST_B);
        else
            playerListElem = document.selectFirst(PLAYER_LIST_A);

        StringBuilder playerListStrBuilder = new StringBuilder();

        int playerIndex = 1;
        for (Element e : playerListElem.getElementsByClass("other_color_text")) {
            playerListStrBuilder.append(playerIndex).append(". ").append(e.text()).append("\n");
            playerIndex++;
        }

        playerList = playerListStrBuilder.toString();

    }

}
