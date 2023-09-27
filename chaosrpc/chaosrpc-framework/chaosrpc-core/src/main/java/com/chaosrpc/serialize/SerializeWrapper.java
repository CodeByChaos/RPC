package com.chaosrpc.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerializeWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
