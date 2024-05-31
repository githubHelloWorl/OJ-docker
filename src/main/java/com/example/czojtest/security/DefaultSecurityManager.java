package com.example.czojtest.security;

import java.security.Permission;

/**
 * 默认安全管理组
 */
public class DefaultSecurityManager extends SecurityManager{

    /**
     * 检查所有的权限
     * @param perm   the requested permission.
     */
    @Override
    public void checkPermission(Permission perm){
        System.out.println("默认不做任何限制");
        super.checkPermission(perm);
//        throw new SecurityException("权限不足" + perm.getActions());
    }

    @Override
    public void checkExec(String cmd) {
        super.checkExec(cmd);
    }

    @Override
    public void checkRead(String file) {
        super.checkRead(file);
    }

    @Override
    public void checkConnect(String host, int port) {
        super.checkConnect(host, port);
    }

    @Override
    public void checkSecurityAccess(String target) {
        super.checkSecurityAccess(target);
    }
}
