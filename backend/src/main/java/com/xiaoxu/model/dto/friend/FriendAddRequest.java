package com.xiaoxu.model.dto.friend;

import lombok.Data;

import java.io.Serializable;

@Data
public class FriendAddRequest implements Serializable {
    private Long friendId;
}
