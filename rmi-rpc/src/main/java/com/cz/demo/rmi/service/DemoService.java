package com.cz.demo.rmi.service;

import com.cz.demo.rmi.pojo.DemoPojo;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RPC demo by RMI
 * business interface
 *
 * @author Zjianru
 */
public interface DemoService extends Remote  {
    /**
     * 业务方法
     *
     * @param count demo count
     * @return demo pojo
     */
    DemoPojo findProcess(int count) throws RemoteException;
}
