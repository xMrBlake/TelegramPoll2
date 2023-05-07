package it.xmrblake.telegrampoll.command;

import com.google.gson.Gson;
import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.PasswordObject;
import it.xmrblake.telegrampoll.model.User;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class GeneratePasswordsCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "genera";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.genera-";
    private static final Gson gson = new Gson();

    private final TelegramPollPlugin plugin;
    public GeneratePasswordsCommand(TelegramPollPlugin plugin){
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
        if(arguments.length != 1 || !isNumeric(arguments[0])){
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
        int passwords = Integer.parseInt(arguments[0]);
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            List<Integer> passwordList = new ArrayList<>();
            for (int i = 0; i < passwords ; i++) {
                int psw =ThreadLocalRandom.current().nextInt(10000, 99999);
                passwordList.add(psw);
                plugin.getPasswordTable().insertPassword(connection, new PasswordObject(psw, 0));
            }
            SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "generated",
                    String.valueOf(passwords), String.valueOf(passwordList)));
            absSender.execute(sendMessage);
            System.out.println();
            generateFile(passwordList);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    private void generateFile(List<Integer> passwords) throws IOException {
        FileWriter writer = new FileWriter(plugin.getConfigFolder().toString()+  "/passwords.json");
        gson.toJson(passwords.toArray(), writer);
        writer.close();
    }

    private boolean isNumeric(String num){
        if(num == null){
            return false;
        }
        try{
            int i = Integer.parseInt(num);
        }catch (NumberFormatException exception){
            return false;
        }
        return true;
    }

}
