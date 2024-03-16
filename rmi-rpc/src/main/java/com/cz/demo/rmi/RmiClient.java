package com.cz.demo.rmi;

import com.cz.demo.rmi.pojo.DemoPojo;
import com.cz.demo.rmi.service.DemoService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RPC demo by RMI
 * rpc client
 *
 * @author Zjianru
 */
public class RmiClient {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        // find remote server
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9998);
        //  find remote interface and cast interface type to DemoService
        DemoService lookup = (DemoService) registry.lookup(DemoService.class.getSimpleName());
        // finish remote invocation
        int params = 99;
        DemoPojo process = lookup.findProcess(params);
        // result output
        System.out.println("client send params is " + params);
        System.out.println("client receive result from server is " + process);
    }
}
