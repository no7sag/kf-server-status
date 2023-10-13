package com.no7sag;

import com.no7sag.core.Commands;
import com.no7sag.core.Functions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class KFServerStatus {

    public static void main(String[] args) throws IOException {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String configPath = rootPath + "config.properties";

        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));

        String token = properties.getProperty("discord_bot_token");
        JDA bot = JDABuilder.createDefault(token)
                .addEventListeners(new Commands())
                .build();

        bot.upsertCommand("info", "Show active players.").queue();

        Functions functions = new Functions();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    bot.getPresence().setActivity(Activity.customStatus(functions.updateStatus()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        timer.schedule(task, 0, 60000);

    }

}
