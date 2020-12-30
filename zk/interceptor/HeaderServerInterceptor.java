package com.han.startup.zk.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

import static io.grpc.Grpc.TRANSPORT_ATTR_REMOTE_ADDR;

/**
 * Created by shan2 on 7/12/2017.
 */
@Slf4j
public class HeaderServerInterceptor implements ServerInterceptor {
    public static Context.Key<String> REMOTE_IP = Context.key("x_remote_ip");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        SocketAddress socketAddress = serverCall.getAttributes().get(TRANSPORT_ATTR_REMOTE_ADDR);
        log.debug("[GRPC_interceptor] remote addr:{}", socketAddress);
        Context context = Context.current().withValue(REMOTE_IP, socketAddress.toString());
        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
