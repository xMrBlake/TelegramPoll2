package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlTransaction;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.PasswordObject;
import it.xmrblake.telegrampoll.model.User;
import it.xmrblake.telegrampoll.service.MessageService;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class LoginCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "login";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.login-";

    private final TelegramPollPlugin plugin;
    private final MessageService messageService;

    public LoginCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
        this.messageService = plugin.getMessageService();
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

   @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments){
        String chatId = String.valueOf(message.getChatId());
        if(!message.hasText() || arguments.length != 1){
            try{
                SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "usage"));
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return;
            }
            return;
        }
       Optional<User> user;
        try(MysqlConnection connection = plugin.getMysqlConnection();
            MysqlTransaction transaction = new MysqlTransaction(connection)){
            user = plugin.getUsersTable().selectPendingUser(connection, Long.parseLong(chatId));
            if(user.isPresent() && user.get().isAccepted()){
                SendMessage sendMessage = new SendMessage(chatId, local("alredy-logged"));
                absSender.execute(sendMessage);
                return;
            }
            if(plugin.getPasswordTable().isAlreadyUsed(connection, Integer.parseInt(arguments[0]))){
                SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "alredy-used"));
                absSender.execute(sendMessage);
                return;
            }

            //TODO devi controllare con isPresent prima di fare Optional#get altrimenti non ha senso usare gli Optional
            plugin.getUsersTable().updateUser(connection, user.get().withAccepted(true));
            plugin.getPasswordTable().updatePassword(connection, new PasswordObject(Integer.parseInt(arguments[0]), true));
            transaction.commit();
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return;
        }
       SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "updated"));
       try {
           absSender.execute(sendMessage);
       } catch (TelegramApiException e) {
           e.printStackTrace();
       }
   }
}
