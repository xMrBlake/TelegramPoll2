package it.xmrblake.telegrampoll.service;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.essentials3.util.localization.Formatting;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.User;
import it.xmrblake.telegrampoll.model.Vote;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class MessageService {

    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "messages.";

    private final TelegramPollPlugin plugin;

    public MessageService(TelegramPollPlugin plugin){
        this.plugin = plugin;
    }

    public void sendMessage(AbsSender sender, SendMessage message){
        try{
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendToAdmin(AbsSender sender, String text, ReplyKeyboard keyboard){
        //TODO get superAdmins
        List<User> admins = new ArrayList<>();
        //TODO non è un vero errore
        if(admins.isEmpty()){
            plugin.log("[TelegramPoll] > no admin registered");
            return;
        }
        SendMessage sendMessage = createMessage(text, keyboard);
        for(User admin : admins){
            sendMessage.setChatId(admin.getChatId());
            try{
                sender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(AbsSender sender, String tesxt, ReplyKeyboard keyboard){
        //TODO get users from db excluding admins
        List<User> users = new ArrayList<>();
        if(users.isEmpty()){
            plugin.log("[TelegramPoll]> no users to send message");
            return;
        }
        SendMessage sendMessage = createMessage(tesxt, keyboard);
        try{
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public SendMessage createMessage(String text, ReplyKeyboard keyboard, String chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        if(keyboard != null){
            sendMessage.setReplyMarkup(keyboard);
        }
        sendMessage.setParseMode(plugin.getConfig().getParseMode());
        if(chatId != null){
            sendMessage.setChatId(chatId);
        }
        return sendMessage;
    }

    public SendMessage createMessage(String text, ReplyKeyboard keyboard){
        return createMessage(text, keyboard, null);
    }


    public String createApplicationMessage(int index){
        Set<String> keySet = plugin.getRoles().keySet();
        String[] indexedKey = keySet.toArray(new String[keySet.size()]);
        String localeKey = LANG_PREFIX + "application";
        return local(localeKey, indexedKey[index], Formatting.readableList(localeKey + ".entry", plugin.getRoles().get(indexedKey[index])));
    }


    public String createResultMessage(int index, String[] indexedKey){
        List<Vote> results;
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            results = plugin.getVotesTable().selectVotes(connection, index);
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return null;
        }
        long contrario = results.stream()
                .filter(vote -> vote.getVote().equalsIgnoreCase("contrario"))
                .count();
        long astenuto = results.stream()
                .filter(vote -> vote.getVote().equalsIgnoreCase("astenuto"))
                .count();
        long annullata = results.stream()
                                 .filter(vote -> vote.getVote().equalsIgnoreCase("Annullata"))
                                 .count();
        long cancellati = results.stream()
                                  .filter(vote -> vote.getVote().equalsIgnoreCase("cancellata"))
                                  .count();

        List<String> entries = plugin.getRoles().get(indexedKey[index]);
        List<String> result = new ArrayList<>();
        for(String entry : entries){
            long count = results.stream().filter(vote -> vote.getVote().equalsIgnoreCase(entry)).count();
            result.add(local(LANG_PREFIX + "format", entry, String.valueOf(count)));
        }

        return local(LANG_PREFIX + "result", indexedKey[index],
                local(LANG_PREFIX + "contrario", String.valueOf(contrario)),
                local(LANG_PREFIX + "astenuto", String.valueOf(astenuto)),
                local(LANG_PREFIX + "annullate", String.valueOf(annullata)),
                local(LANG_PREFIX + "cancellati", String.valueOf(cancellati)),
                Formatting.readableList(LANG_PREFIX + "list", result));
    }
}
