package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.User;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class EndCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "end";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "vote.end-";

    private final TelegramPollPlugin plugin;

    public EndCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Optional<User> user;
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            user = plugin.getUsersTable().selectPendingUser(connection, message.getChatId());
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return;
        }
        if(user.isEmpty() || user.get().getSuperadmin()){
            SendMessage sendMessage = new SendMessage(message.getChatId().toString(), local(LANG_PREFIX + "no-permission"));
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        plugin.setCurrentVoting(false);
        List<User> users;
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            users = plugin.getUsersTable().selectAllUsers(connection);
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return;
        }
        /*for(User u : users){
            if(u.isSuperAdmin() || !u.isAccepted()){
                continue;
            }
            SendMessage sendMessage = new SendMessage(String.valueOf(u.getChatId()), local(LANG_PREFIX + "end"));
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }*/
    }

}
