package com.radcortez.wow.auctions.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Data
public class ConnectedRealm {
    private String id;
    private Status status;
    private Set<Realm> realms;

    public boolean isDown() {
        return Status.Type.DOWN.equals(status.getType());
    }
}
