package it.xmrblake.telegrampoll;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import it.arenacraft.bridgecore.BridgeCore;
import it.arenacraft.bridgecore.api.BridgePlugin;
import it.arenacraft.bridgecore.api.BridgePluginDescription;
import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.essentials3.util.serialization.SerializationException;
import it.arenacraft.mini.orm.api.service.IMiniORMQueryService;
import it.arenacraft.mini.orm.api.service.IMiniORMRelationMappingService;
import it.arenacraft.mini.orm.api.service.IMiniORMService;
import it.xmrblake.telegrampoll.config.Config;
import it.xmrblake.telegrampoll.config.InlineKeyboardMarkupConfig;
import it.xmrblake.telegrampoll.model.Password;
import it.xmrblake.telegrampoll.model.User;
import it.xmrblake.telegrampoll.model.Vote;
import it.xmrblake.telegrampoll.service.KeyboardService;
import it.xmrblake.telegrampoll.service.MessageService;
import it.xmrblake.telegrampoll.service.VotingService;
import it.xmrblake.telegrampoll.tables.PasswordTable;
import it.xmrblake.telegrampoll.tables.UsersTable;
import it.xmrblake.telegrampoll.tables.VotesTable;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

@Getter
public class TelegramPollPlugin extends BridgePlugin {

    public static final String BASE_LANG_PREFIX = "telegrampoll.";
    private final static Gson gson = new Gson();

    private final Config config;
    private final TelegramPoll telegramPoll;

    private final MessageService messageService;
    private final KeyboardService keyboardService;
    private final VotingService votingService;
    private final IMiniORMService ormService;
    private final IMiniORMQueryService miniORMQueryService;
    private final IMiniORMRelationMappingService relationMappingService;

    private final HashMap<String, InlineKeyboardMarkupConfig> keyboards;
    private final LinkedHashMap<String, List<String>> roles;

    @Setter
    private boolean currentVoting = false;
    private final UsersTable usersTable;
    private final PasswordTable passwordTable;
    private final VotesTable votesTable;

    private int totalVoting;
    private int finishVoted;
    @Setter
    private boolean used;

    private int finishedPlayer;

    public TelegramPollPlugin(BridgeCore core, BridgePluginDescription description) throws IOException, SerializationException, SQLException {
        super(core, description);

        this.config = loadConfig("config.json", Config.class);
        Path rolesPath = getConfigFolder().resolve("roles.json");
        try (FileReader fileReader = new FileReader(rolesPath.toFile());
             JsonReader jsonReader = new JsonReader(fileReader)) {
            roles = gson.fromJson(jsonReader, new TypeToken<LinkedHashMap<String, List<String>>>(){}.getType());
        }

        this.telegramPoll = new TelegramPoll(this);

        this.messageService = new MessageService(this);
        this.keyboardService = new KeyboardService(this);
        this.votingService = new VotingService(this);
        this.ormService = getProvider(IMiniORMService.class);
        this.miniORMQueryService = getProvider(IMiniORMQueryService.class);
        this.relationMappingService = getProvider(IMiniORMRelationMappingService.class);
        this.usersTable = new UsersTable(miniORMQueryService);
        this.passwordTable = new PasswordTable(miniORMQueryService);
        this.votesTable = new VotesTable(miniORMQueryService);

        Path keyboardsPath = getConfigFolder().resolve("keyboards.json");
        try (FileReader fileReader = new FileReader(keyboardsPath.toFile());
             JsonReader jsonReader = new JsonReader(fileReader)) {
            keyboards = gson.fromJson(jsonReader, new TypeToken<HashMap<String, InlineKeyboardMarkupConfig>>(){}.getType());
        }
        try(MysqlConnection connection = this.getMysqlConnection()){
            relationMappingService.addAllTables(ormService.deduceAndCreateTablesFromModels(connection, Set.of(Password.class, User.class, Vote.class),
                    Map.of(Password.class, "tp", User.class, "tp", Vote.class, "tp")));
            relationMappingService.addAllModels(ormService.deduceRelationsFromModels(Set.of(Password.class, User.class, Vote.class)));
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramPoll);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addFinished(){
        finishedPlayer++;
    }

    public void addTotalVoting(){
        totalVoting++;
    }

    public int parseBoolean(boolean toPass){
        if(toPass){
            return 1;
        }
        return 0;
    }

    public boolean parseBoolean(int toPass){
        if(toPass == 1){
            return true;
        }
        return false;
    }

}
