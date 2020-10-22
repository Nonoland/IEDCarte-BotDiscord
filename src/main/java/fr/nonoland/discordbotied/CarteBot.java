package fr.nonoland.discordbotied;

import com.google.gson.Gson;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Permission;
import fr.nonoland.discordbotied.json.Settings;
import fr.nonoland.discordbotied.json.Student;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CarteBot {

    private String token;

    private DiscordClient client;
    private GatewayDiscordClient gateway;
    private Settings settings;

    private Gson gson;
    private final Path pathSettings = Paths.get("settings.json");

    public CarteBot(String token) throws IOException {
        this.token = token;
        this.gson = new Gson();

        loadSettings();

        System.out.println(settings.getIdChannel());

        client = DiscordClient.create(this.token);
        gateway = client.login().block();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();
            String[] args = message.getContent().split(" ");

            if(settings.getIdChannel() == 0) {
                if(args[0].equalsIgnoreCase("!setup")) {
                    message.delete().block();
                    /* Setup code */
                    if(event.getMember().get().getBasePermissions().block().contains(Permission.ADMINISTRATOR)) {
                        //Set new Id Channel
                        settings.setIdChannel(event.getMessage().getChannelId().asLong());
                        //Save information
                        try {
                            saveSettings();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Message newMessage = message.getChannel().block()
                                .createMessage("Carte en cours de chargement...").block();
                        settings.setIdMessage(newMessage.getId().asLong());
                    }

                }
            }else if(message.getChannelId().asLong() == settings.getIdChannel()) {

                if (args[0].equalsIgnoreCase("!carte") && args.length == 2) {
                    message.delete().block();

                    List<Student> result = settings.getStudents().stream()
                            .filter(student -> message.getAuthor().get().getId().asLong() == student.getIdStudent())
                            .collect(Collectors.toList());

                    if(result.size() == 1) {
                        settings.getStudents().get(settings.getStudents().indexOf(result.get(0))).setCity(args[1]);
                    } else {
                        Student newStudent = new Student();
                        newStudent.setIdStudent(message.getAuthor().get().getId().asLong());
                        newStudent.setCity(args[1]);

                        settings.getStudents().add(newStudent);
                    }

                    try {
                        saveSettings();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    message.getAuthor().get().getPrivateChannel().block().createMessage(getUtf8("Votre ville a été mis à jours !")).block();
                    updateMainMessage();

                } else if (message.getContent().equalsIgnoreCase(("!stop iedcarte"))) {
                    if(event.getMember().get().getBasePermissions().block().contains(Permission.ADMINISTRATOR)) {
                        gateway.logout().block();
                        while(gateway.getGuilds().blockFirst() != null)
                            System.exit(0);
                    }
                }

            }


        });

        gateway.onDisconnect().block();
    }

    public void updateMainMessage() {
        String content = "";

        for(Student s : settings.getStudents()) {
                content += gateway.getUserById(Snowflake.of(s.getIdStudent())).block().getUsername() + " | "
                    + gateway.getUserById(Snowflake.of(s.getIdStudent())).block().getMention()
                    + " | Ville: " + s.getCity() + "\n"
                    + "Pour mettre à jours votre ville : !carte <ville>";
        }

        MessageEditSpec test = new MessageEditSpec();
        test.setContent(content);

        String finalContent = content;
        gateway.getMessageById(Snowflake.of(settings.getIdChannel()), Snowflake.of(settings.getIdMessage())).block().edit(spec -> spec.setContent(getUtf8(finalContent))).block();
    }

    public void loadSettings() throws IOException {
        if(Files.exists(pathSettings)) {
            Reader reader = Files.newBufferedReader(pathSettings);
            this.settings = this.gson.fromJson(reader, Settings.class);
        } else {
            this.settings = new Settings();
        }
    }

    public void saveSettings() throws IOException {
        Writer writer = Files.newBufferedWriter(pathSettings);
        gson.toJson(this.settings, writer);
        writer.close();
    }

    public static String getUtf8(String str) {
        byte[] value = str.getBytes(StandardCharsets.ISO_8859_1);
        return new String(value, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        new CarteBot(args[0]);
    }
}
