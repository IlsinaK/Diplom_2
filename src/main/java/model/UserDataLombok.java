package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDataLombok {
    private String email;
    private String password;
    private String name;
}


