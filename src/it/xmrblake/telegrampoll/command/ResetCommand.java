package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlTransaction;
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

public class ResetCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "reset";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.reset-";

    private final TelegramPollPlugin plugin;

    public ResetCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        String chatId = String.valueOf(message.getChatId());
        if(arguments.length >= 1){
            SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "usage"));
            try{
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        Optional<User> user;
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            user = plugin.getUsersTable().selectPendingUser(connection, Long.parseLong(chatId));
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return;
        }
        if(user.isEmpty() || !user.get().getSuperadmin()){
            SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "no-permission"));
            try{
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        if(plugin.isCurrentVoting()){
            SendMessage sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "no-during-voting"), null, chatId);
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try (MysqlConnection connection = plugin.getMysqlConnection();
             MysqlTransaction transaction = new MysqlTransaction(connection)){
            plugin.getPasswordTable().dropPasswords(connection);
            plugin.getVotesTable().dropVotes(connection);
            List<User> users = plugin.getUsersTable().selectAllUsers(connection);
            for(User u : users){
                plugin.getUsersTable().updateUser(connection, u.withAccepted(0));
            }
            transaction.commit();
            SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "dropped"));
            absSender.execute(sendMessage);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

}
