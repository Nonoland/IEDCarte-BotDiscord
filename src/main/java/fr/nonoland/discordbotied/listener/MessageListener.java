package fr.nonoland.discordbotied.listener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Permission;
import fr.nonoland.discordbotied.CarteBot;
import fr.nonoland.discordbotied.json.Student;

import java.util.List;
import java.util.stream.Collectors;

public class MessageListener {

    private CarteBot carteBot;
    public MessageListener(CarteBot carteBot) {
        this.carteBot = carteBot;
    }

    public void event(MessageCreateEvent event) {
        System.out.println(carteBot.isSetup);
        Message message = event.getMessage();
        String[] args = message.getContent().split(" ");

        if(!carteBot.isSetup) {

            /* Setup command */
            if(args[0].equalsIgnoreCase("!setup")) {
                /* Delete user message (!setup) */
                message.delete().block();

                /* Verify if user has permission */
                if(event.getMember().get().getBasePermissions().block().contains(Permission.ADMINISTRATOR)) {
                    /* set new idChannel in settings */
                    carteBot.settings.setIdChannel(event.getMessage().getChannelId().asLong());

                    /* Send table message and set id in settings */
                    Message newMessageTable = message.getChannel().block()
                            .createMessage("La liste est vide pour le moment...").block();
                    carteBot.settings.setIdMessageTable(newMessageTable.getId().asLong());

                    /* Send info message and set id in settings */
                    Message newMessageInfo = message.getChannel().block()
                            .createMessage(spec -> {
                                spec.setContent(carteBot.getUtf8("\nPour mettre à jours votre ville : !carte <ville>\nExemple: !carte Paris\nCode source du bot: https://github.com/Nonoland/IEDCarte-BotDiscord"));
                            }).block();
                    carteBot.settings.setIdMessageInfo(newMessageInfo.getId().asLong());

                    /* Save settings */
                    carteBot.saveSettings();
                    carteBot.isSetup = true;
                } else {
                    /* Send error message to user */
                    message.getAuthor().get().getPrivateChannel().block()
                            .createMessage(CarteBot.getUtf8("Vous n'avez pas les permissions administrateurs !")).block();
                }
            }

        } else {

            /* Carte command */
            if (args[0].equalsIgnoreCase("!carte")) {
                /* Delete user message (!carte) */
                message.delete().block();

                if(args.length >= 2) {

                    List<Student> result = carteBot.settings.getStudents().stream()
                            .filter(student -> message.getAuthor().get().getId().asLong() == student.getIdStudent())
                            .collect(Collectors.toList());

                    if(result.size() == 1) {
                        carteBot.settings.getStudents().get(carteBot.settings.getStudents().indexOf(result.get(0))).setCity(args[1]);
                    } else {
                        Student newStudent = new Student();
                        newStudent.setIdStudent(message.getAuthor().get().getId().asLong());
                        newStudent.setCity(args[1]);

                        carteBot.settings.getStudents().add(newStudent);
                    }

                    /* Save new user in settings */
                    carteBot.saveSettings();

                    /* Send info message to user */
                    message.getAuthor().get().getPrivateChannel().block().createMessage(carteBot.getUtf8("Votre ville a été mise à jours !")).block();

                    /* Update table message */
                    carteBot.updateMessageTable();

                } else {

                    message.getAuthor().get().getPrivateChannel().block()
                            .createMessage(CarteBot.getUtf8("Vous avez mal renseigné votre ville !\n!carte <ville>\nExemple: !carte Paris")).block();

                }


            }

            /* Update table message command */
            if(message.getContent().equalsIgnoreCase("!updatecarte")) {
                message.delete().block();
                carteBot.updateMessageTable();
            }

            /* Stop command */
            if (message.getContent().equalsIgnoreCase(("!stopbot"))) {
                if(event.getMember().get().getBasePermissions().block().contains(Permission.ADMINISTRATOR)) {
                    message.delete().block();
                    carteBot.gateway.logout().block();
                    while(carteBot.gateway.getGuilds().blockFirst() != null)
                        System.exit(0);
                }
            }

        }
    }
}
