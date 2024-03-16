package com.cz.demo.rmi;

import com.cz.demo.rmi.service.DemoService;
import com.cz.demo.rmi.service.impl.DemoServiceImpl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RPC demo by RMI
 * <p>
 * rpc server
 * Providing operational capacity externally
 *
 * @author Zjianru
 */
public class RmiServer {
    public static void main(String[] args) {
        try {
            // registry server by RMI
            Registry registry = LocateRegistry.createRegistry(9998);
            // bind business interface
            DemoService demoService = new DemoServiceImpl();
            // specify the interface implementation
            registry.rebind(DemoService.class.getSimpleName(), demoService);
            // output server status
            System.out.println("server started and ready ... ");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }


    }
}
