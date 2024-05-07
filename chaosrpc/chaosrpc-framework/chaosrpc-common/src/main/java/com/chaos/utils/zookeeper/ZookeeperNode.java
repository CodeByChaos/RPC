package com.chaos.utils.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author WongYut
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeeperNode {
    private String nodePath;
    private byte[] data;

}
