package it.xmrblake.telegrampoll.model;

import it.arenacraft.mini.orm.api.annotations.Model;
import it.arenacraft.mini.orm.api.annotations.Primary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

@Data
@AllArgsConstructor
@With
@Model
public class Password {

    @Primary
    private final int id;
    private final int psw;
    private final boolean used;

}
