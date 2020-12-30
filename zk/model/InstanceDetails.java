package com.han.startup.zk.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.ubisoft.hfx.zk.discovery.enumeration.BackendServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by shan2 on 7/20/2017.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("details")
public class InstanceDetails {
    private String description;
    //	private boolean acquireActivated;
//	private boolean releaseActivated;
//	private boolean healthActivated;
    private String externalId;
    private String externalClusterId;
    private BackendServiceType serviceType;
    private boolean maintenance;
    private long lastUpdatedTs;
}
