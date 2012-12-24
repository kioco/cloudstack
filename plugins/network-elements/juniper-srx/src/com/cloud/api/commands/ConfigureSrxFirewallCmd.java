// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the 
// specific language governing permissions and limitations
// under the License.
package com.cloud.api.commands;

import com.cloud.server.api.response.ExternalLoadBalancerResponse;
import org.apache.log4j.Logger;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.IdentityMapper;
import org.apache.cloudstack.api.Implementation;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.PlugService;
import org.apache.cloudstack.api.ServerApiException;
import com.cloud.api.response.SrxFirewallResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.ExternalFirewallDeviceVO;
import com.cloud.network.element.JuniperSRXFirewallElementService;
import com.cloud.user.UserContext;
import com.cloud.utils.exception.CloudRuntimeException;

@Implementation(responseObject=SrxFirewallResponse.class, description="Configures a SRX firewall device")
public class ConfigureSrxFirewallCmd extends BaseAsyncCmd {

    public static final Logger s_logger = Logger.getLogger(ConfigureSrxFirewallCmd.class.getName());
    private static final String s_name = "configuresrxfirewallresponse";
    @PlugService JuniperSRXFirewallElementService _srxFwService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="external_load_balancer_devices")
    @Parameter(name=ApiConstants.FIREWALL_DEVICE_ID, type=CommandType.LONG, required=true, description="SRX firewall device ID")
    private Long fwDeviceId;

    @Parameter(name=ApiConstants.FIREWALL_DEVICE_CAPACITY, type=CommandType.LONG, required=false, description="capacity of the firewall device, Capacity will be interpreted as number of networks device can handle")
    private Long capacity;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getFirewallDeviceId() {
        return fwDeviceId;
    }

    public Long getFirewallCapacity() {
        return capacity;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException, ResourceAllocationException {
        try {
            ExternalFirewallDeviceVO fwDeviceVO = _srxFwService.configureSrxFirewall(this);
            if (fwDeviceVO != null) {
                SrxFirewallResponse response = _srxFwService.createSrxFirewallResponse(fwDeviceVO);
                response.setObjectName("srxfirewall");
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(BaseAsyncCmd.INTERNAL_ERROR, "Failed to configure SRX firewall device due to internal error.");
            }
        }  catch (InvalidParameterValueException invalidParamExcp) {
            throw new ServerApiException(BaseCmd.PARAM_ERROR, invalidParamExcp.getMessage());
        } catch (CloudRuntimeException runtimeExcp) {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, runtimeExcp.getMessage());
        }
    }

    @Override
    public String getEventDescription() {
        return "Configuring a SRX firewall device";
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_EXTERNAL_FIREWALL_DEVICE_CONFIGURE;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return UserContext.current().getCaller().getId();
    }
}
