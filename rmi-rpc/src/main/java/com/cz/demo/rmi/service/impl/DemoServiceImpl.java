package com.cz.demo.rmi.service.impl;

import com.cz.demo.rmi.pojo.DemoPojo;
import com.cz.demo.rmi.service.DemoService;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

/**
 * RPC demo by RMI
 * <p>
 * business interface implements
 *
 * @author Zjianru
 */
public class DemoServiceImpl extends UnicastRemoteObject implements DemoService {

    public DemoServiceImpl() throws RemoteException {
        super();
    }

    /**
     * method
     *
     * @param count param
     * @return demo pojo
     */
    @Override
    public DemoPojo findProcess(int count) {
        return new DemoPojo("process result==> [ server receive count is " + count + " ]", count == 0, count);
    }
}
