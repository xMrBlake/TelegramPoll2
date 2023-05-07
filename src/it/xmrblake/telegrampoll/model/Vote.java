package it.xmrblake.telegrampoll.model;

import it.arenacraft.mini.orm.api.annotations.Model;
import it.arenacraft.mini.orm.api.annotations.Primary;
import lombok.Data;
import lombok.With;

@Data
@With
@Model
public class Vote {
    @Primary
    private final int id;
    private final String chatId;
    private final int applicationId;
    private final String vote;
}
