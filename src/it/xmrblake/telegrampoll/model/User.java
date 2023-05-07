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
    private final long chatid;
    private final int superadmin;
    private final int accepted;

    public boolean getSuperadmin(){
        if(superadmin == 1){
            return true;
        }
        return false;
    }

    public boolean isAccepted(){
        if(accepted == 1){
            return true;
        }
        return false;
    }
}
