package it.xmrblake.telegrampoll.model;

import it.arenacraft.mini.orm.api.annotations.Model;
import it.arenacraft.mini.orm.api.annotations.Primary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

@Data
@With
@AllArgsConstructor
@Model
public class User {
    @Primary
    private final int id;
    private final long chatId;
    private final boolean superAdmin;
    private final boolean accepted;
}
