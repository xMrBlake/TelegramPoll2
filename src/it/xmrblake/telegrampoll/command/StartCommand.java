package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlTransaction;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.User;
import it.xmrblake.telegrampoll.model.UserObject;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class StartCommand extends BotCommand implements IBotCommand {

    private final static String COMMAND_LABEL = "start";
    private final static String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.start-";

    private final TelegramPollPlugin plugin;

    public StartCommand(TelegramPollPlugin plugin) {
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
        UserObject user = new UserObject(chat.getId(), false, false);
        try(MysqlConnection connection = plugin.getMysqlConnection();
            MysqlTransaction transaction = new MysqlTransaction(connection)){
            Optional<User> dbUser = plugin.getUsersTable().selectPendingUser(connection,chat.getId());
            if(dbUser.isPresent()){
                SendMessage sendMessage = new SendMessage(String.valueOf(chat.getId()), local(LANG_PREFIX + "alredy-registered"));
                absSender.execute(sendMessage);
                transaction.commit();
                return;
            }
            plugin.getUsersTable().insertUser(user, connection);
            SendMessage mex = new SendMessage(String.valueOf(message.getChatId()), local(LANG_PREFIX + "welcome",
                    String.valueOf(message.getChatId())));
            absSender.execute(mex);
            transaction.commit();
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return;
        }
    }

}
