package it.xmrblake.telegrampoll;

import at.favre.lib.crypto.bcrypt.BCrypt;
import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlTransaction;
import it.xmrblake.telegrampoll.command.*;
import it.xmrblake.telegrampoll.model.Vote;
import it.xmrblake.telegrampoll.model.VoteObject;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class TelegramPoll extends TelegramLongPollingCommandBot {

    private final TelegramPollPlugin plugin;
    private final int votations;
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "voting-response.";

    public TelegramPoll(TelegramPollPlugin telegramPollPlugin) {
        this.plugin = telegramPollPlugin;
        this.votations = plugin.getRoles().keySet().size();

        registerAll(new StartCommand(plugin),
                new LoginCommand(plugin),
                new GeneratePasswordsCommand(plugin),
                new ResetCommand(plugin),
                new StartPollCommand(plugin),
                new ResultCommand(plugin),
                new EndCommand(plugin),
                new LoggedCommand(plugin),
                new MeCommand(),
                new StartVotingCommand(plugin),
                new AdminCommand(plugin),
                new CancelCommand(plugin)
                );
        registerDefaultAction((absSender, message) -> {
            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(String.valueOf(message.getChatId()));
            commandUnknownMessage.setText(local("telegrampoll.commands.command-not-found"));
            try {
                absSender.execute(commandUnknownMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getBotUsername() {
        return plugin.getConfig().getUsername();
    }

    @Override
    public String getBotToken() {
        return plugin.getConfig().getToken();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }
        Message message = update.getCallbackQuery().getMessage();
        long chatId = message.getChatId();

        String[] callData = update.getCallbackQuery().getData().split("-");
        if (callData[0].equalsIgnoreCase("application")) {
            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(chatId);
            newMessage.setMessageId(message.getMessageId());
            if (!plugin.isCurrentVoting()) {
                newMessage.setText(local(LANG_PREFIX + "not-vote-session"));
                try {
                    execute(newMessage);
                    execute(plugin.getKeyboardService().modifyReplyKeyboard(message, null));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
            VoteObject vote = null;
            String voteCode = callData[callData.length - 1];
            int index = Integer.parseInt(callData[2]);
            if(checkSended(index, voteCode)){
                return;
            }
            String bcryptChatId = BCrypt.withDefaults().hashToString(5, voteCode.toCharArray());
            if (callData[1].equalsIgnoreCase("reject")) {
                vote = new VoteObject(bcryptChatId, index, "contrario");
            }
            if (callData[1].equalsIgnoreCase("astenuto")) {
                vote = new VoteObject(bcryptChatId, index, "astenuto");
            }
            if (callData[1].equalsIgnoreCase("positive")) {
                vote = new VoteObject(bcryptChatId, index, callData[3]);
            }
            if(callData[1].equalsIgnoreCase("blank")){
                return;
            }
            if(callData[1].equalsIgnoreCase("null")){
                nullVote(index, voteCode);
                newMessage.setText(local(LANG_PREFIX + "end"));
                try {
                    execute(newMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            try (MysqlConnection connection = plugin.getMysqlConnection()) {
                plugin.getVotesTable().insertVote(connection, vote);
            } catch (Exception throwables) {
                throwables.printStackTrace();
            }

            if (votations > index + 1) {
                newMessage.setText(plugin.getMessageService().createApplicationMessage(index + 1));
                try {
                    execute(newMessage);
                    execute(plugin.getKeyboardService().modifyReplyKeyboard(message,
                            plugin.getKeyboardService().createKeyboardForRole(index + 1, Integer.parseInt(voteCode))));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
            newMessage.setText(local(LANG_PREFIX + "end"));
            plugin.addFinished();
            try {
                execute(newMessage);
                //execute(plugin.getKeyboardService().modifyReplyKeyboard(message, null));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            return;
        }
        if (callData[0].equalsIgnoreCase("results")) {
            Set<String> keySet = plugin.getRoles().keySet();
            String[] indexedKey = keySet.toArray(new String[keySet.size()]);
            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(chatId);
            newMessage.setMessageId(message.getMessageId());
            newMessage.setText(
                    plugin.getMessageService().createResultMessage(Integer.parseInt(callData[2]), indexedKey));
            newMessage.setReplyMarkup(
                    plugin.getKeyboardService().createResultsKeyboard(Integer.parseInt(callData[2]), keySet.size()));
            try {
                execute(newMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean checkSended(int application, String chatId){
        List<Vote> votes = new ArrayList<>();
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            votes = plugin.getVotesTable().selectVotes(connection, application);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for(Vote vote : votes){
            BCrypt.Result verify = BCrypt.verifyer().verify(chatId.toCharArray(), vote.getChatid());
            if(verify.verified){
                return true;
            }
        }
        return false;
    }

    private void nullVote(int index, String voteCode){
        try(MysqlConnection connection = plugin.getMysqlConnection();
            MysqlTransaction transaction = new MysqlTransaction(connection)){

            List<Vote> votes = plugin.getVotesTable().selectAllVotes(connection);
            for(Vote vote : votes){
                BCrypt.Result verify = BCrypt.verifyer().verify(voteCode.toCharArray(), vote.getChatid());
                if(verify.verified){
                    plugin.getVotesTable().updateVote(connection, vote, new VoteObject(vote.getChatid(), vote.getApplicationid(), "Annullata"));
                }
            }
            for (int i = index; i < plugin.getRoles().size() ; i++) {
                String bcryptChatId = BCrypt.withDefaults().hashToString(5, voteCode.toCharArray());
                VoteObject vote = new VoteObject(bcryptChatId, i, "Annullata");
                plugin.getVotesTable().insertVote(connection, vote);
            }
            transaction.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
