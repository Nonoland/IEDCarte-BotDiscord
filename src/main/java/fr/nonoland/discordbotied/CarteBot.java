package fr.nonoland.discordbotied;

import com.google.gson.Gson;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageEditSpec;
import fr.nonoland.discordbotied.json.Settings;
import fr.nonoland.discordbotied.json.Student;
import fr.nonoland.discordbotied.listener.MessageListener;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CarteBot {

    public String token;

    public DiscordClient client;
    public GatewayDiscordClient gateway;
    public Settings settings;

    public Gson gson;
    public final Path pathSettings = Paths.get("settings.json");

    public boolean isSetup = false;

    /* Listeners */
    private MessageListener messageListener;

    public CarteBot(String token) {
        this.token = token;
        this.gson = new Gson();

        /* Load listeners */
        messageListener = new MessageListener(this);

        loadSettings();

        client = DiscordClient.create(this.token);
        gateway = client.login().block();

        gateway.on(MessageCreateEvent.class).subscribe(event -> messageListener.event(event));

        gateway.onDisconnect().block();
    }

    public void updateMessageTable() {
        String content = "";

        for(Student s : settings.getStudents()) {
                content += gateway.getUserById(Snowflake.of(s.getIdStudent())).block().getUsername() + " | "
                    + gateway.getUserById(Snowflake.of(s.getIdStudent())).block().getMention()
                    + " | " + s.getCity() + "\n";
        }

        String finalContent = content;
        gateway.getMessageById(Snowflake.of(settings.getIdChannel()), Snowflake.of(settings.getIdMessageTable())).block()
                .edit(spec -> {
                    spec.setContent(getUtf8(finalContent));
                }).block();
    }

    public void loadSettings(){
        try {
            if(Files.exists(pathSettings)) {
                Reader reader = Files.newBufferedReader(pathSettings);
                this.settings = this.gson.fromJson(reader, Settings.class);
                if(settings.getIdChannel() != 0)
                    isSetup = true;
            } else {
                this.settings = new Settings();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSettings() {
        try {
            Writer writer = Files.newBufferedWriter(pathSettings);
            gson.toJson(this.settings, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUtf8(String str) {
        return new String(str.getBytes(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        new CarteBot(args[0]);
    }
}
