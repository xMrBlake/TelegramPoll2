package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.User;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class ResultCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "result";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "result-";
    private final TelegramPollPlugin plugin;

    public ResultCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Chat chat = message.getChat();
        if(!message.hasText() || arguments.length > 0 ){
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getId()), local(LANG_PREFIX + "usage"));
            try{
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        if(plugin.isCurrentVoting()){
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getId()), local(LANG_PREFIX+ "alredy-voting"));
            try{
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        Optional<User> user;
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            user = plugin.getUsersTable().selectPendingUser(connection, chat.getId());
        } catch (Exception throwable) {
            throwable.printStackTrace();
            return;
        }
        if(user.isEmpty() || !user.get().getSuperadmin()){
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getId()), local(LANG_PREFIX + "no-permission"));
            try{
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            plugin.getVotingService().result(absSender, 0, message.getChatId().toString());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
