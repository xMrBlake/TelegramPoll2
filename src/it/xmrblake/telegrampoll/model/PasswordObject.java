package it.xmrblake.telegrampoll.model;

import lombok.Data;

@Data
public class PasswordObject {

    private final int psw;
    private final int used;

    public boolean isUsed(){
        if(used == 1){
            return true;
        }
        return false;
    }

}
