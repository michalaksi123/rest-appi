package pl.kurs.model.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateGarageCommand {

    private int places;
    private String address;
    private Boolean lpgAllowed;
}
