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

import java.util.Optional;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class AdminCommand extends BotCommand implements IBotCommand {

    private final static String COMMAND_LABEL = "admin";
    private final static String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.admin-";

    private final TelegramPollPlugin plugin;

    public AdminCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage sendMessage;
        String chatId = message.getChatId().toString();
        if(arguments.length != 2){
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "usage"), null, chatId);
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try(MysqlConnection connection = plugin.getMysqlConnection();
            MysqlTransaction transaction = new MysqlTransaction(connection)
        ){
            Optional<User> user = plugin.getUsersTable().selectPendingUser(connection, Long.parseLong(chatId));
            if(user.isEmpty()){
                sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "user-not found"), null, chatId);
                transaction.commit();
                absSender.execute(sendMessage);
                return;
            }
            SendMessage result = null;
            if(arguments[1].equalsIgnoreCase("promote")){
                plugin.getUsersTable().updateUser(connection, user.get().withSuperAdmin(true));
                sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "success-promoted"), null, chatId);
                result = plugin.getMessageService().createMessage(local(LANG_PREFIX + "promoted"), null, arguments[0]);
            }
            else if(arguments[1].equalsIgnoreCase("demote")){
                plugin.getUsersTable().updateUser(connection, user.get().withSuperAdmin(false));
                sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "success-demoted"), null, chatId);
                result = plugin.getMessageService().createMessage(local(LANG_PREFIX + "demoted"), null, arguments[0]);
            }else{
                sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "usage"), null, chatId);
            }
            transaction.commit();
            if(result != null){
                absSender.execute(result);
            }
            absSender.execute(sendMessage);
            transaction.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
